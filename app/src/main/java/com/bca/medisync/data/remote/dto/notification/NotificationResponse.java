package com.bca.medisync.data.remote.dto.notification;

public record NotificationResponse(
    int id,
    String type,
    String title,
    String message,
    Integer related_id,
    String related_type,
    boolean is_read,
    String created_at) {}
