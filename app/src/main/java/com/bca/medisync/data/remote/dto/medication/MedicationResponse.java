package com.bca.medisync.data.remote.dto.medication;

public class MedicationResponse {
  private int id;
  private int prescription_id;
  private int patient_id;
  private String name;
  private String dosage;
  private String dosage_time;
  private String instruction;
  private int frequency_per_day;
  private String start_date;
  private String end_date;
  private boolean is_active;
  private int duration_days;
  private boolean is_taken;
  private String taken_at;
  private int doctor_id;

  public String getDoctor_name() {
    return doctor_name;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  private String doctor_name;

  public int getId() {
    return id;
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

  public String getDosage_time() {
    return dosage_time;
  }

  public String getInstruction() {
    return instruction;
  }

  public int getFrequency_per_day() {
    return frequency_per_day;
  }

  public int getDuration_days() {
    return duration_days;
  }

  public boolean isIs_taken() {
    return is_taken;
  }

  public String getTaken_at() {
    return taken_at;
  }

  public String getStart_date() {
    return start_date;
  }

  public String getEnd_date() {
    return end_date;
  }

  public boolean isIs_active() {
    return is_active;
  }
}
