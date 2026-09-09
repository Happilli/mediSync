package com.bca.medisync.data.model;

public record MedicalHistoryEntry(
    String date, String title, String description, Integer appointmentId, int doctorId) {}
