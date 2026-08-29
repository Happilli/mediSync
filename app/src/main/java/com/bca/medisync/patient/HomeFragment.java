package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.NotificationApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment implements NotificationCenter.Listener {

  private RecyclerView rvDashboard;
  private TextView txtPatientName;
  private MaterialButton btnNotification;
  private RecyclerView rvUpcomingHome;
  private TextView txtNoUpcoming;

  public HomeFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupDashboard();
  }

  @Override
  public void onResume() {
    super.onResume();
    NotificationCenter.get().register(this);
    loadPatientName();
    loadUpcomingAppointment();
    loadUnreadCount();
  }

  @Override
  public void onPause() {
    super.onPause();
    NotificationCenter.get().unregister(this);
  }

  @Override
  public void onNotificationReceived(NotificationResponse notification) {
    if (!isAdded()) {
      return;
    }
    showUnreadIcon();
  }

  private void initViews(View view) {
    txtPatientName = view.findViewById(R.id.txtPatientName);
    rvDashboard = view.findViewById(R.id.rvDashboard);
    rvUpcomingHome = view.findViewById(R.id.rvUpcomingHome);
    txtNoUpcoming = view.findViewById(R.id.txtNoUpcoming);
    btnNotification = view.findViewById(R.id.btnNotification);

    rvUpcomingHome.setLayoutManager(new LinearLayoutManager(requireContext()));

    view.findViewById(R.id.btnNotification)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
  }

  private void goToTab(int navItemId) {
    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void showUnreadIcon() {
    btnNotification.setIcon(
        ContextCompat.getDrawable(requireContext(), R.drawable.notification_dot));
  }

  private void showReadIcon() {
    btnNotification.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.notification));
  }

  private void loadPatientName() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        body -> txtPatientName.setText(body.getName()),
        (code, msg) ->
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden && isAdded()) {
      loadUpcomingAppointment();
      loadUnreadCount();
    }
  }

  private void loadUnreadCount() {
    NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
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

  private void loadUpcomingAppointment() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointments(null, null),
        this,
        body -> {
          List<AppointmentResponse> upcoming = new ArrayList<>();
          for (AppointmentResponse a : body) {
            String status = a.getStatus();
            if (status != null
                && (status.equalsIgnoreCase("Confirmed") || status.equalsIgnoreCase("Pending"))) {
              upcoming.add(a);
            }
          }
          Collections.sort(upcoming, Comparator.comparing(AppointmentResponse::getAppointment_at));
          List<AppointmentResponse> top3 = upcoming.subList(0, Math.min(3, upcoming.size()));

          if (top3.isEmpty()) {
            if (!isAdded()) return;
            rvUpcomingHome.setVisibility(View.GONE);
            txtNoUpcoming.setVisibility(View.VISIBLE);
            return;
          }

          AppointmentEnricher.enrichAll(
              top3,
              appointments -> {
                if (!isAdded()) return;
                rvUpcomingHome.setVisibility(View.VISIBLE);
                txtNoUpcoming.setVisibility(View.GONE);
                rvUpcomingHome.setAdapter(
                    new AppointmentAdapter(
                        requireContext(),
                        appointments,
                        false,
                        a -> goToTab(R.id.nav_appointments)));
              });
        },
        (code, msg) -> {});
  }

  private void setupDashboard() {
    List<String> titles =
        Arrays.asList(
            "Appointments",
            "Prescriptions",
            "Medications",
            "My Profile",
            "Health Records",
            "Hospitals");
    List<Integer> icons =
        Arrays.asList(
            R.drawable.ic_nav_calendar,
            R.drawable.edit,
            R.drawable.ic_nav_medicine,
            R.drawable.ic_nav_profile,
            R.drawable.record,
            R.drawable.hospital);

    List<Integer> positions = new ArrayList<>();
    for (int i = 0; i < titles.size(); i++) positions.add(i);

    SimpleListAdapter<Integer> adapter =
        new SimpleListAdapter<>(
            R.layout.item_dashboard,
            positions,
            (itemView, position, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtFeature)).setText(titles.get(position));
              ((ImageView) itemView.findViewById(R.id.imgFeature))
                  .setImageResource(icons.get(position));
            },
            position -> {
              switch (position) {
                case 0:
                  goToTab(R.id.nav_appointments);
                  break;
                case 1:
                  ((MainTabActivity) requireActivity())
                      .pushFragment(new PrescriptionListFragment());
                  break;
                case 2:
                  goToTab(R.id.nav_medications);
                  break;
                case 3:
                  goToTab(R.id.nav_profile);
                  break;
                case 4:
                  ((MainTabActivity) requireActivity())
                      .pushFragment(new PatientMedicalHistoryFragment());
                  break;
                case 5:
                  ((MainTabActivity) requireActivity()).pushFragment(new HospitalFragment());
                  break;
              }
            });
    rvDashboard.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    rvDashboard.setAdapter(adapter);
  }
}
