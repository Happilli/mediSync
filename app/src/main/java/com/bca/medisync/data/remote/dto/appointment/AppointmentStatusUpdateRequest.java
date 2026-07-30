package com.bca.medisync.data.remote.dto.appointment;

public class AppointmentStatusUpdateRequest {
    private String status;

    public AppointmentStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
