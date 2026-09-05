package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class MedicalHistoryEntry {
  private final String date;
  private final String title;
  private final String description;
  private final Integer appointmentId;
}
