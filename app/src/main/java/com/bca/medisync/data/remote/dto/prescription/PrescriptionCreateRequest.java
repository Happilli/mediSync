package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationCreateRequest;

import java.util.List;

public class PrescriptionCreateRequest {
  private int appointment_id;
  private String diagnosis;
  private String instructions;
  private String follow_up_date;
  private List<MedicationCreateRequest> medications;

  public PrescriptionCreateRequest(
      int appointment_id,
      String diagnosis,
      String instructions,
      String follow_up_date,
      List<MedicationCreateRequest> medications) {
    this.appointment_id = appointment_id;
    this.diagnosis = diagnosis;
    this.instructions = instructions;
    this.follow_up_date = follow_up_date;
    this.medications = medications;
  }
}
