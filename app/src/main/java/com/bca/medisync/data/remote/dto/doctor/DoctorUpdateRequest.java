package com.bca.medisync.data.remote.dto.doctor;

public class DoctorUpdateRequest {
    private String name;
    private String phone;
    private String bio;
    private String address;
    private Integer years_experience;
    private String speciality;
    private String department;

    public DoctorUpdateRequest(String name, String phone, String bio, String address, Integer years_experience, String speciality, String department) {
        this.name = name;
        this.phone = phone;
        this.bio = bio;
        this.address = address;
        this.years_experience = years_experience;
        this.speciality = speciality;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getBio() {
        return bio;
    }

    public String getAddress() {
        return address;
    }

    public Integer getYears_experience() {
        return years_experience;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getDepartment() {
        return department;
    }
}
