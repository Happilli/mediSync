package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.MedicalHistoryEntry;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    private final List<MedicalHistoryEntry> historyEntries;

    public MedicalHistoryAdapter(List<MedicalHistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalHistoryEntry entry = historyEntries.get(position);
        holder.txtDate.setText(entry.getDate());
        holder.txtDiagnosis.setText(entry.getTitle());
        holder.txtSymptoms.setText(entry.getDescription());
        holder.txtNotes.setText("Placeholder for doctor notes and clinical observations.");

        if (position == 0) {
            holder.lineTop.setVisibility(View.INVISIBLE);
        } else {
            holder.lineTop.setVisibility(View.VISIBLE);
        }

        if (position == getItemCount() - 1) {
            holder.lineBottom.setVisibility(View.INVISIBLE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return historyEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtDiagnosis, txtSymptoms, txtNotes;
        View lineTop, lineBottom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDiagnosis = itemView.findViewById(R.id.txtDiagnosis);
            txtSymptoms = itemView.findViewById(R.id.txtSymptoms);
            txtNotes = itemView.findViewById(R.id.txtNotes);
            lineTop = itemView.findViewById(R.id.timelineLineTop);
            lineBottom = itemView.findViewById(R.id.timelineLineBottom);
        }
    }
}