package com.bca.medisync.adapter;

import android.content.Context;
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
  private final Context context;
  private List<MedicalHistoryEntry> entries;

  public MedicalHistoryAdapter(Context context, List<MedicalHistoryEntry> entries) {
    this.context = context;
    this.entries = entries;
  }

  public void updateData(List<MedicalHistoryEntry> newData) {
    this.entries = newData;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_medical_history, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    MedicalHistoryEntry e = entries.get(position);
    holder.txtTitle.setText(e.getTitle());
    holder.txtDescription.setText(e.getDescription());
    holder.txtDate.setText(e.getDate());
  }

  @Override
  public int getItemCount() {
    return entries.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtTitle, txtDescription, txtDate;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtTitle = itemView.findViewById(R.id.txtTitle);
      txtDescription = itemView.findViewById(R.id.txtDescription);
      txtDate = itemView.findViewById(R.id.txtDate);
    }
  }
}
