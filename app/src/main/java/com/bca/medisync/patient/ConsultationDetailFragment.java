package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.ConsultationApi;
import com.bca.medisync.databinding.FragmentConsultationDetailBinding;
import com.bca.medisync.util.ViewUtils;

public class ConsultationDetailFragment extends Fragment {
  private FragmentConsultationDetailBinding binding;
  private int appointmentId = -1;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentConsultationDetailBinding.inflate(inflater, container, false);
    return binding.getRoot();
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

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadConsultation() {
    ConsultationApi api = ApiClient.api(ConsultationApi.class);
    ApiCallback.handle(
        api.getConsultationForAppointment(appointmentId),
        this,
        c -> {
          if (binding == null) return;
          binding.txtDiagnosis.setText(c.getDiagnosis());
          binding.txtComplaint.setText(c.getComplaint());
          binding.txtSymptoms.setText(c.getSymptoms());
          binding.txtVitals.setText(
              getString(
                  R.string.vitals_format,
                  safe(c.getBlood_pressure()),
                  safe(c.getHeart_rate()),
                  safe(c.getTemperature()),
                  safe(c.getWeight())));
          if (c.getNotes() == null || c.getNotes().isEmpty()) {
            binding.cardNotes.setVisibility(View.GONE);
          } else {
            binding.cardNotes.setVisibility(View.VISIBLE);
            binding.txtNotes.setText(c.getNotes());
          }
        },
        ApiCallback.simpleError(requireContext(), "Failed to load consultation."));
  }

  private String safe(String s) {
    return s == null || s.isEmpty() ? getString(R.string.vitals_not_recorded) : s;
  }
}
