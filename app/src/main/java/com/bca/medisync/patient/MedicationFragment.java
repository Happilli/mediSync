package com.bca.medisync.patient;

import android.os.Bundle;
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
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicationApi;
import com.bca.medisync.data.remote.dto.medication.MedicationResponse;
import com.google.android.material.button.MaterialButton;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationFragment extends Fragment {
  private RecyclerView rvMedications;
  private TextView tvActiveTime, tvActiveName, tvActiveDosage;
  private MaterialButton btnMarkTaken;
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

    btnMarkTaken.setOnClickListener(
        v -> {
          if (activeMedication != null && !activeMedication.isTaken()) {
            markTaken(activeMedication);
          }
        });
  }

  private void setUpRecyclerView() {
    adapter =
        new MedicationAdapter(
            requireContext(),
            new ArrayList<>(),
            medication ->
                Toast.makeText(requireContext(), medication.getInstruction(), Toast.LENGTH_SHORT)
                    .show(),
            this::markTaken);
    rvMedications.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvMedications.setAdapter(adapter);
  }

  private void loadMedications() {
    MedicationApi api = ApiClient.getRetrofit().create(MedicationApi.class);
    api.getMyMedications()
        .enqueue(
            new Callback<List<MedicationResponse>>() {
              @Override
              public void onResponse(
                  Call<List<MedicationResponse>> call,
                  Response<List<MedicationResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  List<Medication> meds = new ArrayList<>();
                  for (MedicationResponse r : response.body()) {
                    meds.add(mapToMedication(r));
                  }
                  bindMedications(meds);
                } else {
                  Toast.makeText(requireContext(), "Failed to load medications", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<MedicationResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindMedications(List<Medication> meds) {
    adapter.updateData(meds);

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

  private void markTaken(Medication medication) {
    MedicationApi api = ApiClient.getRetrofit().create(MedicationApi.class);
    api.markTaken(medication.getId())
        .enqueue(
            new Callback<MedicationResponse>() {
              @Override
              public void onResponse(
                  Call<MedicationResponse> call, Response<MedicationResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                  Toast.makeText(requireContext(), "Marked as taken", Toast.LENGTH_SHORT).show();
                  loadMedications();
                } else if (response.code() == 403) {
                  Toast.makeText(requireContext(), "Not your medication.", Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          requireContext(), "Failed to update medication.", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<MedicationResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
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
        r.getId(),
        r.getName(),
        r.getDosage(),
        frequencyLabel,
        displayTime,
        r.getDuration_days() + " Days",
        r.isIs_taken(),
        r.getInstruction());
  }
}
