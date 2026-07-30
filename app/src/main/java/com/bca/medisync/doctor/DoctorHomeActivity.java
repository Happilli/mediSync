package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.dto.appointment.AppointmentResponse;
import com.bca.medisync.data.remote.dto.doctor.DoctorProfileResponse;
import com.bca.medisync.data.repository.DoctorRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorHomeActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private MaterialButton btnPatients, btnSchedule;

    private TextView txtViewAll;
    private TextView txtDoctorName;
    private TextView txtPending, txtCompleted, txtFollowUps;
    private BottomNavigationView bottomNav;

    private DoctorRepository repository;

    private AppointmentAdapter adapter;

    private final List<Appointment> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        initViews();

        repository = new DoctorRepository();

        loadDoctorProfile();
        setupBottomNav();
        setupAppointments();

        setupListeners();
    }

    private void initViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        btnPatients = findViewById(R.id.btnPatients);
        btnSchedule = findViewById(R.id.btnSchedule);
        bottomNav = findViewById(R.id.bottomNav);

        txtViewAll = findViewById(R.id.txtViewAll);
        txtDoctorName = findViewById(R.id.txtDoctorName);
        
        txtPending = findViewById(R.id.txtPending);
        txtCompleted = findViewById(R.id.txtCompleted);
        txtFollowUps = findViewById(R.id.txtFollowUps);

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, DoctorProfileActivity.class)));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.doc_nav_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.doc_nav_dashboard) {
                return true;
            } else if (id == R.id.doc_nav_schedule) {
                intent = new Intent(this, ScheduleActivity.class);
            } else if (id == R.id.doc_nav_patients) {
                intent = new Intent(this, DoctorPatientsActivity.class);
            } else if (id == R.id.doc_nav_profile) {
                intent = new Intent(this, DoctorProfileActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadDoctorProfile() {
        repository.getProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtDoctorName.setText(response.body().getName());
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                Toast.makeText(DoctorHomeActivity.this, "Failed to load doctor profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAppointments() {
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(
                this,
                appointmentList,
                false,
                appointment -> {
                    Intent intent = new Intent(this, AppointmentDetailActivity.class);
                    intent.putExtra("appointment", appointment);
                    startActivityForResult(intent, 100);
                });
        rvAppointments.setAdapter(adapter);
        loadAppointments();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadAppointments();
        }
    }

    private void loadAppointments() {
        repository.getAppointments().enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AppointmentResponse> list = response.body();
                    appointmentList.clear();
                    for (AppointmentResponse item : list) {
                        appointmentList.add(item.toAppointment());
                    }
                    adapter.notifyDataSetChanged();
                    updateStatistics();
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentResponse>> call, Throwable t) {
                Toast.makeText(DoctorHomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatistics() {
        int pending = 0;
        int completed = 0;
        int followUp = 0;

        for (Appointment a : appointmentList) {
            String status = a.getStatus().toLowerCase();
            if (status.contains("pending")) {
                pending++;
            } else if (status.contains("completed") || status.contains("confirmed")) {
                completed++;
            } else if (status.contains("follow up") || status.contains("follow-up")) {
                followUp++;
            }
        }

        txtPending.setText(String.valueOf(pending));
        txtCompleted.setText(String.valueOf(completed));
        txtFollowUps.setText(String.valueOf(followUp));
    }

    private void setupListeners() {
        btnPatients.setOnClickListener(v -> startActivity(new Intent(this, DoctorPatientsActivity.class)));
        btnSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        txtViewAll.setOnClickListener(v -> { /* Future implementation */ });
    }
}
