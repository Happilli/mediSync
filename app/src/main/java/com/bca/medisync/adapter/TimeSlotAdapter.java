package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bca.medisync.data.model.TimeSlot;
import com.bca.medisync.databinding.ItemTimeSlotBinding;
import com.google.android.material.color.MaterialColors;
import java.util.List;
import com.bca.medisync.R;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
  public interface OnTimeSelectedListener {
    void ontimeSelected(TimeSlot slot);
  }

  private final Context context;
  private final List<TimeSlot> slots;
  private final OnTimeSelectedListener listener;
  private int selectedPositon = -1;

  public TimeSlotAdapter(Context context, List<TimeSlot> slots, OnTimeSelectedListener listener) {
    this.context = context;
    this.slots = slots;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemTimeSlotBinding binding =
        ItemTimeSlotBinding.inflate(LayoutInflater.from(context), parent, false);
    return new ViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    TimeSlot slot = slots.get(position);
    holder.binding.tvTime.setText(slot.getDisplayTime());
    if (!slot.isAvailable()) {
      holder.binding.tvTime.setAlpha(0.4f);
      holder.binding.ivSlotIcon.setImageResource(R.drawable.locked);
      holder.binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              holder.binding.tvTime, com.google.android.material.R.attr.colorOnSurfaceVariant));
      holder.binding.tvTime.setClickable(false);
    } else if (position == selectedPositon) {
      holder.binding.tvTime.setAlpha(1f);
      holder.binding.ivSlotIcon.setImageResource(R.drawable.lock_selected);
      holder.binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              holder.binding.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
      holder.binding.tvTime.setTextColor(
          MaterialColors.getColor(
              holder.binding.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
      holder.binding.tvTime.setClickable(true);
    } else {
      holder.binding.tvTime.setAlpha(1f);
      holder.binding.ivSlotIcon.setImageResource(R.drawable.openforlocked);
      holder.binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              holder.binding.tvTime, com.google.android.material.R.attr.colorOnSurface));
      holder.binding.tvTime.setTextColor(
          MaterialColors.getColor(
              holder.binding.tvTime, com.google.android.material.R.attr.colorOnSurface));
      holder.binding.tvTime.setClickable(true);
    }
    holder.binding.slotContainer.setOnClickListener(
        v -> {
          if (!slot.isAvailable()) {
            return;
          }
          int prev = selectedPositon;
          selectedPositon = holder.getAbsoluteAdapterPosition();
          notifyItemChanged(prev);
          notifyItemChanged(selectedPositon);
          listener.ontimeSelected(slot);
        });
  }

  @Override
  public int getItemCount() {
    return slots.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemTimeSlotBinding binding;

    public ViewHolder(@NonNull ItemTimeSlotBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
