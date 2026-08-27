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
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.helpers.MedicationAlarmScheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class AlarmReceiver extends BroadcastReceiver {
  private static final String CHANNEL_ID = "MEDICATION_CHANNEL";
  private static final String CHANNE_NAME = "Medication Reminders";

  @Override
  public void onReceive(Context context, Intent intent) {
    SessionManager sessionManager = new SessionManager(context);
    int medicationId = intent.getIntExtra("medication_id", -1);
    String medName = intent.getStringExtra("med_name");
    String medDosage = intent.getStringExtra("med_dosage");
    String rawTime = intent.getStringExtra("dosage_time");
    String endDateStr = intent.getStringExtra("end_date");

    if (sessionManager.isNotificationsEnabled()) {
      showNotification(context, medName, medDosage);
    }

    if (medicationId != -1 && endDateStr != null && rawTime != null) {
      LocalDate endDate = LocalDate.parse(endDateStr);
      LocalDate tomorrow = LocalDate.now().plusDays(1);
      if (!tomorrow.isAfter(endDate)) {
        LocalTime time = MedicationAlarmScheduler.parseTime(rawTime);
        if (time != null) {
          long triggerMillis =
              LocalDateTime.of(tomorrow, time)
                  .atZone(ZoneId.systemDefault())
                  .toInstant()
                  .toEpochMilli();
          MedicationAlarmScheduler.setExactAlarm(
              context, medicationId, medName, medDosage, rawTime, triggerMillis, endDate);
        }
      }
    }
  }

  private void showNotification(Context context, String medName, String medDosage) {
    NotificationManager manager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (manager == null) {
      return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(CHANNEL_ID, CHANNE_NAME, NotificationManager.IMPORTANCE_HIGH);
      manager.createNotificationChannel(channel);
    }
    Intent openIntent = new Intent(context, MedicationFragment.class);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_medicine)
            .setContentTitle("Time to take your medicine!")
            .setContentText(medName + " - " + medDosage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

    manager.notify((int) System.currentTimeMillis(), builder.build());
  }
}
