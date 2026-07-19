package com.bca.medisync.data.remote.dto.hospital;

public class HospitalResponse {
  private int id;
  private String name;
  private String address;
  private String phone;
  private String website;
  private String description;
  private String image_url;
  private boolean is_active;
  private String registration_number;

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public String getPhone() {
    return phone;
  }

  public String getWebsite() {
    return website;
  }

  public String getDescription() {
    return description;
  }

  public String getImage_url() {
    return image_url;
  }

  public boolean isIs_active() {
    return is_active;
  }

  public String getRegistration_number() {
    return registration_number;
  }
}
