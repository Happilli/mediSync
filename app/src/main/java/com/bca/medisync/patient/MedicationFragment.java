package com.bca.medisync.patient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.adapter.MedicationAdapter;
import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicationApi;
import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import com.bca.medisync.data.remote.helpers.MedicationAlarmScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicationFragment extends Fragment {
  private RecyclerView rvMedications;
  private TextView tvActiveTime, tvActiveName, tvActiveDosage;
  private MaterialButton btnMarkTaken;
  private CircularProgressIndicator progressAdherence;
  private TextView tvAdherenceCount;

  private MedicationAdapter adapter;
  private Medication activeMedication;

  public MedicationFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_medication, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setUpRecyclerView();
    maybeRequestExactAlarmPermission();
    loadMedications();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadMedications();
  }

  private void initViews(View view) {
    rvMedications = view.findViewById(R.id.rvMedications);
    tvActiveTime = view.findViewById(R.id.tvActiveTime);
    tvActiveName = view.findViewById(R.id.tvActiveName);
    tvActiveDosage = view.findViewById(R.id.tvActiveDosage);
    btnMarkTaken = view.findViewById(R.id.btnMarkTaken);
    progressAdherence = view.findViewById(R.id.progressAdherence);
    tvAdherenceCount = view.findViewById(R.id.tvAdherenceCount);

    btnMarkTaken.setOnClickListener(
        v -> {
          if (activeMedication != null && !activeMedication.isTaken()) {
            markTaken(activeMedication);
          }
        });
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
        new MedicationAdapter(
            med ->
                Toast.makeText(requireContext(), med.getInstruction(), Toast.LENGTH_SHORT).show(),
            (med, taken) -> markTaken(med));
    rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvMedications.setAdapter(adapter);
  }

  private void loadMedications() {
    MedicationApi api = ApiClient.api(MedicationApi.class);
    ApiCallback.handle(
        api.getMyMedications(),
        this,
        body -> {
          List<Medication> meds = new ArrayList<>();
          for (MedicationResponse r : body) {
            meds.add(mapToMedication(r));
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
          r.isIs_taken());
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
    adapter.submitList(meds);

    int total = meds.size();
    int taken = 0;
    for (Medication m : meds) {
      if (m.isTaken()) taken++;
    }
    tvAdherenceCount.setText(taken + "/" + total);
    int percent = total == 0 ? 0 : (int) ((taken / (float) total) * 100);
    progressAdherence.setProgressCompat(percent, true);

    activeMedication = null;
    for (Medication m : meds) {
      if (!m.isTaken()) {
        activeMedication = m;
        break;
      }
    }

    if (activeMedication != null) {
      tvActiveName.setText(activeMedication.getName() + " " + activeMedication.getDosage());
      tvActiveDosage.setText(activeMedication.getFrequency());
      tvActiveTime.setText(activeMedication.getTime());
      btnMarkTaken.setEnabled(true);
      btnMarkTaken.setText("Mark as Taken");
    } else {
      tvActiveName.setText(meds.isEmpty() ? "No medications" : "All medications taken");
      tvActiveDosage.setText("");
      tvActiveTime.setText("--");
      btnMarkTaken.setEnabled(false);
      btnMarkTaken.setText("Nothing Pending");
    }
  }

  private Medication mapToMedication(MedicationResponse r) {
    String displayTime = r.getDosage_time();
    try {
      LocalTime t = LocalTime.parse(r.getDosage_time());
      displayTime = t.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
    } catch (Exception ignored) {
    }

    String frequencyLabel =
        r.getFrequency_per_day() + "x Daily \u2022 " + r.getDuration_days() + " Days";

    return new Medication(
        r.getSchedule_id(),
        r.getMedication_id(),
        r.getName(),
        r.getDosage(),
        frequencyLabel,
        displayTime,
        r.getLabel(),
        r.getDuration_days() + " Days",
        r.isIs_taken(),
        r.getInstruction(),
        r.getDoctor_name());
  }
}
