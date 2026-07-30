package com.bca.medisync.data.remote.dto.doctor;

import com.google.gson.annotations.SerializedName;

public class DoctorResponse {

    private int id;

    @SerializedName("hospital_id")
    private int hospitalId;

    private String name;
    private String phone;
    private String department;
    private String speciality;
    private String bio;
    private String address;

    @SerializedName("years_experience")
    private int yearsExperience;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("profile_pic_url")
    private String profilePicUrl;

    @SerializedName("patients_this_month")
    private int patientsThisMonth;

    @SerializedName("total_patients")
    private int totalPatients;

    public int getId() {
        return id;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getBio() {
        return bio;
    }

    public String getAddress() {
        return address;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public int getPatientsThisMonth() {
        return patientsThisMonth;
    }

    public int getTotalPatients() {
        return totalPatients;
    }
}