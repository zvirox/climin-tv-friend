package io.climintv.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.climintv.home.R;
import io.climintv.home.model.AppEntry;
import io.climintv.home.util.FocusAnimator;

public class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.ViewHolder> {

    public interface OnAppActionListener {
        void onAppClick(AppEntry app);
        void onAppLongClick(AppEntry app, View view);
    }

    private final List<AppEntry> items = new ArrayList<>();
    private final Set<String> pinnedPackages = new HashSet<>();
    private final OnAppActionListener listener;

    public AppGridAdapter(OnAppActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AppEntry> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setPinnedPackages(List<String> pinnedList) {
        pinnedPackages.clear();
        if (pinnedList != null) {
            pinnedPackages.addAll(pinnedList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_m3_grid_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppEntry app = items.get(position);
        boolean isPinned = pinnedPackages.contains(app.getPackageName());
        holder.bind(app, isPinned, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView badgeHomeShelf;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.app_icon);
            titleView = itemView.findViewById(R.id.app_title);
            badgeHomeShelf = itemView.findViewById(R.id.badge_home_shelf);
            FocusAnimator.attach(itemView, 1.10f);
        }

        public void bind(AppEntry app, boolean isPinned, OnAppActionListener listener) {
            if (app.getIcon() != null) {
                iconView.setImageDrawable(app.getIcon());
            } else if (app.getBanner() != null) {
                iconView.setImageDrawable(app.getBanner());
            } else {
                iconView.setImageResource(R.drawable.ic_default_app);
            }

            titleView.setText(app.getLabel());

            if (badgeHomeShelf != null) {
                badgeHomeShelf.setVisibility(isPinned ? View.VISIBLE : View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAppClick(app);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onAppLongClick(app, v);
                    return true;
                }
                return false;
            });
        }
    }
}
