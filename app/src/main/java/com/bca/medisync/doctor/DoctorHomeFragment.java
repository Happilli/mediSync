package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.databinding.FragmentDoctorHomeBinding;
import com.bca.medisync.patient.NotificationsActivity;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.NotificationBadgeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorHomeFragment extends Fragment implements NotificationCenter.Listener {

  private FragmentDoctorHomeBinding binding;
  private SessionManager sessionManager;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentDoctorHomeBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sessionManager = new SessionManager(requireContext());
    initViews();
    setGreeting();
    setupListeners();
  }

  @Override
  public void onResume() {
    super.onResume();
    NotificationCenter.get().register(this);
    refresh();
  }

  @Override
  public void onPause() {
    super.onPause();
    NotificationCenter.get().unregister(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onNotificationReceived(NotificationResponse notification) {
    if (isAdded() && binding != null)
      NotificationBadgeHelper.showUnread(this, binding.btnNotification);
  }

  public void refresh() {
    if (!isAdded() || binding == null) return;
    loadDashboardData();
    loadTodayAppointments();
    loadUnreadCount();
  }

  private void initViews() {
    binding.rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvAppointments.setAdapter(
        new AppointmentAdapter(
            requireContext(),
            new ArrayList<>(),
            true,
            a -> BottomNavGoTo(R.id.nav_doctor_schedule)));
    binding.cardTodaySchedule.setOnClickListener(v -> BottomNavGoTo(R.id.nav_doctor_schedule));
  }

  private void setGreeting() {
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    String greeting;
    if (hour < 12) greeting = "Good morning,";
    else if (hour < 17) greeting = "Good afternoon,";
    else greeting = "Good evening,";
    binding.txtGreeting.setText(greeting);
  }

  private void loadUnreadCount() {
    NotificationBadgeHelper.refresh(this, binding.btnNotification);
  }

  private void loadDashboardData() {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        p -> {
          if (binding == null) return;
          binding.txtDoctorName.setText("Dr. " + p.getName() + " !");
          binding.txtPatientsMonth.setText(String.valueOf(p.getPatients_this_month()));
          binding.txtTotalPatients.setText(String.valueOf(p.getTotal_patients()));
          binding.txtFollowUps.setText(String.valueOf(p.getUpcoming_followups()));
        },
        (code, msg) -> {
          if (code == 401) handleUnauthorized();
          else if (code == -1)
            Toast.makeText(requireContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
        });
  }

  private void loadTodayAppointments() {
    AppointmentApi api = ApiClient.api(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointmentsAsDoctor(null, null),
        this,
        all -> {
          if (binding == null) return;
          String todayStr =
              new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
          List<AppointmentResponse> todayAppointments = new ArrayList<>();

          for (AppointmentResponse r : all) {
            String apptDate =
                com.bca.medisync.util.DateTimeUtils.format(r.getAppointment_at(), "yyyy-MM-dd");
            if (todayStr.equals(apptDate)) todayAppointments.add(r);
          }

          binding.txtScheduledCount.setText(
              todayAppointments.size()
                  + (todayAppointments.size() == 1 ? " appointment today" : " appointments today"));
          updateStats(todayAppointments);
          EmptyState.bind(
              binding.rvAppointments, binding.txtNoAppointments, todayAppointments.isEmpty());

          if (!todayAppointments.isEmpty()) {
            AppointmentEnricher.enrichForDoctor(
                todayAppointments,
                appointments -> {
                  if (!isAdded() || binding == null) return;
                  binding.rvAppointments.setAdapter(
                      new AppointmentAdapter(
                          requireContext(),
                          appointments,
                          true,
                          a -> BottomNavGoTo(R.id.nav_doctor_schedule)));
                });
          }
        },
        (code, msg) -> {
          if (code == 401) handleUnauthorized();
          else if (code == -1)
            Toast.makeText(requireContext(), "Failed to load schedule", Toast.LENGTH_SHORT).show();
        });
  }

  private void BottomNavGoTo(int navItemId) {
    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavDoctor);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void handleUnauthorized() {
    sessionManager.clearSession();
    Intent intent = new Intent(requireContext(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden) {
      refresh();
    }
  }

  private void updateStats(List<AppointmentResponse> appointments) {
    int pendingCount = 0, completedCount = 0;
    for (AppointmentResponse a : appointments) {
      String status = a.getStatus() != null ? a.getStatus().toLowerCase() : "";
      if (status.contains("pending")
          || status.contains("scheduled")
          || status.contains("confirmed")) pendingCount++;
      else if (status.contains("completed") || status.contains("treated")) completedCount++;
    }
    binding.txtPending.setText(String.valueOf(pendingCount));
    binding.txtCompleted.setText(String.valueOf(completedCount));
  }

  private void setupListeners() {
    binding.btnNotification.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
  }
}
