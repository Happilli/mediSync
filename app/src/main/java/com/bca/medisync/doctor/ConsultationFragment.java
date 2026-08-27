package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.DoctorTabActivity;
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

public class ConsultationFragment extends Fragment {
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

  private String patientName, latestDiagnosis;
  private int appointmentId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_consultation, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadData();
    setupListeners();
  }

  private void initViews(View view) {
    tvPatientname = view.findViewById(R.id.tvPatientName);
    etComplaint = view.findViewById(R.id.etComplaint);
    etSymptoms = view.findViewById(R.id.etSymptoms);
    etDiagnosis = view.findViewById(R.id.etDiagnosis);
    etNotes = view.findViewById(R.id.etNotes);
    etBloodPressure = view.findViewById(R.id.etBloodPressure);
    etHeartRate = view.findViewById(R.id.etHeartRate);
    etTemperature = view.findViewById(R.id.etTemperature);
    etWeight = view.findViewById(R.id.etWeight);
    btnNextPrescription = view.findViewById(R.id.btnNextPrescription);
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    latestDiagnosis = args != null ? args.getString("latest_diagnosis") : null;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;

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
                    requireContext(),
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
                      if (!isAdded()) return;
                      btnNextPrescription.setEnabled(true);
                      if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), "Consultation saved.", Toast.LENGTH_SHORT)
                            .show();

                        Bundle args = new Bundle();
                        args.putString("patient_name", patientName);
                        args.putInt("appointment_id", appointmentId);
                        args.putString("diagnosis", diagnosis);
                        args.putString("complaint", complaint);
                        args.putString("notes", notes);

                        PrescriptionFragment fragment = new PrescriptionFragment();
                        fragment.setArguments(args);
                        ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
                      } else if (response.code() == 403) {
                        Toast.makeText(
                                requireContext(), "Not your appointment.", Toast.LENGTH_SHORT)
                            .show();
                      } else if (response.code() == 400) {
                        Toast.makeText(
                                requireContext(),
                                "Appointment must be confirmed, or a consultation already exists for it.",
                                Toast.LENGTH_LONG)
                            .show();
                      } else {
                        Toast.makeText(
                                requireContext(),
                                "Failed to save consultation.",
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onFailure(Call<ConsultationResponse> call, Throwable t) {
                      if (!isAdded()) return;
                      btnNextPrescription.setEnabled(true);
                      Toast.makeText(
                              requireContext(),
                              "Network error: " + t.getMessage(),
                              Toast.LENGTH_LONG)
                          .show();
                    }
                  });
        });
  }
}
