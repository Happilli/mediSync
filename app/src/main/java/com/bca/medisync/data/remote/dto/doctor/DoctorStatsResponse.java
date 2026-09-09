package com.bca.medisync.data.remote.dto.doctor;

import com.bca.medisync.data.remote.dto.MonthlyAppointmentCount;
import java.util.List;
import java.util.Map;

public record DoctorStatsResponse(
    int total_appointments,
    Map<String, Integer> appointments_by_status,
    List<MonthlyAppointmentCount> appointments_last_6_months,
    int upcoming_appointments,
    int total_patients,
    int patients_this_month,
    int total_prescriptions,
    int total_consultations,
    int upcoming_followups) {}
