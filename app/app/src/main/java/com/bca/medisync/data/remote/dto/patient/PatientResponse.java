package com.bca.medisync.data.remote.dto.patient;

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

  public int getId() {
    return id;
  }

  public String getProfile_pic_url() {
    return profile_pic_url;
  }

  public String getName() {
    return name;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddress() {
    return address;
  }

  public String getDate_of_birth() {
    return date_of_birth;
  }

  public String getGender() {
    return gender;
  }

  public String getBlood_group() {
    return blood_group;
  }

  public String getEmergency_contact() {
    return emergency_contact;
  }

  public String getCitizenship_number() {
    return citizenship_number;
  }

  public String getCitizenship_photo_url() {
    return citizenship_photo_url;
  }

  public boolean isIs_verified() {
    return is_verified;
  }
}
