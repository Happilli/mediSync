package com.bca.medisync.data.remote.dto.medication;

public class MedicationTimeCreateRequest {
  private String dosage_time;
  private String label;

  public MedicationTimeCreateRequest(String dosage_time, String label) {
    this.dosage_time = dosage_time;
    this.label = label;
  }
}
