package com.bca.medisync.data.model;

public class Doctor {

  private final String id;
  private final String name;
  private final String speciality;
  private final String info;
  private final String department;
  private final String phone;
  private final String imageUrl;
  private final String bio;
  private final String address;
  private final int hospitalId;
  private final Integer yearsExperience;
  private final boolean isVerified;

  public Doctor(
      String id,
      String name,
      String speciality,
      String info,
      String department,
      String phone,
      String imageUrl,
      String bio,
      String address,
      int hospitalId,
      Integer yearsExperience,
      boolean isVerified) {
    this.id = id;
    this.name = name;
    this.speciality = speciality;
    this.info = info;
    this.department = department;
    this.phone = phone;
    this.imageUrl = imageUrl;
    this.bio = bio;
    this.address = address;
    this.hospitalId = hospitalId;
    this.yearsExperience = yearsExperience;
    this.isVerified = isVerified;
  }

  public String getBio() {
    return bio;
  }

  public String getAddress() {
    return address;
  }

  public int getHospitalId() {
    return hospitalId;
  }

  public Integer getYearsExperience() {
    return yearsExperience;
  }

  public boolean isVerified() {
    return isVerified;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSpeciality() {
    return speciality;
  }

  public String getInfo() {
    return info;
  }

  public String getDepartment() {
    return department;
  }

  public String getPhone() {
    return phone;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}
