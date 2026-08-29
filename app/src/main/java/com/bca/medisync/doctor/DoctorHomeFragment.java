package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.local.SessionManager;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.patient.NotificationsActivity;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorHomeFragment extends Fragment implements NotificationCenter.Listener {

  private RecyclerView rvAppointments;
  private MaterialButton btnNotification;
  private TextView txtGreeting,
      txtDoctorName,
      txtPending,
      txtCompleted,
      txtFollowUps,
      txtScheduledCount,
      txtPatientsMonth,
      txtTotalPatients,
      txtNoAppointments;
  private SessionManager sessionManager;

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
  public void onNotificationReceived(NotificationResponse notification) {
    if (isAdded()) showUnreadIcon();
  }

  public void refresh() {
    if (!isAdded()) return;
    loadDashboardData();
    loadTodayAppointments();
    loadUnreadCount();
  }

  private void initViews(View view) {
    rvAppointments = view.findViewById(R.id.rvAppointments);
    btnNotification = view.findViewById(R.id.btnNotification);
    txtGreeting = view.findViewById(R.id.txtGreeting);
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
        new AppointmentAdapter(
            requireContext(),
            new ArrayList<>(),
            true,
            a -> BottomNavGoTo(R.id.nav_doctor_schedule)));
  }

  private void setGreeting() {
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    String greeting;
    if (hour < 12) greeting = "Good morning";
    else if (hour < 17) greeting = "Good afternoon";
    else greeting = "Good evening";
    txtGreeting.setText(greeting);
  }

  private void showUnreadIcon() {
    btnNotification.setIcon(
        ContextCompat.getDrawable(requireContext(), R.drawable.notification_dot));
  }

  private void showReadIcon() {
    btnNotification.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.notification));
  }

  private void loadUnreadCount() {
    NotificationApi api = ApiClient.api(NotificationApi.class);
    ApiCallback.handle(
        api.getUnreadCount(),
        this,
        body -> {
          Integer count = body.get("unread_count");
          if (count != null && count > 0) showUnreadIcon();
          else showReadIcon();
        },
        (code, msg) -> {});
  }

  private void loadDashboardData() {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        p -> {
          txtDoctorName.setText("Dr. " + p.getName());
          txtPatientsMonth.setText(String.valueOf(p.getPatients_this_month()));
          txtTotalPatients.setText(String.valueOf(p.getTotal_patients()));
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
          String todayStr =
              new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
          List<AppointmentResponse> todayAppointments = new ArrayList<>();

          for (AppointmentResponse r : all) {
            String apptDate =
                com.bca.medisync.util.DateTimeUtils.format(r.getAppointment_at(), "yyyy-MM-dd");
            if (todayStr.equals(apptDate)) todayAppointments.add(r);
          }

          txtScheduledCount.setText(
              todayAppointments.size()
                  + (todayAppointments.size() == 1 ? " appointment today" : " appointments today"));
          updateStats(todayAppointments);
          EmptyState.bind(rvAppointments, txtNoAppointments, todayAppointments.isEmpty());

          if (!todayAppointments.isEmpty()) {
            AppointmentEnricher.enrichForDoctor(
                todayAppointments,
                appointments -> {
                  if (!isAdded()) return;
                  rvAppointments.setAdapter(
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
    int pendingCount = 0, completedCount = 0, followUpCount = 0;
    for (AppointmentResponse a : appointments) {
      String status = a.getStatus() != null ? a.getStatus().toLowerCase() : "";
      if (status.contains("pending")
          || status.contains("scheduled")
          || status.contains("confirmed")) pendingCount++;
      else if (status.contains("completed") || status.contains("treated")) completedCount++;
      else if (status.contains("follow")) followUpCount++;
    }
    txtPending.setText(String.valueOf(pendingCount));
    txtCompleted.setText(String.valueOf(completedCount));
    txtFollowUps.setText(String.valueOf(followUpCount));
  }

  private void setupListeners() {
    btnNotification.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
  }
}
