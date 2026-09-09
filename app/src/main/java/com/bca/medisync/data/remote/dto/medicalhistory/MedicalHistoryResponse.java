package com.bca.medisync.data.remote.dto.medicalhistory;

public record MedicalHistoryResponse(
    int id,
    int doctor_id,
    int patient_id,
    Integer appointment_id,
    String title,
    String description,
    String date) {}
