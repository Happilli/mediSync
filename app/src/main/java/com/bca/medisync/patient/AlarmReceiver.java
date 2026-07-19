package com.bca.medisync.patient;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.bca.medisync.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MEDICATION_CHANNEL";
    private static final String CHANNE_NAME = "Medication Reminders";

    @Override
    public void onReceive(Context context, Intent intent){
        String medName = intent.getStringExtra("med_name");
        String medDosage = intent.getStringExtra("med_dosage");

        NotificationManager manager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager ==null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNE_NAME, NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }
        Intent openIntent = new Intent(context, MedicationFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_medicine).setContentTitle("Time to take your medicine!")
                .setContentText(medName+ " - " + medDosage).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true).setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
