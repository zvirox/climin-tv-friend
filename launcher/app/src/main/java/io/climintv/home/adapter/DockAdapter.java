package io.climintv.home.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.climintv.home.R;
import io.climintv.home.model.AppEntry;
import io.climintv.home.theme.ThemeManager;
import io.climintv.home.util.FocusAnimator;

public class DockAdapter extends RecyclerView.Adapter<DockAdapter.ViewHolder> {

    public interface OnAppClickListener {
        void onAppClick(AppEntry app);
        void onAppLongClick(AppEntry app, View view);
    }

    private final List<AppEntry> items = new ArrayList<>();
    private final OnAppClickListener listener;

    public DockAdapter(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AppEntry> newItems) {
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
                .inflate(R.layout.item_m3_hero_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppEntry app = items.get(position);
        holder.bind(app, position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView badgeOpen;
        private final View iconTileContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.app_icon);
            titleView = itemView.findViewById(R.id.app_title);
            subtitleView = itemView.findViewById(R.id.app_subtitle);
            badgeOpen = itemView.findViewById(R.id.badge_open);
            iconTileContainer = itemView.findViewById(R.id.icon_tile_container);

            FocusAnimator.attach(itemView, FocusAnimator.APP_CARD_FOCUS_SCALE);
        }

        public void bind(AppEntry app, int position, OnAppClickListener listener) {
            if (app.getIcon() != null) {
                iconView.setImageDrawable(app.getIcon());
            } else if (app.getBanner() != null) {
                iconView.setImageDrawable(app.getBanner());
            } else {
                iconView.setImageResource(R.drawable.ic_default_app);
            }

            titleView.setText(app.getLabel());
            subtitleView.setText(app.getPackageName());

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

            // Initial visual styling
            updateCardFocus(itemView.hasFocus(), position);

            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                FocusAnimator.animateSpringScale(v, hasFocus ? FocusAnimator.APP_CARD_FOCUS_SCALE : 1.0f, 0.6f);
                float density = v.getResources().getDisplayMetrics().density;
                v.animate().translationZ(hasFocus ? 10f * density : 0f).setDuration(160).start();
                updateCardFocus(hasFocus, position);
            });
        }

        private void updateCardFocus(boolean hasFocus, int position) {
            ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(itemView.getContext());
            float density = itemView.getResources().getDisplayMetrics().density;

            // Card background morph
            GradientDrawable cardBg = new GradientDrawable();
            float cardRadius = hasFocus ? (26f * density) : (15f * density);
            cardBg.setCornerRadius(cardRadius);
            cardBg.setColor(hasFocus ? p.pc : p.sch);
            if (hasFocus) {
                cardBg.setStroke((int) (2.5f * density), p.pri);
            }
            itemView.setBackground(cardBg);

            // Icon tile container morph & rotation
            if (iconTileContainer != null) {
                GradientDrawable tileBg = new GradientDrawable();
                float tileRadius = hasFocus ? (14f * density) : (21f * density);
                tileBg.setCornerRadius(tileRadius);

                // Cycling accents for unfocused: secondary, tertiary, primary containers
                int[] accents = {p.sc, p.tc, p.pc};
                tileBg.setColor(hasFocus ? p.pri : accents[position % accents.length]);
                iconTileContainer.setBackground(tileBg);

                iconTileContainer.animate()
                        .rotation(hasFocus ? -8f : 0f)
                        .setDuration(220)
                        .start();
            }

            // Open badge
            if (badgeOpen != null) {
                badgeOpen.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                if (hasFocus) {
                    GradientDrawable openBg = new GradientDrawable();
                    openBg.setCornerRadius(10f * density);
                    openBg.setColor(p.pri);
                    badgeOpen.setBackground(openBg);
                    badgeOpen.setTextColor(p.onpri);
                }
            }

            // Text colors
            if (titleView != null) {
                titleView.setTextColor(hasFocus ? p.onpc : p.onbg);
            }
            if (subtitleView != null) {
                subtitleView.setTextColor(hasFocus ? p.onpc : p.osv);
            }
        }
    }
}
