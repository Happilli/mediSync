package com.bca.medisync.data.model;

public class Appointment {

    private String id;
    private String patientName;
    private String doctorName;
    private String department;
    private String speciality;
    private String date;
    private String time;
    private String status;
    private String notes;

    public Appointment(String id,
                       String patientName,
                       String doctorName,
                       String department,
                       String speciality,
                       String date,
                       String time,
                       String status,
                       String notes) {

        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
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

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public String getSpeciality() {
        return speciality;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}