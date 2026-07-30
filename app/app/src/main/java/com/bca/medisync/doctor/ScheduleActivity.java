package com.bca.medisync.doctor;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.adapter.AppointmentAdapter;
import com.bca.medisync.data.model.Appointment;
import com.bca.medisync.data.model.DataProvider;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView rvSchedule;
    private LinearLayout dateStripContainer;

    private List<Appointment> allAppointments;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupDateStrip();
    }
    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        rvSchedule = findViewById(R.id.rvSchedule);
        dateStripContainer = findViewById(R.id.dateStripContainer);
    }
    private void setupToolbar(){
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }


    private void setupRecyclerView(){
       rvSchedule.setLayoutManager(new LinearLayoutManager(this));
       rvSchedule.setAdapter(new AppointmentAdapter(
               this, DataProvider.getAppointments(), true,appointment -> {

       }
       ));
    }


    private void filterByDate(String date){
        List<Appointment> filtered  = new ArrayList<>();
        for(Appointment a:allAppointments){
            if(a.getDate().equals(date)){
                filtered.add(a);
            }
        }
        rvSchedule.setAdapter(new AppointmentAdapter(this, filtered, true, appointment -> {

        }));
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

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
            item.setClickable(true);
            item.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(dpToPx(8));
            item.setLayoutParams(lp);

            TextView txtDay = new TextView(this);
            txtDay.setText(dayLabel);
            txtDay.setTextSize(12);
            txtDay.setGravity(Gravity.CENTER);
            txtDay.setTextColor(getColor(R.color.on_surface_variant));

            TextView txtNum = new TextView(this);
            txtNum.setText(String.valueOf(dayNum));
            txtNum.setTextSize(15);
            txtNum.setGravity(Gravity.CENTER);
            txtNum.setTextColor(getColor(R.color.on_surface));

            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            numLp.topMargin = dpToPx(4);
            txtNum.setLayoutParams(numLp);

            item.addView(txtDay);
            item.addView(txtNum);

            boolean isToday = fullDate.equals(todayFull);
            if (isToday) {
                txtDay.setTextColor(getColor(R.color.primary));
                txtNum.setBackground(circleDrawable());
                txtNum.setTextColor(getColor(R.color.on_primary));
                selectedDate = fullDate;
            }

            item.setOnClickListener(v -> {
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
            txtDay.setTextColor(getColor(isSelected ? R.color.primary : R.color.on_surface_variant));
            txtNum.setBackground(isSelected ? circleDrawable() : null);
            txtNum.setTextColor(getColor(isSelected ? R.color.on_primary : R.color.on_surface));
        }
    }

    private GradientDrawable circleDrawable() {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getColor(R.color.primary));
        return circle;
    }
    private int dpToPx(int dp){
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}