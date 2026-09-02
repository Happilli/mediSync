package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class TimeSlot {
  private final int id;
  private final String appointmentAt;
  private final String displayTime;
  private final boolean available;
}
