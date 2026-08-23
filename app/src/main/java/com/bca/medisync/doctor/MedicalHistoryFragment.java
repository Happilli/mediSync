package com.bca.medisync.doctor;

import android.content.Intent;
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
import com.bca.medisync.data.model.DataProvider;
import com.bca.medisync.data.model.MedicalHistory;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicalHistoryApi;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalHistoryFragment extends Fragment {

  private ExtendedFloatingActionButton btnStartConsultation;
  private String patientName;
  private int patientId = -1;
  private TextView tvHeader,
      tvRxName,
      tvRxDesc,
      tvLabTitle,
      tvLabDesc,
      date1,
      title1,
      desc1,
      date2,
      title2,
      desc2;

  public MedicalHistoryFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_medical_history, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupListener();
    loadData();
  }

  private void initViews(View view) {
    btnStartConsultation = view.findViewById(R.id.fabConsult);
    tvHeader = view.findViewById(R.id.tvHeader);
    tvRxDesc = view.findViewById(R.id.tvRxDesc);
    tvRxName = view.findViewById(R.id.tvRxName);
    tvLabTitle = view.findViewById(R.id.tvLabTitle);
    tvLabDesc = view.findViewById(R.id.tvLabDesc);

    date1 = view.findViewById(R.id.date1);
    title1 = view.findViewById(R.id.title1);
    desc1 = view.findViewById(R.id.desc1);

    date2 = view.findViewById(R.id.date2);
    title2 = view.findViewById(R.id.title2);
    desc2 = view.findViewById(R.id.desc2);
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    patientId = args != null ? args.getInt("patient_id", -1) : -1;

    tvHeader.setText(patientName != null ? patientName + "\nOverview" : "Patient\nOverview");

    if (DoctorDataConfig.USE_REAL_MEDICAL_HISTORY && patientId != -1) {
      loadRealHistory();
    } else {
      bindMockHistory();
    }
  }

  private void bindMockHistory() {
    MedicalHistory history = DataProvider.getMedicalHistory(patientName);
    tvRxName.setText(history.getLatestRxName());
    tvRxDesc.setText(history.getLatestRxDesc());
    tvLabTitle.setText(history.getLatestLabTitle());
    tvLabDesc.setText(history.getLatestLabDesc());

    List<MedicalHistoryEntry> timeline = history.getTimeline();
    bindTimeline(timeline);
  }

  private void loadRealHistory() {
    MedicalHistoryApi api = ApiClient.getRetrofit().create(MedicalHistoryApi.class);
    api.getPatientHistory(patientId)
        .enqueue(
            new Callback<List<MedicalHistoryResponse>>() {
              @Override
              public void onResponse(
                  Call<List<MedicalHistoryResponse>> call,
                  Response<List<MedicalHistoryResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  List<MedicalHistoryEntry> entries = new ArrayList<>();
                  for (MedicalHistoryResponse r : response.body()) {
                    entries.add(
                        new MedicalHistoryEntry(
                            PrescriptionEnricher.formatDate(r.getDate()),
                            r.getTitle(),
                            r.getDescription()));
                  }
                  tvRxName.setText(entries.isEmpty() ? "No records" : "Latest Record");
                  tvRxDesc.setText(entries.isEmpty() ? "" : entries.get(0).getTitle());
                  tvLabTitle.setText("");
                  tvLabDesc.setText("");
                  bindTimeline(entries);
                } else if (response.code() == 403) {
                  Toast.makeText(
                          requireContext(),
                          "You can only view history for patients you've treated.",
                          Toast.LENGTH_LONG)
                      .show();
                } else {
                  Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<MedicalHistoryResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void bindTimeline(List<MedicalHistoryEntry> timeline) {
    if (!timeline.isEmpty()) {
      date1.setText(timeline.get(0).getDate());
      title1.setText(timeline.get(0).getTitle());
      desc1.setText(timeline.get(0).getDescription());
    }
    if (timeline.size() >= 2) {
      date2.setText(timeline.get(1).getDate());
      title2.setText(timeline.get(1).getTitle());
      desc2.setText(timeline.get(1).getDescription());
    }
  }

  private void setupListener() {
    btnStartConsultation.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), ConsultationActivity.class);
          intent.putExtra("patient_name", patientName);
          startActivity(intent);
        });
  }
}
