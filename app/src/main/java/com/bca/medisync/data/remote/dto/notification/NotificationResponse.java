package com.bca.medisync.data.remote.dto.notification;

import lombok.Getter;

@Getter
public class NotificationResponse {
  private int id;
  private String type;
  private String title;
  private String message;
  private Integer related_id;
  private String related_type;
  private boolean is_read;
  private String created_at;
}
