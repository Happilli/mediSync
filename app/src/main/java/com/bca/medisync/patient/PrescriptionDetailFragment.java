package com.bca.medisync.patient;

import com.bca.medisync.databinding.ItemMedicationSummaryBinding;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.data.model.Medication;
import com.bca.medisync.data.model.Prescription;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.PrescriptionApi;
import com.bca.medisync.data.remote.helpers.PrescriptionEnricher;
import com.bca.medisync.databinding.FragmentPrescriptionDetailBinding;
import com.bca.medisync.util.RoundedListStyler;
import com.bca.medisync.util.ViewUtils;
import java.util.List;

public class PrescriptionDetailFragment
    extends BaseBindingFragment<FragmentPrescriptionDetailBinding> {
  private int prescriptionId;

  @Override
  protected FragmentPrescriptionDetailBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentPrescriptionDetailBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    Bundle args = getArguments();
    prescriptionId = args != null ? args.getInt("prescription_id", -1) : -1;
    if (prescriptionId == -1) {
      Toast.makeText(requireContext(), "Invalid prescription", Toast.LENGTH_SHORT).show();
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }
    loadDetail();
  }

  private void loadDetail() {
    PrescriptionApi api = ApiClient.api(PrescriptionApi.class);
    call(
        api.getPrescriptionDetail(prescriptionId),
        body -> {
          bind(PrescriptionEnricher.mapToPrescription(body, null));
          fetchDoctorName(body.getDoctor_id());
        },
        "Failed to load prescription");
  }

  private void fetchDoctorName(int doctorId) {
    DoctorApi doctorApi = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        doctorApi.getDoctorDetail(doctorId),
        this,
        d -> {
          if (binding != null) binding.txtDoctorName.setText(d.getName());
        },
        (code, msg) -> {});
  }

  private void bind(Prescription p) {
    if (binding == null) return;
    binding.txtDiagnosis.setText(p.getDiagnosis());
    binding.txtInstructions.setText(p.getInstructions());
    String followUp = p.getFollowUpDate();
    if (followUp == null || followUp.isEmpty()) {
      binding.txtFollowUp.setVisibility(View.GONE);
    } else {
      binding.txtFollowUp.setVisibility(View.VISIBLE);
      binding.txtFollowUp.setText("Follow-up: " + followUp);
    }
    binding.txtInstructions.setVisibility(
        p.getInstructions() == null || p.getInstructions().isEmpty() ? View.GONE : View.VISIBLE);
    binding.medicationsContainer.removeAllViews();
    List<Medication> meds = p.getMedications();
    if (meds == null || meds.isEmpty()) {
      binding.txtNoMeds.setVisibility(View.VISIBLE);
      return;
    }
    binding.txtNoMeds.setVisibility(View.GONE);
    for (int i = 0; i < meds.size(); i++) {
      View row = buildMedicationRow(meds.get(i));
      RoundedListStyler.apply(row, i, meds.size());
      binding.medicationsContainer.addView(row);
    }
  }

  private View buildMedicationRow(Medication m) {
    ItemMedicationSummaryBinding rowBinding =
        ItemMedicationSummaryBinding.inflate(
            getLayoutInflater(), binding.medicationsContainer, false);
    rowBinding.txtMedName.setText(m.getName() + " " + m.getDosage());
    rowBinding.txtMedFrequency.setText(m.getFrequency() + " \u2022 " + m.getTime());
    if (m.getInstruction() != null && !m.getInstruction().isEmpty()) {
      rowBinding.txtMedInstruction.setVisibility(View.VISIBLE);
      rowBinding.txtMedInstruction.setText(m.getInstruction());
    }
    return rowBinding.getRoot();
  }
}
