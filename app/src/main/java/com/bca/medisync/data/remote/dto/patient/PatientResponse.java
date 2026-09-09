package com.bca.medisync.data.remote.dto.patient;

public record PatientResponse(
    int id,
    String name,
    String phone,
    String address,
    String date_of_birth,
    String gender,
    String blood_group,
    String emergency_contact,
    String citizenship_number,
    String profile_pic_url,
    String citizenship_photo_url,
    boolean is_verified,
    String rejection_reason,
    boolean has_security_answer) {}
