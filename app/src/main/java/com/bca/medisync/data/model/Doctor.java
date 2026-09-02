package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
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
}
