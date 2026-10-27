package io.climintv.home;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * The one place a surface is drawn. Glass UI replays the liquid glass the launcher shipped with; the other looks draw
 * their own recipe through the same calls, so every screen asks for "a panel" and never for a shader.
 */
final class Surfaces {

    /** One paint for every recipe: the UI thread is the only caller. */
    private static final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF RING = new RectF();

    private Surfaces() {}

    /**
     * A panel, with the exact numbers the call site used to pass to {@link Scene#liquid}: the look decides whether they
     * are glass at all. `dark` is the day (0) to night (1) amount.
     */
    static void panel(Canvas c, Scene scene, RectF r, float radius, int tint, float tintAlpha, float sheen,
                      float refraction, float band, float specular, float alpha, float dark) {
        Look look = Look.current();
        if (look.surface == Look.SURFACE_TONAL) {
            // Material 3: a container is opaque, raised by its tone and a hairline, never by blur.
            tonal(c, look.palette, r, radius, look.palette.neutral.at(dark), alpha, dark);
            return;
        }
        scene.liquid(c, r, radius, tint, tintAlpha, sheen, refraction, band, specular, alpha);
    }

    /**
     * A card or button (drawer, quotes, weather, toasts): white by day and smoked by night in Glass UI, one opaque
     * container in the other looks. `dayTint`/`dayAlpha` and `nightTint`/`nightAlpha` are the two glass layers.
     */
    static void card(Canvas c, Scene scene, RectF r, float radius, int dayTint, float dayAlpha,
                     int nightTint, float nightAlpha, float dark) {
        Look look = Look.current();
        if (look.surface == Look.SURFACE_GLASS) {
            if (dark < 0.999f) scene.glass(c, r, radius, dayTint, dayAlpha, 0, 0, 0.02f, 1, 1 - dark);
            if (dark > 0.001f) scene.glass(c, r, radius, nightTint, nightAlpha, 0, 0, 0.02f, 1, dark);
            return;
        }
        tonal(c, look.palette, r, radius, look.palette.neutral.at(dark), 1, dark);
    }

    /** The toast: smoked glass in Glass UI, a dark chip in the other looks so its white text always reads. */
    static void toast(Canvas c, Scene scene, RectF r, float radius) {
        Look look = Look.current();
        if (look.surface == Look.SURFACE_GLASS) {
            scene.glass(c, r, radius, 0xFF111113, 0.55f, 0, 0, 0.02f, 1);
            return;
        }
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(look.palette.surface.night, 0.96f));
        c.drawRoundRect(r, radius, radius, PAINT);
    }

    /**
     * The focused pill. Glass UI draws the white pill it always did; the other looks add a hairline ring so the focus
     * stays visible on a container of the same tone.
     */
    static void focus(Canvas c, RectF r, float radius, int fill, float amount, float dark) {
        if (amount <= 0.01f) return;
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, amount));
        c.drawRoundRect(r, radius, radius, PAINT);
        Look look = Look.current();
        if (look.surface == Look.SURFACE_GLASS) return;
        RING.set(r);
        RING.inset(1f, 1f);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(2f);
        PAINT.setColor(Ui.alpha(look.palette.rim.at(dark), amount));
        c.drawRoundRect(RING, Math.max(0, radius - 1f), Math.max(0, radius - 1f), PAINT);
    }

    /** The focused circle (the round top buttons): the same rule as {@link #focus}, round. */
    static void focusCircle(Canvas c, float cx, float cy, float radius, int fill, float amount, float dark) {
        if (amount <= 0.01f) return;
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, amount));
        c.drawCircle(cx, cy, radius, PAINT);
        Look look = Look.current();
        if (look.surface == Look.SURFACE_GLASS) return;
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(2f);
        PAINT.setColor(Ui.alpha(look.palette.rim.at(dark), amount));
        c.drawCircle(cx, cy, Math.max(0, radius - 1f), PAINT);
    }

    /** An opaque rounded container with a 1 px outline (Material 3's tonal surface). */
    private static void tonal(Canvas c, Palette p, RectF r, float radius, int fill, float alpha, float dark) {
        if (alpha <= 0.004f) return;
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, alpha));
        c.drawRoundRect(r, radius, radius, PAINT);
        RING.set(r);
        RING.inset(0.5f, 0.5f);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(1f);
        PAINT.setColor(Ui.alpha(p.rim.at(dark), 0.55f * alpha));
        c.drawRoundRect(RING, Math.max(0, radius - 0.5f), Math.max(0, radius - 0.5f), PAINT);
    }
}
