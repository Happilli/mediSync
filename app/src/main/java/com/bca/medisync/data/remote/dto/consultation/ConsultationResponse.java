package com.bca.medisync.data.remote.dto.consultation;

import lombok.Getter;

@Getter
public class ConsultationResponse {
  private int id;
  private int appointment_id;
  private int doctor_id;
  private int hospital_id;
  private String complaint;
  private String symptoms;
  private String diagnosis;
  private String notes;
  private String blood_pressure;
  private String heart_rate;
  private String temperature;
  private String weight;
  private String created_at;
}
