package com.bca.medisync.data.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class DoctorProfile {
  private final String name, role, qualification, registrationNo;
  private final String phone, email, specialization;
  private final int experienceYears;
  private final String hospitalName, hospitalRole;
  private final List<AvailabilityDay> availability;
  private final int patientsThisMonth, totalPatients;
  private final int positiveFeedbackPercent;
  private final double rating;
}
