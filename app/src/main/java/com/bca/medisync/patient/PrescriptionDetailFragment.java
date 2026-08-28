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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class PrescriptionDetailFragment extends Fragment {
  private MaterialToolbar toolbar;
  private TextView txtDoctorName, txtDiagnosis, txtInstructions, txtFollowUp, txtNoMeds;
  private LinearLayout medicationsContainer;
  private int prescriptionId;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_prescription_detail, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);

    Bundle args = getArguments();
    prescriptionId = args != null ? args.getInt("prescription_id", -1) : -1;
    if (prescriptionId == -1) {
      Toast.makeText(requireContext(), "Invalid prescription", Toast.LENGTH_SHORT).show();
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }
    loadDetail();
  }

  private void initViews(View view) {
    toolbar = view.findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(
        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    txtDoctorName = view.findViewById(R.id.txtDoctorName);
    txtDiagnosis = view.findViewById(R.id.txtDiagnosis);
    txtInstructions = view.findViewById(R.id.txtInstructions);
    txtFollowUp = view.findViewById(R.id.txtFollowUp);
    medicationsContainer = view.findViewById(R.id.medicationsContainer);
    txtNoMeds = view.findViewById(R.id.txtNoMeds);
  }

  private void loadDetail() {
    PrescriptionApi api = ApiClient.getRetrofit().create(PrescriptionApi.class);
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
    DoctorApi doctorApi = ApiClient.getRetrofit().create(DoctorApi.class);
    ApiCallback.handle(
        doctorApi.getDoctorDetail(doctorId),
        this,
        d -> txtDoctorName.setText(d.getName()),
        (code, msg) -> {});
  }

  private void bind(Prescription p) {
    txtDoctorName.setText(p.getDoctor_name());
    txtDiagnosis.setText(p.getDiagnosis());
    txtInstructions.setText(p.getInstructions());
    txtFollowUp.setText("Follow-up: " + p.getFollowUpDate());

    medicationsContainer.removeAllViews();
    if (p.getMedications() == null || p.getMedications().isEmpty()) {
      txtNoMeds.setVisibility(View.VISIBLE);
      return;
    }
    txtNoMeds.setVisibility(View.GONE);
    for (Medication m : p.getMedications()) {
      medicationsContainer.addView(buildMedicationRow(m));
    }
  }

  private MaterialCardView buildMedicationRow(Medication m) {
    MaterialCardView card = new MaterialCardView(requireContext());
    LinearLayout.LayoutParams cardLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    cardLp.bottomMargin = dp(10);
    card.setLayoutParams(cardLp);
    card.setRadius(dp(16));
    card.setCardElevation(0f);
    card.setCardBackgroundColor(requireContext().getColor(R.color.surface_container));

    LinearLayout content = new LinearLayout(requireContext());
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(dp(16), dp(14), dp(16), dp(14));

    TextView name = new TextView(requireContext());
    name.setText(m.getName() + " " + m.getDosage());
    name.setTextSize(15);
    name.setTypeface(null, android.graphics.Typeface.BOLD);
    name.setTextColor(requireContext().getColor(R.color.on_surface));

    TextView freq = new TextView(requireContext());
    freq.setText(m.getFrequency() + " \u2022 " + m.getTime());
    freq.setTextSize(12);
    freq.setTextColor(requireContext().getColor(R.color.on_surface_variant));

    content.addView(name);
    content.addView(freq);

    if (m.getInstruction() != null && !m.getInstruction().isEmpty()) {
      TextView instr = new TextView(requireContext());
      instr.setText(m.getInstruction());
      instr.setTextSize(12);
      instr.setTextColor(requireContext().getColor(R.color.primary));
      instr.setPadding(0, dp(4), 0, 0);
      content.addView(instr);
    }

    card.addView(content);
    return card;
  }

  private int dp(int v) {
    return (int) (v * getResources().getDisplayMetrics().density);
  }
}
