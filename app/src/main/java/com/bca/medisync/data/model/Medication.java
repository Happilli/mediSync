package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Medication {
  private final int scheduleId;
  private final int medicationId;
  private final String name;
  private final String dosage;
  private final String frequency;
  private final String time;
  private final String label;
  private final String duration;
  private final boolean taken;
  private final String instruction;
  private final String doctorName;
  private final String dispenseStatus;
}
