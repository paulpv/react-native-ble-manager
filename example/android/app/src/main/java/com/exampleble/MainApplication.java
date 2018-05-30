package com.exampleble;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.support.v4.app.NotificationCompat;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.util.Arrays;
import java.util.List;

import it.innove.BleManagerPackage;
import it.innove.NotificationService;
import it.innove.NotificationService.NotificationServiceCallbacks;
import it.innove.NotificationService.NotificationWrapper;

public class MainApplication extends Application implements ReactApplication, NotificationServiceCallbacks {
  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new BleManagerPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);

    initNotificationChannel();
  }

  private static final String NOTIFICATION_CHANNEL_ID          = "notification_channel_exampleble";
  private static final String NOTIFICATION_CHANNEL_NAME        = "ExampleBle Notification Channel";
  private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification used for ExampleBle background notifications";
  private static final int    NOTIFICATION_REQUEST_CODE        = 12345;

  private void initNotificationChannel() {
      if (VERSION.SDK_INT < 26) {
          return;
      }

      NotificationService.createNotificationChannel(this,
              NOTIFICATION_CHANNEL_ID,
              NOTIFICATION_CHANNEL_NAME,
              NotificationManager.IMPORTANCE_LOW,
              NOTIFICATION_CHANNEL_DESCRIPTION);
  }

  public NotificationWrapper getNotificationWrapper() {
    int requestCode = NOTIFICATION_REQUEST_CODE;

    NotificationCompat.Builder builder = new NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scanning)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setContentTitle("Example BLE")
            .setContentText("Scanning...")
            //.setTicker("Ticker")
            .setWhen(System.currentTimeMillis());

    Intent startIntent = new Intent(this, MainActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, requestCode, startIntent, 0);
    builder.setContentIntent(contentIntent);
    Notification notification = builder.build();

    return new NotificationWrapper(requestCode, notification);
  }
}
