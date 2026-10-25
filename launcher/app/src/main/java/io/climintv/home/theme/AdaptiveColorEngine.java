package io.climintv.home.theme;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

public class AdaptiveColorEngine {
    private static final Map<String, Integer> seedCache = new HashMap<>();

    public static int extractSeedColor(String packageName, Drawable drawable) {
        if (seedCache.containsKey(packageName)) {
            return seedCache.get(packageName);
        }

        if (drawable == null) {
            return Color.parseColor("#6750A4");
        }

        try {
            Bitmap bitmap = drawableToBitmap(drawable, 64, 64);
            if (bitmap == null) return Color.parseColor("#6750A4");

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            float maxScore = -1f;
            int bestColor = Color.parseColor("#6750A4");

            float[] hsv = new float[3];
            for (int i = 0; i < pixels.length; i += 4) {
                int c = pixels[i];
                int alpha = Color.alpha(c);
                if (alpha < 128) continue;

                Color.colorToHSV(c, hsv);
                float sat = hsv[1];
                float val = hsv[2];

                // Filter out near-black, near-white, or washed-out grays
                if (sat < 0.25f || val < 0.20f || val > 0.95f) continue;

                // Score favoring vibrant, rich colors
                float score = sat * 0.7f + val * 0.3f;
                if (score > maxScore) {
                    maxScore = score;
                    bestColor = c;
                }
            }

            seedCache.put(packageName, bestColor);
            return bestColor;
        } catch (Exception e) {
            return Color.parseColor("#6750A4");
        }
    }

    public static ThemeManager.ExpressivePalette generatePaletteFromSeed(int seed, boolean isDark) {
        float[] hsv = new float[3];
        Color.colorToHSV(seed, hsv);
        float hue = hsv[0];
        float sat = hsv[1];

        if (isDark) {
            // Dark Mode Scheme
            int bg = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.25f, 0.20f), 0.08f});
            int scl = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.30f, 0.25f), 0.13f});
            int sch = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.35f, 0.30f), 0.17f});
            int pri = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.70f, 0.50f), 0.95f});
            int onpri = Color.HSVToColor(new float[]{hue, sat, 0.20f});
            int pc = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.90f, 0.65f), 0.35f});
            int onpc = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.30f, 0.20f), 0.95f});
            int sc = Color.HSVToColor(new float[]{(hue + 20) % 360, 0.25f, 0.25f});
            int onsc = Color.HSVToColor(new float[]{(hue + 20) % 360, 0.15f, 0.90f});
            int tc = Color.HSVToColor(new float[]{(hue + 45) % 360, 0.35f, 0.30f});
            int ontc = Color.HSVToColor(new float[]{(hue + 45) % 360, 0.15f, 0.95f});

            return new ThemeManager.ExpressivePalette(
                    bg, Color.parseColor("#E6E0E9"), Color.parseColor("#CAC4D0"),
                    pri, onpri, pc, onpc, sc, onsc, tc, ontc, scl, sch,
                    "Adaptive Dark", true
            );
        } else {
            // Light Mode Scheme
            int bg = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.08f, 0.06f), 0.99f});
            int scl = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.12f, 0.10f), 0.96f});
            int sch = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.18f, 0.15f), 0.92f});
            int pri = Color.HSVToColor(new float[]{hue, Math.max(sat, 0.60f), 0.55f});
            int onpri = Color.WHITE;
            int pc = Color.HSVToColor(new float[]{hue, Math.min(sat * 0.35f, 0.30f), 0.90f});
            int onpc = Color.HSVToColor(new float[]{hue, sat, 0.30f});
            int sc = Color.HSVToColor(new float[]{(hue + 20) % 360, 0.15f, 0.90f});
            int onsc = Color.HSVToColor(new float[]{(hue + 20) % 360, 0.60f, 0.20f});
            int tc = Color.HSVToColor(new float[]{(hue + 45) % 360, 0.20f, 0.92f});
            int ontc = Color.HSVToColor(new float[]{(hue + 45) % 360, 0.70f, 0.20f});

            return new ThemeManager.ExpressivePalette(
                    bg, Color.parseColor("#1D1B20"), Color.parseColor("#49454F"),
                    pri, onpri, pc, onpc, sc, onsc, tc, ontc, scl, sch,
                    "Adaptive Light", false
            );
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
            if (bmp != null) return bmp;
        }

        Bitmap bitmap = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
