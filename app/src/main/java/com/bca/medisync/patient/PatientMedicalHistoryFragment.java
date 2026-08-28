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
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.MedicalHistoryEntry;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.MedicalHistoryApi;
import com.bca.medisync.data.remote.dto.medicalhistory.MedicalHistoryResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    return inflater.inflate(R.layout.activity_patient_medical_history, container, false);
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
  }

  private void loadHistory() {
    MedicalHistoryApi api = ApiClient.getRetrofit().create(MedicalHistoryApi.class);
    api.getMyMedicalHistory()
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
                  adapter.updateData(entries);
                  boolean empty = entries.isEmpty();
                  txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                  rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
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
}
