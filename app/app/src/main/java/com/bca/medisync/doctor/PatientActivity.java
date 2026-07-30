package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.PatientAdapter;
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.Patient;
import com.google.android.material.appbar.MaterialToolbar;

public class PatientActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupRecylerView();
        setupToolbar();
    }
    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        rvPatients = findViewById(R.id.rvPatients);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }
    private void setupRecylerView(){
        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        rvPatients.setAdapter(new PatientAdapter(this, DataProvider.getPatients(), patient -> {
            Intent intent = new Intent(PatientActivity.this, PatientDetailsActivity.class);
            intent.putExtra("patient_name", patient.getName());
            intent.putExtra("patient_phone", patient.getPhone());
            intent.putExtra("patient_email", patient.getEmail());
            intent.putExtra("patient_dob", patient.getDateOfBirth());
            intent.putExtra("patient_gender", patient.getGender());
            intent.putExtra("patient_blood", patient.getBloodGroup());
            intent.putExtra("patient_address", patient.getAddress());
            intent.putExtra("patient_emergency", patient.getEmergencyContact());
            startActivity(intent);
        }));
    }
}