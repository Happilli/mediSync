package com.bca.medisync.data.remote.dto.consultation;

public record ConsultationResponse(
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
    String created_at) {}
