package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Notification {
  private final int id;
  private final String type;
  private final String title;
  private final String message;
  private final Integer relatedId;
  private final String relatedType;
  private final boolean isRead;
  private final String createdAt;
}
