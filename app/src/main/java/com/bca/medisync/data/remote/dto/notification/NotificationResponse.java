package com.bca.medisync.data.remote.dto.notification;

public class NotificationResponse {
  private int id;
  private String type;
  private String title;
  private String message;
  private Integer related_id;
  private String related_type;
  private boolean is_read;
  private String created_at;

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public Integer getRelated_id() {
    return related_id;
  }

  public String getRelated_type() {
    return related_type;
  }

  public String getTitle() {
    return title;
  }

  public boolean isIs_read() {
    return is_read;
  }

  public String getCreated_at() {
    return created_at;
  }
}
