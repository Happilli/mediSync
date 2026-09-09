package com.bca.medisync.data.remote.dto.medication;

public record MedicationResponse(
    int schedule_id,
    int medication_id,
    int prescription_id,
    int patient_id,
    String name,
    String dosage,
    String instruction,
    String dosage_time,
    String label,
    int frequency_per_day,
    int duration_days,
    String start_date,
    String end_date,
    boolean is_taken,
    String taken_at,
    boolean is_active,
    int doctor_id,
    String doctor_name,
    String dispense_status) {}
