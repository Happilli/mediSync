package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.DashboardAdapter;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.patient.PatientResponse;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
  private RecyclerView rvDashboard;
  private BottomNavigationView bottomNav;
  private TextView txtPatientName;
  private MaterialCardView cardAppointment;
  private TextView txtAppointmentDoctor,
      txtAppointmentSpeciality,
      txtAppointmentDate,
      txtAppointmentTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_home);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
          return insets;
        });

    initViews();
    setupDashboard();
    setupBottomNav();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadPatientName();
    loadUpcomingAppointment();
  }

  private void initViews() {
    txtPatientName = findViewById(R.id.txtPatientName);
    rvDashboard = findViewById(R.id.rvDashboard);
    bottomNav = findViewById(R.id.bottomNav);
    cardAppointment = findViewById(R.id.cardAppointment);
    txtAppointmentDoctor = findViewById(R.id.txtAppointmentDoctor);
    txtAppointmentSpeciality = findViewById(R.id.txtAppointmentSpeciality);
    txtAppointmentDate = findViewById(R.id.txtAppointmentDate);
    txtAppointmentTime = findViewById(R.id.txtAppointmentTime);

    cardAppointment.setVisibility(View.GONE);
    cardAppointment.setOnClickListener(
        v -> startActivity(new Intent(HomeActivity.this, AppointmentActivity.class)));
  }

  private void loadPatientName() {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    api.getMyProfile()
        .enqueue(
            new Callback<PatientResponse>() {
              @Override
              public void onResponse(
                  Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  txtPatientName.setText(response.body().getName());
                }
              }

              @Override
              public void onFailure(Call<PatientResponse> call, Throwable t) {
                Toast.makeText(
                        HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  private void loadUpcomingAppointment() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    api.getMyAppointments(null, null)
        .enqueue(
            new Callback<List<AppointmentResponse>>() {
              @Override
              public void onResponse(
                  Call<List<AppointmentResponse>> call,
                  Response<List<AppointmentResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                AppointmentResponse next = AppointmentEnricher.findNextUpcoming(response.body());
                if (next == null) {
                  cardAppointment.setVisibility(View.GONE);
                  return;
                }
                AppointmentEnricher.enrichOne(
                    next,
                    appointment -> {
                      txtAppointmentDoctor.setText(appointment.getDoctorName());
                      txtAppointmentSpeciality.setText(appointment.getSpeciality());
                      txtAppointmentDate.setText(appointment.getDate());
                      txtAppointmentTime.setText(appointment.getTime());
                      cardAppointment.setVisibility(View.VISIBLE);
                    });
              }

              @Override
              public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {}
            });
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

    DashboardAdapter adapter =
        new DashboardAdapter(
            this,
            titles,
            icons,
            position -> {
              switch (position) {
                case 0:
                  startActivity(new Intent(HomeActivity.this, AppointmentActivity.class));
                  break;
                case 1:
                  break;
                case 2:
                  startActivity(new Intent(HomeActivity.this, MedicationActivity.class));
                  break;
                case 3:
                  startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                  break;
                case 4:
                  break;
                case 5:
                  startActivity(new Intent(HomeActivity.this, HospitalActivity.class));
                  break;
              }
            });
    rvDashboard.setLayoutManager(new GridLayoutManager(this, 3));
    rvDashboard.setAdapter(adapter);
  }

  private void setupBottomNav() {
    bottomNav.setOnItemSelectedListener(
        item -> {
          int id = item.getItemId();
          if (id == R.id.nav_home) {
            return true;
          } else if (id == R.id.nav_appointments) {
            startActivity(new Intent(HomeActivity.this, AppointmentActivity.class));
            return true;
          } else if (id == R.id.nav_medications) {
            startActivity(new Intent(HomeActivity.this, MedicationActivity.class));
            return true;
          } else if (id == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            return true;
          }
          return false;
        });
  }
}
