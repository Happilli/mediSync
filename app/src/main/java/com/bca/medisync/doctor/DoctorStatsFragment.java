package com.bca.medisync.doctor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.DrawableRes;
import com.bca.medisync.BaseBindingFragment;
import com.bca.medisync.R;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.dto.MonthlyAppointmentCount;
import com.bca.medisync.data.remote.dto.doctor.DoctorStatsResponse;
import com.bca.medisync.databinding.FragmentDoctorStatsBinding;
import com.bca.medisync.databinding.ItemHeroStatBinding;
import com.bca.medisync.util.ApiErrorHandler;
import com.bca.medisync.util.LoadingHelper;
import com.bca.medisync.util.StatsChartHelper;
import com.bca.medisync.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class DoctorStatsFragment extends BaseBindingFragment<FragmentDoctorStatsBinding> {
  private static class HeroStat {
    final String value;
    final String label;
    @DrawableRes final int shapeRes;

    HeroStat(String value, String label, @DrawableRes int shapeRes) {
      this.value = value;
      this.label = label;
      this.shapeRes = shapeRes;
    }
  }

  private final List<HeroStat> heroStats = new ArrayList<>();
  private int heroStatIndex = 0;
  private ItemHeroStatBinding heroBinding;
  private final Handler heroHandler = new Handler(Looper.getMainLooper());
  private final Runnable heroSwapRunnable = this::swapHeroShape;

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
    heroBinding = null;
    super.onDestroyView();
  }

  private void setupHeroShape() {
    binding.heroShapeContainer.removeAllViews();
    heroBinding =
        ItemHeroStatBinding.inflate(getLayoutInflater(), binding.heroShapeContainer, true);
  }

  private void startHeroLoop() {
    heroHandler.removeCallbacksAndMessages(null);
    heroHandler.postDelayed(heroSwapRunnable, 2600);
  }

  private void swapHeroShape() {
    if (binding == null || heroBinding == null || heroStats.isEmpty()) return;
    heroStatIndex = (heroStatIndex + 1) % heroStats.size();
    bindHero(heroStats.get(heroStatIndex));
    heroHandler.postDelayed(heroSwapRunnable, 2600);
  }

  private void bindHero(HeroStat stat) {
    heroBinding.txtHeroValue.setText(stat.value);
    heroBinding.txtHeroLabel.setText(stat.label);
    heroBinding.imgHeroShape.setImageResource(stat.shapeRes);
    heroBinding.imgHeroShape.setColorFilter(requireContext().getColor(R.color.primary_container));
  }

  private void loadStats() {
    DoctorApi api = ApiClient.api(DoctorApi.class);
    LoadingHelper.call(
        binding.loadingIndicator,
        binding.scrollContent,
        this,
        api.getMyStats(),
        this::bind,
        ApiErrorHandler.with(requireContext()).fallback("Failed to load stats.").build());
  }

  private void bind(DoctorStatsResponse stats) {
    if (binding == null) return;
    heroStats.clear();
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.total_appointments()),
            "Total Appointments",
            R.drawable.cookie9sided));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.total_patients()), "Total Patients", R.drawable.clover4leaf));
    heroStats.add(
        new HeroStat(String.valueOf(stats.patients_this_month()), "This Month", R.drawable.pill));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.upcoming_appointments()), "Upcoming", R.drawable.cookie6sided));
    heroStats.add(
        new HeroStat(String.valueOf(stats.total_consultations()), "Consultations", R.drawable.gem));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.total_prescriptions()), "Prescriptions", R.drawable.pentagon));
    heroStats.add(
        new HeroStat(
            String.valueOf(stats.upcoming_followups()), "Follow-ups", R.drawable.ghostish));
    heroStatIndex = 0;
    bindHero(heroStats.get(0));
    startHeroLoop();
    StatsChartHelper.bindStatusBreakdown(
        binding.statusBreakdownContainer,
        stats.appointments_by_status(),
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
    StatsChartHelper.bindMonthlyTrend(binding.monthlyTrendContainer, months, counts, 28);
  }
}
