package com.bca.medisync.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MedicationScheduleStore extends SQLiteOpenHelper {

  private static final String db_name = "medisync_schedules.db";
  private static final int db_version = 1;

  private static final String table = "medication_schedule";
  private static final String col_medication_id = "medication_id";
  private static final String col_name = "name";
  private static final String col_dosage = "dosage";
  private static final String col_dosage_time = "dosage_time";
  private static final String col_start_date = "start_date";
  private static final String col_duration_days = "duration_days";
  private static final String col_days_elapsed = "days_elapsed";

  public MedicationScheduleStore(Context context) {
    super(context.getApplicationContext(), db_name, null, db_version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
        "create table "
            + table
            + " ("
            + col_medication_id
            + " integer primary key, "
            + col_name
            + " text not null, "
            + col_dosage
            + " text not null, "
            + col_dosage_time
            + " text not null, "
            + col_start_date
            + " text not null, "
            + col_duration_days
            + " integer not null, "
            + col_days_elapsed
            + " integer not null default 0"
            + ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists " + table);
    onCreate(db);
  }

  public static class ScheduleEntry {
    public final int medicationId;
    public final String name;
    public final String dosage;
    public final String dosageTime;
    public final String startDate;
    public final int durationDays;
    public final int daysElapsed;

    public ScheduleEntry(
        int medicationId,
        String name,
        String dosage,
        String dosageTime,
        String startDate,
        int durationDays,
        int daysElapsed) {
      this.medicationId = medicationId;
      this.name = name;
      this.dosage = dosage;
      this.dosageTime = dosageTime;
      this.startDate = startDate;
      this.durationDays = durationDays;
      this.daysElapsed = daysElapsed;
    }
  }

  public void upsert(
      int medicationId,
      String name,
      String dosage,
      String dosageTime,
      String startDate,
      int durationDays) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(col_medication_id, medicationId);
    values.put(col_name, name);
    values.put(col_dosage, dosage);
    values.put(col_dosage_time, dosageTime);
    values.put(col_start_date, startDate);
    values.put(col_duration_days, durationDays);
    values.put(col_days_elapsed, 0);
    db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  public void updateDaysElapsed(int medicationId, int newDaysElapsed) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(col_days_elapsed, newDaysElapsed);
    db.update(
        table, values, col_medication_id + " = ?", new String[] {String.valueOf(medicationId)});
  }

  public void delete(int medicationId) {
    SQLiteDatabase db = getWritableDatabase();
    db.delete(table, col_medication_id + " = ?", new String[] {String.valueOf(medicationId)});
  }

  public ScheduleEntry get(int medicationId) {
    SQLiteDatabase db = getReadableDatabase();
    try (Cursor c =
        db.query(
            table,
            null,
            col_medication_id + " = ?",
            new String[] {String.valueOf(medicationId)},
            null,
            null,
            null)) {
      if (c.moveToFirst()) {
        return fromCursor(c);
      }
      return null;
    }
  }

  public List<ScheduleEntry> getAll() {
    List<ScheduleEntry> result = new ArrayList<>();
    SQLiteDatabase db = getReadableDatabase();
    try (Cursor c = db.query(table, null, null, null, null, null, null)) {
      while (c.moveToNext()) {
        result.add(fromCursor(c));
      }
    }
    return result;
  }

  private ScheduleEntry fromCursor(Cursor c) {
    return new ScheduleEntry(
        c.getInt(c.getColumnIndexOrThrow(col_medication_id)),
        c.getString(c.getColumnIndexOrThrow(col_name)),
        c.getString(c.getColumnIndexOrThrow(col_dosage)),
        c.getString(c.getColumnIndexOrThrow(col_dosage_time)),
        c.getString(c.getColumnIndexOrThrow(col_start_date)),
        c.getInt(c.getColumnIndexOrThrow(col_duration_days)),
        c.getInt(c.getColumnIndexOrThrow(col_days_elapsed)));
  }
}
