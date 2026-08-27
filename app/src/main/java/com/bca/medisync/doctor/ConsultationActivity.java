package com.bca.medisync.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.ConsultationApi;
import com.bca.medisync.data.remote.dto.consultation.ConsultationCreateRequest;
import com.bca.medisync.data.remote.dto.consultation.ConsultationResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConsultationActivity extends AppCompatActivity {
  private TextView tvPatientname;
  private TextInputEditText etComplaint,
      etSymptoms,
      etDiagnosis,
      etNotes,
      etBloodPressure,
      etHeartRate,
      etTemperature,
      etWeight;
  private MaterialButton btnNextPrescription;

  private String patientName, latestDiagnosis, latestDate;
  private int appointmentId = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_consultation);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    loadData();
    setupListeners();
  }

  private void initViews() {
    tvPatientname = findViewById(R.id.tvPatientName);
    etComplaint = findViewById(R.id.etComplaint);
    etSymptoms = findViewById(R.id.etSymptoms);
    etDiagnosis = findViewById(R.id.etDiagnosis);
    etNotes = findViewById(R.id.etNotes);
    etBloodPressure = findViewById(R.id.etBloodPressure);
    etHeartRate = findViewById(R.id.etHeartRate);
    etTemperature = findViewById(R.id.etTemperature);
    etWeight = findViewById(R.id.etWeight);
    btnNextPrescription = findViewById(R.id.btnNextPrescription);
  }

  private void loadData() {
    patientName = getIntent().getStringExtra("patient_name");
    latestDiagnosis = getIntent().getStringExtra("latest_diagnosis");
    latestDate = getIntent().getStringExtra("latest_date");
    appointmentId = getIntent().getIntExtra("appointment_id", -1);

    if (patientName != null) {
      tvPatientname.setText("Consultation - " + patientName);
    }
    if (latestDiagnosis != null) {
      etDiagnosis.setText(latestDiagnosis);
    }
  }

  private void setupListeners() {
    btnNextPrescription.setOnClickListener(
        v -> {
          String complaint = etComplaint.getText().toString().trim();
          String symptoms = etSymptoms.getText().toString().trim();
          String diagnosis = etDiagnosis.getText().toString().trim();
          String notes = etNotes.getText().toString().trim();
          String bp = etBloodPressure.getText().toString().trim();
          String hr = etHeartRate.getText().toString().trim();
          String temp = etTemperature.getText().toString().trim();
          String weight = etWeight.getText().toString().trim();

          if (complaint.isEmpty()) {
            etComplaint.setError("Chief compliant is required...");
            return;
          }
          if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Diagnosis is requierd..");
            return;
          }
          if (appointmentId == -1) {
            Toast.makeText(
                    this,
                    "Missing appointment reference. Go back and try again.",
                    Toast.LENGTH_LONG)
                .show();
            return;
          }

          btnNextPrescription.setEnabled(false);

          ConsultationApi api = ApiClient.getRetrofit().create(ConsultationApi.class);
          api.createConsultation(
                  new ConsultationCreateRequest(
                      appointmentId, complaint, symptoms, diagnosis, notes, bp, hr, temp, weight))
              .enqueue(
                  new Callback<ConsultationResponse>() {
                    @Override
                    public void onResponse(
                        Call<ConsultationResponse> call, Response<ConsultationResponse> response) {
                      btnNextPrescription.setEnabled(true);
                      if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(
                                ConsultationActivity.this,
                                "Consultation saved.",
                                Toast.LENGTH_SHORT)
                            .show();

                        Intent intent =
                            new Intent(ConsultationActivity.this, PrescriptionActivity.class);
                        intent.putExtra("patient_name", patientName);
                        intent.putExtra("appointment_id", appointmentId);
                        intent.putExtra("diagnosis", diagnosis);
                        intent.putExtra("complaint", complaint);
                        intent.putExtra("notes", notes);
                        startActivity(intent);
                      } else if (response.code() == 403) {
                        Toast.makeText(
                                ConsultationActivity.this,
                                "Not your appointment.",
                                Toast.LENGTH_SHORT)
                            .show();
                      } else if (response.code() == 400) {
                        Toast.makeText(
                                ConsultationActivity.this,
                                "Appointment must be confirmed, or a consultation already exists for it.",
                                Toast.LENGTH_LONG)
                            .show();
                      } else {
                        Toast.makeText(
                                ConsultationActivity.this,
                                "Failed to save consultation.",
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onFailure(Call<ConsultationResponse> call, Throwable t) {
                      btnNextPrescription.setEnabled(true);
                      Toast.makeText(
                              ConsultationActivity.this,
                              "Network error: " + t.getMessage(),
                              Toast.LENGTH_LONG)
                          .show();
                    }
                  });
        });
  }
}
