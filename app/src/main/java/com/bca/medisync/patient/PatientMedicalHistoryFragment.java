package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.bca.medisync.databinding.FragmentPatientMedicalHistoryBinding;
import com.bca.medisync.databinding.ItemMedicalHistoryBinding;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class PatientMedicalHistoryFragment
    extends BaseBindingFragment<FragmentPatientMedicalHistoryBinding> {
  private SimpleListAdapter<MedicalHistoryEntry, ItemMedicalHistoryBinding> adapter;

  @Override
  protected FragmentPatientMedicalHistoryBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentPatientMedicalHistoryBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews();
    loadHistory();
  }

  private void initViews() {
    ViewUtils.setupBackNav(this, binding.toolbar);
    binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new SimpleListAdapter<>(
            ItemMedicalHistoryBinding::inflate,
            new ArrayList<>(),
            (rowBinding, entry, pos) -> {
              rowBinding.txtDate.setText(entry.getDate());
              rowBinding.txtTitle.setText(entry.getTitle());
              rowBinding.txtDescription.setText(entry.getDescription());
            },
            entry -> {
              if (entry.getAppointmentId() == null) return;
              Bundle args = new Bundle();
              args.putInt("appointment_id", entry.getAppointmentId());
              ConsultationDetailFragment fragment = new ConsultationDetailFragment();
              fragment.setArguments(args);
              ((MainTabActivity) requireActivity()).pushFragment(fragment);
            });
    binding.rvHistory.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadHistory() {
    MedicalHistoryApi api = ApiClient.api(MedicalHistoryApi.class);
    ApiCallback.handle(
        api.getMyMedicalHistory(),
        this,
        body -> {
          if (binding == null) return;
          List<MedicalHistoryEntry> entries = new ArrayList<>();
          for (MedicalHistoryResponse r : body) {
            entries.add(
                new MedicalHistoryEntry(
                    PrescriptionEnricher.formatDate(r.getDate()),
                    r.getTitle(),
                    r.getDescription(),
                    r.getAppointment_id()));
          }
          adapter.updateData(entries);
          EmptyState.bind(binding.rvHistory, binding.txtEmpty, entries.isEmpty());
        },
        ApiCallback.simpleError(requireContext(), "Failed to load history."));
  }
}
