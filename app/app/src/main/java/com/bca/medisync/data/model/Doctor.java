package com.bca.medisync.data.model;

public class Doctor {
    private final String id, name, speciality, experience, department, phone, profilePicUrl;

    public Doctor(String id, String name, String speciality, String experience, String department, String phone, String profilePicUrl) {
        this.id = id;
        this.name = name;
        this.speciality = speciality;
        this.experience = experience;
        this.department = department;
        this.phone = phone;
        this.profilePicUrl = profilePicUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpeciality() { return speciality; }
    public String getExperience() { return experience; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }
    public String getProfilePicUrl() { return profilePicUrl; }
    
    public String getInfo() {
        return speciality + " - " + experience;
    }
}
