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
import com.bca.medisync.util.ViewUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientStatsFragment extends BaseBindingFragment<FragmentPatientStatsBinding> {

  private final int[] dotShapes = {
    R.drawable.cookie9sided,
    R.drawable.clover4leaf,
    R.drawable.burst,
    R.drawable.softboom,
    R.drawable.gem,
    R.drawable.pentagon,
    R.drawable.flower,
  };

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
    bindStatusBreakdown(stats.getAppointments_by_status(), stats.getTotal_appointments());
    bindMonthlyTrend(stats.getAppointments_last_6_months());
    bindLatestVitals(stats.getVitals_trend());
  }

  private void bindStatusBreakdown(Map<String, Integer> statusMap, int total) {
    binding.statusBreakdownContainer.removeAllViews();
    if (statusMap == null || statusMap.isEmpty() || total == 0) {
      TextView empty = new TextView(requireContext());
      empty.setText("No appointments yet");
      empty.setTextColor(requireContext().getColor(R.color.on_surface_variant));
      binding.statusBreakdownContainer.addView(empty);
      return;
    }
    int i = 0;
    for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
      binding.statusBreakdownContainer.addView(
          buildStatusRow(capitalize(entry.getKey()), entry.getValue(), i));
      i++;
    }
  }

  private View buildStatusRow(String label, int count, int index) {
    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, ViewUtils.dp(requireContext(), 10), 0, ViewUtils.dp(requireContext(), 10));

    ImageView dot = new ImageView(requireContext());
    int dotSize = ViewUtils.dp(requireContext(), 18);
    LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
    dotLp.setMarginEnd(ViewUtils.dp(requireContext(), 10));
    dot.setLayoutParams(dotLp);
    dot.setImageResource(dotShapes[index % dotShapes.length]);
    dot.setColorFilter(requireContext().getColor(statusColor(label)), PorterDuff.Mode.SRC_IN);

    TextView txtLabel = new TextView(requireContext());
    txtLabel.setText(label);
    txtLabel.setTextColor(requireContext().getColor(R.color.on_surface));
    txtLabel.setTextSize(13);
    LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    txtLabel.setLayoutParams(lp);

    TextView txtCount = new TextView(requireContext());
    txtCount.setText(String.valueOf(count));
    txtCount.setTextColor(requireContext().getColor(R.color.on_surface));
    txtCount.setTextSize(13);
    txtCount.setTypeface(null, android.graphics.Typeface.BOLD);

    row.addView(dot);
    row.addView(txtLabel);
    row.addView(txtCount);
    return row;
  }

  private int statusColor(String status) {
    switch (status) {
      case "Confirmed":
        return R.color.tertiary;
      case "Pending":
        return R.color.secondary;
      case "Cancelled":
        return R.color.error;
      default:
        return R.color.primary;
    }
  }

  private void bindMonthlyTrend(List<PatientStatsResponse.MonthlyAppointmentCount> monthly) {
    binding.monthlyTrendContainer.removeAllViews();
    if (monthly == null || monthly.isEmpty()) return;

    int max = 1;
    for (PatientStatsResponse.MonthlyAppointmentCount m : monthly) {
      max = Math.max(max, m.getCount());
    }

    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.BOTTOM);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dp(requireContext(), 140)));

    for (PatientStatsResponse.MonthlyAppointmentCount m : monthly) {
      LinearLayout col = new LinearLayout(requireContext());
      col.setOrientation(LinearLayout.VERTICAL);
      col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
      LinearLayout.LayoutParams colLp =
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
      col.setLayoutParams(colLp);

      TextView countText = new TextView(requireContext());
      countText.setText(String.valueOf(m.getCount()));
      countText.setTextSize(11);
      countText.setTextColor(requireContext().getColor(R.color.on_surface_variant));
      countText.setGravity(Gravity.CENTER);

      View bar = new View(requireContext());
      int barHeight =
          (int) (((m.getCount() / (float) max)) * ViewUtils.dp(requireContext(), 90))
              + ViewUtils.dp(requireContext(), 4);
      LinearLayout.LayoutParams barLp =
          new LinearLayout.LayoutParams(ViewUtils.dp(requireContext(), 20), barHeight);
      barLp.topMargin = ViewUtils.dp(requireContext(), 4);
      bar.setLayoutParams(barLp);
      GradientDrawable barDrawable = new GradientDrawable();
      barDrawable.setColor(requireContext().getColor(R.color.primary));
      barDrawable.setCornerRadius(ViewUtils.dp(requireContext(), 6));
      bar.setBackground(barDrawable);

      TextView monthLabel = new TextView(requireContext());
      monthLabel.setText(shortMonth(m.getMonth()));
      monthLabel.setTextSize(11);
      monthLabel.setGravity(Gravity.CENTER);
      monthLabel.setTextColor(requireContext().getColor(R.color.on_surface_variant));
      LinearLayout.LayoutParams monthLp =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      monthLp.topMargin = ViewUtils.dp(requireContext(), 4);
      monthLabel.setLayoutParams(monthLp);

      col.addView(countText);
      col.addView(bar);
      col.addView(monthLabel);
      row.addView(col);
    }
    binding.monthlyTrendContainer.addView(row);
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

  private String shortMonth(String yyyyMm) {
    try {
      String[] parts = yyyyMm.split("-");
      int monthNum = Integer.parseInt(parts[1]);
      String[] names = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
      };
      return names[monthNum - 1];
    } catch (Exception e) {
      return yyyyMm;
    }
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
