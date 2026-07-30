package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.R;
import com.bca.medisync.data.model.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(Notification notification);
  }

  private final Context context;
  private List<Notification> notifications;
  private final OnItemClickListener listener;

  public NotificationAdapter(
      Context context, List<Notification> notifications, OnItemClickListener listener) {
    this.context = context;
    this.notifications = notifications;
    this.listener = listener;
  }

  public void updateData(List<Notification> newData) {
    this.notifications = newData;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Notification n = notifications.get(position);
    holder.txtTitle.setText(n.getTitle());
    holder.txtMessage.setText(n.getMessage());
    holder.txtTime.setText(formatTime(n.getCreatedAt()));

    if (n.isRead()) {
      holder.unreadDot.setVisibility(View.INVISIBLE);
      holder.itemView.setAlpha(0.6f);
    } else {
      holder.unreadDot.setVisibility(View.VISIBLE);
      holder.itemView.setAlpha(1f);
    }

    holder.itemView.setOnClickListener(v -> listener.onItemClick(n));
  }

  @Override
  public int getItemCount() {
    return notifications.size();
  }

  private String formatTime(String iso) {
    if (iso == null) return "";
    try {
      SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
      SimpleDateFormat outFmt = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
      return outFmt.format(inFmt.parse(iso.length() > 19 ? iso.substring(0, 19) : iso));
    } catch (Exception e) {
      return iso;
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtTitle, txtMessage, txtTime;
    View unreadDot;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtTitle = itemView.findViewById(R.id.txtNotifTitle);
      txtMessage = itemView.findViewById(R.id.txtNotifMessage);
      txtTime = itemView.findViewById(R.id.txtNotifTime);
      unreadDot = itemView.findViewById(R.id.unreadDot);
    }
  }
}
