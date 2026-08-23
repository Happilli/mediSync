package com.bca.medisync.data.remote.dto.patient;

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

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getPhone() {
    return phone;
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

  public String getProfile_pic_url() {
    return profile_pic_url;
  }

  public String getEmail() {
    return email;
  }

  public String getAddress() {
    return address;
  }

  public String getDate_of_birth() {
    return date_of_birth;
  }
}
