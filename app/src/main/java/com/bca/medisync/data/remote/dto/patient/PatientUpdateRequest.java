package com.bca.medisync.data.remote.dto.patient;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PatientUpdateRequest {
  private String name;
  private String phone;
  private String address;
  private String emergency_contact;
}
