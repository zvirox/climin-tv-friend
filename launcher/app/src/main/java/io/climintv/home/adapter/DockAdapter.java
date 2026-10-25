package io.climintv.home.adapter;

import android.graphics.drawable.BitmapDrawable;
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
        void onAppLongClick(AppEntry app, View view, int position);
        void onAppFocused(AppEntry app, View view);
    }

    private final List<AppEntry> items = new ArrayList<>();
    private final OnAppClickListener listener;
    private boolean isReordering = false;
    private int reorderingPosition = -1;

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

    public void setReordering(boolean reordering, int position) {
        this.isReordering = reordering;
        this.reorderingPosition = position;
        notifyDataSetChanged();
    }

    public boolean isReordering() {
        return isReordering;
    }

    public int getReorderingPosition() {
        return reorderingPosition;
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
        holder.bind(app, position, isReordering, reorderingPosition, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView bannerView;
        private final ImageView iconView;
        private final TextView badgeReorder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerView = itemView.findViewById(R.id.app_banner);
            iconView = itemView.findViewById(R.id.app_icon);
            badgeReorder = itemView.findViewById(R.id.badge_reorder);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                itemView.setClipToOutline(true);
            }
            FocusAnimator.attach(itemView, FocusAnimator.APP_CARD_FOCUS_SCALE);
        }

        public void bind(AppEntry app, int position, boolean isReordering, int reorderingPosition, OnAppClickListener listener) {
            if (app.getBanner() != null) {
                if (bannerView != null) {
                    bannerView.setVisibility(View.VISIBLE);
                    bannerView.setImageDrawable(app.getBanner());
                    if (app.getBanner() instanceof BitmapDrawable) {
                        ((BitmapDrawable) app.getBanner()).setAntiAlias(true);
                        ((BitmapDrawable) app.getBanner()).setFilterBitmap(true);
                    }
                }
                if (iconView != null) {
                    iconView.setVisibility(View.GONE);
                }
            } else if (app.getIcon() != null) {
                if (bannerView != null) {
                    bannerView.setVisibility(View.GONE);
                }
                if (iconView != null) {
                    iconView.setVisibility(View.VISIBLE);
                    iconView.setImageDrawable(app.getIcon());
                    if (app.getIcon() instanceof BitmapDrawable) {
                        ((BitmapDrawable) app.getIcon()).setAntiAlias(true);
                        ((BitmapDrawable) app.getIcon()).setFilterBitmap(true);
                    }
                }
            } else {
                if (bannerView != null) {
                    bannerView.setVisibility(View.GONE);
                }
                if (iconView != null) {
                    iconView.setVisibility(View.VISIBLE);
                    iconView.setImageResource(R.drawable.ic_default_app);
                }
            }

            // No visible text on home cards - full accessible content description for TalkBack
            itemView.setContentDescription(app.getLabel());

            boolean isCurrentMovingItem = isReordering && (position == reorderingPosition);

            if (badgeReorder != null) {
                badgeReorder.setVisibility(isCurrentMovingItem ? View.VISIBLE : View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAppClick(app);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onAppLongClick(app, v, position);
                    return true;
                }
                return false;
            });

            updateCardFocus(itemView.hasFocus() || isCurrentMovingItem, isCurrentMovingItem);

            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                float targetScale = isCurrentMovingItem ? 1.08f : (hasFocus ? FocusAnimator.APP_CARD_FOCUS_SCALE : 1.0f);
                FocusAnimator.animateSpringScale(v, targetScale, 0.6f);
                float density = v.getResources().getDisplayMetrics().density;
                float targetZ = isCurrentMovingItem ? (10f * density) : (hasFocus ? 4f * density : 0f);
                v.animate().translationZ(targetZ).setDuration(160).start();
                updateCardFocus(hasFocus || isCurrentMovingItem, isCurrentMovingItem);

                if (hasFocus && listener != null) {
                    listener.onAppFocused(app, v);
                }
            });
        }

        private void updateCardFocus(boolean hasFocus, boolean isMoving) {
            ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(itemView.getContext());
            float density = itemView.getResources().getDisplayMetrics().density;

            // Card background morph
            GradientDrawable cardBg = new GradientDrawable();
            float cardRadius = hasFocus ? (15f * density) : (12f * density);
            cardBg.setCornerRadius(cardRadius);
            cardBg.setColor(hasFocus ? p.pc : p.sch);

            if (isMoving) {
                cardBg.setStroke((int) (2.5f * density), p.pri);
            } else if (hasFocus) {
                cardBg.setStroke((int) (2f * density), p.pri);
            }
            itemView.setBackground(cardBg);

            // High-contrast foreground stroke on focus so border is visible on top of full banners
            GradientDrawable border = new GradientDrawable();
            border.setCornerRadius(cardRadius);
            border.setColor(android.graphics.Color.TRANSPARENT);
            if (isMoving) {
                border.setStroke((int) (2.5f * density), p.pri);
            } else if (hasFocus) {
                border.setStroke((int) (2f * density), p.pri);
            }
            itemView.setForeground(border);
        }
    }
}
