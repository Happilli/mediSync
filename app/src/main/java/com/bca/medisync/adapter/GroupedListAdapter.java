package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.bca.medisync.databinding.ItemMedicationGroupHeaderBinding;
import com.bca.medisync.util.RoundedListStyler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedListAdapter<T, VB extends ViewBinding>
    extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int TYPE_HEADER = 0;
  private static final int TYPE_ITEM = 1;

  public interface Inflater<VB extends ViewBinding> {
    VB inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);
  }

  public interface KeyExtractor<T> {
    String getKey(T item);
  }

  public interface ItemBinder<T, VB> {
    void bind(VB binding, T item, int positionInGroup, int groupSize);
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

  private final Inflater<VB> inflater;
  private final KeyExtractor<T> keyExtractor;
  private final ItemBinder<T, VB> binder;
  private final OnItemClick<T> listener;
  private final List<Row<T>> rows = new ArrayList<>();

  public GroupedListAdapter(
      Inflater<VB> inflater,
      KeyExtractor<T> keyExtractor,
      ItemBinder<T, VB> binder,
      OnItemClick<T> listener) {
    this.inflater = inflater;
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
    LayoutInflater li = LayoutInflater.from(parent.getContext());
    if (viewType == TYPE_HEADER) {
      return new HeaderVH(ItemMedicationGroupHeaderBinding.inflate(li, parent, false));
    }
    return new ItemVH<>(inflater.inflate(li, parent, false));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Row<T> row = rows.get(position);
    if (row.isHeader) {
      ((HeaderVH) holder).binding.txtGroupHeader.setText(row.headerLabel);
      return;
    }
    ItemVH<VB> itemHolder = (ItemVH<VB>) holder;
    binder.bind(itemHolder.binding, row.item, row.positionInGroup, row.groupSize);
    RoundedListStyler.apply(itemHolder.itemView, row.positionInGroup, row.groupSize);
    if (listener != null) {
      itemHolder.itemView.setOnClickListener(v -> listener.onClick(row.item));
    }
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static class HeaderVH extends RecyclerView.ViewHolder {
    final ItemMedicationGroupHeaderBinding binding;

    HeaderVH(ItemMedicationGroupHeaderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  static class ItemVH<VB extends ViewBinding> extends RecyclerView.ViewHolder {
    final VB binding;

    ItemVH(VB binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
