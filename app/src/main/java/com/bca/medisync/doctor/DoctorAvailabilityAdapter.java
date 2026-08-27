package com.bca.medisync.doctor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.remote.dto.TimeSlotResponse;
import com.bca.medisync.util.DateTimeUtils;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DoctorAvailabilityAdapter extends RecyclerView.Adapter<DoctorAvailabilityAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(TimeSlotResponse slot);
    }

    public interface OnEditClickListener {
        void onEditClick(TimeSlotResponse slot);
    }

    private final Context context;
    private final List<TimeSlotResponse> slots;
    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;

    public DoctorAvailabilityAdapter(Context context, List<TimeSlotResponse> slots, OnDeleteClickListener deleteListener, OnEditClickListener editListener) {
        this.context = context;
        this.slots = slots;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_availability, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlotResponse slot = slots.get(position);
        holder.txtDateTime.setText(DateTimeUtils.format(slot.getAppointment_at(), "EEEE dd MMM, hh:mm a"));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(slot));
        holder.itemView.setOnClickListener(v -> editListener.onEditClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDateTime;
        MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
