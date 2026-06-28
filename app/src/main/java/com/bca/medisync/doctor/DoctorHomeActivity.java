package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.MainActivity;
import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Doctor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

public class DoctorHomeActivity extends AppCompatActivity {
    private MaterialColors toolbar;
    private RecyclerView rvAppointments;
    private MaterialButton btnPatients, btnSchedule;
    private TextView txtViewAll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupAppointments();
        setupListeners();
    }
    private void initViews(){
        rvAppointments = findViewById(R.id.rvAppointments);
        btnPatients = findViewById(R.id.btnPatients);
        btnSchedule = findViewById(R.id.btnSchedule);
        txtViewAll = findViewById(R.id.txtViewAll);

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(DoctorHomeActivity.this, DoctorProfileActivity.class));
        });
    }
    private void setupAppointments(){
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(new AppointmentAdapter(
                this, DataProvider.getDoctorSchedule(),true, appointment -> {}
        ));
    }
    private void setupListeners(){
        btnPatients.setOnClickListener(v->{
            startActivity(new Intent(DoctorHomeActivity.this, PatientActivity.class));
        });
        btnSchedule.setOnClickListener(v->{
            startActivity(new Intent(DoctorHomeActivity.this, ScheduleActivity.class));
        });
        txtViewAll.setOnClickListener(v->{});
    }
}