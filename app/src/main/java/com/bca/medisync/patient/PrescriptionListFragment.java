package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bca.medisync.adapter.SimpleListAdapter;
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PrescriptionApi;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.databinding.FragmentPrescriptionListBinding;
import com.bca.medisync.databinding.ItemPrescriptionBinding;
import com.bca.medisync.util.EmptyState;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionListFragment extends Fragment {
  private FragmentPrescriptionListBinding binding;
  private SimpleListAdapter<Prescription, ItemPrescriptionBinding> adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPrescriptionListBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews();
    loadPrescriptions();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void initViews() {
    ViewUtils.setupBackNav(this, binding.toolbar);
    binding.rvPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new SimpleListAdapter<>(
            ItemPrescriptionBinding::inflate,
            new ArrayList<>(),
            (rowBinding, prescription, pos) -> {
              rowBinding.txtDiagnosis.setText(prescription.getDiagnosis());
              rowBinding.txtDoctorName.setText(prescription.getDoctor_name());
              rowBinding.txtDate.setText(prescription.getCreatedAt());
            },
            this::onPrescriptionClicked);
    binding.rvPrescriptions.setAdapter(adapter);
    adapter.setRoundedList(true);
  }

  private void onPrescriptionClicked(Prescription prescription) {
    String status = prescription.getDispenseStatus();
    if (status == null || "not_required".equals(status)) {
      openDetail(prescription);
      return;
    }
    switch (status) {
      case "collected":
        openDetail(prescription);
        break;
      case "ready":
        Toast.makeText(
                requireContext(),
                "Ready! Come collect your medicines at the hospital.",
                Toast.LENGTH_SHORT)
            .show();
        break;
      default:
        Toast.makeText(
                requireContext(), "Your medicines are still being prepared.", Toast.LENGTH_SHORT)
            .show();
    }
  }

  private void openDetail(Prescription prescription) {
    Bundle args = new Bundle();
    args.putInt("prescription_id", prescription.getId());
    PrescriptionDetailFragment fragment = new PrescriptionDetailFragment();
    fragment.setArguments(args);
    ((MainTabActivity) requireActivity()).pushFragment(fragment);
  }

  private void loadPrescriptions() {
    PrescriptionApi api = ApiClient.api(PrescriptionApi.class);
    ApiCallback.handle(
        api.getMyPrescriptions(),
        this,
        body -> {
          if (body.isEmpty()) {
            adapter.updateData(new ArrayList<>());
            showEmpty(true);
            return;
          }
          PrescriptionEnricher.enrichAll(
              body,
              (List<Prescription> enriched) -> {
                if (!isAdded() || binding == null) return;
                adapter.updateData(enriched);
                showEmpty(enriched.isEmpty());
              });
        },
        ApiCallback.simpleError(requireContext(), "Failed to load prescriptions."));
  }

  private void showEmpty(boolean empty) {
    if (binding == null) return;
    EmptyState.bind(binding.rvPrescriptions, binding.txtEmpty, empty);
  }
}
