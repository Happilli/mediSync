package com.bca.medisync.doctor;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
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
import com.bca.medisync.util.ViewUtils;
import java.util.List;
import java.util.Map;

public class DoctorStatsFragment extends BaseBindingFragment<FragmentDoctorStatsBinding> {

  private final int[] shapes = {
    R.drawable.cookie9sided,
    R.drawable.clover4leaf,
    R.drawable.burst,
    R.drawable.softboom,
    R.drawable.gem,
    R.drawable.pentagon,
    R.drawable.flower,
  };

  private int shapeIndex = 0;
  private int heroShapeIndex = 0;
  private ImageView heroShape;
  private TextView heroValue;
  private TextView heroLabel;
  private final Handler heroHandler = new Handler(Looper.getMainLooper());
  private Runnable heroMorphRunnable;

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
    super.onDestroyView();
  }

  private void setupHeroShape() {
    binding.heroShapeContainer.removeAllViews();

    heroShape = new ImageView(requireContext());
    heroShape.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    heroShape.setImageResource(shapes[heroShapeIndex]);
    heroShape.setColorFilter(
        requireContext().getColor(R.color.primary_container), PorterDuff.Mode.SRC_IN);

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
    if (binding == null || heroShape == null) return;
    heroShape
        .animate()
        .rotationBy(180f)
        .scaleX(0.82f)
        .scaleY(0.82f)
        .setDuration(280)
        .withEndAction(
            () -> {
              if (binding == null || heroShape == null) return;
              heroShapeIndex = (heroShapeIndex + 1) % shapes.length;
              heroShape.setImageResource(shapes[heroShapeIndex]);
              heroShape
                  .animate()
                  .scaleX(1f)
                  .scaleY(1f)
                  .setDuration(280)
                  .withEndAction(
                      () -> {
                        if (binding == null) return;
                        heroHandler.postDelayed(heroMorphRunnable, 2600);
                      })
                  .start();
            })
        .start();
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

    heroValue.setText(String.valueOf(stats.getTotal_appointments()));
    heroLabel.setText("Total Appointments");
    startHeroMorphLoop();

    shapeIndex = 0;
    binding.shapeStatsGrid.removeAllViews();
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getTotal_patients()), "Total Patients"));
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getPatients_this_month()), "This Month"));
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getUpcoming_appointments()), "Upcoming"));
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getTotal_consultations()), "Consultations"));
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getTotal_prescriptions()), "Prescriptions"));
    binding.shapeStatsGrid.addView(
        buildShapeStat(String.valueOf(stats.getUpcoming_followups()), "Follow-ups"));

    bindStatusBreakdown(stats.getAppointments_by_status());
    bindMonthlyTrend(stats.getAppointments_last_6_months());
  }

  private View buildShapeStat(String value, String label) {
    FrameLayout container = new FrameLayout(requireContext());
    int size = ViewUtils.dp(requireContext(), 100);
    GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
    lp.width = size;
    lp.height = size;
    lp.setMargins(
        ViewUtils.dp(requireContext(), 6),
        ViewUtils.dp(requireContext(), 6),
        ViewUtils.dp(requireContext(), 6),
        ViewUtils.dp(requireContext(), 6));
    container.setLayoutParams(lp);

    ImageView shape = new ImageView(requireContext());
    shape.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    shape.setImageResource(shapes[shapeIndex % shapes.length]);
    shapeIndex++;
    shape.setColorFilter(
        requireContext().getColor(R.color.primary_container), PorterDuff.Mode.SRC_IN);

    LinearLayout textCol = new LinearLayout(requireContext());
    textCol.setOrientation(LinearLayout.VERTICAL);
    textCol.setGravity(Gravity.CENTER);
    int inset = ViewUtils.dp(requireContext(), 18);
    textCol.setPadding(inset, inset, inset, inset);
    textCol.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    TextView txtValue = new TextView(requireContext());
    txtValue.setText(value);
    txtValue.setGravity(Gravity.CENTER);
    txtValue.setTextColor(requireContext().getColor(R.color.on_primary_container));
    txtValue.setTextSize(20);
    txtValue.setTypeface(null, Typeface.BOLD);
    txtValue.setMaxLines(1);

    TextView txtLabel = new TextView(requireContext());
    txtLabel.setText(label);
    txtLabel.setGravity(Gravity.CENTER);
    txtLabel.setTextColor(requireContext().getColor(R.color.on_primary_container));
    txtLabel.setAlpha(0.75f);
    txtLabel.setTextSize(10);
    txtLabel.setMaxLines(2);
    LinearLayout.LayoutParams labelLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    labelLp.topMargin = ViewUtils.dp(requireContext(), 2);
    txtLabel.setLayoutParams(labelLp);

    textCol.addView(txtValue);
    textCol.addView(txtLabel);

    container.addView(shape);
    container.addView(textCol);
    return container;
  }

  private void bindStatusBreakdown(Map<String, Integer> statusMap) {
    binding.statusBreakdownContainer.removeAllViews();
    if (statusMap == null || statusMap.isEmpty()) {
      TextView empty = new TextView(requireContext());
      empty.setText("No appointments yet");
      empty.setTextColor(requireContext().getColor(R.color.on_surface_variant));
      binding.statusBreakdownContainer.addView(empty);
      return;
    }
    for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
      binding.statusBreakdownContainer.addView(buildStatusRow(entry.getKey(), entry.getValue()));
    }
  }

  private View buildStatusRow(String label, int count) {
    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, ViewUtils.dp(requireContext(), 10), 0, ViewUtils.dp(requireContext(), 10));

    ImageView dot = new ImageView(requireContext());
    int dotSize = ViewUtils.dp(requireContext(), 18);
    LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
    dotLp.setMarginEnd(ViewUtils.dp(requireContext(), 10));
    dot.setLayoutParams(dotLp);
    dot.setImageResource(shapes[shapeIndex % shapes.length]);
    dot.setColorFilter(
        requireContext().getColor(R.color.primary_container), PorterDuff.Mode.SRC_IN);
    shapeIndex++;

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
    txtCount.setTypeface(null, Typeface.BOLD);

    row.addView(dot);
    row.addView(txtLabel);
    row.addView(txtCount);
    return row;
  }

  private void bindMonthlyTrend(List<DoctorStatsResponse.MonthlyAppointmentCount> monthly) {
    binding.monthlyTrendContainer.removeAllViews();
    if (monthly == null || monthly.isEmpty()) return;

    int max = 1;
    for (DoctorStatsResponse.MonthlyAppointmentCount m : monthly) {
      max = Math.max(max, m.getCount());
    }

    LinearLayout row = new LinearLayout(requireContext());
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.BOTTOM);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dp(requireContext(), 140)));

    for (DoctorStatsResponse.MonthlyAppointmentCount m : monthly) {
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

      ImageView bar = new ImageView(requireContext());
      int barHeight =
          (int) (((m.getCount() / (float) max)) * ViewUtils.dp(requireContext(), 90))
              + ViewUtils.dp(requireContext(), 4);
      LinearLayout.LayoutParams barLp =
          new LinearLayout.LayoutParams(ViewUtils.dp(requireContext(), 28), barHeight);
      barLp.topMargin = ViewUtils.dp(requireContext(), 4);
      bar.setLayoutParams(barLp);
      bar.setImageResource(R.drawable.bun);
      bar.setColorFilter(requireContext().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
      bar.setScaleType(ImageView.ScaleType.FIT_XY);

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
}
