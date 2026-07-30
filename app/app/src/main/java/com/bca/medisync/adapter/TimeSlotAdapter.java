package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bca.medisync.data.model.TimeSlot;

import com.bca.medisync.R;
import com.google.android.material.color.MaterialColors;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    public interface OnTimeSelectedListener{
        void ontimeSelected(String time);
    }
    private final Context context;
    private final List<TimeSlot> slots;
    private final OnTimeSelectedListener listener;
    private int selectedPositon = -1;

    public TimeSlotAdapter(Context context, List<TimeSlot> slots,  OnTimeSelectedListener listener){
        this.context = context;
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);
        holder.tvTime.setText(slot.getTime());

        if(!slot.isAvailable()){
            //unavailable
            holder.tvTime.setAlpha(0.4f);
            holder.ivSlotIcon.setImageResource(R.drawable.locked);
            holder.ivSlotIcon.setColorFilter(MaterialColors.getColor(holder.tvTime, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.tvTime.setClickable(false);
        } else if (position == selectedPositon) { // selected
            holder.tvTime.setAlpha(1f);
            holder.ivSlotIcon.setImageResource(R.drawable.lock_selected);
            holder.tvTime.setTextColor(context.getColor(R.color.on_primary_container));
            holder.ivSlotIcon.setColorFilter(MaterialColors.getColor(holder.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
            holder.tvTime.setTextColor(MaterialColors.getColor(holder.tvTime, com.google.android.material.R.attr.colorOnPrimaryContainer));
            holder.tvTime.setClickable(true);
        }else {
            //available for is not selected
            holder.tvTime.setAlpha(1f);
            holder.ivSlotIcon.setImageResource(R.drawable.openforlocked);
            holder.ivSlotIcon.setColorFilter(MaterialColors.getColor(holder.tvTime, com.google.android.material.R.attr.colorOnSurface));
            holder.tvTime.setTextColor(MaterialColors.getColor(holder.tvTime, com.google.android.material.R.attr.colorOnSurface));
            holder.tvTime.setClickable(true);
        }
        holder.slotContainer.setOnClickListener(v -> {
            if(!slot.isAvailable()){
                return;
            }
            int prev = selectedPositon;
            selectedPositon = holder.getAbsoluteAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPositon);
            listener.ontimeSelected(slot.getTime());
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTime;
        ImageView ivSlotIcon;
        LinearLayout slotContainer;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivSlotIcon = itemView.findViewById(R.id.ivSlotIcon);
            slotContainer = itemView.findViewById(R.id.slotContainer);
        }
    }
}
