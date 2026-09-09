package com.bca.medisync.data.model;

public record Patient(
    String id,
    String name,
    String email,
    String phone,
    String address,
    String dateOfBirth,
    String gender,
    String bloodGroup,
    String emergencyContact,
    String profilePicUrl) {}
