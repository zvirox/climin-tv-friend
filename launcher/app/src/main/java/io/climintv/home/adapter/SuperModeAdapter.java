package io.climintv.home.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.climintv.home.R;
import io.climintv.home.model.MediaItem;
import io.climintv.home.theme.ThemeManager;
import io.climintv.home.util.FocusAnimator;
import io.climintv.home.util.ImageLoader;

public class SuperModeAdapter extends RecyclerView.Adapter<SuperModeAdapter.ViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(MediaItem item);
    }

    public interface OnMediaFocusedListener {
        void onMediaFocused(MediaItem item, View view, int position);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnMediaClickListener listener;
    private OnMediaFocusedListener focusedListener;

    public SuperModeAdapter(OnMediaClickListener listener) {
        this.listener = listener;
    }

    public void setOnMediaFocusedListener(OnMediaFocusedListener focusedListener) {
        this.focusedListener = focusedListener;
    }

    public void setItems(List<MediaItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_m3_super_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.bind(item, position, listener, focusedListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardRoot;
        private final ImageView backdropView;
        private final TextView titleView;
        private final TextView yearView;
        private final TextView providerBadge;
        private final TextView ratingBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.super_card_root);
            backdropView = itemView.findViewById(R.id.media_backdrop);
            titleView = itemView.findViewById(R.id.media_title);
            yearView = itemView.findViewById(R.id.media_year);
            providerBadge = itemView.findViewById(R.id.badge_provider);
            ratingBadge = itemView.findViewById(R.id.badge_rating);

            FocusAnimator.attach(itemView, 1.05f);
        }

        public void bind(MediaItem item, int position, OnMediaClickListener listener, OnMediaFocusedListener focusedListener) {
            Context context = itemView.getContext();
            float density = context.getResources().getDisplayMetrics().density;

            // Configure width: Wide cards (180dp) vs Square cards (110dp)
            ViewGroup.LayoutParams lp = cardRoot.getLayoutParams();
            if (lp != null) {
                lp.width = (int) ((item.isWide() ? 180f : 110f) * density);
                cardRoot.setLayoutParams(lp);
            }

            titleView.setText(item.getTitle());
            yearView.setText(item.getYear());
            providerBadge.setText("on " + item.getProviderName());
            ratingBadge.setText(String.format("★ %.1f", item.getRating()));

            // Load TMDB image
            ImageLoader.load(context, item.getImageUrl(), backdropView, R.drawable.bg_m3_shortcut_chip);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMediaClick(item);
                else item.launch(context);
            });

            // Focus morphing styling
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                FocusAnimator.animateSpringScale(v, hasFocus ? 1.05f : 1.0f, 0.6f);
                v.animate().translationZ(hasFocus ? (4f * density) : 0f).setDuration(160).start();

                ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(context);
                cardRoot.setRadius(hasFocus ? (15f * density) : (12f * density));

                GradientDrawable border = new GradientDrawable();
                border.setCornerRadius(hasFocus ? (15f * density) : (12f * density));
                border.setColor(Color.TRANSPARENT);
                if (hasFocus) {
                    border.setStroke((int) (2f * density), p.pri);
                }
                cardRoot.setForeground(border);

                // Provider badge highlight on focus
                GradientDrawable pBg = new GradientDrawable();
                pBg.setCornerRadius(8f * density);
                pBg.setColor(hasFocus ? p.pri : p.pc);
                providerBadge.setBackground(pBg);
                providerBadge.setTextColor(hasFocus ? p.onpri : p.onpc);

                if (hasFocus && focusedListener != null) {
                    focusedListener.onMediaFocused(item, v, position);
                }
            });
        }
    }
}
