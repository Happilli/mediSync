package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.ConsultationApi;
import com.bca.medisync.databinding.FragmentConsultationDetailBinding;
import com.bca.medisync.util.ViewUtils;

public class ConsultationDetailFragment
    extends BaseBindingFragment<FragmentConsultationDetailBinding> {
  private int appointmentId = -1;

  @Override
  protected FragmentConsultationDetailBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentConsultationDetailBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    Bundle args = getArguments();
    appointmentId = args != null ? args.getInt("appointment_id", -1) : -1;
    if (appointmentId == -1) {
      requireActivity().getOnBackPressedDispatcher().onBackPressed();
      return;
    }
    loadConsultation();
  }

  private void loadConsultation() {
    ConsultationApi api = ApiClient.api(ConsultationApi.class);
    call(
        api.getConsultationForAppointment(appointmentId),
        c -> {
          if (binding == null) return;
          binding.txtDiagnosis.setText(c.diagnosis());
          binding.txtComplaint.setText(c.complaint());
          binding.txtSymptoms.setText(c.symptoms());
          binding.txtVitals.setText(
              getString(
                  R.string.vitals_format,
                  safe(c.blood_pressure()),
                  safe(c.heart_rate()),
                  safe(c.temperature()),
                  safe(c.weight())));
          if (c.notes() == null || c.notes().isEmpty()) {
            binding.cardNotes.setVisibility(View.GONE);
          } else {
            binding.cardNotes.setVisibility(View.VISIBLE);
            binding.txtNotes.setText(c.notes());
          }
        },
        "Failed to load consultation.");
  }

  private String safe(String s) {
    return s == null || s.isEmpty() ? getString(R.string.vitals_not_recorded) : s;
  }
}
