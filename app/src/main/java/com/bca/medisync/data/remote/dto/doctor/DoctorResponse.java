package com.bca.medisync.data.remote.dto.doctor;

public record DoctorResponse(
    int id,
    int hospital_id,
    String name,
    String phone,
    String department,
    String speciality,
    String bio,
    String address,
    Integer years_experience,
    boolean is_verified,
    String profile_pic_url) {}
