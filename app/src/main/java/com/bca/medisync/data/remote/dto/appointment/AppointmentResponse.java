package com.bca.medisync.data.remote.dto.appointment;

public class AppointmentResponse {
  private int id;
  private int doctor_id;
  private int patient_id;
  private int hospital_id;
  private String appointment_at;
  private String status;
  private String notes;

  public int getId() {
    return id;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public int getHospital_id() {
    return hospital_id;
  }

  public String getAppointment_at() {
    return appointment_at;
  }

  public String getStatus() {
    return status;
  }

  public String getNotes() {
    return notes;
  }
}
