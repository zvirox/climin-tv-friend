package io.climintv.home.model;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppEntry {
    private final String packageName;
    private final String activityName;
    private final CharSequence label;
    private final Drawable icon;
    private final Drawable banner;
    private final boolean isLeanback;
    private boolean isPinned;
    private boolean isHidden;

    public AppEntry(String packageName, String activityName, CharSequence label,
                    Drawable icon, Drawable banner, boolean isLeanback) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label != null ? label : packageName;
        this.icon = icon;
        this.banner = banner;
        this.isLeanback = isLeanback;
        this.isPinned = false;
        this.isHidden = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public CharSequence getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getBanner() {
        return banner;
    }

    public boolean isLeanback() {
        return isLeanback;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }

    public Intent getLaunchIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        if (isLeanback) {
            intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        } else {
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }
}
