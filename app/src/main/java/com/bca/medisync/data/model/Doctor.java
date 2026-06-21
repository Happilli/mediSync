package com.bca.medisync.data.model;

public class Doctor {

    private final String id;
    private final String name;
    private final String speciality;
    private final String info;
    private final String department;
    private final String phone;
    private final String imageUrl;

    public Doctor(String id, String name, String speciality, String info,String department, String phone, String imageUrl){
        this.id=id;
        this.name=name;
        this.speciality = speciality;
        this.info = info;
        this.department = department;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getInfo() {
        return info;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
