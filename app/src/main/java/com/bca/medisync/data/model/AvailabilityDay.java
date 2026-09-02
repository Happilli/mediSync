package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class AvailabilityDay {
  private final String day;
  private final String startTime;
  private final String endtime;
}
