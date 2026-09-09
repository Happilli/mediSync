package com.bca.medisync.data.remote.dto.patient;

public record PatientUpdateRequest(
    String name, String phone, String address, String emergency_contact) {}
