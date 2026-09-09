package com.bca.medisync.data.model;

public record Notification(
    int id,
    String type,
    String title,
    String message,
    Integer relatedId,
    String relatedType,
    boolean isRead,
    String createdAt) {}
