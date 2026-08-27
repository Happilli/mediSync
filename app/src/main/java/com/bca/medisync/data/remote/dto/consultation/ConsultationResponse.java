package com.bca.medisync.data.remote.dto.consultation;

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

  public ConsultationResponse(
      int id,
      int appointment_id,
      int doctor_id,
      int hospital_id,
      String complaint,
      String symptoms,
      String diagnosis,
      String notes,
      String blood_pressure,
      String heart_rate,
      String temperature,
      String weight,
      String created_at) {
    this.id = id;
    this.appointment_id = appointment_id;
    this.doctor_id = doctor_id;
    this.hospital_id = hospital_id;
    this.complaint = complaint;
    this.symptoms = symptoms;
    this.diagnosis = diagnosis;
    this.notes = notes;
    this.blood_pressure = blood_pressure;
    this.heart_rate = heart_rate;
    this.temperature = temperature;
    this.weight = weight;
    this.created_at = created_at;
  }
}
