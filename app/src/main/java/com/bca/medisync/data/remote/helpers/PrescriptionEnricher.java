package com.bca.medisync.data.remote.helpers;

import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import com.bca.medisync.data.remote.dto.prescription.PrescriptionResponse;
import com.bca.medisync.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionEnricher {

  public interface Callback1<T> {
    void onResult(T result);
  }

  public static void enrichAll(
      List<PrescriptionResponse> responses, Callback1<List<Prescription>> callback) {
    if (responses.isEmpty()) {
      callback.onResult(new ArrayList<>());
      return;
    }
    List<Prescription> result = new ArrayList<>();
    AtomicInteger remaining = new AtomicInteger(responses.size());
    DoctorApi doctorApi = ApiClient.getRetrofit().create(DoctorApi.class);

    for (PrescriptionResponse r : responses) {
      doctorApi
          .getDoctorDetail(r.getDoctor_id())
          .enqueue(
              new Callback<DoctorResponse>() {
                @Override
                public void onResponse(Call<DoctorResponse> call, Response<DoctorResponse> resp) {
                  Prescription p = mapToPrescription(r, resp.isSuccessful() ? resp.body() : null);
                  synchronized (result) {
                    result.add(p);
                  }
                  if (remaining.decrementAndGet() == 0) callback.onResult(result);
                }

                @Override
                public void onFailure(Call<DoctorResponse> call, Throwable t) {
                  Prescription p = mapToPrescription(r, null);
                  synchronized (result) {
                    result.add(p);
                  }
                  if (remaining.decrementAndGet() == 0) callback.onResult(result);
                }
              });
    }
  }

  public static Prescription mapToPrescription(PrescriptionResponse r, DoctorResponse d) {
    String doctorName = d != null ? d.getName() : "Doctor #" + r.getDoctor_id();
    List<Medication> meds = new ArrayList<>();
    if (r.getMedications() != null) {
      for (MedicationResponse m : r.getMedications()) {
        meds.add(mapMedication(m));
      }
    }
    return new Prescription(
        r.getId(),
        doctorName,
        r.getDiagnosis(),
        r.getInstructions(),
        formatDate(r.getCreated_at()),
        formatDate(r.getFollow_up_date()),
        meds);
  }

  public static Medication mapMedication(MedicationResponse m) {
    String displayTime = m.getDosage_time();
    try {
      LocalTime t = LocalTime.parse(m.getDosage_time());
      displayTime = t.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    } catch (Exception ignored) {
    }
    String frequencyLabel =
        m.getFrequency_per_day() + "x Daily \u2022 " + m.getDuration_days() + " Days";
    return new Medication(
        m.getId(),
        m.getName(),
        m.getDosage(),
        frequencyLabel,
        displayTime,
        m.getDuration_days() + " Days",
        m.isIs_taken(),
        m.getInstruction(),
        m.getDoctor_name());
  }

  public static String formatDate(String iso) {
    return DateTimeUtils.format(iso, "dd MMM yyyy");
  }
}
