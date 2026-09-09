package com.bca.medisync.data.model;

import java.util.List;

public record Prescription(
    int id,
    String doctor_name,
    String diagnosis,
    String instructions,
    String createdAt,
    String followUpDate,
    String dispenseStatus,
    List<Medication> medications) {}
