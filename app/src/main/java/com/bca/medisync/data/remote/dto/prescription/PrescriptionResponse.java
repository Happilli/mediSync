package com.bca.medisync.data.remote.dto.prescription;

import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import java.util.List;

public record PrescriptionResponse(
    int id,
    int doctor_id,
    int appointment_id,
    int patient_id,
    String diagnosis,
    String instructions,
    String created_at,
    String follow_up_date,
    String dispense_status,
    List<MedicationResponse> medications) {}
