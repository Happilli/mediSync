package com.bca.medisync.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicalHistoryApi;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.databinding.FragmentMedicalHistoryBinding;
import com.bca.medisync.databinding.ItemMedicalHistoryBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class MedicalHistoryFragment extends BaseBindingFragment<FragmentMedicalHistoryBinding> {
  private String patientName;
  private int patientId = -1;
  private int appointmentId = -1;
  private SimpleListAdapter<MedicalHistoryEntry, ItemMedicalHistoryBinding> adapter;

  public MedicalHistoryFragment() {}

  @Override
  protected FragmentMedicalHistoryBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentMedicalHistoryBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    initViews();
    setupListener();
    loadData();
  }

  private void initViews() {
    binding.rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new SimpleListAdapter<>(
            ItemMedicalHistoryBinding::inflate,
            new ArrayList<>(),
            (rowBinding, entry, pos) -> {
              rowBinding.txtDate.setText(entry.getDate());
              rowBinding.txtTitle.setText(entry.getTitle());
              rowBinding.txtDescription.setText(entry.getDescription());
            },
            null);
    binding.rvTimeline.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadData() {
    Bundle args = getArguments();
    patientName = args != null ? args.getString("patient_name") : null;
    patientId = args != null ? args.getInt("patient_id", -1) : -1;
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;
    binding.tvHeader.setText(
        patientName != null ? patientName + "\nOverview" : "Patient\nOverview");
    binding.fabConsult.setVisibility(appointmentId != -1 ? View.VISIBLE : View.GONE);
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
                    r.getDescription(),
                    r.getAppointment_id()));
          }
          binding.tvRxName.setText(entries.isEmpty() ? "No records" : "Latest Record");
          binding.tvRxDesc.setText(entries.isEmpty() ? "" : entries.get(0).getTitle());
          bindTimeline(entries);
        },
        (code, msg) ->
            ApiErrorHandler.with(requireContext())
                .on(403, "You can only view history for patients you've treated.")
                .fallback("Failed to load history")
                .build()
                .run(code, msg));
  }

  private void bindTimeline(List<MedicalHistoryEntry> timeline) {
    adapter.updateData(timeline);
    EmptyState.bind(binding.rvTimeline, binding.txtEmptyTimeline, timeline.isEmpty());
  }

  private void setupListener() {
    binding.fabConsult.setOnClickListener(
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
