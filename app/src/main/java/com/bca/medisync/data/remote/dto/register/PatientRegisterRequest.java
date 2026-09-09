package com.bca.medisync.data.remote.dto.register;

public record PatientRegisterRequest(
    String email,
    String password,
    String name,
    String phone,
    String address,
    String date_of_birth,
    String gender,
    String blood_group,
    String emergency_contact,
    String security_answer) {}
