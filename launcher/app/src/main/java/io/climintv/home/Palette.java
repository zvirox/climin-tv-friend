package io.climintv.home;

/**
 * The colors of one look, by day and by night. A look never decides day or night: the Mode (automatic, light, dark)
 * picks which of the two columns is used, and the 2 s day ⇄ night transition blends between them. Keeping both columns
 * in one object is what lets the transition, the night glass and every screen keep working exactly as before.
 */
final class Palette {

    /** One color: its day value and its night value. */
    static final class Pair {
        final int day, night;

        Pair(int day, int night) {
            this.day = day;
            this.night = night;
        }

        /** The color for how dark it is: 0 = day, 1 = night, in between during the transition. */
        int at(float dark) {
            return blend(day, night, dark);
        }
    }

    // MARK: - Slots
    /** Glass of the platter, panels and cards. */
    Pair surface;
    /** Titles, the clock, focused-row text, the tip line. */
    Pair ink;
    /** Idle tabs. */
    Pair tab;
    /** Card meta, date, weather, row values. */
    Pair dim;
    /** Quotes. */
    Pair faint;
    /** A quote past its freshness window. */
    Pair faintStale;
    /** Drawer row and card titles. */
    Pair drawerRow;
    /** Drawer secondary text, hour and day labels. */
    Pair drawerDim;
    /** Drawer right-hand text. */
    Pair drawerFaint;
    /** The small coin glyphs beside prices on the home screen. */
    Pair coinGlyph;
    /** The coin glyphs in the quotes card. */
    Pair quotesCoin;
    /** Chevrons and small marks that sit on the photo, not on text. */
    Pair mark;
    /** Ink at full strength over the drawer's glass. */
    Pair inkOpaque;
    /** Titles over a photo. */
    Pair onPhoto;
    /** The shelf's focus rim and a surface's hairline. */
    Pair rim;
    /** The look's signature colour: the focus ring, and the small marks. */
    Pair accent;
    /** The empty card placeholder. */
    Pair cardBack;
    /** Hairlines inside a card. */
    Pair divider;
    Pair up;
    Pair down;
    /** The glass shown when there is no photo behind it. */
    Pair neutral;
    Pair platterShadow, panelShadow;
    /** A settings row's label, with and without focus. */
    Pair rowOn, rowOff;
    /** A settings row's fill, and its focused variant. */
    Pair rowFill, rowFillAlt;
    /** The ink the panels need for alpha-tinted labels: black by day, white at night. No alpha byte: callers OR one in. */
    Pair inkBase;
    /** A settings row's value. */
    Pair valueInk;
    /** The fill behind a focused drawer button, and behind a focused tab or top button. */
    Pair focusBack, focusBackSoft;
    /** The fill behind a focused settings row or rail row. */
    Pair focusBackPanel;
    /** The text and icons on a focused tab or top button, and on a focused row or drawer button. */
    Pair focusInk, focusInkPanel;

    // MARK: - The looks

    /** Glass UI: the original look, unchanged. */
    static Palette glass() {
        Palette p = new Palette();
        p.surface = new Pair(0xFFFFFFFF, 0xFF111113);
        p.ink = new Pair(0xFF1D1D1F, 0xFFF5F5F7);
        p.tab = new Pair(0xFF6E6E73, 0xFF8E8E93);
        p.dim = new Pair(0xFF6E6E73, 0xFF98989D);
        p.faint = new Pair(0xFF8E8E93, 0xB8FFFFFF);
        p.faintStale = new Pair(0xFF8E8E93, 0x99FFFFFF);
        p.drawerRow = new Pair(0xFF6E6E73, 0xB3FFFFFF);
        p.drawerDim = new Pair(0xFF6E6E73, 0xADFFFFFF);
        p.drawerFaint = new Pair(0xFF8E8E93, 0x99FFFFFF);
        p.coinGlyph = new Pair(0xB38E8E93, 0x8CFFFFFF);
        p.quotesCoin = new Pair(0xFF6E6E73, 0xCCFFFFFF);
        p.mark = new Pair(0x99000000, 0xCCFFFFFF);
        p.inkOpaque = new Pair(0xFF1D1D1F, 0xFFFFFFFF);
        p.onPhoto = new Pair(0xFF1D1D1F, 0xEBFFFFFF);
        p.rim = new Pair(0xFF3A3A3C, 0xFFFFFFFF);
        p.accent = new Pair(0xFF1D1D1F, 0xFFFFFFFF);   // never drawn: Glass UI has no focus ring
        p.cardBack = new Pair(0xFFDADADF, 0xFF2C2C2E);
        p.divider = new Pair(0x1A000000, 0x24FFFFFF);
        p.up = new Pair(0xFF248A3D, 0xFF30D158);
        p.down = new Pair(0xFFD70015, 0xFFFF453A);
        p.neutral = new Pair(0xFFEBEBF0, 0xFF141416);
        p.platterShadow = new Pair(0x1F000000, 0x52000000);
        p.panelShadow = new Pair(0x26000000, 0x4D000000);
        p.rowOn = new Pair(0xFF1D1D1F, 0xF2FFFFFF);
        p.rowOff = new Pair(0xFF8E8E93, 0x6BFFFFFF);
        p.rowFill = new Pair(0x1F1C1C1E, 0x26FFFFFF);
        p.rowFillAlt = new Pair(0x141C1C1E, 0x2EFFFFFF);
        p.inkBase = new Pair(0x001C1C1E, 0x00FFFFFF);
        p.valueInk = new Pair(0x991C1C1E, 0xFFFFFFFF);
        p.focusBack = new Pair(0xFFF5F5F7, 0xFFF5F5F7);
        p.focusBackSoft = new Pair(0xFFFFFFFF, 0xFFFFFFFF);
        p.focusBackPanel = new Pair(0xFFFFFFFF, 0xFFF5F5F7);
        p.focusInk = new Pair(0xFF1D1D1F, 0xFF1D1D1F);
        p.focusInkPanel = new Pair(0xFF1C1C1E, 0xFF1C1C1E);
        return p;
    }

