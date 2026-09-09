package com.bca.medisync.data.remote.dto.hospital;

public record HospitalResponse(
    int id,
    String name,
    String address,
    String phone,
    String website,
    String description,
    String image_url,
    boolean is_active,
    String registration_number) {}
