package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class MedicalHistory {
  private final String latestRxName;
  private final String latestRxDesc;
  private final String latestLabTitle;
  private final String latestLabDesc;
  private final List<MedicalHistoryEntry> timeline;
}
