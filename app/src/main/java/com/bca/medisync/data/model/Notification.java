package com.bca.medisync.data.model;

public class Notification {
  private final int id;
  private final String type;
  private final String title;
  private final String message;
  private final Integer relatedId;
  private final String relatedType;
  private final boolean isRead;
  private final String createdAt;

  public Notification(
      int id,
      String type,
      String title,
      String message,
      Integer relatedId,
      String relatedType,
      boolean isRead,
      String createdAt) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.message = message;
    this.relatedId = relatedId;
    this.relatedType = relatedType;
    this.isRead = isRead;
    this.createdAt = createdAt;
  }

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public Integer getRelatedId() {
    return relatedId;
  }

  public String getRelatedType() {
    return relatedType;
  }

  public boolean isRead() {
    return isRead;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
