package com.bca.medisync.data.remote.dto.doctor;

public record DoctorProfileResponse(
    DoctorResponse doctor,
    int patients_this_month,
    int total_patients,
    boolean has_security_answer,
    int upcoming_followups) {}
