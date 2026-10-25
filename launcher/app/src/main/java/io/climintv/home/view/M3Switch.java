package io.climintv.home.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.climintv.home.theme.ThemeManager;

/**
 * Custom Material 3 Expressive TV Toggle Switch Component.
 * Replaces unstyled platform Switch widgets to eliminate black text artifacts on TV screens.
 */
public class M3Switch extends FrameLayout {

    private boolean isChecked = false;
    private View trackView;
    private View thumbView;
    private TextView labelView;
    private ValueAnimator slideAnimator;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(M3Switch switchView, boolean isChecked);
    }

    private OnCheckedChangeListener listener;

    public M3Switch(Context context) {
        super(context);
        init();
    }

    public M3Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public M3Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        int trackW = (int) (58f * density);
        int trackH = (int) (32f * density);
        int thumbSize = (int) (24f * density);

        setLayoutParams(new LayoutParams(trackW, trackH));
        setClipChildren(false);
        setClipToPadding(false);

        // Track View
        trackView = new View(getContext());
        FrameLayout.LayoutParams trackLp = new FrameLayout.LayoutParams(trackW, trackH);
        trackView.setLayoutParams(trackLp);
        addView(trackView);

        // Label View ("ON" / "OFF")
        labelView = new TextView(getContext());
        FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelLp.gravity = Gravity.CENTER_VERTICAL;
        labelView.setLayoutParams(labelLp);
        labelView.setTextSize(10);
        labelView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        addView(labelView);

        // Thumb View
        thumbView = new View(getContext());
        FrameLayout.LayoutParams thumbLp = new FrameLayout.LayoutParams(thumbSize, thumbSize);
        thumbLp.gravity = Gravity.CENTER_VERTICAL;
        thumbLp.leftMargin = (int) (4f * density);
        thumbView.setLayoutParams(thumbLp);
        addView(thumbView);

        updateVisualState(false);

        setOnClickListener(v -> toggle());
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (this.isChecked == checked) return;
        this.isChecked = checked;
        updateVisualState(animate);
        if (listener != null) {
            listener.onCheckedChanged(this, isChecked);
        }
    }

    public void toggle() {
        setChecked(!isChecked, true);
    }

    public void updateVisualState(boolean animate) {
        ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(getContext());
        float density = getResources().getDisplayMetrics().density;

        float maxShift = 26f * density;
        float targetTranslation = isChecked ? maxShift : 0f;

        if (animate) {
            if (slideAnimator != null && slideAnimator.isRunning()) {
                slideAnimator.cancel();
            }
            slideAnimator = ValueAnimator.ofFloat(thumbView.getTranslationX(), targetTranslation);
            slideAnimator.setDuration(180);
            slideAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
            slideAnimator.addUpdateListener(anim -> {
                float val = (float) anim.getAnimatedValue();
                thumbView.setTranslationX(val);
            });
            slideAnimator.start();
        } else {
            thumbView.setTranslationX(targetTranslation);
        }

        // Track styling
        GradientDrawable trackDrawable = new GradientDrawable();
        trackDrawable.setCornerRadius(16f * density);
        if (isChecked) {
            trackDrawable.setColor(p.pri);
            trackDrawable.setStroke((int) (1.5f * density), p.pri);
        } else {
            trackDrawable.setColor(p.sch);
            trackDrawable.setStroke((int) (1.5f * density), p.sc);
        }
        trackView.setBackground(trackDrawable);

        // Thumb styling
        GradientDrawable thumbDrawable = new GradientDrawable();
        thumbDrawable.setShape(GradientDrawable.OVAL);
        if (isChecked) {
            thumbDrawable.setColor(p.onpri);
        } else {
            thumbDrawable.setColor(p.osv);
        }
        thumbView.setBackground(thumbDrawable);

        // Label styling
        if (isChecked) {
            labelView.setText("ON");
            labelView.setTextColor(p.onpri);
            labelView.setPadding((int) (8f * density), 0, 0, 0);
            labelView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else {
            labelView.setText("OFF");
            labelView.setTextColor(p.osv);
            labelView.setPadding((int) (28f * density), 0, 0, 0);
            labelView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        }
    }
}
