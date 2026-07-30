package com.bca.medisync.data.remote.remote.dto.timeslot;

public class TimeSlotRequest {

    private String appointment_at;

    public TimeSlotRequest(String appointment_at) {
        this.appointment_at = appointment_at;
    }

    public String getAppointment_at() {
        return appointment_at;
    }

    public void setAppointment_at(String appointment_at) {
        this.appointment_at = appointment_at;
    }
}