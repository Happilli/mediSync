package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Patient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PatientDetailsActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView txtName, txtGender, txtBlood, txtPhone, txtEmail, txtDob, txtAddress, txtEmergency;
    private MaterialButton btnHistory, btnConsultation;

    private String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        loadData();
        setupListener();
    }
    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        txtName = findViewById(R.id.txtPatientName);
        txtGender = findViewById(R.id.txtGender);
        txtBlood = findViewById(R.id.txtBloodGroup);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        txtDob = findViewById(R.id.txtDob);
        txtAddress = findViewById(R.id.txtAddress);
        txtEmergency = findViewById(R.id.txtEmergency);
        btnHistory = findViewById(R.id.btnHistory);
        btnConsultation = findViewById(R.id.btnConsultation);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }
    private void loadData(){
        patientName = getIntent().getStringExtra("patient_name");
        toolbar.setTitle(patientName);
        txtName.setText(patientName);
        txtGender.setText(getIntent().getStringExtra("patient_gender"));
        txtBlood.setText(getIntent().getStringExtra("patient_blood"));
        txtPhone.setText(getIntent().getStringExtra("patient_phone"));
        txtEmail.setText(getIntent().getStringExtra("patient_email"));
        txtDob.setText(getIntent().getStringExtra("patient_dob"));
        txtAddress.setText(getIntent().getStringExtra("patient_address"));
        txtEmergency.setText(getIntent().getStringExtra("patient_emergency"));
    }
    private  void setupListener(){
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, MedicalHistoryActivity.class);
            intent.putExtra("patient_name", patientName);
            startActivity(intent);
        });
        btnConsultation.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, ConsultationActivity.class);
            intent.putExtra("patient_name", patientName);
            startActivity(intent);
        });
    }
}