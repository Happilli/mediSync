package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Patient {
  private final String id;
  private final String name;
  private final String email;
  private final String phone;
  private final String address;
  private final String dateOfBirth;
  private final String gender;
  private final String bloodGroup;
  private final String emergencyContact;
  private final String profilePicUrl;
}
