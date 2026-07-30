package com.bca.medisync.data.model;

import java.util.List;

public class DoctorProfile {
    private final String name, role, qualification, registrationNo;
    private final String phone, email, specialization;
    private final int experienceYears;
    private final String hospitalName, hospitalRole;
    private final List<AvailabilityDay> availability;
    private final int patientsThisMonth, totalPatients;
    private final int positiveFeedbackPercent;
    private final double rating;

    public DoctorProfile(String name, String role, String qualification, String registrationNo, String phone, String email, String specialization, int experienceYears, String hospitalName, String hospitalRole, List<AvailabilityDay> availability, int patientsThisMonth, int totalPatients, int positiveFeedbackPercent, double rating) {
        this.name = name;
        this.role = role;
        this.qualification = qualification;
        this.registrationNo = registrationNo;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.hospitalName = hospitalName;
        this.hospitalRole = hospitalRole;
        this.availability = availability;
        this.patientsThisMonth = patientsThisMonth;
        this.totalPatients = totalPatients;
        this.positiveFeedbackPercent = positiveFeedbackPercent;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getQualification() {
        return qualification;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getHospitalRole() {
        return hospitalRole;
    }

    public List<AvailabilityDay> getAvailability() {
        return availability;
    }

    public int getPatientsThisMonth() {
        return patientsThisMonth;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public int getPositiveFeedbackPercent() {
        return positiveFeedbackPercent;
    }

    public double getRating() {
        return rating;
    }
}
