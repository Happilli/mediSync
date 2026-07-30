package com.bca.medisync.data.remote.dto.doctor;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DoctorProfileResponse {

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
    private boolean verified;

    @SerializedName("profile_pic_url")
    private String profilePicUrl;

    private String email;

    @SerializedName("hospital_name")
    private String hospitalName;

    @SerializedName("license_number")
    private String licenseNumber;

    @SerializedName("patients_this_month")
    private int patientsThisMonth;

    @SerializedName("total_patients")
    private int totalPatients;

    @SerializedName("rating")
    private double rating;

    @SerializedName("positive_feedback")
    private int positiveFeedback;

    private List<AvailabilityDayResponse> availability;

    public int getId() {
        return id;
    }

    public List<AvailabilityDayResponse> getAvailability() {
        return availability;
    }

    public static class AvailabilityDayResponse {
        private String day;
        @SerializedName("start_time")
        private String startTime;
        @SerializedName("end_time")
        private String endTime;

        public String getDay() {
            return day;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }
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
        return verified;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public int getPatientsThisMonth() {
        return patientsThisMonth;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public double getRating() {
        return rating;
    }

    public int getPositiveFeedback() {
        return positiveFeedback;
    }
}