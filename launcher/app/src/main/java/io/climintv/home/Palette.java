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
    /** The shelf's focus rim. */
    Pair rim;
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
    /** A pressed/selected white. */
    Pair white;

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
        p.white = new Pair(0xFFFFFFFF, 0xFFF5F5F7);
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
