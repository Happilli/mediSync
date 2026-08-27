package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.remote.dto.patient.PatientPublicResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorHomeFragment extends Fragment {
  private RecyclerView rvAppointments;
  private MaterialButton btnPatients, btnSchedule;
  private TextView txtDoctorName,
      txtPending,
      txtCompleted,
      txtFollowUps,
      txtScheduledCount,
      txtPatientsMonth,
      txtTotalPatients,
      txtNoAppointments;
  private SessionManager sessionManager;

  public DoctorHomeFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_doctor_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());
    initViews(view);
    setupListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadDashboardData();
    loadTodayAppointments();
  }

  private void initViews(View view) {
    rvAppointments = view.findViewById(R.id.rvAppointments);
    btnPatients = view.findViewById(R.id.btnPatients);
    btnSchedule = view.findViewById(R.id.btnSchedule);
    txtDoctorName = view.findViewById(R.id.txtDoctorName);
    txtPending = view.findViewById(R.id.txtPending);
    txtCompleted = view.findViewById(R.id.txtCompleted);
    txtFollowUps = view.findViewById(R.id.txtFollowUps);
    txtScheduledCount = view.findViewById(R.id.txtScheduledCount);
    txtPatientsMonth = view.findViewById(R.id.txtPatientsMonth);
    txtTotalPatients = view.findViewById(R.id.txtTotalPatients);
    txtNoAppointments = view.findViewById(R.id.txtNoAppointments);

    rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvAppointments.setAdapter(
        new AppointmentAdapter(requireContext(), new ArrayList<>(), true, a -> {}));

    ShapeableImageView btnProfile = view.findViewById(R.id.btnProfile);
    btnProfile.setOnClickListener(
        v -> {
          BottomNavGoTo(R.id.nav_doctor_profile);
        });
  }

  private void BottomNavGoTo(int navItemId) {
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
        requireActivity().findViewById(R.id.bottomNavDoctor);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void loadDashboardData() {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<DoctorProfileResponse>() {
              @Override
              public void onResponse(
                  Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (!isAdded()) return;
                if (response.code() == 401) {
                  handleUnauthorized();
                  return;
                }
                if (response.isSuccessful() && response.body() != null) {
                  DoctorProfileResponse p = response.body();
                  txtDoctorName.setText("Dr. " + p.getName());
                  txtPatientsMonth.setText(String.valueOf(p.getPatients_this_month()));
                  txtTotalPatients.setText(String.valueOf(p.getTotal_patients()));
                }
              }

              @Override
              public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private void loadTodayAppointments() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);

    // Fetch ALL appointments for the doctor and filter on Android
    // this ensures we don't miss appointments due to backend query param differences
    api.getMyAppointmentsAsDoctor(null, null)
        .enqueue(
            new Callback<List<AppointmentResponse>>() {
              @Override
              public void onResponse(
                  Call<List<AppointmentResponse>> call,
                  Response<List<AppointmentResponse>> response) {
                if (!isAdded()) return;

                Log.d("DoctorHomeFragment", "Appointments Response Code: " + response.code());

                if (response.code() == 401) {
                  handleUnauthorized();
                  return;
                }
                if (response.isSuccessful() && response.body() != null) {
                  List<AppointmentResponse> all = response.body();
                  Log.d(
                      "DoctorHomeFragment",
                      "Received " + all.size() + " total appointments from backend");

                  String todayStr =
                      new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                  List<AppointmentResponse> todayAppointments = new ArrayList<>();

                  for (AppointmentResponse r : all) {
                    String apptDate =
                        com.bca.medisync.util.DateTimeUtils.format(
                            r.getAppointment_at(), "yyyy-MM-dd");
                    Log.d(
                        "DoctorHomeFragment",
                        "Appt ID: "
                            + r.getId()
                            + ", Raw: "
                            + r.getAppointment_at()
                            + ", Formatted: "
                            + apptDate
                            + ", Today: "
                            + todayStr
                            + ", Status: "
                            + r.getStatus());

                    if (todayStr.equals(apptDate)) {
                      todayAppointments.add(r);
                    }
                  }

                  Log.d(
                      "DoctorHomeFragment",
                      "Filtered "
                          + todayAppointments.size()
                          + " appointments for today ("
                          + todayStr
                          + ")");

                  txtScheduledCount.setText(todayAppointments.size() + " appointments scheduled");
                  updateStats(all); // Use ALL appointments for stats (pending/completed total)
                  // or should stats also be for today? Dashboard usually shows today's stats.
                  // The previous implementation used 'all' for updateStats.
                  // I'll update stats based on today's appointments if it's "Today's stats".
                  updateStats(todayAppointments);

                  if (todayAppointments.isEmpty()) {
                    txtNoAppointments.setVisibility(View.VISIBLE);
                    rvAppointments.setVisibility(View.GONE);
                  } else {
                    txtNoAppointments.setVisibility(View.GONE);
                    rvAppointments.setVisibility(View.VISIBLE);
                    AppointmentEnricher.enrichForDoctor(
                        todayAppointments,
                        appointments -> {
                          if (!isAdded()) return;
                          rvAppointments.setAdapter(
                              new AppointmentAdapter(
                                  requireContext(), appointments, true, appointment -> {}));
                        });
                  }
                }
              }

              @Override
              public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("DoctorHomeFragment", "Appointments Request Failed: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Failed to load schedule", Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private void handleUnauthorized() {
    sessionManager.clearSession();
    Intent intent = new Intent(requireContext(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }

  private void updateStats(List<AppointmentResponse> appointments) {
    int pendingCount = 0;
    int completedCount = 0;
    int followUpCount = 0;

    for (AppointmentResponse a : appointments) {
      String status = a.getStatus() != null ? a.getStatus().toLowerCase() : "";
      Log.d(
          "DoctorHomeFragment",
          "Processing stat for Appt ID: " + a.getId() + ", Status: " + status);

      if (status.contains("pending")
          || status.contains("scheduled")
          || status.contains("confirmed")) {
        pendingCount++;
      } else if (status.contains("completed") || status.contains("treated")) {
        completedCount++;
      } else if (status.contains("follow")) {
        followUpCount++;
      }
    }

    txtPending.setText(String.valueOf(pendingCount));
    txtCompleted.setText(String.valueOf(completedCount));
    txtFollowUps.setText(String.valueOf(followUpCount));
  }

  private void setupListeners() {
    btnPatients.setOnClickListener(v -> BottomNavGoTo(R.id.nav_doctor_patients));
    btnSchedule.setOnClickListener(v -> BottomNavGoTo(R.id.nav_doctor_schedule));
  }
}
