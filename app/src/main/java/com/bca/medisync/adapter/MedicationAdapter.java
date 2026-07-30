package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.Medication;
import com.bca.medisync.R;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(Medication medication);
  }

  public interface OnMarkTakenListener {
    void onMarkTaken(Medication medication);
  }

  private final Context context;
  private List<Medication> medications;
  private final OnItemClickListener listener;
  private final OnMarkTakenListener markTakenListener;

  public MedicationAdapter(
      Context context,
      List<Medication> medications,
      OnItemClickListener listener,
      OnMarkTakenListener markTakenListener) {
    this.context = context;
    this.medications = medications;
    this.listener = listener;
    this.markTakenListener = markTakenListener;
  }

  public void updateData(List<Medication> newData) {
    this.medications = newData;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Medication med = medications.get(position);
    holder.tvMedName.setText(med.getName() + " " + med.getDosage());
    holder.tvMedFrequency.setText(med.getFrequency());
    holder.tvMedTime.setText(med.getTime());

    if (med.isTaken()) {
      holder.tvMedTime.setText("Taken");
      holder.itemView.setAlpha(0.6f);
    } else {
      holder.itemView.setAlpha(1f);
    }

    holder.itemView.setOnClickListener(
        v -> {
          if (!med.isTaken()) {
            markTakenListener.onMarkTaken(med);
          } else {
            listener.onItemClick(med);
          }
        });
  }

  @Override
  public int getItemCount() {
    return medications.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvMedName, tvMedFrequency, tvMedTime;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMedFrequency = itemView.findViewById(R.id.tvMedFrequency);
      tvMedName = itemView.findViewById(R.id.tvMedName);
      tvMedTime = itemView.findViewById(R.id.tvMedTime);
    }
  }
}
