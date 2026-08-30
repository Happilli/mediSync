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

import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PrescriptionApi;
import com.bca.medisync.data.remote.dto.medication.MedicationCreateRequest;
import com.bca.medisync.data.remote.dto.medication.MedicationTimeCreateRequest;
import com.bca.medisync.data.remote.dto.prescription.PrescriptionCreateRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PrescriptionFragment extends Fragment {

  private TextView tvPatientName, tvDiagnosis;
  private TextInputEditText etMedicine, etDosage, etDuration, etInstructions;
  private MaterialButton btnAddDosageTime, btnSelectFollowUp, btnSavePrescription;
  private ChipGroup chipDosageTimes;
  private TextView txtSelectedFollowUp;
  private String patientName, diagnosis, notes;
  private int appointmentId = -1;

  private final List<MedicationTimeCreateRequest> dosageTimes = new ArrayList<>();
  private String selectedFollowUpIso;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_prescription, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadData();
    setupListeners();
  }

  private void initViews(View view) {
    tvPatientName = view.findViewById(R.id.tvPatientName);
    tvDiagnosis = view.findViewById(R.id.tvDiagnosis);
    etMedicine = view.findViewById(R.id.etMedicine);
    etDosage = view.findViewById(R.id.etDosage);
    etDuration = view.findViewById(R.id.etDuration);
    etInstructions = view.findViewById(R.id.etInstructions);
    btnAddDosageTime = view.findViewById(R.id.btnSelectDosageTime);
    chipDosageTimes = view.findViewById(R.id.chipDosageTimes);
    btnSelectFollowUp = view.findViewById(R.id.btnSelectFollowUp);
    txtSelectedFollowUp = view.findViewById(R.id.txtSelectedFollowUp);
    btnSavePrescription = view.findViewById(R.id.btnSavePrescription);
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    diagnosis = args != null ? args.getString("diagnosis") : null;
    notes = args != null ? args.getString("notes") : null;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;

    if (patientName != null) tvPatientName.setText("Prescription -> " + patientName);
    if (diagnosis != null && !diagnosis.isEmpty()) tvDiagnosis.setText("Diagnosis: " + diagnosis);
  }

  private void setupListeners() {
    btnAddDosageTime.setOnClickListener(v -> showAddDosageTimePicker());
    btnSelectFollowUp.setOnClickListener(v -> showFollowUpDatePicker());
    btnSavePrescription.setOnClickListener(v -> attemptSave());
  }

  private void showAddDosageTimePicker() {
    MaterialTimePicker picker =
        new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText("Add Dosage Time")
            .build();
    picker.addOnPositiveButtonClickListener(
        v -> {
          int hour = picker.getHour();
          int minute = picker.getMinute();
          String time24 = String.format(Locale.US, "%02d:%02d:00", hour, minute);
          String label = inferLabel(hour);

          dosageTimes.add(new MedicationTimeCreateRequest(time24, label));
          addDosageTimeChip(time24, label, hour, minute);
        });
    picker.show(getParentFragmentManager(), "DOSAGE_TIME_PICKER");
  }

  private String inferLabel(int hour) {
    if (hour < 11) return "Morning";
    if (hour < 16) return "Afternoon";
    if (hour < 20) return "Evening";
    return "Night";
  }

  private void addDosageTimeChip(String time24, String label, int hour, int minute) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    String display = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.getTime());

    Chip chip = new Chip(requireContext());
    chip.setText(label + " \u2022 " + display);
    chip.setCloseIconVisible(true);
    chip.setOnCloseIconClickListener(
        v -> {
          int index = chipDosageTimes.indexOfChild(chip);
          chipDosageTimes.removeView(chip);
          if (index >= 0 && index < dosageTimes.size()) {
            dosageTimes.remove(index);
          }
        });
    chipDosageTimes.addView(chip);
  }

  private void showFollowUpDatePicker() {
    MaterialDatePicker<Long> picker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select Follow-up Date").build();
    picker.addOnPositiveButtonClickListener(
        dateMillis -> {
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(dateMillis);
          selectedFollowUpIso =
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                  .format(cal.getTime());
          String display =
              new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.getTime());
          txtSelectedFollowUp.setText(display);
        });
    picker.show(getParentFragmentManager(), "FOLLOWUP_DATE_PICKER");
  }

  private void attemptSave() {
    String medicine = etMedicine.getText().toString().trim();
    String dosage = etDosage.getText().toString().trim();
    String durationStr = etDuration.getText().toString().trim();
    String instructions = etInstructions.getText().toString().trim();

    if (medicine.isEmpty()) {
      etMedicine.setError("Medicine name is required");
      return;
    }
    if (dosage.isEmpty()) {
      etDosage.setError("Dosage is required");
      return;
    }
    if (durationStr.isEmpty()) {
      etDuration.setError("Duration is required");
      return;
    }
    if (dosageTimes.isEmpty()) {
      Toast.makeText(requireContext(), "Add at least one dosage time", Toast.LENGTH_SHORT).show();
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

    int durationDays;
    try {
      durationDays = Integer.parseInt(durationStr);
    } catch (NumberFormatException e) {
      Toast.makeText(requireContext(), "Duration must be a number", Toast.LENGTH_SHORT).show();
      return;
    }
    if (durationDays < 1) {
      etDuration.setError("Duration must be at least 1 day");
      return;
    }

    MedicationCreateRequest medication =
        new MedicationCreateRequest(
            medicine,
            dosage,
            instructions.isEmpty() ? "" : instructions,
            durationDays,
            new ArrayList<>(dosageTimes));

    List<MedicationCreateRequest> medications = Collections.singletonList(medication);

    PrescriptionCreateRequest request =
        new PrescriptionCreateRequest(
            appointmentId,
            diagnosis != null ? diagnosis : "",
            instructions.isEmpty() ? (notes != null ? notes : "") : instructions,
            selectedFollowUpIso,
            medications);

    btnSavePrescription.setEnabled(false);

    PrescriptionApi api = ApiClient.api(PrescriptionApi.class);
    ApiCallback.handle(
        api.createPrescription(request),
        this,
        body -> {
          btnSavePrescription.setEnabled(true);
          Toast.makeText(requireContext(), "Prescription saved.", Toast.LENGTH_SHORT).show();
          ((DoctorTabActivity) requireActivity()).popToRoot();
        },
        (code, msg) -> {
          btnSavePrescription.setEnabled(true);
          if (code == 403) {
            Toast.makeText(requireContext(), "Not your appointment.", Toast.LENGTH_SHORT).show();
          } else if (code == 400) {
            Toast.makeText(
                    requireContext(),
                    "Appointment must be confirmed with a consultation recorded, and can't already have a prescription.",
                    Toast.LENGTH_LONG)
                .show();
          } else if (code == 404) {
            Toast.makeText(requireContext(), "Appointment not found.", Toast.LENGTH_SHORT).show();
          } else if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(requireContext(), "Failed to save prescription.", Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
