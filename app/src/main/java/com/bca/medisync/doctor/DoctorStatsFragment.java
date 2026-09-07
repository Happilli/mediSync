package com.bca.medisync.doctor;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.doctor.DoctorStatsResponse;
import com.bca.medisync.databinding.FragmentDoctorStatsBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.MorphShapeView;
import com.bca.medisync.util.StatsChartHelper;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class DoctorStatsFragment extends BaseBindingFragment<FragmentDoctorStatsBinding> {

  private static class HeroStat {
    final String value;
    final String label;
    final float[] shapeData;

    HeroStat(String value, String label, float[] shapeData) {
      this.value = value;
      this.label = label;
      this.shapeData = shapeData;
    }
  }

  private final List<HeroStat> heroStats = new ArrayList<>();
  private int heroStatIndex = 0;
  private MorphShapeView heroShape;
  private TextView heroValue;
  private TextView heroLabel;
  private final Handler heroHandler = new Handler(Looper.getMainLooper());
  private Runnable heroMorphRunnable;
  private boolean heroMorphing = false;

  @Override
  protected FragmentDoctorStatsBinding inflateBinding(
      LayoutInflater inflater, ViewGroup container) {
    return FragmentDoctorStatsBinding.inflate(inflater, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ViewUtils.setupBackNav(this, binding.toolbar);
    setupHeroShape();
    loadStats();
  }

  @Override
  public void onDestroyView() {
    heroHandler.removeCallbacksAndMessages(null);
    if (heroShape != null) heroShape.destroy();
    super.onDestroyView();
  }

  private void setupHeroShape() {
    binding.heroShapeContainer.removeAllViews();

    heroShape = new MorphShapeView(requireContext());
    heroShape.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    heroShape.setShapeColor(requireContext().getColor(R.color.primary_container));
    heroShape.setShapeImmediate(MorphShapeView.Shapes.ROUND);

    LinearLayout textCol = new LinearLayout(requireContext());
    textCol.setOrientation(LinearLayout.VERTICAL);
    textCol.setGravity(Gravity.CENTER);
    int inset = ViewUtils.dp(requireContext(), 40);
    textCol.setPadding(inset, inset, inset, inset);
    textCol.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    heroValue = new TextView(requireContext());
    heroValue.setGravity(Gravity.CENTER);
    heroValue.setTextColor(requireContext().getColor(R.color.on_primary_container));
    heroValue.setTextSize(44);
    heroValue.setTypeface(null, Typeface.BOLD);
    heroValue.setMaxLines(1);

    heroLabel = new TextView(requireContext());
    heroLabel.setGravity(Gravity.CENTER);
    heroLabel.setTextColor(requireContext().getColor(R.color.on_primary_container));
    heroLabel.setAlpha(0.75f);
    heroLabel.setTextSize(13);
    heroLabel.setTypeface(null, Typeface.BOLD);
    LinearLayout.LayoutParams labelLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    labelLp.topMargin = ViewUtils.dp(requireContext(), 4);
    heroLabel.setLayoutParams(labelLp);

    textCol.addView(heroValue);
    textCol.addView(heroLabel);

    binding.heroShapeContainer.addView(heroShape);
    binding.heroShapeContainer.addView(textCol);
  }

  private void startHeroMorphLoop() {
    heroHandler.removeCallbacksAndMessages(null);
    heroMorphRunnable = this::morphHeroShape;
    heroHandler.postDelayed(heroMorphRunnable, 2600);
  }

  private void morphHeroShape() {
    if (binding == null || heroShape == null || heroStats.isEmpty() || heroMorphing) return;
    heroMorphing = true;
    heroStatIndex = (heroStatIndex + 1) % heroStats.size();
    HeroStat next = heroStats.get(heroStatIndex);
    heroValue.setText(next.value);
    heroLabel.setText(next.label);
    heroShape.morphTo(
        next.shapeData,
        100,
        () -> {
          heroMorphing = false;
          if (binding == null) return;
          heroHandler.postDelayed(heroMorphRunnable, 100);
        });
  }

  private void loadStats() {
    LoadingHelper.show(binding.loadingIndicator);
    binding.scrollContent.setVisibility(View.GONE);
    DoctorApi api = ApiClient.api(DoctorApi.class);
    ApiCallback.handle(
        api.getMyStats(),
        this,
        LoadingHelper.wrapSuccess(binding.loadingIndicator, binding.scrollContent, this::bind),
        LoadingHelper.wrapError(
            binding.loadingIndicator,
            binding.scrollContent,
            ApiErrorHandler.with(requireContext()).fallback("Failed to load stats.").build()));
  }

  private void bind(DoctorStatsResponse stats) {
    if (binding == null) return;

    heroStats.clear();
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getTotal_appointments()),
            "Total Appointments",
            MorphShapeView.Shapes.ROUND));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getTotal_patients()),
            "Total Patients",
            MorphShapeView.Shapes.CLOVER));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getPatients_this_month()),
            "This Month",
            MorphShapeView.Shapes.BURST));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getUpcoming_appointments()),
            "Upcoming",
            MorphShapeView.Shapes.SOFTBOOM));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getTotal_consultations()),
            "Consultations",
            MorphShapeView.Shapes.GEM));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getTotal_prescriptions()),
            "Prescriptions",
            MorphShapeView.Shapes.PENTAGON));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.getUpcoming_followups()),
            "Follow-ups",
            MorphShapeView.Shapes.FLOWER));

    heroStatIndex = 0;
    HeroStat first = heroStats.get(0);
    heroShape.setShapeImmediate(first.shapeData);
    heroValue.setText(first.value);
    heroLabel.setText(first.label);
    startHeroMorphLoop();

    StatsChartHelper.bindStatusBreakdown(
        binding.statusBreakdownContainer,
        stats.getAppointments_by_status(),
        stats.getTotal_appointments(),
        "No appointments yet");

    List<String> months = new ArrayList<>();
    List<Integer> counts = new ArrayList<>();
    if (stats.getAppointments_last_6_months() != null) {
      for (DoctorStatsResponse.MonthlyAppointmentCount m : stats.getAppointments_last_6_months()) {
        months.add(m.getMonth());
        counts.add(m.getCount());
      }
    }
    StatsChartHelper.bindMonthlyTrend(binding.monthlyTrendContainer, months, counts, 28);
  }
}
