package io.climintv.home.util;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import io.climintv.home.theme.ThemeManager;

/**
 * Interior.dev tactile spring motion system for Material 3 Expressive Android TV.
 * Provides snappy bouncy focus scaling, squircle radius morphing, tactile press squish,
 * and fluid spring overshoot transitions across all UI elements.
 */
public class FocusAnimator {
    public static final float CIRCLE_FOCUS_SCALE = 1.08f;
    public static final float APP_CARD_FOCUS_SCALE = 1.05f;
    public static final float PILL_FOCUS_SCALE = 1.04f;
    public static final float BTN_SQUISH_PRESS_SCALE = 0.94f;
    public static final float PILL_SQUISH_PRESS_SCALE = 0.96f;
    public static final float CARD_SQUISH_PRESS_SCALE = 0.96f;
    public static final float UNFOCUS_SCALE = 1.0f;

    public static void attach(View view) {
        attach(view, PILL_FOCUS_SCALE);
    }

    public static void attach(View view, float targetScale) {
        if (view == null) return;

        view.setOnFocusChangeListener((v, hasFocus) -> {
            animateSpringScale(v, hasFocus ? targetScale : UNFOCUS_SCALE, SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            float density = v.getResources().getDisplayMetrics().density;
            float targetElev = hasFocus ? (4f * density) : 0f;
            v.animate()
                    .translationZ(targetElev)
                    .setDuration(180)
                    .start();
        });

        float pressScale = (targetScale == CIRCLE_FOCUS_SCALE) ? BTN_SQUISH_PRESS_SCALE
                : (targetScale == APP_CARD_FOCUS_SCALE) ? CARD_SQUISH_PRESS_SCALE
                : PILL_SQUISH_PRESS_SCALE;

        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    animateSpringScale(v, pressScale, SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    animateSpringScale(v, targetScale, SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
                }
            }
            return false;
        });

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                animateSpringScale(v, pressScale, SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                float target = v.hasFocus() ? targetScale : UNFOCUS_SCALE;
                animateSpringScale(v, target, SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            }
            return false;
        });
    }

    public static void attachCircleButton(View view, ImageView iconView) {
        if (view == null) return;
        attach(view, CIRCLE_FOCUS_SCALE);

        view.setOnFocusChangeListener((v, hasFocus) -> {
            animateSpringScale(v, hasFocus ? CIRCLE_FOCUS_SCALE : UNFOCUS_SCALE, SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            float density = v.getResources().getDisplayMetrics().density;
            v.animate().translationZ(hasFocus ? 4f * density : 0f).setDuration(160).start();

            ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(v.getContext());
            GradientDrawable gd = new GradientDrawable();
            // Morph between circle (20dp) and squircle (14dp) on focus!
            float radius = hasFocus ? (14f * density) : (20f * density);
            gd.setCornerRadius(radius);
            gd.setColor(hasFocus ? p.pc : p.sch);
            if (hasFocus) {
                gd.setStroke((int) (2f * density), p.pri);
            }
            v.setBackground(gd);

            if (iconView != null) {
                iconView.setColorFilter(hasFocus ? p.onpc : p.onbg);
            }
        });
    }

    public static void attachPill(View view, ImageView iconView, TextView labelView) {
        if (view == null) return;
        attach(view, PILL_FOCUS_SCALE);

        view.setOnFocusChangeListener((v, hasFocus) -> {
            animateSpringScale(v, hasFocus ? PILL_FOCUS_SCALE : UNFOCUS_SCALE, SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            float density = v.getResources().getDisplayMetrics().density;
            v.animate().translationZ(hasFocus ? 4f * density : 0f).setDuration(160).start();

            ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(v.getContext());
            GradientDrawable gd = new GradientDrawable();
            // Morph radius on focus!
            float radius = hasFocus ? (20f * density) : (17f * density);
            gd.setCornerRadius(radius);
            gd.setColor(hasFocus ? p.pc : p.sch);
            if (hasFocus) {
                gd.setStroke((int) (2f * density), p.pri);
            }
            v.setBackground(gd);

            if (iconView != null) {
                iconView.setColorFilter(hasFocus ? p.onpc : p.onbg);
            }
            if (labelView != null) {
                labelView.setTextColor(hasFocus ? p.onpc : p.onbg);
            }
        });
    }

    public static void animateSpringScale(View v, float targetScale, float damping) {
        try {
            SpringAnimation springX = (SpringAnimation) v.getTag(0x7F000001);
            SpringAnimation springY = (SpringAnimation) v.getTag(0x7F000002);

            if (springX == null) {
                springX = new SpringAnimation(v, DynamicAnimation.SCALE_X);
                springY = new SpringAnimation(v, DynamicAnimation.SCALE_Y);
                v.setTag(0x7F000001, springX);
                v.setTag(0x7F000002, springY);
            }

            SpringForce force = new SpringForce(targetScale);
            force.setDampingRatio(damping);
            force.setStiffness(SpringForce.STIFFNESS_LOW);

            springX.setSpring(force);
            springY.setSpring(force);

            springX.start();
            springY.start();
        } catch (Exception fallback) {
            v.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .setDuration(140)
                    .start();
        }
    }
}
