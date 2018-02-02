package it.innove;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class NotificationService
        extends Service
{
    private static final String TAG = "NotificationService";

    public static class NotificationWrapper
    {
        final int          requestCode;
        final Notification notification;

        public NotificationWrapper(int requestCode, @NonNull Notification notification)
        {
            this.requestCode = requestCode;
            this.notification = notification;
        }
    }

    public interface NotificationServiceCallbacks
    {
        NotificationWrapper getNotificationWrapper();
    }

    @RequiresApi(api = 26)
    public static void createNotificationChannel(@NonNull Context context,
                                                 String id, String name, int importance,
                                                 String description)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        createNotificationChannel(context, channel);
    }

    @RequiresApi(api = 26)
    public static void createNotificationChannel(@NonNull Context context,
                                                 NotificationChannel channel)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static final String EXTRA_NOTIFICATION_REQUEST_CODE = "EXTRA_NOTIFICATION_REQUEST_CODE";
    public static final String EXTRA_NOTIFICATION              = "EXTRA_NOTIFICATION";

    public static boolean showNotification(@NonNull Context context, @NonNull NotificationWrapper notificationWrapper)
    {
        return showNotification(context, notificationWrapper.requestCode, notificationWrapper.notification);
    }

    public static boolean showNotification(@NonNull Context context, int requestCode, @NonNull Notification notification)
    {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra(EXTRA_NOTIFICATION_REQUEST_CODE, requestCode);
        intent.putExtra(EXTRA_NOTIFICATION, notification);
        ComponentName componentName = context.startService(intent);
        //noinspection UnnecessaryLocalVariable
        boolean started = (componentName != null);
        return started;
    }

    public static boolean stopNotification(@NonNull Context context)
    {
        Intent intent = new Intent(context, NotificationService.class);
        //noinspection UnnecessaryLocalVariable
        boolean stopped = context.stopService(intent);
        return stopped;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "+onCreate()");
        super.onCreate();
        Log.d(TAG, "-onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try
        {
            Log.d(TAG,
                    "+onStartCommand(intent=" + Utils.toString(intent) +
                    ", flags=" + flags +
                    ", startId=" + startId + ")");
            if (intent != null)
            {
                Bundle extras = intent.getExtras();
                if (extras != null)
                {
                    if (extras.containsKey(EXTRA_NOTIFICATION_REQUEST_CODE))
                    {
                        int requestCode = extras.getInt(EXTRA_NOTIFICATION_REQUEST_CODE);

                        if (extras.containsKey(EXTRA_NOTIFICATION))
                        {
                            Object temp = extras.getParcelable(EXTRA_NOTIFICATION);
                            if (temp instanceof Notification)
                            {
                                Notification notification = (Notification) temp;

                                startForeground(requestCode, notification);
                            }
                        }
                    }
                }
            }
            // NOTE:(pv) I am making the tough choice here of *NOT* wanting the app to restart if it crashes.
            //  Restarting after a crash might seem desirable, but it causes more problems than it solves.
            return START_NOT_STICKY;
        }
        finally
        {
            Log.d(TAG,
                    "-onStartCommand(intent=" + Utils.toString(intent) +
                    ", flags=" + flags +
                    ", startId=" + startId + ")");
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "+onDestroy()");
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "-onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        try
        {
            Log.d(TAG, "+onBind(...)");
            return null;
        }
        finally
        {
            Log.d(TAG, "-onBind(...)");
        }
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        try
        {
            Log.d(TAG, "+onUnbind(...)");
            return true;
        }
        finally
        {
            Log.d(TAG, "-onUnbind(...)");
        }
    }
}
