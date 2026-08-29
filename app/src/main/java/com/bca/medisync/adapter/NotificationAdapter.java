package com.bca.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Notification;
import com.bca.medisync.util.DateTimeUtils;
import com.bca.medisync.util.RoundedListStyler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ItemVH> {

  public interface OnItemClickListener {
    void onItemClick(Notification notification);
  }

  private static class Row {
    final Notification notification;
    final int positionInGroup;
    final int groupSize;
    final boolean isGroupStart;

    Row(Notification notification, int positionInGroup, int groupSize, boolean isGroupStart) {
      this.notification = notification;
      this.positionInGroup = positionInGroup;
      this.groupSize = groupSize;
      this.isGroupStart = isGroupStart;
    }
  }

  private final List<Row> rows = new ArrayList<>();
  private final OnItemClickListener listener;

  public NotificationAdapter(OnItemClickListener listener) {
    this.listener = listener;
  }

  public void submitList(List<Notification> notifications) {
    rows.clear();

    Map<String, List<Notification>> grouped = new LinkedHashMap<>();
    for (Notification n : notifications) {
      String key = n.getType() == null ? "other" : n.getType();
      grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(n);
    }

    for (List<Notification> group : grouped.values()) {
      for (int i = 0; i < group.size(); i++) {
        rows.add(new Row(group.get(i), i, group.size(), i == 0));
      }
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
    return new ItemVH(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ItemVH holder, int position) {
    Row row = rows.get(position);
    Notification n = row.notification;

    holder.txtTitle.setText(n.getTitle());
    holder.txtMessage.setText(n.getMessage());
    holder.txtTime.setText(DateTimeUtils.format(n.getCreatedAt(), "dd MMM, hh:mm a"));

    if (n.isRead()) {
      holder.unreadDot.setVisibility(View.INVISIBLE);
      holder.itemView.setAlpha(0.6f);
    } else {
      holder.unreadDot.setVisibility(View.VISIBLE);
      holder.itemView.setAlpha(1f);
    }

    RoundedListStyler.apply(holder.itemView, row.positionInGroup, row.groupSize);

    ViewGroup.MarginLayoutParams lp =
        (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
    if (lp != null) {
      int density = (int) (holder.itemView.getResources().getDisplayMetrics().density * 16);
      lp.topMargin = (row.isGroupStart && position != 0) ? density : 0;
      holder.itemView.setLayoutParams(lp);
    }

    holder.itemView.setOnClickListener(v -> listener.onItemClick(n));
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static class ItemVH extends RecyclerView.ViewHolder {
    TextView txtTitle, txtMessage, txtTime;
    View unreadDot;

    ItemVH(View itemView) {
      super(itemView);
      txtTitle = itemView.findViewById(R.id.txtNotifTitle);
      txtMessage = itemView.findViewById(R.id.txtNotifMessage);
      txtTime = itemView.findViewById(R.id.txtNotifTime);
      unreadDot = itemView.findViewById(R.id.unreadDot);
    }
  }
}
