package com.bca.medisync.patient;

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
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.patient.HospitalActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import com.bca.medisync.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        v -> startActivity(new android.content.Intent(requireContext(), HospitalActivity.class)));
  }

  private void loadAppointments() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    api.getMyAppointments(null, null)
        .enqueue(
            new Callback<List<AppointmentResponse>>() {
              @Override
              public void onResponse(
                  Call<List<AppointmentResponse>> call,
                  Response<List<AppointmentResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  AppointmentEnricher.enrichAll(
                      response.body(), AppointmentFragment.this::bindLists);
                } else {
                  Toast.makeText(
                          requireContext(), "failed to load appointments", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network err: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
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
        new AppointmentAdapter(requireContext(), upcoming, false, appointment -> {}));
    rvHistory.setAdapter(
        new AppointmentAdapter(requireContext(), history, false, appointment -> {}));
  }
}
