package com.bca.medisync.patient;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import com.bca.medisync.R;

public class AppointmentFragment extends Fragment {
  private RecyclerView rvUpcoming, rvHistory;
  private TabLayout tabLayout;
  private MaterialToolbar toolbar;
  private ExtendedFloatingActionButton fabBookAppointment;

  public AppointmentFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_appointment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupToolbar();
    setupTabs();
    setUpRecylerViews();
    setupFab();
    loadAppointments();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadAppointments();
  }

  public void refresh() {
    loadAppointments();
  }

  private void initViews(View view) {
    rvUpcoming = view.findViewById(R.id.rvUpcoming);
    rvHistory = view.findViewById(R.id.rvHistory);
    tabLayout = view.findViewById(R.id.tabLayout);
    toolbar = view.findViewById(R.id.toolbar);
    fabBookAppointment = view.findViewById(R.id.fabBookAppointment);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(null);
  }

  private void setupTabs() {
    tabLayout.addTab(tabLayout.newTab().setText("UpcominG"));
    tabLayout.addTab(tabLayout.newTab().setText("History"));
    tabLayout.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            if (tab.getPosition() == 0) {
              rvUpcoming.setVisibility(View.VISIBLE);
              rvHistory.setVisibility(View.GONE);
            } else {
              rvUpcoming.setVisibility(View.GONE);
              rvHistory.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {}
        });
  }

  private void setUpRecylerViews() {
    rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
  }

  private void setupFab() {
    fabBookAppointment.setOnClickListener(
        v -> ((MainTabActivity) requireActivity()).pushFragment(new HospitalFragment()));
  }

  private void bindLists(List<Appointment> all) {
    if (!isAdded()) return;
    List<Appointment> upcoming = new ArrayList<>();
    List<Appointment> history = new ArrayList<>();
    for (Appointment a : all) {
      if (a.getStatus().equalsIgnoreCase("Confirmed")
          || a.getStatus().equalsIgnoreCase("Pending")) {
        upcoming.add(a);
      } else {
        history.add(a);
      }
    }
    rvUpcoming.setAdapter(
        new AppointmentAdapter(requireContext(), upcoming, false, this::onAppointmentClicked));
    rvHistory.setAdapter(
        new AppointmentAdapter(requireContext(), history, false, this::onAppointmentClicked));
  }

  private void onAppointmentClicked(Appointment appointment) {
    boolean cancellable = appointment.getStatus().equalsIgnoreCase("Pending");

    if (!cancellable) {
      Toast.makeText(
              requireContext(), "Only pending appointments can be cancelled.", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    new AlertDialog.Builder(requireContext())
        .setTitle("Cancel Appointment")
        .setMessage(
            "Cancel your appointment with "
                + appointment.getDoctorName()
                + " on "
                + appointment.getDate()
                + " at "
                + appointment.getTime()
                + "?")
        .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelAppointment(appointment))
        .setNegativeButton("Keep it", null)
        .show();
  }

  private void loadAppointments() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointments(null, null),
        this,
        body -> AppointmentEnricher.enrichAll(body, AppointmentFragment.this::bindLists),
        (code, msg) ->
            Toast.makeText(requireContext(), "failed to load appointments", Toast.LENGTH_SHORT)
                .show());
  }

  private void cancelAppointment(Appointment appointment) {
    int appointmentId;
    try {
      appointmentId = Integer.parseInt(appointment.getId());
    } catch (NumberFormatException e) {
      Toast.makeText(requireContext(), "Invalid appointment reference", Toast.LENGTH_SHORT).show();
      return;
    }

    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    ApiCallback.handle(
        api.cancelAppointment(appointmentId),
        this,
        body -> {
          Toast.makeText(requireContext(), "Appointment cancelled.", Toast.LENGTH_SHORT).show();
          loadAppointments();
        },
        (code, msg) -> {
          if (code == 400) {
            Toast.makeText(
                    requireContext(),
                    "This appointment can no longer be cancelled.",
                    Toast.LENGTH_LONG)
                .show();
            loadAppointments();
          } else if (code == 403) {
            Toast.makeText(requireContext(), "Not your appointment.", Toast.LENGTH_SHORT).show();
          } else if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(requireContext(), "Failed to cancel appointment.", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