    /**
     * Material 3 Expressive: tonal surfaces instead of glass, the default purple seed, and an accent-tinted focus
     * instead of the white pill. Day is the light scheme, night the dark one.
     */
    static Palette material() {
        Palette p = new Palette();
        p.surface = new Pair(0xFFFEF7FF, 0xFF141218);
        p.ink = new Pair(0xFF1D1B20, 0xFFE6E0E9);
        p.tab = new Pair(0xFF49454F, 0xFFCAC4D0);
        p.dim = new Pair(0xFF49454F, 0xFFCAC4D0);
        p.faint = new Pair(0xFF79747E, 0xFF938F99);
        p.faintStale = new Pair(0xFF79747E, 0xFF938F99);
        p.drawerRow = new Pair(0xFF49454F, 0xFFCAC4D0);
        p.drawerDim = new Pair(0xFF49454F, 0xFFCAC4D0);
        p.drawerFaint = new Pair(0xFF79747E, 0xFF938F99);
        p.coinGlyph = new Pair(0xFF6750A4, 0xFFD0BCFF);
        p.quotesCoin = new Pair(0xFF6750A4, 0xFFD0BCFF);
        p.mark = new Pair(0xFF49454F, 0xFFCAC4D0);
        p.inkOpaque = new Pair(0xFF1D1B20, 0xFFE6E0E9);
        p.onPhoto = new Pair(0xFF1D1B20, 0xFFE6E0E9);
        p.rim = new Pair(0xFF79747E, 0xFF938F99);
        p.accent = new Pair(0xFF6750A4, 0xFFD0BCFF);
        p.cardBack = new Pair(0xFFE7E0EC, 0xFF2B2930);
        p.divider = new Pair(0xFFCAC4D0, 0xFF49454F);
        p.up = new Pair(0xFF146C2E, 0xFF6DD58C);
        p.down = new Pair(0xFFBA1A1A, 0xFFFFB4AB);
        p.neutral = new Pair(0xFFF3EDF7, 0xFF211F26);
        p.platterShadow = new Pair(0x0D000000, 0x33000000);
        p.panelShadow = new Pair(0x0D000000, 0x33000000);
        p.rowOn = new Pair(0xFF1D1B20, 0xFFE6E0E9);
        p.rowOff = new Pair(0xFF79747E, 0xFF938F99);
        p.rowFill = new Pair(0xFFEDE6F4, 0xFF2B2930);
        p.rowFillAlt = new Pair(0xFFE7E0EC, 0xFF332D3A);
        p.inkBase = new Pair(0x001D1B20, 0x00E6E0E9);
        p.valueInk = new Pair(0x991D1B20, 0x99E6E0E9);
        p.focusBack = new Pair(0xFFEADDFF, 0xFF4F378B);
        p.focusBackSoft = new Pair(0xFFEADDFF, 0xFF4F378B);
        p.focusBackPanel = new Pair(0xFFEADDFF, 0xFF4F378B);
        p.focusInk = new Pair(0xFF1D192B, 0xFFE8DEF8);
        p.focusInkPanel = new Pair(0xFF1D192B, 0xFFE8DEF8);
        return p;
    }

