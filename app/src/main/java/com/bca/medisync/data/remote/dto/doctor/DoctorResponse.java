package com.bca.medisync.data.remote.dto.doctor;

import lombok.Getter;

@Getter
public class DoctorResponse {
  private int id;
  private int hospital_id;
  private String name;
  private String phone;
  private String department;
  private String speciality;
  private String bio;
  private String address;
  private Integer years_experience;
  private boolean is_verified;
  private String profile_pic_url;
}
