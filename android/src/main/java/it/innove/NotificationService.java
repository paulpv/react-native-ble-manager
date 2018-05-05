package it.innove;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
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

    private static NotificationService sNotificationService;
    private static boolean sStopped = true;

    public static boolean startForeground(@NonNull final Context context,
                                          final int requestCode,
                                          @NonNull final Notification notification)
    {
        sStopped = false;

        if (sNotificationService != null)
        {
            sNotificationService.startForeground(requestCode, notification);
            return true;
        }

        Intent intent = new Intent(context, NotificationService.class);

        context.bindService(intent, new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                sNotificationService = ((NotificationServiceBinder) service).getService();

                if (sStopped)
                {
                    return;
                }

                startForeground(context, requestCode, notification);
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                sNotificationService = null;
            }
        }, BIND_AUTO_CREATE);

        return false;
    }

    public static void stopForeground()
    {
        sStopped = true;

        if (sNotificationService != null)
        {
            sNotificationService.stopForeground(true);
        }
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
                    "+onStartCommand(intent=" + Utils.toString(intent) + ", flags=" + flags + ", startId="
                    + startId + ")");
            //Log.s(TAG, StringUtils.separateCamelCaseWords("onStartCommand"));

            // NOTE:(pv) I am making the tough choice here of *NOT* wanting the app to restart if it crashes.
            //  Restarting after a crash might seem desirable, but it causes more problems than it solves.
            return START_NOT_STICKY;
        }
        finally
        {
            Log.d(TAG,
                    "-onStartCommand(intent=" + Utils.toString(intent) + ", flags=" + flags + ", startId="
                    + startId + ")");
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "+onDestroy()");
        super.onDestroy();
        Log.d(TAG, "-onDestroy()");
    }

    public class NotificationServiceBinder
            extends Binder
    {
        public NotificationService getService()
        {
            return NotificationService.this;
        }
    }

    private NotificationServiceBinder mBinder = new NotificationServiceBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        try
        {
            Log.d(TAG, "+onBind(...)");
            return mBinder;
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
