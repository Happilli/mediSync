package com.bca.medisync.doctor;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.remote.ApiCallback;
import com.bca.medisync.data.remote.ApiClient;
import com.bca.medisync.data.remote.api.AppointmentApi;
import com.bca.medisync.data.remote.api.DoctorApi;
import com.bca.medisync.data.remote.api.PatientApi;
import com.bca.medisync.data.remote.dto.appointment.AppointmentStatusUpdateRequest;
import com.bca.medisync.data.remote.dto.doctor.TimeSlotCreateRequest;
import com.bca.medisync.data.remote.helpers.AppointmentEnricher;
import com.bca.medisync.util.EmptyState;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

  private RecyclerView rvSchedule;
  private LinearLayout dateStripContainer;
  private TextView txtNoAppointments;
  private ExtendedFloatingActionButton fabAddTimeslot;

  private List<Appointment> allAppointments = new ArrayList<>();
  private String selectedDate;

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
    rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
    setupSwipe();
    setupDateStrip();
    setupFab();
    loadRealSchedule();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadRealSchedule();
  }

  private void initViews(View view) {
    rvSchedule = view.findViewById(R.id.rvSchedule);
    dateStripContainer = view.findViewById(R.id.dateStripContainer);
    txtNoAppointments = view.findViewById(R.id.txtNoAppointments);
    fabAddTimeslot = view.findViewById(R.id.fabAddTimeslot);
  }

  private void setupFab() {
    fabAddTimeslot.setOnClickListener(v -> showAddTimeslotDialog());
  }

  private void setupSwipe() {
    ItemTouchHelper helper =
        new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(0, 0) {
              @Override
              public int getMovementFlags(
                  @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                AppointmentAdapter adapter = (AppointmentAdapter) recyclerView.getAdapter();
                if (adapter == null) return 0;
                Appointment a = adapter.getItemAt(viewHolder.getAbsoluteAdapterPosition());
                if (a == null) return 0;
                if (a.getStatus().equalsIgnoreCase("Pending"))
                  return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                if (a.getStatus().equalsIgnoreCase("Confirmed"))
                  return makeMovementFlags(0, ItemTouchHelper.LEFT);
                return 0;
              }

              @Override
              public boolean onMove(
                  @NonNull RecyclerView recyclerView,
                  @NonNull RecyclerView.ViewHolder viewHolder,
                  @NonNull RecyclerView.ViewHolder target) {
                return false;
              }

              private float spToPx(float sp) {
                return sp * getResources().getDisplayMetrics().scaledDensity;
              }

              @Override
              public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AppointmentAdapter adapter = (AppointmentAdapter) rvSchedule.getAdapter();
                if (adapter == null) return;
                Appointment a = adapter.getItemAt(viewHolder.getAbsoluteAdapterPosition());
                if (a == null) return;
                int appointmentId = Integer.parseInt(a.getId());
                updateStatus(
                    appointmentId, direction == ItemTouchHelper.RIGHT ? "confirmed" : "cancelled");
              }

              @Override
              public void onChildDraw(
                  @NonNull Canvas c,
                  @NonNull RecyclerView recyclerView,
                  @NonNull RecyclerView.ViewHolder viewHolder,
                  float dX,
                  float dY,
                  int actionState,
                  boolean isCurrentlyActive) {
                View item = viewHolder.itemView;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && Math.abs(dX) > 4) {
                  boolean swipingRight = dX > 0;
                  float gapWidth = Math.abs(dX);
                  float gapMargin = dpToPx(6);
                  float top = item.getTop() + gapMargin;
                  float bottom = item.getBottom() - gapMargin;
                  float bubbleHeight = bottom - top;

                  RectF bubbleRect;
                  if (swipingRight) {
                    bubbleRect =
                        new RectF(
                            item.getLeft() + gapMargin,
                            top,
                            item.getLeft() + gapWidth - gapMargin,
                            bottom);
                  } else {
                    bubbleRect =
                        new RectF(
                            item.getRight() - gapWidth + gapMargin,
                            top,
                            item.getRight() - gapMargin,
                            bottom);
                  }

                  float radius = Math.min(bubbleHeight / 2f, bubbleRect.width() / 2f);

                  Paint bubblePaint = new Paint();
                  bubblePaint.setAntiAlias(true);
                  bubblePaint.setColor(
                      ContextCompat.getColor(
                          requireContext(),
                          swipingRight ? R.color.tertiary_container : R.color.error_container));
                  c.drawRoundRect(bubbleRect, radius, radius, bubblePaint);

                  float maxSwipe = item.getWidth() * 0.5f;
                  float progress = Math.min(gapWidth / maxSwipe, 1f);
                  float cx = bubbleRect.centerX();
                  float cy = bubbleRect.centerY();

                  if (bubbleRect.width() > dpToPx(40)) {
                    Paint iconPaint = new Paint();
                    iconPaint.setAntiAlias(true);
                    iconPaint.setColor(
                        ContextCompat.getColor(
                            requireContext(),
                            swipingRight
                                ? R.color.on_tertiary_container
                                : R.color.on_error_container));
                    iconPaint.setStrokeWidth(2.5f * getResources().getDisplayMetrics().density);
                    iconPaint.setStrokeCap(Paint.Cap.ROUND);
                    iconPaint.setStyle(Paint.Style.STROKE);

                    float iconSize = dpToPx(9) + dpToPx(3) * progress;

                    if (swipingRight) {
                      c.drawLine(
                          cx - iconSize,
                          cy,
                          cx - iconSize * 0.25f,
                          cy + iconSize * 0.8f,
                          iconPaint);
                      c.drawLine(
                          cx - iconSize * 0.25f,
                          cy + iconSize * 0.8f,
                          cx + iconSize,
                          cy - iconSize * 0.7f,
                          iconPaint);
                    } else {
                      float s = iconSize * 0.85f;
                      c.drawLine(cx - s, cy - s, cx + s, cy + s, iconPaint);
                      c.drawLine(cx + s, cy - s, cx - s, cy + s, iconPaint);
                    }
                  }
                }
                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
              }
            });
    helper.attachToRecyclerView(rvSchedule);
  }

  private void showAddTimeslotDialog() {
    MaterialDatePicker<Long> datePicker =
        MaterialDatePicker.Builder.datePicker().setTitleText("Select Date").build();

    datePicker.addOnPositiveButtonClickListener(
        dateMillis -> {
          MaterialTimePicker timePicker =
              new MaterialTimePicker.Builder()
                  .setTimeFormat(TimeFormat.CLOCK_12H)
                  .setTitleText("Select Time")
                  .build();

          timePicker.addOnPositiveButtonClickListener(
              v -> {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dateMillis);
                cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                cal.set(Calendar.MINUTE, timePicker.getMinute());
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                  Toast.makeText(
                          requireContext(), "Please select a future time.", Toast.LENGTH_SHORT)
                      .show();
                  return;
                }

                String iso =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                        .format(cal.getTime());
                createTimeslot(iso);
              });
          timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });
    datePicker.show(getParentFragmentManager(), "DATE_PICKER");
  }

  private void filterByDate(String date) {
    List<Appointment> filtered = new ArrayList<>();
    if (date != null) {
      for (Appointment a : allAppointments) {
        if (date.equals(a.getDate())) filtered.add(a);
      }
    }
    rvSchedule.setAdapter(
        new AppointmentAdapter(requireContext(), filtered, true, true, this::onAppointmentClicked));
    EmptyState.bind(rvSchedule, txtNoAppointments, filtered.isEmpty());
  }

  private void onAppointmentClicked(Appointment appointment) {
    if (appointment.getStatus().equalsIgnoreCase("Confirmed")) {
      openPatientDetail(appointment, Integer.parseInt(appointment.getId()));
    }
  }

  private void createTimeslot(String iso) {
    DoctorApi api = ApiClient.getRetrofit().create(DoctorApi.class);
    ApiCallback.handle(
        api.createTimeslot(new TimeSlotCreateRequest(iso)),
        this,
        body -> Toast.makeText(requireContext(), "Timeslot added.", Toast.LENGTH_SHORT).show(),
        (code, msg) -> {
          if (code == 400)
            Toast.makeText(
                    requireContext(), "Timeslot already exists for this time.", Toast.LENGTH_LONG)
                .show();
          else
            Toast.makeText(requireContext(), "Failed to add timeslot.", Toast.LENGTH_SHORT).show();
        });
  }

  private void loadRealSchedule() {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    ApiCallback.handle(
        api.getMyAppointmentsAsDoctor(null, null),
        this,
        body -> {
          if (body.isEmpty()) {
            allAppointments = new ArrayList<>();
            filterByDate(selectedDate);
            return;
          }
          AppointmentEnricher.enrichForDoctor(
              body,
              enriched -> {
                if (!isAdded()) return;
                allAppointments = enriched;
                filterByDate(selectedDate);
              });
        },
        (code, msg) ->
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show());
  }

  private void openPatientDetail(Appointment appointment, int appointmentId) {
    PatientApi api = ApiClient.getRetrofit().create(PatientApi.class);
    ApiCallback.handle(
        api.getPatientDetailForDoctor(appointment.getPatientId()),
        this,
        p -> {
          Bundle args = new Bundle();
          args.putInt("patient_id", p.getId());
          args.putInt("appointment_id", appointmentId);
          args.putString("patient_name", p.getName());
          args.putString("patient_phone", p.getPhone());
          args.putString("patient_gender", p.getGender());
          args.putString("patient_blood", p.getBlood_group());
          args.putString("patient_emergency", p.getEmergency_contact());
          args.putString("patient_email", p.getEmail());
          args.putString("patient_address", p.getAddress());
          args.putString("patient_dob", p.getDate_of_birth());
          args.putString("patient_pic_url", p.getProfile_pic_url());

          PatientDetailsFragment fragment = new PatientDetailsFragment();
          fragment.setArguments(args);
          ((DoctorTabActivity) requireActivity()).pushFragment(fragment);
        },
        (code, msg) -> {
          if (code == -1)
            Toast.makeText(requireContext(), "Network error: " + msg, Toast.LENGTH_LONG).show();
          else
            Toast.makeText(requireContext(), "Failed to load patient.", Toast.LENGTH_SHORT).show();
        });
  }

  private void updateStatus(int appointmentId, String newStatus) {
    AppointmentApi api = ApiClient.getRetrofit().create(AppointmentApi.class);
    ApiCallback.handle(
        api.updateAppointmentStatus(appointmentId, new AppointmentStatusUpdateRequest(newStatus)),
        this,
        body -> {
          Toast.makeText(requireContext(), "Appointment " + newStatus, Toast.LENGTH_SHORT).show();
          loadRealSchedule();
          ((DoctorTabActivity) requireActivity()).refreshHomeIfPresent();
        },
        (code, msg) -> {
          if (code == 400) {
            Toast.makeText(
                    requireContext(), "Appointment can no longer be modified.", Toast.LENGTH_LONG)
                .show();
            loadRealSchedule();
          } else if (code == 403) {
            Toast.makeText(requireContext(), "Not your appointment.", Toast.LENGTH_SHORT).show();
            loadRealSchedule();
          } else {
            Toast.makeText(requireContext(), "Failed to update status.", Toast.LENGTH_SHORT).show();
            loadRealSchedule();
          }
        });
  }

  private void setupDateStrip() {
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
