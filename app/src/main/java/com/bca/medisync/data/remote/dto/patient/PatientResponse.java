package com.bca.medisync.data.remote.dto.patient;

import lombok.Getter;

@Getter
public class PatientResponse {
  private int id;
  private String name;
  private String phone;
  private String address;
  private String date_of_birth;
  private String gender;
  private String blood_group;
  private String emergency_contact;
  private String citizenship_number;
  private String profile_pic_url;
  private String citizenship_photo_url;
  private boolean is_verified;
  private String rejection_reason;
  private boolean has_security_answer;
}
