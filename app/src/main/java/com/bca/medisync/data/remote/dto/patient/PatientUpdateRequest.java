package com.bca.medisync.data.remote.dto.patient;

public class PatientUpdateRequest {
  private String name;
  private String phone;
  private String address;
  private String emergency_contact;

  public PatientUpdateRequest(String name, String phone, String address, String emergency_contact) {
    this.name = name;
    this.phone = phone;
    this.address = address;
    this.emergency_contact = emergency_contact;
  }
}
