package io.climintv.home;

/**
 * A look: its colors, the recipe its surfaces are drawn with, and the shape and type it uses. The Mode (automatic,
 * light, dark) picks which column of the palette is used. A look never has its own motion, geometry, icons or
 * navigation — those are the same for every look.
 */
final class Look {

    // MARK: - Surface recipes (see Surfaces)
    /** The liquid glass the launcher shipped with. */
    static final int SURFACE_GLASS = 0;
    /** Frosted acrylic with a rim (Fluent). */
    static final int SURFACE_ACRYLIC = 1;
    /** An opaque tonal container with a 1 px outline (Material 3). */
    static final int SURFACE_TONAL = 2;
    /** A dark panel with a glowing stroke (Cyberpunk). */
    static final int SURFACE_NEON = 3;
    /** A flat fill with a hard offset shadow (Retro). */
    static final int SURFACE_FLAT = 4;

    /** Glass UI: the original look, and the default. */
    static final Look GLASS = new Look("glass", "Glass UI", SURFACE_GLASS, Palette.glass());
    /** Material 3 Expressive: tonal surfaces, the default purple seed, an accent-tinted focus. */
    static final Look MATERIAL = new Look("material", "Material 3", SURFACE_TONAL, Palette.material());
    /** Win UI: mica and acrylic layers, a thin stroke, Segoe blue, a rectangular focus. */
    static final Look WINUI = new Look("winui", "Win UI", SURFACE_ACRYLIC, Palette.winui());

    /** Cyberpunk: a near-black panel with a glowing cyan hairline. */
    static final Look CYBERPUNK = new Look("cyberpunk", "Cyberpunk", SURFACE_NEON, Palette.cyber());
    /** Every look, in the order the Theme pane cycles them. */
    static final Look[] ALL = {GLASS, MATERIAL, WINUI, CYBERPUNK};

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

    /** The last answer, and the settings version it was read at: every panel asks for this many times a frame. */
    private static Look cached;
    private static int cachedAt = -1;

    /** The look in use. Glass UI until the settings say otherwise. Cached until the settings change. */
    static Look current() {
        int v = Prefs.version;
        if (cached != null && v == cachedAt) return cached;
        cached = byKey(Prefs.look());
        cachedAt = v;
        return cached;
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
