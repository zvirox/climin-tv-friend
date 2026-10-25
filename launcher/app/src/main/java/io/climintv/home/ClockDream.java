package io.climintv.home;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.climintv.home.theme.ThemeManager;
import io.climintv.home.util.AppPreferences;

/**
 * Ambient Screensaver (Daydream) for Android TV devices with Material 3 Expressive aesthetic.
 */
public class ClockDream extends DreamService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView timeView;
    private TextView dateView;
    private AppPreferences prefs;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);

        prefs = new AppPreferences(this);
        ThemeManager.ExpressivePalette palette = ThemeManager.getCurrentPalette(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#080B10"));

        timeView = new TextView(this);
        timeView.setTextColor(palette.pri);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 84);
        timeView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        timeView.setGravity(Gravity.CENTER);

        dateView = new TextView(this);
        dateView.setTextColor(Color.parseColor("#94A3B8"));
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        dateView.setGravity(Gravity.CENTER);
        dateView.setPadding(0, 16, 0, 0);

        layout.addView(timeView);
        layout.addView(dateView);
        setContentView(layout);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        handler.post(tickRunnable);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        handler.removeCallbacks(tickRunnable);
    }

    private void updateClock() {
        Date now = new Date();
        String timePattern = prefs.isClock24h() ? "HH:mm" : "hh:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern, Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

        if (timeView != null) timeView.setText(timeFormat.format(now));
        if (dateView != null) dateView.setText(dateFormat.format(now));
    }
}
