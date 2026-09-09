package com.bca.medisync.data.remote.dto.patient;

public record PatientPublicResponse(
    int id,
    String name,
    String phone,
    String gender,
    String blood_group,
    String emergency_contact,
    String profile_pic_url,
    String email,
    String address,
    String date_of_birth) {}
