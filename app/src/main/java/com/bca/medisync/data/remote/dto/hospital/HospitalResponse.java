package com.bca.medisync.data.remote.dto.hospital;

import lombok.Getter;

@Getter
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
}
