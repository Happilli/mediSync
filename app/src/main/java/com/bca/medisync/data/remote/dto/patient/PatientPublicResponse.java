package com.bca.medisync.data.remote.dto.patient;

import lombok.Getter;

@Getter
public class PatientPublicResponse {
  private int id;
  private String name;
  private String phone;
  private String gender;
  private String blood_group;
  private String emergency_contact;
  private String profile_pic_url;
  private String email;
  private String address;
  private String date_of_birth;
}
