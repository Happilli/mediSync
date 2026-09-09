package com.bca.medisync.data.model;

public record Medication(
    int scheduleId,
    int medicationId,
    String name,
    String dosage,
    String frequency,
    String time,
    String label,
    String duration,
    boolean taken,
    String instruction,
    String doctorName,
    String dispenseStatus) {}
