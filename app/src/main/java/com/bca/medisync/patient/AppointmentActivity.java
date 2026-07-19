package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentActivity extends AppCompatActivity {
  private RecyclerView rvUpcoming, rvHistory;
  private TabLayout tabLayout;
  private MaterialToolbar toolbar;
  private ExtendedFloatingActionButton faBookAppointment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_appointment);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    setupToolbar();
    setupTabs();
    setUpRecylerViews();
    setupFab();
    loadAppointments();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadAppointments();
  }

  private void initViews() {
    rvUpcoming = findViewById(R.id.rvUpcoming);
    rvHistory = findViewById(R.id.rvHistory);
    tabLayout = findViewById(R.id.tabLayout);
    toolbar = findViewById(R.id.toolbar);
    faBookAppointment = findViewById(R.id.fabBookAppointment);
  }

  private void setupToolbar() {
    toolbar.setNavigationOnClickListener(v -> finish());
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
    rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
    rvHistory.setLayoutManager(new LinearLayoutManager(this));
  }

  private void setupFab() {
    faBookAppointment.setOnClickListener(
        v -> {
          // booking appointment flow nav something
          startActivity(new Intent(AppointmentActivity.this, HospitalActivity.class));
        });
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
                if (response.isSuccessful() && response.body() != null) {
                  AppointmentEnricher.enrichAll(
                      response.body(), AppointmentActivity.this::bindLists);
                } else {
                  Toast.makeText(
                          AppointmentActivity.this,
                          "failed to load appointments",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                Toast.makeText(
                        AppointmentActivity.this,
                        "Network err: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindLists(List<Appointment> all) {
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
    rvUpcoming.setAdapter(new AppointmentAdapter(this, upcoming, false, appointment -> {}));
    rvHistory.setAdapter(new AppointmentAdapter(this, history, false, appointment -> {}));
  }
}
