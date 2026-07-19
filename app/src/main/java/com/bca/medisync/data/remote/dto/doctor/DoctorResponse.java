package com.bca.medisync.data.remote.dto.doctor;

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

  public int getId() {
    return id;
  }

  public int getHospital_id() {
    return hospital_id;
  }

  public String getName() {
    return name;
  }

  public String getPhone() {
    return phone;
  }

  public String getDepartment() {
    return department;
  }

  public String getSpeciality() {
    return speciality;
  }

  public String getBio() {
    return bio;
  }

  public String getAddress() {
    return address;
  }

  public Integer getYears_experience() {
    return years_experience;
  }

  public boolean isIs_verified() {
    return is_verified;
  }

  public String getProfile_pic_url() {
    return profile_pic_url;
  }
}
