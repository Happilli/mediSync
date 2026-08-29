package com.bca.medisync.data.remote.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

  public static void schedule(
      Context context,
      int medicationId,
      String name,
      String dosage,
      String rawDosageTime,
      LocalDate endDate,
      boolean takenToday) {

    if (!canScheduleExactAlarms(context)) return;

    LocalDate today = LocalDate.now();
    if (today.isAfter(endDate)) {
      cancelOsAlarm(context, medicationId);
      return;
    }

    LocalTime time = parseTime(rawDosageTime);
    if (time == null) return;

    LocalDate targetDay = today;
    if (takenToday) {
      targetDay = today.plusDays(1);
      if (targetDay.isAfter(endDate)) {
        cancelOsAlarm(context, medicationId);
        return;
      }
    }

    LocalDateTime trigger = LocalDateTime.of(targetDay, time);
    if (trigger.isBefore(LocalDateTime.now())) {
      trigger = trigger.plusDays(1);
      if (trigger.toLocalDate().isAfter(endDate)) {
        cancelOsAlarm(context, medicationId);
        return;
      }
    }

    long triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    setExactAlarm(context, medicationId, name, dosage, rawDosageTime, triggerMillis, endDate);
  }

  public static void setExactAlarm(
      Context context,
      int medicationId,
      String name,
      String dosage,
      String rawDosageTime,
      long triggerMillis,
      LocalDate endDate) {
    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.putExtra("medication_id", medicationId);
    intent.putExtra("med_name", name);
    intent.putExtra("med_dosage", dosage);
    intent.putExtra("dosage_time", rawDosageTime);
    intent.putExtra("end_date", endDate.toString());

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager == null) return;

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
  }

  public static void cancelOsAlarm(Context context, int medicationId) {
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

  public static LocalTime parseTime(String raw) {
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
