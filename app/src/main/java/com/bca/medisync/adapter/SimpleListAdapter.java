package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SimpleListAdapter<T> extends RecyclerView.Adapter<SimpleListAdapter.VH> {

  public interface Binder<T> {
    void bind(View itemView, T item, int position);
  }

  public interface OnItemClick<T> {
    void onClick(T item);
  }

  public interface Matcher<T> {
    boolean matches(T item, String lowerCaseQuery);
  }

  private final int layoutRes;
  private List<T> items;
  private List<T> unfiltered;
  private final Binder<T> binder;
  private final OnItemClick<T> listener;
  private final Matcher<T> matcher;

  public SimpleListAdapter(
      int layoutRes, List<T> items, Binder<T> binder, OnItemClick<T> listener) {
    this(layoutRes, items, binder, listener, null);
  }

  public SimpleListAdapter(
      int layoutRes, List<T> items, Binder<T> binder, OnItemClick<T> listener, Matcher<T> matcher) {
    this.layoutRes = layoutRes;
    this.items = items;
    this.unfiltered = new ArrayList<>(items);
    this.binder = binder;
    this.listener = listener;
    this.matcher = matcher;
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
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
    return new VH(v);
  }

  @Override
  public void onBindViewHolder(@NonNull VH holder, int position) {
    T item = items.get(position);
    binder.bind(holder.itemView, item, position);
    if (listener != null) {
      holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class VH extends RecyclerView.ViewHolder {
    VH(View itemView) {
      super(itemView);
    }
  }
}
