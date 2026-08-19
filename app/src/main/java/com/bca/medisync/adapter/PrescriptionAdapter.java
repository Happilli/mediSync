package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Prescription;

import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(Prescription prescription);
  }

  private final Context context;
  private List<Prescription> prescriptions;
  private final OnItemClickListener listener;

  public PrescriptionAdapter(
      Context context, List<Prescription> prescriptions, OnItemClickListener listener) {
    this.context = context;
    this.prescriptions = prescriptions;
    this.listener = listener;
  }

  public void updateData(List<Prescription> newData) {
    this.prescriptions = newData;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Prescription p = prescriptions.get(position);
    holder.txtDiagnosis.setText(p.getDiagnosis());
    holder.txtDoctorName.setText(p.getDoctor_name());
    holder.txtDate.setText(p.getCreatedAt());
    holder.itemView.setOnClickListener(v -> listener.onItemClick(p));
  }

  @Override
  public int getItemCount() {
    return prescriptions.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtDiagnosis, txtDoctorName, txtDate;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtDiagnosis = itemView.findViewById(R.id.txtDiagnosis);
      txtDoctorName = itemView.findViewById(R.id.txtDoctorName);
      txtDate = itemView.findViewById(R.id.txtDate);
    }
  }
}
