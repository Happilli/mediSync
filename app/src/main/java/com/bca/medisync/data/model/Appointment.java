package com.bca.medisync.data.model;

import java.io.Serializable;

public class Appointment implements Serializable {
    private final String id;
    private final String doctorName;
    private final String patientName;
    private final String patientPhone;
    private final String patientGender;
    private final String patientBloodGroup;
    private final String patientProfilePicUrl;
    private final String department;
    private final String date;
    private final String speciality;
    private final String time;
    private final String status;
    private final String notes;

    public Appointment(String id, String patientName, String doctorName, String department, String speciality, String date, String time, String status, String notes) {
        this(id, patientName, null, null, null, null, doctorName, department, speciality, date, time, status, notes);
    }

    public Appointment(String id, String patientName, String patientPhone, String patientGender, String patientBloodGroup, String patientProfilePicUrl, String doctorName, String department, String speciality, String date, String time, String status, String notes) {
        this.id = id;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientGender = patientGender;
        this.patientBloodGroup = patientBloodGroup;
        this.patientProfilePicUrl = patientProfilePicUrl;
        this.department = department;
        this.speciality = speciality;
        this.date = date;
        this.time = time;
        this.status = status;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public String getDate() {
        return date;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public String getPatientProfilePicUrl() {
        return patientProfilePicUrl;
    }

    public String getNotes() {
        return notes;
    }
}
