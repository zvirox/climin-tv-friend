package io.climintv.home;

/**
 * A look: its colors, the recipe its surfaces are drawn with, and the shape and type it uses. The Mode (automatic,
 * light, dark) picks which column of the palette is used. A look never has its own motion, geometry, icons or
 * navigation — those are the same for every look.
 */
final class Look {

    // MARK: - Surface recipes (see Surfaces)
    static final int SURFACE_GLASS = 0;

    /** Glass UI: the original look, and the default. */
    static final Look GLASS = new Look("glass", "Glass UI", SURFACE_GLASS, Palette.glass());

    /** Every look, in the order the Theme pane cycles them. */
    static final Look[] ALL = {GLASS};

    final String key, name;
    /** How this look draws a panel: glass, acrylic, tonal, neon or flat. */
    final int surface;
    final Palette palette;

    private Look(String key, String name, int surface, Palette palette) {
        this.key = key;
        this.name = name;
        this.surface = surface;
        this.palette = palette;
    }

    /** The look in use. Glass UI until the settings say otherwise. */
    static Look current() {
        String key = Prefs.look();
        for (Look look : ALL) if (look.key.equals(key)) return look;
        return GLASS;
    }

    /** The look for a saved key, or Glass UI when the key is missing or unknown. */
    static Look byKey(String key) {
        for (Look look : ALL) if (look.key.equals(key)) return look;
        return GLASS;
    }

    /** The position of this look in the cycle. */
    static int indexOf(Look look) {
        for (int i = 0; i < ALL.length; i++) if (ALL[i] == look) return i;
        return 0;
    }
}
