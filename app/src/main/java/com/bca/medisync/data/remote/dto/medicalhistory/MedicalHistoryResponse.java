package com.bca.medisync.data.remote.dto.medicalhistory;

public class MedicalHistoryResponse {
  private int id;
  private int doctor_id;
  private int patient_id;
  private String title;
  private String description;
  private String date;

  public int getId() {
    return id;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getDate() {
    return date;
  }
}
