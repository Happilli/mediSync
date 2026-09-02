package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.R;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicalHistoryApi;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.databinding.FragmentPatientMedicalHistoryBinding;
import com.bca.medisync.util.EmptyState;
import java.util.ArrayList;
import java.util.List;

public class PatientMedicalHistoryFragment extends Fragment {

  private FragmentPatientMedicalHistoryBinding binding;
  private SimpleListAdapter<MedicalHistoryEntry> adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPatientMedicalHistoryBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews();
    loadHistory();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void initViews() {
    binding.toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

    binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                    r.getDescription()));
          }
          adapter.updateData(entries);
          EmptyState.bind(binding.rvHistory, binding.txtEmpty, entries.isEmpty());
        },
        ApiCallback.simpleError(requireContext(), "Failed to load history."));
  }
}
