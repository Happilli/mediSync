package com.bca.medisync.adapter;

import android.content.Context;
import com.bca.medisync.R;
import com.bca.medisync.data.model.TimeSlot;
import com.bca.medisync.databinding.ItemTimeSlotBinding;
import com.google.android.material.color.MaterialColors;
import java.util.List;

public class TimeSlotAdapter extends SimpleListAdapter<TimeSlot, ItemTimeSlotBinding> {
  public interface OnTimeSelectedListener {
    void ontimeSelected(TimeSlot slot);
  }

  private int selectedPositon = -1;

  public TimeSlotAdapter(Context context, List<TimeSlot> slots, OnTimeSelectedListener listener) {
    super(ItemTimeSlotBinding::inflate, slots, null, null);
    setBinder((binding, slot, position) -> bindRow(binding, slot, position, listener));
  }

  private void bindRow(
      ItemTimeSlotBinding binding, TimeSlot slot, int position, OnTimeSelectedListener listener) {
    binding.tvTime.setText(slot.getDisplayTime());
    if (!slot.isAvailable()) {
      binding.tvTime.setAlpha(0.4f);
      binding.ivSlotIcon.setImageResource(R.drawable.locked);
      binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              binding.tvTime, com.google.android.material.R.attr.colorOnSurfaceVariant));
      binding.tvTime.setClickable(false);
    } else if (position == selectedPositon) {
      binding.tvTime.setAlpha(1f);
      binding.ivSlotIcon.setImageResource(R.drawable.lock_selected);
      binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              binding.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
      binding.tvTime.setTextColor(
          MaterialColors.getColor(
              binding.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
      binding.tvTime.setClickable(true);
    } else {
      binding.tvTime.setAlpha(1f);
      binding.ivSlotIcon.setImageResource(R.drawable.openforlocked);
      binding.ivSlotIcon.setColorFilter(
          MaterialColors.getColor(
              binding.tvTime, com.google.android.material.R.attr.colorOnSurface));
      binding.tvTime.setTextColor(
          MaterialColors.getColor(
              binding.tvTime, com.google.android.material.R.attr.colorOnSurface));
      binding.tvTime.setClickable(true);
    }
    binding.slotContainer.setOnClickListener(
        v -> {
          if (!slot.isAvailable()) return;
          int prev = selectedPositon;
          selectedPositon = position;
          notifyItemChanged(prev);
          notifyItemChanged(selectedPositon);
          listener.ontimeSelected(slot);
        });
  }
}
