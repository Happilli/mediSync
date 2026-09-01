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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicalHistoryApi;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MedicalHistoryFragment extends Fragment {

  private ExtendedFloatingActionButton btnStartConsultation;
  private String patientName;
  private int patientId = -1;
  private int appointmentId = -1;
  private TextView tvHeader, tvRxName, tvRxDesc, tvLabTitle, tvLabDesc, txtEmptyTimeline;
  private RecyclerView rvTimeline;
  private SimpleListAdapter<MedicalHistoryEntry> adapter;

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
    txtEmptyTimeline = view.findViewById(R.id.txtEmptyTimeline);
    rvTimeline = view.findViewById(R.id.rvTimeline);

    rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new SimpleListAdapter<>(
            R.layout.item_medical_history,
            new ArrayList<>(),
            (itemView, entry, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtDate)).setText(entry.getDate());
              ((TextView) itemView.findViewById(R.id.txtTitle)).setText(entry.getTitle());
              ((TextView) itemView.findViewById(R.id.txtDescription))
                  .setText(entry.getDescription());
            },
            null);
    rvTimeline.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    patientId = args != null ? args.getInt("patient_id", -1) : -1;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;

    tvHeader.setText(patientName != null ? patientName + "\nOverview" : "Patient\nOverview");

    btnStartConsultation.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);

    if (patientId == -1) {
      Toast.makeText(requireContext(), "Missing patient reference.", Toast.LENGTH_SHORT).show();
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }

    loadRealHistory();
  }

  private void loadRealHistory() {
    MedicalHistoryApi api = ApiClient.api(MedicalHistoryApi.class);
    ApiCallback.handle(
        api.getPatientHistory(patientId),
        this,
        body -> {
          List<MedicalHistoryEntry> entries = new ArrayList<>();
          for (MedicalHistoryResponse r : body) {
            entries.add(
                new MedicalHistoryEntry(
                    PrescriptionEnricher.formatDate(r.getDate()),
                    r.getTitle(),
                    r.getDescription()));
          }
          tvRxName.setText(entries.isEmpty() ? "No records" : "Latest Record");
          tvRxDesc.setText(entries.isEmpty() ? "" : entries.get(0).getTitle());
          bindTimeline(entries);
        },
        (code, msg) -> {
          if (code == 403) {
            Toast.makeText(
                    requireContext(),
                    "You can only view history for patients you've treated.",
                    Toast.LENGTH_LONG)
                .show();
          } else if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void bindTimeline(List<MedicalHistoryEntry> timeline) {
    adapter.updateData(timeline);
    EmptyState.bind(rvTimeline, txtEmptyTimeline, timeline.isEmpty());
  }

  private void setupListener() {
    btnStartConsultation.setOnClickListener(
        v -> {
          Bundle args = new Bundle();
          args.putString("patient_name", patientName);
          args.putInt("appointment_id", appointmentId);
          ConsultationFragment fragment = new ConsultationFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        });
  }
}
