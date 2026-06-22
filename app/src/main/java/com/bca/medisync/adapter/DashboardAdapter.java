package com.bca.medisync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import com.bca.medisync.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    private final Context context;
    private final List<String> titles;
    private final List<Integer> icons;
    private final OnItemClickListener listener;

    public DashboardAdapter(Context context, List<String> titles, List<Integer> icons, OnItemClickListener listener) {
        this.context = context;
        this.titles = titles;
        this.icons = icons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        holder.txtTitle.setText(titles.get(position));
        holder.imgIcon.setImageResource(icons.get(position));
        holder.itemView.setOnClickListener(v-> listener.onItemClick(position));
    }
    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgFeature);
            txtTitle = itemView.findViewById(R.id.txtFeature);
        }
    }
}