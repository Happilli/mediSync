package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PrescriptionApi;
import com.bca.medisync.data.remote.dto.prescription.PrescriptionResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionDetailActivity extends AppCompatActivity {
  private MaterialToolbar toolbar;
  private TextView txtDoctorName, txtDiagnosis, txtInstructions, txtFollowUp, txtNoMeds;
  private LinearLayout medicationsContainer;
  private int prescriptionId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_prescription_detail);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });
    initViews();
    prescriptionId = getIntent().getIntExtra("prescription_id", -1);
    if (prescriptionId == -1) {
      Toast.makeText(this, "Invalid prescription", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    loadDetail();
  }

  private void initViews() {
    toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());
    txtDoctorName = findViewById(R.id.txtDoctorName);
    txtDiagnosis = findViewById(R.id.txtDiagnosis);
    txtInstructions = findViewById(R.id.txtInstructions);
    txtFollowUp = findViewById(R.id.txtFollowUp);
    medicationsContainer = findViewById(R.id.medicationsContainer);
    txtNoMeds = findViewById(R.id.txtNoMeds);
  }

  private void loadDetail() {
    PrescriptionApi api = ApiClient.getRetrofit().create(PrescriptionApi.class);
    api.getPrescriptionDetail(prescriptionId)
        .enqueue(
            new Callback<PrescriptionResponse>() {
              @Override
              public void onResponse(
                  Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  bind(PrescriptionEnricher.mapToPrescription(response.body(), null));
                  // fetch doctor name separately so the screen isn't blocked on it
                  fetchDoctorName(response.body().getDoctor_id());
                } else {
                  Toast.makeText(
                          PrescriptionDetailActivity.this,
                          "Failed to load prescription",
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                Toast.makeText(
                        PrescriptionDetailActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void fetchDoctorName(int doctorId) {
    com.bca.medisync.data.remote.api.DoctorApi doctorApi =
        ApiClient.getRetrofit().create(com.bca.medisync.data.remote.api.DoctorApi.class);
    doctorApi
        .getDoctorDetail(doctorId)
        .enqueue(
            new Callback<com.bca.medisync.data.remote.dto.doctor.DoctorResponse>() {
              @Override
              public void onResponse(
                  Call<com.bca.medisync.data.remote.dto.doctor.DoctorResponse> call,
                  Response<com.bca.medisync.data.remote.dto.doctor.DoctorResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  txtDoctorName.setText(response.body().getName());
                }
              }

              @Override
              public void onFailure(
                  Call<com.bca.medisync.data.remote.dto.doctor.DoctorResponse> call, Throwable t) {}
            });
  }

  private void bind(Prescription p) {
    txtDoctorName.setText(p.getDoctor_name());
    txtDiagnosis.setText(p.getDiagnosis());
    txtInstructions.setText(p.getInstructions());
    txtFollowUp.setText("Follow-up: " + p.getFollowUpDate());

    medicationsContainer.removeAllViews();
    if (p.getMedications() == null || p.getMedications().isEmpty()) {
      txtNoMeds.setVisibility(android.view.View.VISIBLE);
      return;
    }
    txtNoMeds.setVisibility(android.view.View.GONE);
    for (Medication m : p.getMedications()) {
      medicationsContainer.addView(buildMedicationRow(m));
    }
  }

  private MaterialCardView buildMedicationRow(Medication m) {
    MaterialCardView card = new MaterialCardView(this);
    LinearLayout.LayoutParams cardLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    cardLp.bottomMargin = dp(10);
    card.setLayoutParams(cardLp);
    card.setRadius(dp(16));
    card.setCardElevation(0f);
    card.setCardBackgroundColor(getColor(R.color.surface_container));

    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(dp(16), dp(14), dp(16), dp(14));

    TextView name = new TextView(this);
    name.setText(m.getName() + " " + m.getDosage());
    name.setTextSize(15);
    name.setTypeface(null, android.graphics.Typeface.BOLD);
    name.setTextColor(getColor(R.color.on_surface));

    TextView freq = new TextView(this);
    freq.setText(m.getFrequency() + " \u2022 " + m.getTime());
    freq.setTextSize(12);
    freq.setTextColor(getColor(R.color.on_surface_variant));

    content.addView(name);
    content.addView(freq);

    if (m.getInstruction() != null && !m.getInstruction().isEmpty()) {
      TextView instr = new TextView(this);
      instr.setText(m.getInstruction());
      instr.setTextSize(12);
      instr.setTextColor(getColor(R.color.primary));
      instr.setPadding(0, dp(4), 0, 0);
      content.addView(instr);
    }

    card.addView(content);
    return card;
  }

  private int dp(int v) {
    return (int) (v * getResources().getDisplayMetrics().density);
  }
}
