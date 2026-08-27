package com.bca.medisync.data.remote.dto.appointment;

public class AppointmentResponse {
  private int id;
  private int doctor_id;
  private int patient_id;
  private int hospital_id;
  private String appointment_at;
  private String status;
  private String notes;
  private String patient_name;
  private String doctor_name;
  private String speciality;
  private String department;

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

  public String getPatient_name() {
    return patient_name;
  }

  public String getDoctor_name() {
    return doctor_name;
  }

  public String getSpeciality() {
    return speciality;
  }

  public String getDepartment() {
    return department;
  }
}
