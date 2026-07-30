package com.bca.medisync.doctor;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PrescriptionActivity extends AppCompatActivity {

    private TextView tvPatientName, tvDiagnosis;
    private TextInputEditText etMedicine, etDosage, etFrequency, etDuration, etInstructions, etFollowUp;
    private MaterialButton btnSavePrescription;

    private String patientName, diagnosis, complaint, notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        loadData();
        setupListeners();
    }
    private void initViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        etMedicine = findViewById(R.id.etMedicine);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etDuration = findViewById(R.id.etDuration);
        etInstructions = findViewById(R.id.etInstructions);
        etFollowUp = findViewById(R.id.etFollowUp);
        btnSavePrescription = findViewById(R.id.btnSavePrescription);
    }

    private void loadData() {
        patientName = getIntent().getStringExtra("patient_name");
        diagnosis = getIntent().getStringExtra("diagnosis");
        complaint = getIntent().getStringExtra("complaint");
        notes = getIntent().getStringExtra("notes");

        if (patientName != null)
            tvPatientName.setText("Prescription ->" + patientName);

        if (diagnosis != null && !diagnosis.isEmpty())
            tvDiagnosis.setText("Diagnosis: " + diagnosis);
    }

    private void setupListeners() {
        btnSavePrescription.setOnClickListener(v -> {
            String medicine     = etMedicine.getText().toString().trim();
            String dosage       = etDosage.getText().toString().trim();
            String frequency    = etFrequency.getText().toString().trim();
            String duration     = etDuration.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();
            String followUp     = etFollowUp.getText().toString().trim();

            if (medicine.isEmpty()) {
                etMedicine.setError("Medicine name is required");
                return;
            }
            if (dosage.isEmpty()) {
                etDosage.setError("Dosage is required");
                return;
            }
            if (frequency.isEmpty()) {
                etFrequency.setError("Frequency is required");
                return;
            }

            Toast.makeText(this, "Prescription saved for " + patientName + "\n" + medicine + " " + dosage
                            + "\n" + frequency + " - " + duration, Toast.LENGTH_LONG).show();
        });
    }
}