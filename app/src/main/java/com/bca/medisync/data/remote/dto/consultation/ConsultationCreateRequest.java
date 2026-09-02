package com.bca.medisync.data.remote.dto.consultation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConsultationCreateRequest {
  private int appointment_id;
  private String complaint;
  private String symptoms;
  private String diagnosis;
  private String notes;
  private String blood_pressure;
  private String heart_rate;
  private String temperature;
  private String weight;
}
