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
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PrescriptionApi;
import com.bca.medisync.data.remote.dto.prescription.PrescriptionResponse;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionListFragment extends Fragment {
  private MaterialToolbar toolbar;
  private RecyclerView rvPrescriptions;
  private TextView txtEmpty;
  private SimpleListAdapter<Prescription> adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_prescription_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    loadPrescriptions();
  }

  private void initViews(View view) {
    toolbar = view.findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

    rvPrescriptions = view.findViewById(R.id.rvPrescriptions);
    txtEmpty = view.findViewById(R.id.txtEmpty);
    rvPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));

    adapter =
        new SimpleListAdapter<>(
            R.layout.item_prescription,
            new ArrayList<>(),
            (itemView, prescription, pos) -> {
              ((TextView) itemView.findViewById(R.id.txtDiagnosis))
                  .setText(prescription.getDiagnosis());
              ((TextView) itemView.findViewById(R.id.txtDoctorName))
                  .setText(prescription.getDoctor_name());
              ((TextView) itemView.findViewById(R.id.txtDate)).setText(prescription.getCreatedAt());
            },
            prescription -> {
              Bundle args = new Bundle();
              args.putInt("prescription_id", prescription.getId());
              PrescriptionDetailFragment fragment = new PrescriptionDetailFragment();
              fragment.setArguments(args);
              ((MainTabActivity) requireActivity()).pushFragment(fragment);
            });

    rvPrescriptions.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void loadPrescriptions() {
    PrescriptionApi api = ApiClient.getRetrofit().create(PrescriptionApi.class);
    api.getMyPrescriptions()
        .enqueue(
            new Callback<List<PrescriptionResponse>>() {
              @Override
              public void onResponse(
                  Call<List<PrescriptionResponse>> call,
                  Response<List<PrescriptionResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                  if (response.body().isEmpty()) {
                    adapter.updateData(new ArrayList<>());
                    showEmpty(true);
                    return;
                  }
                  PrescriptionEnricher.enrichAll(
                      response.body(),
                      (List<Prescription> enriched) -> {
                        if (!isAdded()) return;
                        adapter.updateData(enriched);
                        showEmpty(enriched.isEmpty());
                      });
                } else {
                  Toast.makeText(
                          requireContext(), "Failed to load prescriptions", Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailure(Call<List<PrescriptionResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(
                        requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  private void showEmpty(boolean empty) {
    EmptyState.bind(rvPrescriptions, txtEmpty, empty);
  }
}
