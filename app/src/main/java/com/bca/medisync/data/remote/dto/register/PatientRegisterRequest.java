package com.bca.medisync.data.remote.dto.register;

import lombok.AllArgsConstructor;

@AllArgsConstructor
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
  private String security_answer;
}
