package com.bca.medisync.data.remote.helpers;

import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import com.bca.medisync.data.remote.dto.prescription.PrescriptionResponse;
import com.bca.medisync.util.DateTimeUtils;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrescriptionEnricher {
  public static void enrichAll(
      List<PrescriptionResponse> responses,
      ParallelEnricher.Callback1<List<Prescription>> callback) {
    DoctorApi doctorApi = ApiClient.api(DoctorApi.class);
    ParallelEnricher.run(
        responses,
        r -> doctorApi.getDoctorDetail(r.doctor_id()),
        PrescriptionEnricher::mapToPrescription,
        callback);
  }

  public static Prescription mapToPrescription(PrescriptionResponse r, DoctorResponse d) {
    String doctorName = d != null ? d.name() : "Doctor #" + r.doctor_id();
    List<Medication> meds = new ArrayList<>();
    if (r.medications() != null) {
      for (MedicationResponse m : r.medications()) meds.add(mapMedication(m));
    }
    return new Prescription(
        r.id(),
        doctorName,
        r.diagnosis(),
        r.instructions(),
        formatDate(r.created_at()),
        formatDate(r.follow_up_date()),
        r.dispense_status(),
        meds);
  }

  public static Medication mapMedication(MedicationResponse m) {
    String displayTime = m.dosage_time();
    try {
      LocalTime t = LocalTime.parse(m.dosage_time());
      displayTime = t.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    } catch (Exception ignored) {
    }
    String frequencyLabel = m.frequency_per_day() + "x Daily \u2022 " + m.duration_days() + " Days";
    return new Medication(
        m.schedule_id(),
        m.medication_id(),
        m.name(),
        m.dosage(),
        frequencyLabel,
        displayTime,
        m.label(),
        m.duration_days() + " Days",
        m.is_taken(),
        m.instruction(),
        m.doctor_name(),
        m.dispense_status());
  }

  public static String formatDate(String iso) {
    return DateTimeUtils.format(iso, "dd MMM yyyy");
  }
}
