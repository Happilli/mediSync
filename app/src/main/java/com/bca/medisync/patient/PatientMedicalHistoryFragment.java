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
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class PatientMedicalHistoryFragment extends Fragment {
  private MaterialToolbar toolbar;
  private RecyclerView rvHistory;
  private android.widget.TextView txtEmpty;
  private SimpleListAdapter<MedicalHistoryEntry> adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_patient_medical_history, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadHistory();
  }

  private void initViews(View view) {
    toolbar = view.findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    rvHistory = view.findViewById(R.id.rvHistory);
    txtEmpty = view.findViewById(R.id.txtEmpty);
    rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

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
    rvHistory.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadHistory() {
    MedicalHistoryApi api = ApiClient.getRetrofit().create(MedicalHistoryApi.class);
    ApiCallback.handle(
        api.getMyMedicalHistory(),
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
          adapter.updateData(entries);
          EmptyState.bind(rvHistory, txtEmpty, entries.isEmpty());
        },
        ApiCallback.simpleError(requireContext(), "Failed to load history."));
  }
}
