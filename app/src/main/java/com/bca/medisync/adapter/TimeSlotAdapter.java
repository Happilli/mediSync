package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.dto.timeslot.TimeSlotResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlotResponse slot);
    }

    private final Context context;
    private final List<TimeSlotResponse> timeSlots;
    private final OnTimeSlotClickListener listener;

    public TimeSlotAdapter(Context context, List<TimeSlotResponse> timeSlots) {
        this(context, timeSlots, null);
    }

    public TimeSlotAdapter(Context context, List<TimeSlotResponse> timeSlots, OnTimeSlotClickListener listener) {
        this.context = context;
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeslot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlotResponse slot = timeSlots.get(position);

        String formattedTime = formatTime(slot.getAppointment_at());
        holder.txtTime.setText(formattedTime);

        if (slot.isIs_available()) {
            holder.txtStatusBadge.setText("Available");
            holder.txtStatusBadge.setTextColor(context.getColor(R.color.primary));
            holder.txtStatusBadge.setBackgroundTintList(context.getColorStateList(R.color.primary_container));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTimeSlotClick(slot);
                }
            });
        } else {
            holder.txtStatusBadge.setText("Booked");
            holder.txtStatusBadge.setTextColor(context.getColor(R.color.secondary));
            holder.txtStatusBadge.setBackgroundTintList(context.getColorStateList(R.color.secondary_container));

            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void updateList(List<TimeSlotResponse> newList) {
        this.timeSlots.clear();
        this.timeSlots.addAll(newList);
        notifyDataSetChanged();
    }

    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            // Match ISO formats including microseconds
            SimpleDateFormat input;
            if (timestamp.contains(".")) {
                input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            } else {
                input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            }
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(timestamp);
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            output.setTimeZone(TimeZone.getTimeZone("UTC"));
            return output.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime, txtStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
        }
    }
}