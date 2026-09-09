package com.bca.medisync.data.model;

public record Hospital(
    String id,
    String name,
    String address,
    String phone,
    String website,
    String description,
    double rating,
    String imageUrl) {}
