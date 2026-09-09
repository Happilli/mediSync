package com.bca.medisync.data.model;

public record Appointment(
    String id,
    String patientName,
    String doctorName,
    String department,
    String speciality,
    String date,
    String time,
    String status,
    String notes,
    int patientId) {}
