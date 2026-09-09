package com.bca.medisync.data.remote.dto.consultation;

public record ConsultationCreateRequest(
    int appointment_id,
    String complaint,
    String symptoms,
    String diagnosis,
    String notes,
    String blood_pressure,
    String heart_rate,
    String temperature,
    String weight) {}
