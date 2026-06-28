package com.bca.medisync.data.model;

public class Appointment {
    private final String id;
    private final String doctorName;

    private final String patientName;
    private final String department;
    private final String date;
    private final String speciality;
    private final String time;
    private final String status;
    private final String notes;

public Appointment(String id,String patientName,  String doctorName, String department, String speciality, String date, String time, String status, String notes){
    this.id = id;
    this.doctorName = doctorName;
    this.patientName = patientName;
    this.department =department;
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

    public String getNotes() {
        return notes;
    }
}
