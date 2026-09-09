package com.bca.medisync.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.databinding.ItemVitalBinding;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.MonthlyAppointmentCount;
import com.bca.medisync.data.remote.dto.patient.PatientStatsResponse;
import com.bca.medisync.databinding.FragmentPatientStatsBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.StatsChartHelper;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientStatsFragment extends BaseBindingFragment<FragmentPatientStatsBinding> {

  @Override
  protected FragmentPatientStatsBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentPatientStatsBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    loadStats();
  }

  private void loadStats() {
    LoadingHelper.show(binding.loadingIndicator);
    binding.scrollContent.setVisibility(View.GONE);
    PatientApi api = ApiClient.api(PatientApi.class);
    ApiCallback.handle(
        api.getMyStats(),
        this,
        LoadingHelper.wrapSuccess(binding.loadingIndicator, binding.scrollContent, this::bind),
        LoadingHelper.wrapError(
            binding.loadingIndicator,
            binding.scrollContent,
            ApiErrorHandler.with(requireContext()).fallback("Failed to load stats.").build()));
  }

  private void bind(PatientStatsResponse stats) {
    if (binding == null) return;
    int adherence = (int) Math.round(stats.medication_adherence_percent());
    binding.progressAdherence.setProgressCompat(adherence, true);
    binding.txtAdherencePercent.setText(adherence + "%");
    binding.txtAdherenceSub.setText(
        stats.doses_taken() + " / " + stats.doses_expected() + " doses taken");
    binding.txtTotalAppointments.setText(String.valueOf(stats.total_appointments()));
    binding.txtUpcomingAppointments.setText(String.valueOf(stats.upcoming_appointments()));
    binding.txtTotalPrescriptions.setText(String.valueOf(stats.total_prescriptions()));
    binding.txtTotalConsultations.setText(String.valueOf(stats.total_consultations()));
    Map<String, Integer> capitalized = new LinkedHashMap<>();
    if (stats.appointments_by_status() != null) {
      for (Map.Entry<String, Integer> e : stats.appointments_by_status().entrySet()) {
        capitalized.put(capitalize(e.getKey()), e.getValue());
      }
    }
    StatsChartHelper.bindStatusBreakdown(
        binding.statusBreakdownContainer,
        capitalized,
        stats.total_appointments(),
        "No appointments yet");
    List<String> months = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    if (stats.appointments_last_6_months() != null) {
      for (MonthlyAppointmentCount m : stats.appointments_last_6_months()) {
        months.add(m.month());
        counts.add(m.count());
      }
    }
    StatsChartHelper.bindMonthlyTrend(binding.monthlyTrendContainer, months, counts, 20);
    bindLatestVitals(stats.vitals_trend());
  }

  private void bindLatestVitals(PatientStatsResponse.VitalsTrend vitals) {
    binding.vitalsContainer.removeAllViews();
    if (vitals == null) {
      binding.txtNoVitals.setVisibility(View.VISIBLE);
      return;
    }
    boolean any = false;
    any |= addVitalIfPresent("Blood Pressure", vitals.blood_pressure());
    any |= addVitalIfPresent("Heart Rate", vitals.heart_rate());
    any |= addVitalIfPresent("Temperature", vitals.temperature());
    any |= addVitalIfPresent("Weight", vitals.weight());
    binding.txtNoVitals.setVisibility(any ? View.GONE : View.VISIBLE);
  }

  private boolean addVitalIfPresent(String label, List<PatientStatsResponse.VitalPoint> points) {
    if (points == null || points.isEmpty()) return false;
    PatientStatsResponse.VitalPoint latest = points.get(points.size() - 1);
    ItemVitalBinding rowBinding =
        ItemVitalBinding.inflate(getLayoutInflater(), binding.vitalsContainer, false);
    rowBinding.txtVitalLabel.setText(label);
    rowBinding.txtVitalValue.setText(latest.value());
    binding.vitalsContainer.addView(rowBinding.getRoot());
    return true;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
