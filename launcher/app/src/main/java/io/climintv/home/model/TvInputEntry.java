package io.climintv.home.model;

import android.content.Context;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.net.Uri;

public class TvInputEntry {
    private final String id;
    private final CharSequence label;
    private final boolean isPassthrough;
    private final int type;

    public TvInputEntry(String id, CharSequence label, boolean isPassthrough, int type) {
        this.id = id;
        this.label = label;
        this.isPassthrough = isPassthrough;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public CharSequence getLabel() {
        return label;
    }

    public boolean isPassthrough() {
        return isPassthrough;
    }

    public int getType() {
        return type;
    }

    public Uri buildUri() {
        if (isPassthrough) {
            return TvContract.buildChannelUriForPassthroughInput(id);
        }
        return null;
    }

    public static CharSequence resolveLabel(Context context, TvInputInfo info) {
        try {
            CharSequence custom = info.loadCustomLabel(context);
            if (custom != null && custom.length() > 0) {
                return custom;
            }
            CharSequence std = info.loadLabel(context);
            if (std != null && std.length() > 0) {
                return std;
            }
        } catch (Exception ignored) {}

        String id = info.getId().toLowerCase();
        if (id.contains("hdmi1") || id.contains("hdmi_1")) return "HDMI 1";
        if (id.contains("hdmi2") || id.contains("hdmi_2")) return "HDMI 2";
        if (id.contains("hdmi3") || id.contains("hdmi_3")) return "HDMI 3";
        if (id.contains("hdmi4") || id.contains("hdmi_4")) return "HDMI 4";
        if (id.contains("hdmi")) return "HDMI";
        if (id.contains("av") || id.contains("composite")) return "AV";
        if (id.contains("tuner") || id.contains("dtv")) return "Live TV";
        return info.getId();
    }
}
