package com.bca.medisync.patient;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.PatientApi;
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

    int adherence = (int) Math.round(stats.getMedication_adherence_percent());
    binding.progressAdherence.setProgressCompat(adherence, true);
    binding.txtAdherencePercent.setText(adherence + "%");
    binding.txtAdherenceSub.setText(
        stats.getDoses_taken() + " / " + stats.getDoses_expected() + " doses taken");
    binding.txtTotalAppointments.setText(String.valueOf(stats.getTotal_appointments()));
    binding.txtUpcomingAppointments.setText(String.valueOf(stats.getUpcoming_appointments()));
    binding.txtTotalPrescriptions.setText(String.valueOf(stats.getTotal_prescriptions()));
    binding.txtTotalConsultations.setText(String.valueOf(stats.getTotal_consultations()));
    Map<String, Integer> capitalized = new LinkedHashMap<>();
    if (stats.getAppointments_by_status() != null) {
      for (Map.Entry<String, Integer> e : stats.getAppointments_by_status().entrySet()) {
        capitalized.put(capitalize(e.getKey()), e.getValue());
      }
    }
    StatsChartHelper.bindStatusBreakdown(
        binding.statusBreakdownContainer,
        capitalized,
        stats.getTotal_appointments(),
        "No appointments yet");

    List<String> months = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    if (stats.getAppointments_last_6_months() != null) {
      for (PatientStatsResponse.MonthlyAppointmentCount m : stats.getAppointments_last_6_months()) {
        months.add(m.getMonth());
        counts.add(m.getCount());
      }
    }
    StatsChartHelper.bindMonthlyTrend(binding.monthlyTrendContainer, months, counts, 20);
    bindLatestVitals(stats.getVitals_trend());
  }

  private void bindLatestVitals(PatientStatsResponse.VitalsTrend vitals) {
    binding.vitalsContainer.removeAllViews();
    if (vitals == null) {
      binding.txtNoVitals.setVisibility(View.VISIBLE);
      return;
    }
    boolean any = false;
    any |= addVitalIfPresent("Blood Pressure", vitals.getBlood_pressure());
    any |= addVitalIfPresent("Heart Rate", vitals.getHeart_rate());
    any |= addVitalIfPresent("Temperature", vitals.getTemperature());
    any |= addVitalIfPresent("Weight", vitals.getWeight());
    binding.txtNoVitals.setVisibility(any ? View.GONE : View.VISIBLE);
  }

  private boolean addVitalIfPresent(String label, List<PatientStatsResponse.VitalPoint> points) {
    if (points == null || points.isEmpty()) return false;
    PatientStatsResponse.VitalPoint latest = points.get(points.size() - 1);

    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, ViewUtils.dp(requireContext(), 10), 0, ViewUtils.dp(requireContext(), 10));

    TextView txtLabel = new TextView(requireContext());
    txtLabel.setText(label);
    txtLabel.setTextColor(requireContext().getColor(R.color.on_surface_variant));
    txtLabel.setTextSize(13);
    LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    txtLabel.setLayoutParams(lp);

    TextView txtValue = new TextView(requireContext());
    txtValue.setText(latest.getValue());
    txtValue.setTextColor(requireContext().getColor(R.color.on_surface));
    txtValue.setTextSize(14);
    txtValue.setTypeface(null, android.graphics.Typeface.BOLD);

    row.addView(txtLabel);
    row.addView(txtValue);
    binding.vitalsContainer.addView(row);
    return true;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
