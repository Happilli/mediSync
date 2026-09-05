package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.ConsultationApi;
import com.bca.medisync.data.remote.dto.consultation.ConsultationCreateRequest;
import com.bca.medisync.databinding.FragmentConsultationBinding;

public class ConsultationFragment extends Fragment {

  private FragmentConsultationBinding binding;

  private String patientName, latestDiagnosis;
  private int appointmentId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentConsultationBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadData();
    setupListeners();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    latestDiagnosis = args != null ? args.getString("latest_diagnosis") : null;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;
    String bookingNotes = args != null ? args.getString("booking_notes") : null;
    if (patientName != null) {
      binding.tvPatientName.setText("Consultation - " + patientName);
    }
    if (latestDiagnosis != null) {
      binding.etDiagnosis.setText(latestDiagnosis);
    }
    if (bookingNotes != null && !bookingNotes.trim().isEmpty()) {
      binding.etComplaint.setText(bookingNotes);
    }
    checkExistingConsultation();
  }

  private void checkExistingConsultation() {
    if (appointmentId == -1) return;

    ConsultationApi api = ApiClient.api(ConsultationApi.class);
    ApiCallback.handle(
        api.getConsultationForAppointment(appointmentId),
        this,
        consultation -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("appointment_id", appointmentId);
          args.putString("diagnosis", consultation.getDiagnosis());
          args.putString("complaint", consultation.getComplaint());
          args.putString("notes", consultation.getNotes());

          PrescriptionFragment fragment = new PrescriptionFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).replaceCurrentFragment(fragment);
        },
        (code, msg) -> {});
  }

  private void setupListeners() {
    binding.btnNextPrescription.setOnClickListener(
        v -> {
          String complaint = binding.etComplaint.getText().toString().trim();
          String symptoms = binding.etSymptoms.getText().toString().trim();
          String diagnosis = binding.etDiagnosis.getText().toString().trim();
          String notes = binding.etNotes.getText().toString().trim();
          String bp = binding.etBloodPressure.getText().toString().trim();
          String hr = binding.etHeartRate.getText().toString().trim();
          String temp = binding.etTemperature.getText().toString().trim();
          String weight = binding.etWeight.getText().toString().trim();

          if (complaint.isEmpty()) {
            binding.etComplaint.setError("Chief compliant is required...");
            return;
          }
          if (diagnosis.isEmpty()) {
            binding.etDiagnosis.setError("Diagnosis is requierd..");
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

          binding.btnNextPrescription.setEnabled(false);

          ConsultationApi api = ApiClient.api(ConsultationApi.class);
          ApiCallback.handle(
              api.createConsultation(
                  new ConsultationCreateRequest(
                      appointmentId, complaint, symptoms, diagnosis, notes, bp, hr, temp, weight)),
              this,
              body -> {
                if (binding == null) return;
                binding.btnNextPrescription.setEnabled(true);
                Toast.makeText(requireContext(), "Consultation saved.", Toast.LENGTH_SHORT).show();

                Bundle args = new Bundle();
                args.putString("patient_name", patientName);
                args.putInt("appointment_id", appointmentId);
                args.putString("diagnosis", diagnosis);
                args.putString("complaint", complaint);
                args.putString("notes", notes);

                PrescriptionFragment fragment = new PrescriptionFragment();
                fragment.setArguments(args);
                ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
              },
              (code, msg) -> {
                if (binding == null) return;
                binding.btnNextPrescription.setEnabled(true);
                if (code == 403) {
                  Toast.makeText(requireContext(), "Not your appointment.", Toast.LENGTH_SHORT)
                      .show();
                } else if (code == 409) {
                  fetchExistingConsultationAndContinue();
                } else if (code == 400) {
                  Toast.makeText(
                          requireContext(),
                          "Appointment must be confirmed, or a consultation already exists for it.",
                          Toast.LENGTH_LONG)
                      .show();
                } else if (code == -1) {
                  Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(
                          requireContext(), "Failed to save consultation.", Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });
  }

  private void fetchExistingConsultationAndContinue() {
    if (appointmentId == -1) return;
    ConsultationApi api = ApiClient.api(ConsultationApi.class);
    ApiCallback.handle(
        api.getConsultationForAppointment(appointmentId),
        this,
        consultation -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("appointment_id", appointmentId);
          args.putString("diagnosis", consultation.getDiagnosis());
          args.putString("complaint", consultation.getComplaint());
          args.putString("notes", consultation.getNotes());

          PrescriptionFragment fragment = new PrescriptionFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).replaceCurrentFragment(fragment);
        },
        (code, msg) ->
            Toast.makeText(
                    requireContext(), "Couldn't load existing consultation.", Toast.LENGTH_SHORT)
                .show());
  }
}
