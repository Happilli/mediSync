package com.bca.medisync.data.remote.dto.medication;

public class MedicationCreateRequest {
  private String name;
  private String dosage;
  private String dosage_time;
  private String instruction;
  private int frequency_per_day;
  private int duration_days;

  public MedicationCreateRequest(
      String name,
      String dosage,
      String dosage_time,
      String instruction,
      int frequency_per_day,
      int duration_days) {
    this.name = name;
    this.dosage = dosage;
    this.dosage_time = dosage_time;
    this.instruction = instruction;
    this.frequency_per_day = frequency_per_day;
    this.duration_days = duration_days;
  }
}
