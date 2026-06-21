package com.bca.medisync.data.model;

public class Patient {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final String dateOfBirth;
    private final String gender;
    private  final String bloodGroup;
    private final String emergencyContact;


    public Patient(String id, String name, String email, String phone, String address, String dateOfBirth, String gender, String bloodGroup, String emergencyContact){
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender  = gender;
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }
}
