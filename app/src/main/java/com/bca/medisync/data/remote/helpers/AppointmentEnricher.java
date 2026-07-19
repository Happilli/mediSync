package com.bca.medisync.data.remote.helpers;

import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorResponse;

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

  public static Date parseIso(String iso) {
    if (iso == null) return null;
    try {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
      return fmt.parse(iso);
    } catch (Exception e) {
      return null;
    }
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
