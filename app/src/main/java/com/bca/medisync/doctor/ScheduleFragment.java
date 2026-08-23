package com.bca.medisync.doctor;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.model.DataProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

  private RecyclerView rvSchedule;
  private LinearLayout dateStripContainer;

  private List<Appointment> allAppointments;
  private String selectedDate;

  public ScheduleFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_schedule, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initViews(view);
    setupRecyclerView();
    setupDateStrip();
  }

  private void initViews(View view) {
    rvSchedule = view.findViewById(R.id.rvSchedule);
    dateStripContainer = view.findViewById(R.id.dateStripContainer);
  }

  private void setupRecyclerView() {
    rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
    rvSchedule.setAdapter(
        new AppointmentAdapter(
            requireContext(), DataProvider.getAppointments(), true, appointment -> {}));
  }

  private void filterByDate(String date) {
    List<Appointment> filtered = new ArrayList<>();
    for (Appointment a : allAppointments) {
      if (a.getDate().equals(date)) {
        filtered.add(a);
      }
    }
    rvSchedule.setAdapter(
        new AppointmentAdapter(requireContext(), filtered, true, appointment -> {}));
  }

  private void setupDateStrip() {
    allAppointments = DataProvider.getDoctorSchedule();

    Calendar cal = Calendar.getInstance();
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

    SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", Locale.getDefault());
    SimpleDateFormat fullFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    String todayFull = fullFmt.format(new Date());

    for (int i = 0; i < 7; i++) {
      Date date = cal.getTime();
      String dayLabel = dayFmt.format(date);
      String fullDate = fullFmt.format(date);
      int dayNum = cal.get(Calendar.DAY_OF_MONTH);

      LinearLayout item = new LinearLayout(requireContext());
      item.setOrientation(LinearLayout.VERTICAL);
      item.setGravity(Gravity.CENTER);
      item.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
      item.setClickable(true);
      item.setFocusable(true);

      LinearLayout.LayoutParams lp =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      lp.setMarginEnd(dpToPx(8));
      item.setLayoutParams(lp);

      TextView txtDay = new TextView(requireContext());
      txtDay.setText(dayLabel);
      txtDay.setTextSize(12);
      txtDay.setGravity(Gravity.CENTER);
      txtDay.setTextColor(requireContext().getColor(R.color.on_surface_variant));

      TextView txtNum = new TextView(requireContext());
      txtNum.setText(String.valueOf(dayNum));
      txtNum.setTextSize(15);
      txtNum.setGravity(Gravity.CENTER);
      txtNum.setTextColor(requireContext().getColor(R.color.on_surface));

      LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
      numLp.topMargin = dpToPx(4);
      txtNum.setLayoutParams(numLp);

      item.addView(txtDay);
      item.addView(txtNum);

      boolean isToday = fullDate.equals(todayFull);
      if (isToday) {
        txtDay.setTextColor(requireContext().getColor(R.color.primary));
        txtNum.setBackground(circleDrawable());
        txtNum.setTextColor(requireContext().getColor(R.color.on_primary));
        selectedDate = fullDate;
      }

      item.setOnClickListener(
          v -> {
            selectedDate = fullDate;
            filterByDate(fullDate);
            refreshDateStripSelection(item);
          });

      dateStripContainer.addView(item);
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }

    filterByDate(selectedDate);
  }

  private void refreshDateStripSelection(LinearLayout selected) {
    for (int i = 0; i < dateStripContainer.getChildCount(); i++) {
      LinearLayout child = (LinearLayout) dateStripContainer.getChildAt(i);
      boolean isSelected = child == selected;
      TextView txtDay = (TextView) child.getChildAt(0);
      TextView txtNum = (TextView) child.getChildAt(1);
      txtDay.setTextColor(
          requireContext().getColor(isSelected ? R.color.primary : R.color.on_surface_variant));
      txtNum.setBackground(isSelected ? circleDrawable() : null);
      txtNum.setTextColor(
          requireContext().getColor(isSelected ? R.color.on_primary : R.color.on_surface));
    }
  }

  private GradientDrawable circleDrawable() {
    GradientDrawable circle = new GradientDrawable();
    circle.setShape(GradientDrawable.OVAL);
    circle.setColor(requireContext().getColor(R.color.primary));
    return circle;
  }

  private int dpToPx(int dp) {
    return (int) (dp * getResources().getDisplayMetrics().density);
  }
}