    /**
     * Win UI (Fluent): mica layers, a thin stroke instead of a shadow, Segoe blue as the accent, and a rectangular
     * focus. Day is the light theme, night the dark one.
     */
    static Palette winui() {
        Palette p = new Palette();
        p.surface = new Pair(0xFFF3F3F3, 0xFF202020);
        p.ink = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        p.tab = new Pair(0xFF5D5D5D, 0xFFC7C7C7);
        p.dim = new Pair(0xFF5D5D5D, 0xFFC7C7C7);
        p.faint = new Pair(0xFF757575, 0xFFA0A0A0);
        p.faintStale = new Pair(0xFF757575, 0xFFA0A0A0);
        p.drawerRow = new Pair(0xFF5D5D5D, 0xFFC7C7C7);
        p.drawerDim = new Pair(0xFF5D5D5D, 0xFFC7C7C7);
        p.drawerFaint = new Pair(0xFF757575, 0xFFA0A0A0);
        p.coinGlyph = new Pair(0xFF0078D4, 0xFF4CC2FF);
        p.quotesCoin = new Pair(0xFF5D5D5D, 0xFFC7C7C7);
        p.mark = new Pair(0x99000000, 0xCCFFFFFF);
        p.inkOpaque = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        p.onPhoto = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        p.rim = new Pair(0x0F000000, 0x14FFFFFF);
        p.accent = new Pair(0xFF0078D4, 0xFF4CC2FF);
        p.cardBack = new Pair(0xFFE5E5E5, 0xFF2C2C2C);
        p.divider = new Pair(0x14000000, 0x1AFFFFFF);
        p.up = new Pair(0xFF0F7B0F, 0xFF6CCB5F);
        p.down = new Pair(0xFFC42B1C, 0xFFFF99A4);
        p.neutral = new Pair(0xFFFFFFFF, 0xFF2C2C2C);
        p.platterShadow = new Pair(0x14000000, 0x4D000000);
        p.panelShadow = new Pair(0x14000000, 0x4D000000);
        p.rowOn = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        p.rowOff = new Pair(0xFF5D5D5D, 0xFFA0A0A0);
        p.rowFill = new Pair(0x0A000000, 0x0FFFFFFF);
        p.rowFillAlt = new Pair(0x12000000, 0x17FFFFFF);
        p.inkBase = new Pair(0x001A1A1A, 0x00FFFFFF);
        p.valueInk = new Pair(0x991A1A1A, 0x99FFFFFF);
        p.focusBack = new Pair(0xFFFFFFFF, 0xFF2C2C2C);
        p.focusBackSoft = new Pair(0xFFFFFFFF, 0xFF2C2C2C);
        p.focusBackPanel = new Pair(0xFFFFFFFF, 0xFF2C2C2C);
        p.focusInk = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        p.focusInkPanel = new Pair(0xFF1A1A1A, 0xFFFFFFFF);
        return p;
    }

    /**
     * Cyberpunk: a near-black night and a desaturated "daylight neon" day, cyan as the signature, sharp corners and
     * a glowing hairline instead of a shadow.
     */
    static Palette cyber() {
        Palette p = new Palette();
        p.surface = new Pair(0xFFDCE7F0, 0xFF0A0A0F);
        p.ink = new Pair(0xFF0B1A24, 0xFFE6F7FF);
        p.tab = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.dim = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.faint = new Pair(0xFF5C7484, 0xFF6F8A9B);
        p.faintStale = new Pair(0xFF5C7484, 0xFF6F8A9B);
        p.drawerRow = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.drawerDim = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.drawerFaint = new Pair(0xFF5C7484, 0xFF6F8A9B);
        p.coinGlyph = new Pair(0xFF006E7F, 0xFF00E5FF);
        p.quotesCoin = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.mark = new Pair(0xB30B1A24, 0xCCE6F7FF);
        p.inkOpaque = new Pair(0xFF0B1A24, 0xFFE6F7FF);
        p.onPhoto = new Pair(0xFF0B1A24, 0xFFE6F7FF);
        p.rim = new Pair(0xFF006E7F, 0xFF00E5FF);
        p.accent = new Pair(0xFF006E7F, 0xFF00E5FF);
        p.cardBack = new Pair(0xFFC9D9E6, 0xFF0F1119);
        p.divider = new Pair(0x1F0B1A24, 0x24E6F7FF);
        p.up = new Pair(0xFF0A5C43, 0xFF2BFFA8);
        p.down = new Pair(0xFFB3143F, 0xFFFF2A6D);
        p.neutral = new Pair(0xFFC9D9E6, 0xFF0F1119);
        p.platterShadow = new Pair(0x1A000000, 0x99000000);
        p.panelShadow = new Pair(0x1A000000, 0x99000000);
        p.rowOn = new Pair(0xFF0B1A24, 0xFFE6F7FF);
        p.rowOff = new Pair(0xFF3C5566, 0xFF8FA9B8);
        p.rowFill = new Pair(0x14006E7F, 0x1400E5FF);
        p.rowFillAlt = new Pair(0x24006E7F, 0x2400E5FF);
        p.inkBase = new Pair(0x000B1A24, 0x00E6F7FF);
        p.valueInk = new Pair(0x993C5566, 0x99E6F7FF);
        p.focusBack = new Pair(0x1F006E7F, 0x1F00E5FF);
        p.focusBackSoft = new Pair(0x1F006E7F, 0x1F00E5FF);
        p.focusBackPanel = new Pair(0x1F006E7F, 0x1F00E5FF);
        p.focusInk = new Pair(0xFF00505C, 0xFF00E5FF);
        p.focusInkPanel = new Pair(0xFF00505C, 0xFF00E5FF);
        return p;
    }

    /** The same blend the screens have always used: per channel, rounded, alpha included. */
    static int blend(int a, int b, float f) {
        int aa = a >>> 24, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = b >>> 24, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (Math.round(aa + (ba - aa) * f) << 24) | (Math.round(ar + (br - ar) * f) << 16)
                | (Math.round(ag + (bg - ag) * f) << 8) | Math.round(ab + (bb - ab) * f);
    }

    private Palette() {}
}
