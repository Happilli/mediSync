package com.bca.medisync.data.remote.dto.medication;

import java.util.List;

public class MedicationCreateRequest {
  private String name;
  private String dosage;
  private String instruction;
  private int duration_days;
  private List<MedicationTimeCreateRequest> dosage_times;

  public MedicationCreateRequest(
      String name,
      String dosage,
      String instruction,
      int duration_days,
      List<MedicationTimeCreateRequest> dosage_times) {
    this.name = name;
    this.dosage = dosage;
    this.instruction = instruction;
    this.duration_days = duration_days;
    this.dosage_times = dosage_times;
  }
}
