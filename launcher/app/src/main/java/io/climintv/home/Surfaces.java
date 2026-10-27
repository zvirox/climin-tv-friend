package io.climintv.home;

import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * The one place a surface is drawn. Glass UI replays the liquid glass the launcher shipped with; the other looks will
 * draw their own recipe through the same call, so every screen asks for "a panel" and never for a shader.
 */
final class Surfaces {

    private Surfaces() {}

    /**
     * A panel, with the exact numbers the call site used to pass to {@link Scene#liquid}: the look decides whether they
     * are glass at all. `dark` is the day (0) to night (1) amount.
     */
    static void panel(Canvas c, Scene scene, RectF r, float radius, int tint, float tintAlpha, float sheen,
                      float refraction, float band, float specular, float alpha, float dark) {
        scene.liquid(c, r, radius, tint, tintAlpha, sheen, refraction, band, specular, alpha);
    }

    /** A flat glass card (drawer, quotes, weather, toasts): the tint-and-frost recipe. */
    static void card(Canvas c, Scene scene, RectF r, float radius, int tint, float tintAlpha, float alpha) {
        scene.glass(c, r, radius, tint, tintAlpha, 0, 0, 0.02f, 1, alpha);
    }
}
