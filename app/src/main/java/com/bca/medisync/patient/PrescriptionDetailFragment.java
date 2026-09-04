package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
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

public class PrescriptionDetailFragment extends Fragment {
  private FragmentPrescriptionDetailBinding binding;
  private int prescriptionId;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPrescriptionDetailBinding.inflate(inflater, container, false);
    return binding.getRoot();
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

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadDetail() {
    PrescriptionApi api = ApiClient.api(PrescriptionApi.class);
    ApiCallback.handle(
        api.getPrescriptionDetail(prescriptionId),
        this,
        body -> {
          bind(PrescriptionEnricher.mapToPrescription(body, null));
          fetchDoctorName(body.getDoctor_id());
        },
        (code, msg) -> {
          if (code == -1) {
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(requireContext(), "Failed to load prescription", Toast.LENGTH_SHORT)
                .show();
          }
        });
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
    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.VERTICAL);
    row.setPadding(
        ViewUtils.dp(requireContext(), 16),
        ViewUtils.dp(requireContext(), 14),
        ViewUtils.dp(requireContext(), 16),
        ViewUtils.dp(requireContext(), 14));
    TextView name = new TextView(requireContext());
    name.setText(m.getName() + " " + m.getDosage());
    name.setTextSize(15);
    name.setTypeface(null, android.graphics.Typeface.BOLD);
    name.setTextColor(requireContext().getColor(R.color.on_surface));
    TextView freq = new TextView(requireContext());
    freq.setText(m.getFrequency() + " \u2022 " + m.getTime());
    freq.setTextSize(12);
    freq.setTextColor(requireContext().getColor(R.color.on_surface_variant));
    LinearLayout.LayoutParams freqLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    freqLp.topMargin = ViewUtils.dp(requireContext(), 2);
    freq.setLayoutParams(freqLp);
    row.addView(name);
    row.addView(freq);
    if (m.getInstruction() != null && !m.getInstruction().isEmpty()) {
      TextView instr = new TextView(requireContext());
      instr.setText(m.getInstruction());
      instr.setTextSize(12);
      instr.setTextColor(requireContext().getColor(R.color.primary));
      LinearLayout.LayoutParams instrLp =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      instrLp.topMargin = ViewUtils.dp(requireContext(), 6);
      instr.setLayoutParams(instrLp);
      row.addView(instr);
    }
    return row;
  }
}
