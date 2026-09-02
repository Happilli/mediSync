package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class Hospital {
  private final String id;
  private final String name;
  private final String address;
  private final String phone;
  private final String website;
  private final String description;
  private final double rating;
  private final String imageUrl;
}
