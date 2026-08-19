package com.bca.medisync.data.remote.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bca.medisync.data.model.Medication;
import com.bca.medisync.patient.AlarmReceiver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MedicationAlarmScheduler {

  public static boolean canScheduleExactAlarms(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      return am != null && am.canScheduleExactAlarms();
    }
    return true;
  }

  public static void schedule(Context context, Medication medication, String rawDosageTime) {
    if (medication.isTaken()) {
      cancel(context, medication.getId());
      return;
    }
    if (!canScheduleExactAlarms(context)) {
      return;
    }

    LocalTime time = parseTime(rawDosageTime);
    if (time == null) return;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime trigger = LocalDateTime.of(LocalDate.now(), time);
    if (trigger.isBefore(now)) {
      trigger = trigger.plusDays(1);
    }

    long triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.putExtra("med_name", medication.getName());
    intent.putExtra("med_dosage", medication.getDosage());

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context,
            medication.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager == null) return;

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
  }

  public static void cancel(Context context, int medicationId) {
    Intent intent = new Intent(context, AlarmReceiver.class);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
  }

  private static LocalTime parseTime(String raw) {
    if (raw == null) return null;
    try {
      return LocalTime.parse(raw);
    } catch (Exception ignored) {
    }
    try {
      return LocalTime.parse(raw, DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    } catch (Exception ignored) {
    }
    return null;
  }
}
