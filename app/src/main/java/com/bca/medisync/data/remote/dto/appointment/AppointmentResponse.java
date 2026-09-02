package com.bca.medisync.data.remote.dto.appointment;

import lombok.Getter;

@Getter
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
}
