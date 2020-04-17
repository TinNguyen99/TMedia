package com.example.tmedia;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class CreateNotification {
    public static final String channel_id = "channel";

    public static final String PLAYACTION = "playaction";
    public static final String BACKACTION = "backaction";
    public static final String NEXTACTION = "nextaction";

    public static Notification notification;

    public static void NotificationCreate(Context context, ItemModel itemModel, int playbutton, int pos, int size){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.tmedia);


            PendingIntent pendingIntentBack;
            int drw_back;
            if (pos == 0){
                pendingIntentBack = null;
                drw_back = 0;
            } else {
                Intent intentBack = new Intent(context, NotificationActionService.class).setAction(BACKACTION);

                pendingIntentBack = PendingIntent.getBroadcast(context, 0, intentBack, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_back = R.drawable.ic_skip_previous_black_24dp;
            }


            PendingIntent pendingIntentNext;
            int drw_next;
            if (pos == size){
                pendingIntentNext = null;
                drw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionService.class).setAction(NEXTACTION);

                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next = R.drawable.ic_skip_next_black_24dp;
            }


            Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(PLAYACTION);

            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            // create notification
            notification = new NotificationCompat.Builder(context, channel_id)
                    .setSmallIcon(R.drawable.ic_audiotrack_black_24dp)
                    .setContentTitle(itemModel.getTitle())
                    .setContentText(itemModel.getArtist())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drw_back, "Back", pendingIntentBack)
                    .addAction(playbutton, "Play", pendingIntentPlay)
                    .addAction(drw_next, "Next", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(2, notification);
        }
    }
}
