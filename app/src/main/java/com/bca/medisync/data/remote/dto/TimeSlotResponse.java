package com.bca.medisync.data.remote.dto;

public record TimeSlotResponse(
    int id, int doctor_id, int hospital_id, String appointment_at, boolean is_available) {}
