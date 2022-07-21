package com.gotaxi.driver.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;

import androidx.core.app.NotificationCompat;

import com.gotaxi.driver.Activity.MainActivity;
import com.gotaxi.driver.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {
    private static final int NOTIFICATION_ID = 200;
    private static final String PUSH_NOTIFICATION = "pushNotification";
    private static final String CHANNEL_ID = "myChannel";
    private static final String CHANNEL_NAME = "myChannelName";
    private static final String URL = "url";
    private static final String ACTIVITY = "activity";
    Map<String, Class> activityMap = new HashMap<>();
    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
        //Populate activity map
        activityMap.put("MainActivity", MainActivity.class);
    }

    /**
     * Displays notification based on parameters
     *
     * @param notificationVO
     * @param resultIntent
     */
    public void displayNotification(NotificationVO notificationVO, Intent resultIntent) {

        String message = notificationVO.getMessage();
        String title = notificationVO.getTitle();
        String iconUrl = notificationVO.getIconUrl();
        String action = notificationVO.getAction();
        String destination = notificationVO.getActionDestination();
        Bitmap iconBitMap = null;
        if (iconUrl != null) {
            iconBitMap = getBitmapFromURL(iconUrl);
        }
        final int icon = R.drawable.ic_notif;

        PendingIntent resultPendingIntent;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
        );
        if (URL.equals(action)) {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(destination));
            resultPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        } else if (ACTIVITY.equals(action) && activityMap.containsKey(destination)) {
            resultIntent = new Intent(mContext, activityMap.get(destination));
            resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_IMMUTABLE
                    );
        } else {
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_IMMUTABLE
                    );
        }


        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext, CHANNEL_ID);

        Notification notification;

        if (iconBitMap == null) {
            //When Inbox Style is applied, user can expand the notification
            notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setSmallIcon(R.drawable.ic_notif)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setContentText(message)
                    .setSound(defaultSoundUri)
                    .build();

        } else {
            //If Bitmap is created from URL, show big icon
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle(title);
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
            bigPictureStyle.bigPicture(iconBitMap);
            notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(bigPictureStyle)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setContentText(message)
                    .setSound(defaultSoundUri)
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //All notifications should go through NotificationChannel on Android 26 & above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(defaultSoundUri, audioAttributes);
            channel.setLockscreenVisibility(NotificationCompat.PRIORITY_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(message);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channel);

        }
        notificationManager.notify(NOTIFICATION_ID, notification);

    }

    /**
     * Downloads push notification image before displaying it in
     * the notification tray
     *
     * @param strURL : URL of the notification Image
     * @return : BitMap representation of notification Image
     */
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Playing notification sound
     */
//    public void playNotificationSound() {
//        try {
//            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
//                    + "://" + mContext.getPackageName() + "/raw/notification");
//            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}