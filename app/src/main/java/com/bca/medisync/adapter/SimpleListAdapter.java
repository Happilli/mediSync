package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bca.medisync.util.RoundedListStyler;

import java.util.ArrayList;
import java.util.List;

public class SimpleListAdapter<T, VB extends ViewBinding>
    extends RecyclerView.Adapter<SimpleListAdapter.VH<VB>> {

  public interface Inflater<VB extends ViewBinding> {
    VB inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);
  }

  public interface Binder<T, VB> {
    void bind(VB binding, T item, int position);
  }

  public interface OnItemClick<T> {
    void onClick(T item);
  }

  public interface Matcher<T> {
    boolean matches(T item, String lowerCaseQuery);
  }

  private final Inflater<VB> inflater;
  private List<T> items;
  private List<T> unfiltered;
  private final Binder<T, VB> binder;
  private final OnItemClick<T> listener;
  private final Matcher<T> matcher;
  private boolean roundedList = false;

  public SimpleListAdapter(
      Inflater<VB> inflater, List<T> items, Binder<T, VB> binder, OnItemClick<T> listener) {
    this(inflater, items, binder, listener, null);
  }

  public SimpleListAdapter(
      Inflater<VB> inflater,
      List<T> items,
      Binder<T, VB> binder,
      OnItemClick<T> listener,
      Matcher<T> matcher) {
    this.inflater = inflater;
    this.items = items;
    this.unfiltered = new ArrayList<>(items);
    this.binder = binder;
    this.listener = listener;
    this.matcher = matcher;
  }

  public void setRoundedList(boolean roundedList) {
    this.roundedList = roundedList;
    notifyDataSetChanged();
  }

  public void updateData(List<T> newItems) {
    this.items = newItems;
    this.unfiltered = new ArrayList<>(newItems);
    notifyDataSetChanged();
  }

  public void prependItem(T item) {
    items.add(0, item);
    unfiltered.add(0, item);
    notifyItemInserted(0);
  }

  public void filter(String query) {
    if (matcher == null) return;
    if (query == null || query.isEmpty()) {
      items = new ArrayList<>(unfiltered);
    } else {
      String q = query.toLowerCase();
      List<T> filtered = new ArrayList<>();
      for (T t : unfiltered) if (matcher.matches(t, q)) filtered.add(t);
      items = filtered;
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public VH<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    VB binding = inflater.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new VH<>(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull VH<VB> holder, int position) {
    T item = items.get(position);
    binder.bind(holder.binding, item, position);
    if (listener != null) {
      holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }
    if (roundedList) {
      RoundedListStyler.apply(holder.itemView, position, getItemCount());
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class VH<VB extends ViewBinding> extends RecyclerView.ViewHolder {
    final VB binding;

    VH(VB binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
