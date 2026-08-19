package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationResponse;

import java.util.List;

public class PrescriptionResponse {
  private int id;
  private int doctor_id;
  private int appointment_id;
  private int patient_id;
  private String diagnosis;
  private String instructions;
  private String created_at;
  private String follow_up_date;
  private List<MedicationResponse> medications;

  public int getId() {
    return id;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public int getAppointment_id() {
    return appointment_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getInstructions() {
    return instructions;
  }

  public String getCreated_at() {
    return created_at;
  }

  public String getFollow_up_date() {
    return follow_up_date;
  }

  public List<MedicationResponse> getMedications() {
    return medications;
  }
}
