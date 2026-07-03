package com.bca.medisync.data.remote.dto.register;

public class PatientRegisterRequest {
  private String email;
  private String password;
  private String name;
  private String phone;
  private String address;
  private String date_of_birth;
  private String gender;
  private String blood_group;
  private String emergency_contact;

  public PatientRegisterRequest(
      String email,
      String password,
      String name,
      String phone,
      String address,
      String date_of_birth,
      String gender,
      String blood_group,
      String emergency_contact) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
    this.address = address;
    this.date_of_birth = date_of_birth;
    this.gender = gender;
    this.blood_group = blood_group;
    this.emergency_contact = emergency_contact;
  }
}
