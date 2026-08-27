package com.bca.medisync.data.remote.helpers;

import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bca.medisync.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentEnricher {

  public interface Callback1<T> {
    void onResult(T result);
  }

  public static void enrichAll(
      List<AppointmentResponse> responses, Callback1<List<Appointment>> callback) {
    if (responses.isEmpty()) {
      callback.onResult(new ArrayList<>());
      return;
    }

    List<Appointment> result = new ArrayList<>();
    AtomicInteger remaining = new AtomicInteger(responses.size());
    DoctorApi doctorApi = ApiClient.getRetrofit().create(DoctorApi.class);

    for (AppointmentResponse r : responses) {
      doctorApi
          .getDoctorDetail(r.getDoctor_id())
          .enqueue(
              new Callback<DoctorResponse>() {
                @Override
                public void onResponse(
                    Call<DoctorResponse> call, Response<DoctorResponse> doctorResp) {
                  Appointment appointment =
                      mapToAppointment(r, doctorResp.isSuccessful() ? doctorResp.body() : null);
                  synchronized (result) {
                    result.add(appointment);
                  }
                  if (remaining.decrementAndGet() == 0) {
                    callback.onResult(result);
                  }
                }

                @Override
                public void onFailure(Call<DoctorResponse> call, Throwable t) {
                  Appointment appointment = mapToAppointment(r, null);
                  synchronized (result) {
                    result.add(appointment);
                  }
                  if (remaining.decrementAndGet() == 0) {
                    callback.onResult(result);
                  }
                }
              });
    }
  }

  public static void enrichOne(AppointmentResponse r, Callback1<Appointment> callback) {
    DoctorApi doctorApi = ApiClient.getRetrofit().create(DoctorApi.class);
    doctorApi
        .getDoctorDetail(r.getDoctor_id())
        .enqueue(
            new Callback<DoctorResponse>() {
              @Override
              public void onResponse(
                  Call<DoctorResponse> call, Response<DoctorResponse> doctorResp) {
                callback.onResult(
                    mapToAppointment(r, doctorResp.isSuccessful() ? doctorResp.body() : null));
              }

              @Override
              public void onFailure(Call<DoctorResponse> call, Throwable t) {
                callback.onResult(mapToAppointment(r, null));
              }
            });
  }

  public static void enrichForDoctor(
      List<AppointmentResponse> responses, Callback1<List<Appointment>> callback) {
    if (responses.isEmpty()) {
      callback.onResult(new ArrayList<>());
      return;
    }

    // Check if we even need to enrich (if patient_name is already present in first item)
    if (responses.get(0).getPatient_name() != null) {
      List<Appointment> result = new ArrayList<>();
      for (AppointmentResponse r : responses) {
        result.add(mapToAppointmentForDoctor(r, null));
      }
      callback.onResult(result);
      return;
    }

    List<Appointment> result = new ArrayList<>();
    AtomicInteger remaining = new AtomicInteger(responses.size());
    PatientApi patientApi = ApiClient.getRetrofit().create(PatientApi.class);

    for (AppointmentResponse r : responses) {
      patientApi
          .getPatientDetail(r.getPatient_id())
          .enqueue(
              new Callback<PatientPublicResponse>() {
                @Override
                public void onResponse(
                    Call<PatientPublicResponse> call, Response<PatientPublicResponse> patientResp) {
                  Appointment appointment =
                      mapToAppointmentForDoctor(r, patientResp.isSuccessful() ? patientResp.body() : null);
                  synchronized (result) {
                    result.add(appointment);
                  }
                  if (remaining.decrementAndGet() == 0) {
                    callback.onResult(result);
                  }
                }

                @Override
                public void onFailure(Call<PatientPublicResponse> call, Throwable t) {
                  Appointment appointment = mapToAppointmentForDoctor(r, null);
                  synchronized (result) {
                    result.add(appointment);
                  }
                  if (remaining.decrementAndGet() == 0) {
                    callback.onResult(result);
                  }
                }
              });
    }
  }

  public static Appointment mapToAppointment(AppointmentResponse r, DoctorResponse d) {
    String doctorName = d != null ? d.getName() : "Doctor #" + r.getDoctor_id();
    String speciality = d != null ? d.getSpeciality() : "";
    String department = d != null ? d.getDepartment() : "";

    Date date = parseIso(r.getAppointment_at());
    String dateStr =
        date != null ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date) : "";
    String timeStr =
        date != null ? new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date) : "";
    String status = capitalize(r.getStatus());

    return new Appointment(
        String.valueOf(r.getId()),
        "",
        doctorName,
        department,
        speciality,
        dateStr,
        timeStr,
        status,
        r.getNotes());
  }

  public static Appointment mapToAppointmentForDoctor(
      AppointmentResponse r, PatientPublicResponse p) {
    String patientName = r.getPatient_name();
    if (patientName == null || patientName.isEmpty()) {
      patientName = p != null ? p.getName() : "Patient #" + r.getPatient_id();
    }

    Date date = parseIso(r.getAppointment_at());
    String dateStr =
        date != null ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date) : "";
    String timeStr =
        date != null ? new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date) : "";
    String status = capitalize(r.getStatus());

    return new Appointment(
        String.valueOf(r.getId()),
        patientName,
        r.getDoctor_name() != null ? r.getDoctor_name() : "",
        r.getDepartment() != null ? r.getDepartment() : "",
        r.getSpeciality() != null ? r.getSpeciality() : "",
        dateStr,
        timeStr,
        status,
        r.getNotes());
  }

  public static Date parseIso(String iso) {
    return DateTimeUtils.parseIsoToDate(iso);
  }

  public static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }

  public static AppointmentResponse findNextUpcoming(List<AppointmentResponse> all) {
    AppointmentResponse best = null;
    for (AppointmentResponse a : all) {
      String status = a.getStatus();
      if (status == null) continue;
      if (!status.equalsIgnoreCase("confirmed") && !status.equalsIgnoreCase("pending")) {
        continue;
      }
      if (best == null || compareIso(a.getAppointment_at(), best.getAppointment_at()) < 0) {
        best = a;
      }
    }
    return best;
  }

  private static int compareIso(String a, String b) {
    if (a == null) return 1;
    if (b == null) return -1;
    return a.compareTo(b);
  }
}
