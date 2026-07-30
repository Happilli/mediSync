package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Medication;

import java.util.List;

public class DoctorMedicationAdapter extends RecyclerView.Adapter<DoctorMedicationAdapter.ViewHolder> {

    public interface OnMedicationActionListener {
        void onEdit(Medication medication);
        void onDelete(Medication medication);
    }

    private final List<Medication> medications;
    private final OnMedicationActionListener listener;

    public DoctorMedicationAdapter(List<Medication> medications, OnMedicationActionListener listener) {
        this.medications = medications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = medications.get(position);
        holder.txtMedName.setText(med.getName());
        holder.txtDosage.setText(med.getDosage() + " • " + med.getFrequency() + " • " + med.getDuration());
        holder.txtInstructions.setText(med.getTime() != null ? "Take " + med.getTime() : "");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(med));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(med));
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedName, txtDosage, txtInstructions;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedName = itemView.findViewById(R.id.txtMedName);
            txtDosage = itemView.findViewById(R.id.txtDosage);
            txtInstructions = itemView.findViewById(R.id.txtInstructions);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}