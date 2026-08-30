package com.bca.medisync.data.remote.dto.medication;

public class MedicationResponse {
  private int schedule_id;
  private int medication_id;
  private int prescription_id;
  private int patient_id;
  private String name;
  private String dosage;
  private String instruction;
  private String dosage_time;
  private String label;
  private int frequency_per_day;
  private int duration_days;
  private String start_date;
  private String end_date;
  private boolean is_taken;
  private String taken_at;
  private boolean is_active;
  private int doctor_id;
  private String doctor_name;

  public int getSchedule_id() {
    return schedule_id;
  }

  public int getMedication_id() {
    return medication_id;
  }

  public int getPrescription_id() {
    return prescription_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public String getName() {
    return name;
  }

  public String getDosage() {
    return dosage;
  }

  public String getInstruction() {
    return instruction;
  }

  public String getDosage_time() {
    return dosage_time;
  }

  public String getLabel() {
    return label;
  }

  public int getFrequency_per_day() {
    return frequency_per_day;
  }

  public int getDuration_days() {
    return duration_days;
  }

  public String getStart_date() {
    return start_date;
  }

  public String getEnd_date() {
    return end_date;
  }

  public boolean isIs_taken() {
    return is_taken;
  }

  public String getTaken_at() {
    return taken_at;
  }

  public boolean isIs_active() {
    return is_active;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public String getDoctor_name() {
    return doctor_name;
  }
}
