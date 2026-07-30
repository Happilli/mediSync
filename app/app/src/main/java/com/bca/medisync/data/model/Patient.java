package com.bca.medisync.data.model;

public class Patient {
    private final String id, name, email, phone, address, dob, gender, bloodGroup, emergencyContact;

    public Patient(String id, String name, String email, String phone, String address, String dob, String gender, String bloodGroup, String emergencyContact) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getBloodGroup() { return bloodGroup; }
    public String getEmergencyContact() { return emergencyContact; }
    
    public String getDateOfBirth() { return dob; }
}
