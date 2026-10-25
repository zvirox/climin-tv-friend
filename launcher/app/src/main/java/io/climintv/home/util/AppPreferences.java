package io.climintv.home.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppPreferences {
    private static final String PREF_NAME = "climintv_home_prefs";
    private static final String KEY_PINNED = "pinned_packages";
    private static final String KEY_HIDDEN = "hidden_packages";
    private static final String KEY_CLOCK_24H = "clock_24h";
    private static final String KEY_SHOW_INPUTS = "show_inputs";
    private static final String KEY_THEME = "theme_accent";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_WEATHER_TEMP = "weather_temp";
    private static final String KEY_CITY = "weather_city";
    private static final String KEY_ADAPTIVE_COLOR = "adaptive_color";
    private static final String KEY_HOME_LAYOUT = "home_layout";
    private static final String KEY_WEATHER_CACHE = "weather_cache";

    public static final String THEME_CORAL = "coral";
    public static final String THEME_INDIGO = "indigo";
    public static final String THEME_EMERALD = "emerald";
    public static final String THEME_OCEAN = "ocean";
    public static final String THEME_AMBER = "amber";
    public static final String THEME_VIOLET = "violet";

    public static final String LAYOUT_MINIMAL = "minimal";
    public static final String LAYOUT_SUPER = "super";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getPinnedPackages() {
        String raw = prefs.getString(KEY_PINNED, "");
        if (raw.isEmpty()) {
            return new ArrayList<>(Arrays.asList(
                    "com.google.android.youtube.tv",
                    "com.netflix.ninja",
                    "com.amazon.amazonvideo.livingroom",
                    "com.disney.disneyplus",
                    "org.videolan.vlc"
            ));
        }
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public void setPinnedPackages(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        prefs.edit().putString(KEY_PINNED, sb.toString()).apply();
    }

    public boolean isPinned(String packageName) {
        return getPinnedPackages().contains(packageName);
    }

    public void pinPackage(String packageName) {
        List<String> list = getPinnedPackages();
        if (!list.contains(packageName)) {
            list.add(packageName);
            setPinnedPackages(list);
        }
    }

    public void unpinPackage(String packageName) {
        List<String> list = getPinnedPackages();
        if (list.remove(packageName)) {
            setPinnedPackages(list);
        }
    }

    public Set<String> getHiddenPackages() {
        return prefs.getStringSet(KEY_HIDDEN, new HashSet<>());
    }

    public void setHidden(String packageName, boolean hidden) {
        Set<String> set = new HashSet<>(getHiddenPackages());
        if (hidden) {
            set.add(packageName);
        } else {
            set.remove(packageName);
        }
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply();
    }

    public boolean isClock24h() {
        return prefs.getBoolean(KEY_CLOCK_24H, false);
    }

    public void setClock24h(boolean is24h) {
        prefs.edit().putBoolean(KEY_CLOCK_24H, is24h).apply();
    }

    public boolean isShowInputs() {
        return prefs.getBoolean(KEY_SHOW_INPUTS, true);
    }

    public void setShowInputs(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_INPUTS, show).apply();
    }

    public String getThemeAccent() {
        return prefs.getString(KEY_THEME, THEME_CORAL);
    }

    public void setThemeAccent(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, true);
    }

    public void setDarkMode(boolean darkMode) {
        prefs.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    public boolean isCelsius() {
        return prefs.getBoolean(KEY_WEATHER_TEMP, true);
    }

    public void setCelsius(boolean celsius) {
        prefs.edit().putBoolean(KEY_WEATHER_TEMP, celsius).apply();
    }

    public String getCity() {
        return prefs.getString(KEY_CITY, "New York");
    }

    public void setCity(String city) {
        prefs.edit().putString(KEY_CITY, city).apply();
    }

    public boolean isAdaptiveColor() {
        return prefs.getBoolean(KEY_ADAPTIVE_COLOR, false);
    }

    public void setAdaptiveColor(boolean adaptive) {
        prefs.edit().putBoolean(KEY_ADAPTIVE_COLOR, adaptive).apply();
    }

    public String getHomeLayout() {
        return prefs.getString(KEY_HOME_LAYOUT, LAYOUT_MINIMAL);
    }

    public void setHomeLayout(String layout) {
        prefs.edit().putString(KEY_HOME_LAYOUT, layout).apply();
    }

    public String getWeatherCache() {
        return prefs.getString(KEY_WEATHER_CACHE, "");
    }

    public void setWeatherCache(String json) {
        prefs.edit().putString(KEY_WEATHER_CACHE, json).apply();
    }
}
