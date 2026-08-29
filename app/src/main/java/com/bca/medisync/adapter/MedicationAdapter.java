package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.data.model.Medication;
import com.bca.medisync.util.RoundedListStyler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_HEADER = 0;
  private static final int TYPE_ITEM = 1;

  public interface OnItemClickListener {
    void onItemClick(Medication medication);
  }

  private static class Row {
    final boolean isHeader;
    final String headerLabel;
    final Medication medication;
    final int positionInGroup;
    final int groupSize;

    Row(String headerLabel) {
      this.isHeader = true;
      this.headerLabel = headerLabel;
      this.medication = null;
      this.positionInGroup = 0;
      this.groupSize = 0;
    }

    Row(Medication medication, int positionInGroup, int groupSize) {
      this.isHeader = false;
      this.headerLabel = null;
      this.medication = medication;
      this.positionInGroup = positionInGroup;
      this.groupSize = groupSize;
    }
  }

  private final List<Row> rows = new ArrayList<>();
  private final OnItemClickListener listener;

  public MedicationAdapter(OnItemClickListener listener) {
    this.listener = listener;
  }

  public void submitList(List<Medication> medications) {
    rows.clear();

    Map<String, List<Medication>> grouped = new LinkedHashMap<>();
    for (Medication m : medications) {
      String key = m.getDoctorName() == null ? "Unknown" : m.getDoctorName();
      grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
    }

    for (Map.Entry<String, List<Medication>> entry : grouped.entrySet()) {
      rows.add(new Row("Dr. " + entry.getKey()));
      List<Medication> group = entry.getValue();
      for (int i = 0; i < group.size(); i++) {
        rows.add(new Row(group.get(i), i, group.size()));
      }
    }
    notifyDataSetChanged();
  }

  @Override
  public int getItemViewType(int position) {
    return rows.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == TYPE_HEADER) {
      View v =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_medication_group_header, parent, false);
      return new HeaderVH(v);
    }
    View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
    return new ItemVH(v);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Row row = rows.get(position);
    if (row.isHeader) {
      ((HeaderVH) holder).txtHeader.setText(row.headerLabel);
      return;
    }

    ItemVH itemHolder = (ItemVH) holder;
    Medication m = row.medication;
    itemHolder.tvMedName.setText(m.getName() + " " + m.getDosage());
    itemHolder.tvMedFrequency.setText(m.getFrequency());

    if (m.isTaken()) {
      itemHolder.tvMedTime.setText("Taken");
      itemHolder.itemView.setAlpha(0.6f);
    } else {
      itemHolder.tvMedTime.setText(m.getTime());
      itemHolder.itemView.setAlpha(1f);
    }

    RoundedListStyler.apply(itemHolder.itemView, row.positionInGroup, row.groupSize);
    itemHolder.itemView.setOnClickListener(v -> listener.onItemClick(m));
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static class HeaderVH extends RecyclerView.ViewHolder {
    TextView txtHeader;

    HeaderVH(View itemView) {
      super(itemView);
      txtHeader = itemView.findViewById(R.id.txtGroupHeader);
    }
  }

  static class ItemVH extends RecyclerView.ViewHolder {
    TextView tvMedName, tvMedFrequency, tvMedTime;

    ItemVH(View itemView) {
      super(itemView);
      tvMedName = itemView.findViewById(R.id.tvMedName);
      tvMedFrequency = itemView.findViewById(R.id.tvMedFrequency);
      tvMedTime = itemView.findViewById(R.id.tvMedTime);
    }
  }
}
