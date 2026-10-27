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
        switch (look.surface) {
            case Look.SURFACE_TONAL:
                // Material 3: a container is opaque, raised by its tone and a hairline, never by blur.
                tonal(c, look.palette, r, radius, look.palette.neutral.at(dark), alpha, dark);
                return;
            case Look.SURFACE_ACRYLIC:
                // Win UI: a mica layer over the desktop, frosted rather than liquid, with its own thin rim.
                scene.glass(c, r, radius, tint, Math.max(0.55f, tintAlpha), 0, 0, 0.03f, 1, alpha);
                return;
            case Look.SURFACE_NEON:
                neon(c, look.palette, r, radius, look.palette.neutral.at(dark), alpha, dark);
                return;
            case Look.SURFACE_FLAT:
                flat(c, look.palette, r, radius, look.palette.neutral.at(dark), alpha, dark);
                return;
            default:
                scene.liquid(c, r, radius, tint, tintAlpha, sheen, refraction, band, specular, alpha);
        }
    }

    /**
     * A card or button (drawer, quotes, weather, toasts): white by day and smoked by night in Glass UI, one opaque
     * container in the other looks. `dayTint`/`dayAlpha` and `nightTint`/`nightAlpha` are the two glass layers.
     */
    static void card(Canvas c, Scene scene, RectF r, float radius, int dayTint, float dayAlpha,
                     int nightTint, float nightAlpha, float dark) {
        Look look = Look.current();
        switch (look.surface) {
            case Look.SURFACE_GLASS:
                if (dark < 0.999f) scene.glass(c, r, radius, dayTint, dayAlpha, 0, 0, 0.02f, 1, 1 - dark);
                if (dark > 0.001f) scene.glass(c, r, radius, nightTint, nightAlpha, 0, 0, 0.02f, 1, dark);
                return;
            case Look.SURFACE_ACRYLIC:
                // One layer, not two: the light and dark layers are the same card at different columns.
                scene.glass(c, r, radius, look.palette.neutral.at(dark), dark > 0.5f ? 0.5f : 0.7f, 0, 0, 0.02f, 1);
                return;
            case Look.SURFACE_NEON:
                neon(c, look.palette, r, radius, look.palette.neutral.at(dark), 1, dark);
                return;
            case Look.SURFACE_FLAT:
                flat(c, look.palette, r, radius, look.palette.neutral.at(dark), 1, dark);
                return;
            default:
                tonal(c, look.palette, r, radius, look.palette.neutral.at(dark), 1, dark);
        }
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
        float rr = shape(look, radius);
        c.drawRoundRect(r, rr, rr, PAINT);
    }

    /**
     * The focused pill. Glass UI draws the white pill it always did; Material 3 adds an accent ring; Win UI is a
     * rectangle; Cyberpunk is sharp with a glowing rim; Retro is an inverted block.
     */
    static void focus(Canvas c, RectF r, float radius, int fill, float amount, float dark) {
        if (amount <= 0.01f) return;
        Look look = Look.current();
        radius = shape(look, radius);
        if (look.surface == Look.SURFACE_FLAT) {
            PAINT.setStyle(Paint.Style.FILL);
            PAINT.setColor(Ui.alpha(fill, amount));
            c.drawRect(r, PAINT);
            PAINT.setStyle(Paint.Style.STROKE);
            PAINT.setStrokeWidth(2f);
            PAINT.setColor(Ui.alpha(look.palette.ink.at(dark), amount));
            c.drawRect(r, PAINT);
            return;
        }
        base(c, look, r, radius, amount, dark, false);
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, amount));
        c.drawRoundRect(r, radius, radius, PAINT);
        if (look.surface == Look.SURFACE_GLASS) return;
        RING.set(r);
        RING.inset(1f, 1f);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(look.surface == Look.SURFACE_NEON ? 1.5f : 2f);
        PAINT.setColor(Ui.alpha(look.palette.accent.at(dark), amount));
        c.drawRoundRect(RING, Math.max(0, radius - 1f), Math.max(0, radius - 1f), PAINT);
    }

    /** The focused circle (the round top buttons): the same rule as {@link #focus}, round. */
    static void focusCircle(Canvas c, float cx, float cy, float radius, int fill, float amount, float dark) {
        Look look = Look.current();
        if (look.surface == Look.SURFACE_FLAT) {
            // Retro has no circles: the button focuses as a square block.
            RING.set(cx - radius, cy - radius, cx + radius, cy + radius);
            focus(c, RING, 0, fill, amount, dark);
            return;
        }
        if (amount <= 0.01f) return;
        RING.set(cx - radius, cy - radius, cx + radius, cy + radius);
        base(c, look, RING, radius, amount, dark, true);
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, amount));
        c.drawCircle(cx, cy, radius, PAINT);
        if (look.surface == Look.SURFACE_GLASS) return;
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(look.surface == Look.SURFACE_NEON ? 1.5f : 2f);
        PAINT.setColor(Ui.alpha(look.palette.accent.at(dark), amount));
        c.drawCircle(cx, cy, Math.max(0, radius - 1f), PAINT);
    }

    /**
     * The body a translucent focus needs something to sit on: a tinted focus over the raw shadow reads as a smudge,
     * so the non-glass looks paint the container first (Glass UI's focus pill is opaque and needs nothing).
     */
    private static void base(Canvas c, Look look, RectF r, float radius, float amount, float dark, boolean circle) {
        if (look.surface == Look.SURFACE_GLASS || ((look.palette.focusBack.at(dark) >>> 24) & 255) >= 200) return;
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(look.palette.neutral.at(dark), amount));
        if (circle) c.drawCircle(r.centerX(), r.centerY(), radius, PAINT);
        else c.drawRoundRect(r, radius, radius, PAINT);
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

    /** A near-black panel with a cyan hairline and a two-pass glow (Cyberpunk). */
    private static void neon(Canvas c, Palette p, RectF r, float radius, int fill, float alpha, float dark) {
        if (alpha <= 0.004f) return;
        float rr = Math.min(radius, 2f);
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(fill, alpha));
        c.drawRoundRect(r, rr, rr, PAINT);
        RING.set(r);
        RING.inset(0.5f, 0.5f);
        PAINT.setStyle(Paint.Style.STROKE);
        // The glow is two stacked strokes: no blur filter, so it stays cheap on the oldest TV.
        PAINT.setStrokeWidth(8f);
        PAINT.setColor(Ui.alpha(p.accent.at(dark), 0.10f * alpha));
        c.drawRoundRect(RING, rr, rr, PAINT);
        PAINT.setStrokeWidth(1.5f);
        PAINT.setColor(Ui.alpha(p.accent.at(dark), 0.85f * alpha));
        c.drawRoundRect(RING, rr, rr, PAINT);
    }

    /** A flat fill, a 2 px ink border and a hard 4 px offset block, no blur (Retro). */
    private static void flat(Canvas c, Palette p, RectF r, float radius, int fill, float alpha, float dark) {
        if (alpha <= 0.004f) return;
        RING.set(r);
        RING.offset(4f, 4f);
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(Ui.alpha(p.ink.at(dark), 0.35f * alpha));
        c.drawRect(RING, PAINT);
        PAINT.setColor(Ui.alpha(fill, alpha));
        c.drawRect(r, PAINT);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(2f);
        PAINT.setColor(Ui.alpha(p.ink.at(dark), alpha));
        c.drawRect(r, PAINT);
    }

    /**
     * The corner radius a look draws a shape with: a look may sharpen what the layout asked for, never round it more.
     * Every shadow and rim that follows a panel asks for this, so the depth matches the shape.
     */
    static float shape(float radius) {
        return shape(Look.current(), radius);
    }

    /** False for Retro: a flat block carries its own hard offset, so the soft shadow is left out. */
    static boolean softShadow() {
        return Look.current().surface != Look.SURFACE_FLAT;
    }

    private static float shape(Look look, float radius) {
        switch (look.surface) {
            case Look.SURFACE_ACRYLIC: return Math.min(radius, 8f);
            case Look.SURFACE_NEON: return Math.min(radius, 2f);
            case Look.SURFACE_FLAT: return 0;
            default: return radius;
        }
    }
}
