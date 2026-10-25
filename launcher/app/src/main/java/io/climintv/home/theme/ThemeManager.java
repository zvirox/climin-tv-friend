package io.climintv.home.theme;

import android.content.Context;
import android.graphics.Color;

import io.climintv.home.util.AppPreferences;

public class ThemeManager {

    public static class ExpressivePalette {
        public final int bg;
        public final int onbg;
        public final int osv;
        public final int pri;
        public final int onpri;
        public final int pc;
        public final int onpc;
        public final int sc;
        public final int onsc;
        public final int tc;
        public final int ontc;
        public final int scl;
        public final int sch;
        public final String name;
        public final boolean isDark;

        public ExpressivePalette(int bg, int onbg, int osv, int pri, int onpri,
                                 int pc, int onpc, int sc, int onsc,
                                 int tc, int ontc, int scl, int sch,
                                 String name, boolean isDark) {
            this.bg = bg;
            this.onbg = onbg;
            this.osv = osv;
            this.pri = pri;
            this.onpri = onpri;
            this.pc = pc;
            this.onpc = onpc;
            this.sc = sc;
            this.onsc = onsc;
            this.tc = tc;
            this.ontc = ontc;
            this.scl = scl;
            this.sch = sch;
            this.name = name;
            this.isDark = isDark;
        }
    }

    public static ExpressivePalette getPalette(Context context, String themeKey, boolean isDark) {
        if (isDark) {
            return new ExpressivePalette(
                    Color.parseColor("#141218"), // bg
                    Color.parseColor("#E6E0E9"), // onbg
                    Color.parseColor("#CAC4D0"), // osv
                    Color.parseColor("#D0BCFF"), // pri
                    Color.parseColor("#381E72"), // onpri
                    Color.parseColor("#4F378B"), // pc
                    Color.parseColor("#EADDFF"), // onpc
                    Color.parseColor("#4A4458"), // sc
                    Color.parseColor("#E8DEF8"), // onsc
                    Color.parseColor("#633B48"), // tc
                    Color.parseColor("#FFD8E4"), // ontc
                    Color.parseColor("#211F26"), // scl
                    Color.parseColor("#2B2930"), // sch
                    "Material 3 Dark",
                    true
            );
        } else {
            return new ExpressivePalette(
                    Color.parseColor("#FEF7FF"), // bg
                    Color.parseColor("#1D1B20"), // onbg
                    Color.parseColor("#49454F"), // osv
                    Color.parseColor("#6750A4"), // pri
                    Color.parseColor("#FFFFFF"), // onpri
                    Color.parseColor("#EADDFF"), // pc
                    Color.parseColor("#21005D"), // onpc
                    Color.parseColor("#E8DEF8"), // sc
                    Color.parseColor("#1D192B"), // onsc
                    Color.parseColor("#FFD8E4"), // tc
                    Color.parseColor("#31111D"), // ontc
                    Color.parseColor("#F3EDF7"), // scl
                    Color.parseColor("#ECE6F0"), // sch
                    "Material 3 Light",
                    false
            );
        }
    }

    public static ExpressivePalette getCurrentPalette(Context context) {
        AppPreferences prefs = new AppPreferences(context);
        return getPalette(context, prefs.getThemeAccent(), prefs.isDarkMode());
    }
}
