package com.bca.medisync.data.remote.dto.appointment;

public record AppointmentResponse(
    int id,
    int doctor_id,
    int patient_id,
    int hospital_id,
    String appointment_at,
    String status,
    String notes,
    String patient_name,
    String doctor_name,
    String speciality,
    String department) {}
