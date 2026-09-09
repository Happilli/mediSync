package com.bca.medisync.data.model;

public record Doctor(
    String id,
    String name,
    String speciality,
    String info,
    String department,
    String phone,
    String imageUrl,
    String bio,
    String address,
    int hospitalId,
    Integer yearsExperience,
    boolean isVerified) {}
