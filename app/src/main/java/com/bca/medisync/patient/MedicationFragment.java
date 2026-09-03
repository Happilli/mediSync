package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.adapter.GroupedListAdapter;
import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicationApi;
import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import com.bca.medisync.data.remote.helpers.MedicationAlarmScheduler;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.databinding.FragmentMedicationBinding;
import com.bca.medisync.databinding.ItemMedicationBinding;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicationFragment extends Fragment {
  private FragmentMedicationBinding binding;
  private GroupedListAdapter<Medication, ItemMedicationBinding> adapter;
  private Medication activeMedication;

  public MedicationFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMedicationBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.btnMarkTaken.setOnClickListener(
        v -> {
          if (activeMedication != null && !activeMedication.isTaken()) {
            markTaken(activeMedication);
          }
        });
    setUpRecyclerView();
    maybeRequestExactAlarmPermission();
    loadMedications();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadMedications();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void maybeRequestExactAlarmPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        && !MedicationAlarmScheduler.canScheduleExactAlarms(requireContext())) {
      Toast.makeText(
              requireContext(),
              "Allow exact alarms so mediSync can remind you to take medicine on time.",
              Toast.LENGTH_LONG)
          .show();
      Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
      startActivity(intent);
    }
  }

  private void setUpRecyclerView() {
    adapter =
        new GroupedListAdapter<>(
            ItemMedicationBinding::inflate,
            med -> med.getDoctorName() == null ? "Unknown" : "Dr. " + med.getDoctorName(),
            this::bindMedicationRow,
            med -> {
              if (!med.isTaken()) {
                Toast.makeText(requireContext(), med.getInstruction(), Toast.LENGTH_SHORT).show();
              }
            });
    binding.rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvMedications.setAdapter(adapter);
  }

  private void bindMedicationRow(
      ItemMedicationBinding rowBinding, Medication m, int posInGroup, int groupSize) {
    rowBinding.tvMedName.setText(m.getName() + " " + m.getDosage());
    rowBinding.tvMedFrequency.setText(m.getFrequency());
    rowBinding.cbTaken.setOnCheckedChangeListener(null);
    rowBinding.cbTaken.setChecked(m.isTaken());
    rowBinding.cbTaken.setEnabled(!m.isTaken());
    rowBinding.cbTaken.setClickable(!m.isTaken());
    if (!m.isTaken()) {
      rowBinding.cbTaken.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            if (isChecked) markTaken(m);
          });
    }
    if (m.isTaken()) {
      rowBinding.tvMedTime.setText("Taken");
      rowBinding.getRoot().setAlpha(0.6f);
    } else {
      rowBinding.tvMedTime.setText(m.getTime());
      rowBinding.getRoot().setAlpha(1f);
    }
  }

  private void loadMedications() {
    MedicationApi api = ApiClient.api(MedicationApi.class);
    ApiCallback.handle(
        api.getMyMedications(),
        this,
        body -> {
          if (binding == null) return;
          List<Medication> meds = new ArrayList<>();
          for (MedicationResponse r : body) {
            meds.add(PrescriptionEnricher.mapMedication(r));
          }
          bindMedications(meds);
          scheduleAllReminders(body);
        },
        ApiCallback.simpleError(requireContext(), "Failed to load medications."));
  }

  private void scheduleAllReminders(List<MedicationResponse> responses) {
    if (!MedicationAlarmScheduler.canScheduleExactAlarms(requireContext())) return;
    for (MedicationResponse r : responses) {
      String endDateStr = r.getEnd_date();
      if (endDateStr == null) continue;
      LocalDate endDate;
      try {
        endDate = LocalDate.parse(endDateStr);
      } catch (Exception e) {
        continue;
      }
      MedicationAlarmScheduler.schedule(
          requireContext(),
          r.getSchedule_id(),
          r.getName(),
          r.getDosage(),
          r.getDosage_time(),
          endDate,
          r.is_taken());
    }
  }

  private void markTaken(Medication medication) {
    MedicationApi api = ApiClient.api(MedicationApi.class);
    ApiCallback.handle(
        api.markTaken(medication.getScheduleId()),
        this,
        body -> {
          Toast.makeText(requireContext(), "Marked as taken", Toast.LENGTH_SHORT).show();
          loadMedications();
        },
        (code, msg) -> {
          if (code == 403) {
            Toast.makeText(requireContext(), "Not your medication.", Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(requireContext(), "Failed to update medication.", Toast.LENGTH_SHORT)
                .show();
          }
          loadMedications();
        });
  }

  private void bindMedications(List<Medication> meds) {
    if (binding == null) return;
    adapter.submitList(meds);
    int total = meds.size();
    int taken = 0;
    for (Medication m : meds) {
      if (m.isTaken()) taken++;
    }
    binding.tvAdherenceCount.setText(taken + "/" + total);
    int percent = total == 0 ? 0 : (int) ((taken / (float) total) * 100);
    binding.progressAdherence.setProgressCompat(percent, true);
    activeMedication = null;
    for (Medication m : meds) {
      if (!m.isTaken()) {
        activeMedication = m;
        break;
      }
    }
    if (activeMedication != null) {
      binding.tvActiveName.setText(activeMedication.getName() + " " + activeMedication.getDosage());
      binding.tvActiveDosage.setText(activeMedication.getFrequency());
      binding.tvActiveTime.setText(activeMedication.getTime());
      binding.btnMarkTaken.setEnabled(true);
      binding.btnMarkTaken.setText("Mark as Taken");
    } else {
      binding.tvActiveName.setText(meds.isEmpty() ? "No medications" : "All medications taken");
      binding.tvActiveDosage.setText("");
      binding.tvActiveTime.setText("--");
      binding.btnMarkTaken.setEnabled(false);
      binding.btnMarkTaken.setText("Nothing Pending");
    }
  }
}
