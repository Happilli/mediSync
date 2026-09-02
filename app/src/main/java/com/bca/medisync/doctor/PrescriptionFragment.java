package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.bca.medisync.databinding.FragmentPrescriptionBinding;
import com.bca.medisync.util.ViewUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PrescriptionFragment extends Fragment {

  private FragmentPrescriptionBinding binding;

  private String patientName, diagnosis, notes;
  private int appointmentId = -1;

  private final List<MedicationTimeCreateRequest> draftDosageTimes = new ArrayList<>();

  private final List<MedicationCreateRequest> medications = new ArrayList<>();
  private final List<String> medicationSummaries = new ArrayList<>();

  private String selectedFollowUpIso;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPrescriptionBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadData();
    setupListeners();
    refreshAddedMedicinesUi();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    diagnosis = args != null ? args.getString("diagnosis") : null;
    notes = args != null ? args.getString("notes") : null;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;

    if (patientName != null) binding.tvPatientName.setText("Prescription -> " + patientName);
    if (diagnosis != null && !diagnosis.isEmpty())
      binding.tvDiagnosis.setText("Diagnosis: " + diagnosis);
  }

  private void setupListeners() {
    binding.btnSelectDosageTime.setOnClickListener(v -> showAddDosageTimePicker());
    binding.btnAddMedicine.setOnClickListener(v -> attemptAddMedicine());
    binding.btnSelectFollowUp.setOnClickListener(v -> showFollowUpDatePicker());
    binding.btnClearFollowUp.setOnClickListener(v -> clearFollowUp());
    binding.btnSavePrescription.setOnClickListener(v -> attemptSavePrescription());
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

          draftDosageTimes.add(new MedicationTimeCreateRequest(time24, label));
          addDosageTimeChip(label, hour, minute);
        });
    picker.show(getParentFragmentManager(), "DOSAGE_TIME_PICKER");
  }

  private String inferLabel(int hour) {
    if (hour < 11) return "Morning";
    if (hour < 16) return "Afternoon";
    if (hour < 20) return "Evening";
    return "Night";
  }

  private void addDosageTimeChip(String label, int hour, int minute) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    String display = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.getTime());

    Chip chip = new Chip(requireContext());
    chip.setText(label + " \u2022 " + display);
    chip.setCloseIconVisible(true);
    chip.setOnCloseIconClickListener(
        v -> {
          int index = binding.chipDosageTimes.indexOfChild(chip);
          binding.chipDosageTimes.removeView(chip);
          if (index >= 0 && index < draftDosageTimes.size()) {
            draftDosageTimes.remove(index);
          }
        });
    binding.chipDosageTimes.addView(chip);
  }

  private void attemptAddMedicine() {
    String medicine = binding.etMedicine.getText().toString().trim();
    String dosage = binding.etDosage.getText().toString().trim();
    String durationStr = binding.etDuration.getText().toString().trim();
    String instructions = binding.etInstructions.getText().toString().trim();

    if (medicine.isEmpty()) {
      binding.etMedicine.setError("Medicine name is required");
      return;
    }
    if (dosage.isEmpty()) {
      binding.etDosage.setError("Dosage is required");
      return;
    }
    if (durationStr.isEmpty()) {
      binding.etDuration.setError("Duration is required");
      return;
    }
    if (draftDosageTimes.isEmpty()) {
      Toast.makeText(requireContext(), "Add at least one dosage time", Toast.LENGTH_SHORT).show();
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
      binding.etDuration.setError("Duration must be at least 1 day");
      return;
    }

    MedicationCreateRequest medication =
        new MedicationCreateRequest(
            medicine,
            dosage,
            instructions.isEmpty() ? "" : instructions,
            durationDays,
            new ArrayList<>(draftDosageTimes));

    medications.add(medication);
    medicationSummaries.add(
        medicine
            + " "
            + dosage
            + " \u2022 "
            + draftDosageTimes.size()
            + "x Daily \u2022 "
            + durationDays
            + " Days");

    refreshAddedMedicinesUi();
    clearDraftForm();

    Toast.makeText(requireContext(), medicine + " added.", Toast.LENGTH_SHORT).show();
  }

  private void clearDraftForm() {
    binding.etMedicine.setText("");
    binding.etDosage.setText("");
    binding.etDuration.setText("");
    binding.etInstructions.setText("");
    draftDosageTimes.clear();
    binding.chipDosageTimes.removeAllViews();
  }

  private void refreshAddedMedicinesUi() {
    binding.addedMedicinesContainer.removeAllViews();

    if (medications.isEmpty()) {
      binding.txtNoMedicinesAdded.setVisibility(View.VISIBLE);
      return;
    }
    binding.txtNoMedicinesAdded.setVisibility(View.GONE);

    for (int i = 0; i < medications.size(); i++) {
      binding.addedMedicinesContainer.addView(buildAddedMedicineRow(i));
    }
  }

  private View buildAddedMedicineRow(int index) {
    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(android.view.Gravity.CENTER_VERTICAL);
    row.setPadding(dp(16), dp(12), dp(16), dp(12));

    TextView txt = new TextView(requireContext());
    txt.setText(medicationSummaries.get(index));
    txt.setTextColor(requireContext().getColor(R.color.on_surface));
    txt.setTextSize(13);
    LinearLayout.LayoutParams txtLp =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    txt.setLayoutParams(txtLp);

    TextView remove = new TextView(requireContext());
    remove.setText("Remove");
    remove.setTextColor(requireContext().getColor(R.color.error));
    remove.setTextSize(12);
    remove.setTypeface(null, android.graphics.Typeface.BOLD);
    remove.setPadding(dp(8), dp(4), dp(8), dp(4));
    remove.setOnClickListener(
        v -> {
          medications.remove(index);
          medicationSummaries.remove(index);
          refreshAddedMedicinesUi();
        });

    row.addView(txt);
    row.addView(remove);
    return row;
  }

  private int dp(int v) {
    return ViewUtils.dp(requireContext(), v);
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
          binding.txtSelectedFollowUp.setText(display);
          binding.btnClearFollowUp.setVisibility(View.VISIBLE);
        });
    picker.show(getParentFragmentManager(), "FOLLOWUP_DATE_PICKER");
  }

  private void clearFollowUp() {
    selectedFollowUpIso = null;
    binding.txtSelectedFollowUp.setText("No follow-up needed");
    binding.btnClearFollowUp.setVisibility(View.GONE);
  }

  private void attemptSavePrescription() {
    if (medications.isEmpty()) {
      Toast.makeText(requireContext(), "Add at least one medicine.", Toast.LENGTH_SHORT).show();
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

    String instructions = binding.etInstructions.getText().toString().trim();

    PrescriptionCreateRequest request =
        new PrescriptionCreateRequest(
            appointmentId,
            diagnosis != null ? diagnosis : "",
            instructions.isEmpty() ? (notes != null ? notes : "") : instructions,
            selectedFollowUpIso,
            new ArrayList<>(medications));

    binding.btnSavePrescription.setEnabled(false);

    PrescriptionApi api = ApiClient.api(PrescriptionApi.class);
    ApiCallback.handle(
        api.createPrescription(request),
        this,
        body -> {
          if (binding != null) binding.btnSavePrescription.setEnabled(true);
          Toast.makeText(requireContext(), "Prescription saved.", Toast.LENGTH_SHORT).show();
          ((DoctorTabActivity) requireActivity()).popToRoot();
        },
        (code, msg) -> {
          if (binding == null) return;
          binding.btnSavePrescription.setEnabled(true);
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
