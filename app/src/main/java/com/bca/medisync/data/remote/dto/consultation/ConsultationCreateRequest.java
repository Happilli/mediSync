package com.bca.medisync.data.remote.dto.consultation;

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

  public ConsultationCreateRequest(
      int appointment_id,
      String complaint,
      String symptoms,
      String diagnosis,
      String notes,
      String blood_pressure,
      String heart_rate,
      String temperature,
      String weight) {
    this.appointment_id = appointment_id;
    this.complaint = complaint;
    this.symptoms = symptoms;
    this.diagnosis = diagnosis;
    this.notes = notes;
    this.blood_pressure = blood_pressure;
    this.heart_rate = heart_rate;
    this.temperature = temperature;
    this.weight = weight;
  }
}
