package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.NotificationCenter;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.notification.NotificationResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.databinding.FragmentHomeBinding;
import com.bca.medisync.databinding.ItemDashboardBinding;
import com.bca.medisync.util.NotificationBadgeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment implements NotificationCenter.Listener {
  private FragmentHomeBinding binding;

  public HomeFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews();
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
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onNotificationReceived(NotificationResponse notification) {
    if (!isAdded() || binding == null) return;
    NotificationBadgeHelper.showUnread(this, binding.btnNotification);
  }

  private void initViews() {
    binding.rvUpcomingHome.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.btnNotification.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
  }

  private void goToTab(int navItemId) {
    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
    bottomNav.setSelectedItemId(navItemId);
  }

  private void loadPatientName() {
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.getMyProfile(),
        this,
        body -> {
          if (binding != null) binding.txtPatientName.setText(body.getName());
        },
        ApiCallback.simpleError(requireContext(), "Failed to load profile."));
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
    NotificationBadgeHelper.refresh(this, binding.btnNotification);
  }

  private void loadUpcomingAppointment() {
    AppointmentApi api = ApiClient.api(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointments(null, null),
        this,
        body -> {
          if (binding == null) return;
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
            binding.rvUpcomingHome.setVisibility(View.GONE);
            binding.txtNoUpcoming.setVisibility(View.VISIBLE);
            return;
          }
          AppointmentEnricher.enrichAll(
              top3,
              appointments -> {
                if (!isAdded() || binding == null) return;
                binding.rvUpcomingHome.setVisibility(View.VISIBLE);
                binding.txtNoUpcoming.setVisibility(View.GONE);
                binding.rvUpcomingHome.setAdapter(
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
    SimpleListAdapter<Integer, ItemDashboardBinding> adapter =
        new SimpleListAdapter<>(
            ItemDashboardBinding::inflate,
            positions,
            (rowBinding, position, pos) -> {
              rowBinding.txtFeature.setText(titles.get(position));
              rowBinding.imgFeature.setImageResource(icons.get(position));
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
    binding.rvDashboard.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    binding.rvDashboard.setAdapter(adapter);
  }
}
