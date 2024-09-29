package de.nulide.findmydevice.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.ui.MainActivity;

public class Notifications {

    public static final int CHANNEL_USAGE = 42;
    // public static final int CHANNEL_LIFE = 43;
    public static final int CHANNEL_PIN = 44;
    public static final int CHANNEL_SERVER = 45;
    public static final int CHANNEL_SECURITY = 46;
    public static final int CHANNEL_FAILED = 47;
    public static final int CHANNEL_IN_APP = 48;

    public static void notify(Context context, String title, String text, int channelID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Integer.valueOf(channelID).toString())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        if (channelID == CHANNEL_SECURITY) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Logger.log("Notifications", "Cannot send notification: missing permission POST_NOTIFICATIONS");
            Logger.log("Notifications", title + ": " + text);
            return;
        }

        int notificationId = (int) System.currentTimeMillis(); // any unique ID

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(Integer.valueOf(CHANNEL_USAGE).toString(), context.getString(R.string.Notification_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription(context.getString(R.string.Notification_Usage_Description));
            NotificationChannel channel3 = new NotificationChannel(Integer.valueOf(CHANNEL_PIN).toString(), context.getString(R.string.Pin_Usage), NotificationManager.IMPORTANCE_DEFAULT);
            channel3.setDescription(context.getString(R.string.Notification_Pin_Usage_Description));
            NotificationChannel channel4 = new NotificationChannel(Integer.valueOf(CHANNEL_SERVER).toString(), context.getString(R.string.Notification_Server), NotificationManager.IMPORTANCE_DEFAULT);
            channel4.setDescription(context.getString(R.string.Notification_Server_Description));
            NotificationChannel channel5 = new NotificationChannel(Integer.valueOf(CHANNEL_SECURITY).toString(), context.getString(R.string.Notification_Security), NotificationManager.IMPORTANCE_DEFAULT);
            channel5.setDescription(context.getString(R.string.Notification_Security_Description));
            NotificationChannel channel6 = new NotificationChannel(Integer.valueOf(CHANNEL_FAILED).toString(), context.getString(R.string.Notification_FAIL), NotificationManager.IMPORTANCE_HIGH);
            channel6.setDescription(context.getString(R.string.Notification_Fail_Description));
            NotificationChannel channel7 = new NotificationChannel(Integer.valueOf(CHANNEL_IN_APP).toString(), context.getString(R.string.Notification_InApp), NotificationManager.IMPORTANCE_DEFAULT);
            channel7.setDescription(context.getString(R.string.Notification_InApp_Description));

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel3);
            notificationManager.createNotificationChannel(channel4);
            notificationManager.createNotificationChannel(channel5);
            notificationManager.createNotificationChannel(channel6);
            notificationManager.createNotificationChannel(channel7);
        }
    }

}
