package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.R;
import com.bca.medisync.util.RoundedListStyler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_HEADER = 0;
  private static final int TYPE_ITEM = 1;

  public interface KeyExtractor<T> {
    String getKey(T item);
  }

  public interface ItemBinder<T> {
    void bind(View itemView, T item, int positionInGroup, int groupSize);
  }

  public interface OnItemClick<T> {
    void onClick(T item);
  }

  private static class Row<T> {
    final boolean isHeader;
    final String headerLabel;
    final T item;
    final int positionInGroup;
    final int groupSize;

    Row(String headerLabel) {
      this.isHeader = true;
      this.headerLabel = headerLabel;
      this.item = null;
      this.positionInGroup = 0;
      this.groupSize = 0;
    }

    Row(T item, int positionInGroup, int groupSize) {
      this.isHeader = false;
      this.headerLabel = null;
      this.item = item;
      this.positionInGroup = positionInGroup;
      this.groupSize = groupSize;
    }
  }

  private final int itemLayoutRes;
  private final KeyExtractor<T> keyExtractor;
  private final ItemBinder<T> binder;
  private final OnItemClick<T> listener;
  private final List<Row<T>> rows = new ArrayList<>();

  public GroupedListAdapter(
      int itemLayoutRes,
      KeyExtractor<T> keyExtractor,
      ItemBinder<T> binder,
      OnItemClick<T> listener) {
    this.itemLayoutRes = itemLayoutRes;
    this.keyExtractor = keyExtractor;
    this.binder = binder;
    this.listener = listener;
  }

  public void submitList(List<T> items) {
    rows.clear();
    Map<String, List<T>> grouped = new LinkedHashMap<>();
    for (T item : items) {
      String key = keyExtractor.getKey(item);
      grouped
          .computeIfAbsent(key == null || key.isEmpty() ? "Other" : key, k -> new ArrayList<>())
          .add(item);
    }
    for (Map.Entry<String, List<T>> entry : grouped.entrySet()) {
      rows.add(new Row<>(entry.getKey()));
      List<T> group = entry.getValue();
      for (int i = 0; i < group.size(); i++) {
        rows.add(new Row<>(group.get(i), i, group.size()));
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
    View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false);
    return new ItemVH(v);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Row<T> row = rows.get(position);
    if (row.isHeader) {
      ((HeaderVH) holder).txtHeader.setText(row.headerLabel);
      return;
    }
    View itemView = holder.itemView;
    binder.bind(itemView, row.item, row.positionInGroup, row.groupSize);
    RoundedListStyler.apply(itemView, row.positionInGroup, row.groupSize);
    if (listener != null) {
      itemView.setOnClickListener(v -> listener.onClick(row.item));
    }
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static class HeaderVH extends RecyclerView.ViewHolder {
    TextView txtHeader;

    HeaderVH(View v) {
      super(v);
      txtHeader = v.findViewById(R.id.txtGroupHeader);
    }
  }

  static class ItemVH extends RecyclerView.ViewHolder {
    ItemVH(View v) {
      super(v);
    }
  }
}
