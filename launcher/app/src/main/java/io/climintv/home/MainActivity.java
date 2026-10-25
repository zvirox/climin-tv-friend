package io.climintv.home;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.climintv.home.adapter.AppGridAdapter;
import io.climintv.home.adapter.DockAdapter;
import io.climintv.home.adapter.SuperModeAdapter;
import io.climintv.home.model.AppEntry;
import io.climintv.home.model.MediaItem;
import io.climintv.home.model.TvInputEntry;
import io.climintv.home.theme.AdaptiveColorEngine;
import io.climintv.home.theme.ThemeManager;
import io.climintv.home.tif.TvInputHelper;
import io.climintv.home.util.AppPreferences;
import io.climintv.home.util.FocusAnimator;
import io.climintv.home.util.TmdbRepository;
import io.climintv.home.util.WeatherRepository;
import io.climintv.home.view.M3Switch;
import io.climintv.home.view.TvScrollView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AppPreferences prefs;

    // Scroll View & Root Layout (Focus-aware scrolling prevents TV navigation freezes)
    private TvScrollView mainScrollView;
    private View rootLayout;
    private Space spacerClockAppMenu;

    // Top Header: Big Live Clock (Left) & 5 Circular Actions (Right)
    private TextView textClockTime;
    private TextView textClockSeconds;
    private TextView textClockAmPm;
    private TextView textClockDate;

    private View btnCircleInputs;
    private View btnCircleUiSettings;
    private View btnCircleWifi;
    private View btnCircleBluetooth;
    private View btnCircleTvSettings;

    private ImageView icCircleInputs;
    private ImageView icCircleUiSettings;
    private ImageView icCircleWifi;
    private ImageView icCircleBluetooth;
    private ImageView icCircleTvSettings;

    // Super Mode TMDB Recommendations Carousel
    private LinearLayout superModeContainer;
    private TextView labelSuperMode;
    private RecyclerView recyclerSuperMode;
    private SuperModeAdapter superModeAdapter;

    // App Cards Carousel (Full Widescreen 16:9 Tiles)
    private RecyclerView recyclerAppsMenu;
    private DockAdapter appsMenuAdapter;
    private final List<AppEntry> allApps = new ArrayList<>();
    private final List<AppEntry> featuredApps = new ArrayList<>();

    // Reorder / Move Mode state
    private boolean isReordering = false;
    private int reorderPosition = -1;
    private List<AppEntry> reorderOriginalList = null;
    private int lastFocusedAppPosition = 0;
    private int lastFocusedSuperPosition = 0;

    // Inputs & Shortcuts (120 px / 60 dp high)
    private View chipInputHdmi1;
    private View chipInputHdmi2;
    private View chipInputHdmi3;
    private View chipInputUsb;
    private View chipInputAv;
    private View inputsDivider;

    private View chipShortcutAllApps;
    private View chipShortcutUiSettings;
    private View chipShortcutTvSettings;
    private View chipShortcutSearch;

    private ImageView icHdmi1, icHdmi2, icHdmi3, icUsb, icAv;
    private ImageView icAllApps, icUiSettings, icTvSettings, icSearch;

    private TextView textHdmi1, textHdmi2, textHdmi3, textUsb, textAv;
    private TextView textAllApps, textUiSettings, textTvSettings, textSearch;

    // Weather Widget (184 px / 92 dp high with 6-slot hourly strip)
    private View weatherWidgetCard;
    private FrameLayout weatherBadgeContainer;
    private ImageView weatherIcon;
    private TextView textWeatherTemp;
    private TextView textWeatherCondition;
    private TextView textWeatherDetails;
    private TextView textWeatherHighLow;

    private View forecastChip1, forecastChip2, forecastChip3, forecastChip4, forecastChip5, forecastChip6;
    private TextView textForecastTime1, textForecastTime2, textForecastTime3, textForecastTime4, textForecastTime5, textForecastTime6;
    private TextView textForecastTemp1, textForecastTemp2, textForecastTemp3, textForecastTemp4, textForecastTemp5, textForecastTemp6;
    private ImageView icForecast1, icForecast2, icForecast3, icForecast4, icForecast5, icForecast6;

    private WeatherRepository.WeatherData currentWeatherData;

    // Clock Updater - Aligned to system second boundary
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClockDisplay();
            long now = System.currentTimeMillis();
            long delay = 1000 - (now % 1000);
            handler.postDelayed(this, delay);
        }
    };

    // Debounced Adaptive Color Theme updater
    private final Runnable adaptiveColorRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDestroyed()) return;
            if (lastFocusedAppPosition >= 0 && lastFocusedAppPosition < featuredApps.size()) {
                AppEntry app = featuredApps.get(lastFocusedAppPosition);
                int seed = AdaptiveColorEngine.extractSeedColor(app.getPackageName(), app.getIcon());
                ThemeManager.ExpressivePalette adaptivePalette =
                        AdaptiveColorEngine.generatePaletteFromSeed(seed, prefs.isDarkMode());
                applyPalette(adaptivePalette);
            }
        }
    };

    // Package change receiver
    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadInstalledApps();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        setContentView(R.layout.activity_main);

        prefs = new AppPreferences(this);
        initViews();
        setupAdapters();
        setupListeners();
        loadInstalledApps();
        updateLayoutMode();
        refreshWeather();

        // Listen for package changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(packageReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyMaterial3Theme();
        updateClockDisplay();
        handler.post(clockRunnable);
        refreshWeather();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View focused = getCurrentFocus();
                if (focused == null || focused == mainScrollView || focused == rootLayout || !focused.isFocusable()) {
                    if (isSuperModeVisible()) {
                        focusSuperMode();
                    } else {
                        focusAppMenu();
                    }
                }
            }
        }, 150);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (isReordering) {
                return true; // Lock D-pad vertical movement during card reorder
            }

            View focused = getCurrentFocus();
            if (focused == null || focused == mainScrollView || focused == rootLayout || !focused.isFocusable()) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP && isSuperModeVisible()) {
                        focusSuperMode();
                    } else {
                        focusAppMenu();
                    }
                }
                return true;
            }

            if (isViewDescendantOf(focused, recyclerSuperMode)) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        focusAppMenu();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        focusHeader();
                    }
                    return true;
                }
            } else if (isViewDescendantOf(focused, recyclerAppsMenu)) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (isSuperModeVisible()) {
                            focusSuperMode();
                        } else {
                            focusHeader();
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        focusInputsRow();
                    }
                    return true;
                }
            } else if (isViewDescendantOf(focused, findViewById(R.id.top_header_row))) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (isSuperModeVisible()) {
                            focusSuperMode();
                        } else {
                            focusAppMenu();
                        }
                    }
                    return true;
                }
            } else if (isViewDescendantOf(focused, findViewById(R.id.inputs_shortcuts_row))) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        focusAppMenu();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (weatherWidgetCard != null && weatherWidgetCard.getVisibility() == View.VISIBLE) {
                        if (action == KeyEvent.ACTION_DOWN) {
                            weatherWidgetCard.requestFocus();
                            if (mainScrollView != null) {
                                float density = getResources().getDisplayMetrics().density;
                                mainScrollView.smoothScrollTo(0, (int) (380f * density));
                            }
                        }
                        return true;
                    }
                }
            } else if (focused == weatherWidgetCard || isViewDescendantOf(focused, weatherWidgetCard)) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        focusInputsRow();
                    }
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private boolean isViewDescendantOf(View child, View parent) {
        if (child == null || parent == null) return false;
        if (child == parent) return true;
        for (android.view.ViewParent p = child.getParent(); p != null; p = p.getParent()) {
            if (p == parent) return true;
        }
        return false;
    }

    private boolean isSuperModeVisible() {
        return superModeContainer != null &&
                superModeContainer.getVisibility() == View.VISIBLE &&
                superModeAdapter != null &&
                superModeAdapter.getItemCount() > 0;
    }

    private void focusAppMenu() {
        if (recyclerAppsMenu == null || appsMenuAdapter == null || appsMenuAdapter.getItemCount() == 0) return;

        View focused = getCurrentFocus();
        int targetPos = lastFocusedAppPosition;

        if (focused != null && isViewDescendantOf(focused, recyclerSuperMode)) {
            int[] focusedLoc = new int[2];
            focused.getLocationOnScreen(focusedLoc);
            int focusedCenterX = focusedLoc[0] + focused.getWidth() / 2;

            int closestDiff = Integer.MAX_VALUE;
            int closestPos = RecyclerView.NO_POSITION;

            for (int i = 0; i < recyclerAppsMenu.getChildCount(); i++) {
                View child = recyclerAppsMenu.getChildAt(i);
                if (child != null && child.isFocusable()) {
                    int[] childLoc = new int[2];
                    child.getLocationOnScreen(childLoc);
                    int childCenterX = childLoc[0] + child.getWidth() / 2;
                    int diff = Math.abs(focusedCenterX - childCenterX);
                    if (diff < closestDiff) {
                        closestDiff = diff;
                        int pos = recyclerAppsMenu.getChildAdapterPosition(child);
                        if (pos != RecyclerView.NO_POSITION) {
                            closestPos = pos;
                        }
                    }
                }
            }
            if (closestPos != RecyclerView.NO_POSITION) {
                targetPos = closestPos;
            }
        }

        targetPos = Math.max(0, Math.min(targetPos, appsMenuAdapter.getItemCount() - 1));
        lastFocusedAppPosition = targetPos;

        final int finalPos = targetPos;
        android.util.Log.i(TAG, "focusAppMenu: targetPos=" + finalPos);

        View direct = recyclerAppsMenu.getLayoutManager() != null ?
                recyclerAppsMenu.getLayoutManager().findViewByPosition(finalPos) : null;
        if (direct == null && recyclerAppsMenu.getChildCount() > 0) {
            direct = recyclerAppsMenu.getChildAt(0);
        }

        if (direct != null) {
            boolean req = direct.requestFocus();
            android.util.Log.i(TAG, "focusAppMenu: direct requestFocus=" + req + " on " + direct);
        } else {
            recyclerAppsMenu.scrollToPosition(finalPos);
            recyclerAppsMenu.post(new Runnable() {
                @Override
                public void run() {
                    View child = recyclerAppsMenu.getLayoutManager() != null ?
                            recyclerAppsMenu.getLayoutManager().findViewByPosition(finalPos) : null;
                    if (child != null) {
                        child.requestFocus();
                    } else if (recyclerAppsMenu.getChildCount() > 0) {
                        recyclerAppsMenu.getChildAt(0).requestFocus();
                    }
                }
            });
        }
    }

    private void focusSuperMode() {
        if (recyclerSuperMode == null || superModeAdapter == null || superModeAdapter.getItemCount() == 0) return;

        View focused = getCurrentFocus();
        int targetPos = lastFocusedSuperPosition;

        if (focused != null && isViewDescendantOf(focused, recyclerAppsMenu)) {
            int[] focusedLoc = new int[2];
            focused.getLocationOnScreen(focusedLoc);
            int focusedCenterX = focusedLoc[0] + focused.getWidth() / 2;

            int closestDiff = Integer.MAX_VALUE;
            int closestPos = RecyclerView.NO_POSITION;

            for (int i = 0; i < recyclerSuperMode.getChildCount(); i++) {
                View child = recyclerSuperMode.getChildAt(i);
                if (child != null && child.isFocusable()) {
                    int[] childLoc = new int[2];
                    child.getLocationOnScreen(childLoc);
                    int childCenterX = childLoc[0] + child.getWidth() / 2;
                    int diff = Math.abs(focusedCenterX - childCenterX);
                    if (diff < closestDiff) {
                        closestDiff = diff;
                        int pos = recyclerSuperMode.getChildAdapterPosition(child);
                        if (pos != RecyclerView.NO_POSITION) {
                            closestPos = pos;
                        }
                    }
                }
            }
            if (closestPos != RecyclerView.NO_POSITION) {
                targetPos = closestPos;
            }
        }

        targetPos = Math.max(0, Math.min(targetPos, superModeAdapter.getItemCount() - 1));
        lastFocusedSuperPosition = targetPos;

        final int finalPos = targetPos;
        android.util.Log.i(TAG, "focusSuperMode: targetPos=" + finalPos);

        View direct = recyclerSuperMode.getLayoutManager() != null ?
                recyclerSuperMode.getLayoutManager().findViewByPosition(finalPos) : null;
        if (direct == null && recyclerSuperMode.getChildCount() > 0) {
            direct = recyclerSuperMode.getChildAt(0);
        }

        if (direct != null) {
            boolean req = direct.requestFocus();
            android.util.Log.i(TAG, "focusSuperMode: direct requestFocus=" + req + " on " + direct);
        } else {
            recyclerSuperMode.scrollToPosition(finalPos);
            recyclerSuperMode.post(new Runnable() {
                @Override
                public void run() {
                    View child = recyclerSuperMode.getLayoutManager() != null ?
                            recyclerSuperMode.getLayoutManager().findViewByPosition(finalPos) : null;
                    if (child != null) {
                        child.requestFocus();
                    } else if (recyclerSuperMode.getChildCount() > 0) {
                        recyclerSuperMode.getChildAt(0).requestFocus();
                    }
                }
            });
        }
    }

    private void focusHeader() {
        View focused = getCurrentFocus();
        View target = btnCircleInputs;
        if (focused != null) {
            int[] loc = new int[2];
            focused.getLocationOnScreen(loc);
            int focusedX = loc[0] + focused.getWidth() / 2;

            View[] headerButtons = new View[]{btnCircleInputs, btnCircleUiSettings, btnCircleWifi, btnCircleBluetooth, btnCircleTvSettings};
            int closestDiff = Integer.MAX_VALUE;
            for (View btn : headerButtons) {
                if (btn != null && btn.getVisibility() == View.VISIBLE) {
                    int[] bLoc = new int[2];
                    btn.getLocationOnScreen(bLoc);
                    int bX = bLoc[0] + btn.getWidth() / 2;
                    int diff = Math.abs(focusedX - bX);
                    if (diff < closestDiff) {
                        closestDiff = diff;
                        target = btn;
                    }
                }
            }
        }
        if (target != null) {
            target.requestFocus();
        }
        if (mainScrollView != null) {
            mainScrollView.smoothScrollTo(0, 0);
        }
    }

    private void focusInputsRow() {
        View focused = getCurrentFocus();
        View target = chipShortcutAllApps;

        View[] chips = new View[]{
                chipInputHdmi1, chipInputHdmi2, chipInputHdmi3, chipInputUsb, chipInputAv,
                chipShortcutAllApps, chipShortcutUiSettings, chipShortcutTvSettings, chipShortcutSearch
        };

        if (focused != null) {
            int[] loc = new int[2];
            focused.getLocationOnScreen(loc);
            int focusedX = loc[0] + focused.getWidth() / 2;

            int closestDiff = Integer.MAX_VALUE;
            for (View chip : chips) {
                if (chip != null && chip.getVisibility() == View.VISIBLE) {
                    int[] cLoc = new int[2];
                    chip.getLocationOnScreen(cLoc);
                    int cX = cLoc[0] + chip.getWidth() / 2;
                    int diff = Math.abs(focusedX - cX);
                    if (diff < closestDiff) {
                        closestDiff = diff;
                        target = chip;
                    }
                }
            }
        }

        if (target != null) {
            target.requestFocus();
        }
        if (mainScrollView != null) {
            float density = getResources().getDisplayMetrics().density;
            mainScrollView.smoothScrollTo(0, (int) (260f * density));
        }
    }

    @Override
    public void onBackPressed() {
        if (isReordering) {
            cancelReorder();
            return;
        }
        // A TV launcher should never exit/finish on Back!
        if (recyclerAppsMenu != null) {
            recyclerAppsMenu.requestFocus();
            recyclerAppsMenu.smoothScrollToPosition(0);
        }
        if (mainScrollView != null) {
            mainScrollView.smoothScrollTo(0, 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isReordering) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (reorderPosition > 0) {
                    Collections.swap(featuredApps, reorderPosition, reorderPosition - 1);
                    appsMenuAdapter.notifyItemMoved(reorderPosition, reorderPosition - 1);
                    reorderPosition--;
                    appsMenuAdapter.setReordering(true, reorderPosition);
                    recyclerAppsMenu.smoothScrollToPosition(reorderPosition);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (reorderPosition < featuredApps.size() - 1) {
                    Collections.swap(featuredApps, reorderPosition, reorderPosition + 1);
                    appsMenuAdapter.notifyItemMoved(reorderPosition, reorderPosition + 1);
                    reorderPosition++;
                    appsMenuAdapter.setReordering(true, reorderPosition);
                    recyclerAppsMenu.smoothScrollToPosition(reorderPosition);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                dropReorder();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                cancelReorder();
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (isReordering) {
                cancelReorder();
            }
            if (recyclerAppsMenu != null) {
                recyclerAppsMenu.requestFocus();
                recyclerAppsMenu.smoothScrollToPosition(0);
            }
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(clockRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(packageReceiver);
        } catch (Exception ignored) {}
    }

    private void initViews() {
        mainScrollView = findViewById(R.id.main_scroll_view);
        rootLayout = findViewById(R.id.root_layout);
        spacerClockAppMenu = findViewById(R.id.spacer_clock_app_menu);

        // Header Clock
        textClockTime = findViewById(R.id.text_clock_time);
        textClockSeconds = findViewById(R.id.text_clock_seconds);
        textClockAmPm = findViewById(R.id.text_clock_am_pm);
        textClockDate = findViewById(R.id.text_clock_date);

        // Header Circular 5 Controls (48x48 dp / 96x96 px)
        btnCircleInputs = findViewById(R.id.btn_circle_inputs);
        btnCircleUiSettings = findViewById(R.id.btn_circle_ui_settings);
        btnCircleWifi = findViewById(R.id.btn_circle_wifi);
        btnCircleBluetooth = findViewById(R.id.btn_circle_bluetooth);
        btnCircleTvSettings = findViewById(R.id.btn_circle_tv_settings);

        icCircleInputs = findViewById(R.id.ic_circle_inputs);
        icCircleUiSettings = findViewById(R.id.ic_circle_ui_settings);
        icCircleWifi = findViewById(R.id.ic_circle_wifi);
        icCircleBluetooth = findViewById(R.id.ic_circle_bluetooth);
        icCircleTvSettings = findViewById(R.id.ic_circle_tv_settings);

        FocusAnimator.attachCircleButton(btnCircleInputs, icCircleInputs);
        FocusAnimator.attachCircleButton(btnCircleUiSettings, icCircleUiSettings);
        FocusAnimator.attachCircleButton(btnCircleWifi, icCircleWifi);
        FocusAnimator.attachCircleButton(btnCircleBluetooth, icCircleBluetooth);
        FocusAnimator.attachCircleButton(btnCircleTvSettings, icCircleTvSettings);

        // Super Mode Container & Recycler
        superModeContainer = findViewById(R.id.super_mode_container);
        labelSuperMode = findViewById(R.id.label_super_mode);
        recyclerSuperMode = findViewById(R.id.recycler_super_mode);

        // App Carousel (Widescreen 16:9 full tiles)
        recyclerAppsMenu = findViewById(R.id.recycler_apps_menu);

        // Inputs & Shortcuts (60 dp / 120 px)
        chipInputHdmi1 = findViewById(R.id.chip_input_hdmi1);
        chipInputHdmi2 = findViewById(R.id.chip_input_hdmi2);
        chipInputHdmi3 = findViewById(R.id.chip_input_hdmi3);
        chipInputUsb = findViewById(R.id.chip_input_usb);
        chipInputAv = findViewById(R.id.chip_input_av);
        inputsDivider = findViewById(R.id.inputs_divider);

        chipShortcutAllApps = findViewById(R.id.chip_shortcut_all_apps);
        chipShortcutUiSettings = findViewById(R.id.chip_shortcut_ui_settings);
        chipShortcutTvSettings = findViewById(R.id.chip_shortcut_tv_settings);
        chipShortcutSearch = findViewById(R.id.chip_shortcut_search);

        icHdmi1 = findViewById(R.id.ic_hdmi1);
        icHdmi2 = findViewById(R.id.ic_hdmi2);
        icHdmi3 = findViewById(R.id.ic_hdmi3);
        icUsb = findViewById(R.id.ic_usb);
        icAv = findViewById(R.id.ic_av);
        icAllApps = findViewById(R.id.ic_all_apps);
        icUiSettings = findViewById(R.id.ic_ui_settings);
        icTvSettings = findViewById(R.id.ic_tv_settings);
        icSearch = findViewById(R.id.ic_search);

        textHdmi1 = findViewById(R.id.text_hdmi1);
        textHdmi2 = findViewById(R.id.text_hdmi2);
        textHdmi3 = findViewById(R.id.text_hdmi3);
        textUsb = findViewById(R.id.text_usb);
        textAv = findViewById(R.id.text_av);
        textAllApps = findViewById(R.id.text_all_apps);
        textUiSettings = findViewById(R.id.text_ui_settings);
        textTvSettings = findViewById(R.id.text_tv_settings);
        textSearch = findViewById(R.id.text_search);

        FocusAnimator.attachPill(chipInputHdmi1, icHdmi1, textHdmi1);
        FocusAnimator.attachPill(chipInputHdmi2, icHdmi2, textHdmi2);
        FocusAnimator.attachPill(chipInputHdmi3, icHdmi3, textHdmi3);
        FocusAnimator.attachPill(chipInputUsb, icUsb, textUsb);
        FocusAnimator.attachPill(chipInputAv, icAv, textAv);
        FocusAnimator.attachPill(chipShortcutAllApps, icAllApps, textAllApps);
        FocusAnimator.attachPill(chipShortcutUiSettings, icUiSettings, textUiSettings);
        FocusAnimator.attachPill(chipShortcutTvSettings, icTvSettings, textTvSettings);
        FocusAnimator.attachPill(chipShortcutSearch, icSearch, textSearch);

        // Weather Widget (184 px / 92 dp)
        weatherWidgetCard = findViewById(R.id.weather_widget_card);
        weatherBadgeContainer = findViewById(R.id.weather_badge_container);
        weatherIcon = findViewById(R.id.weather_icon);
        textWeatherTemp = findViewById(R.id.text_weather_temp);
        textWeatherCondition = findViewById(R.id.text_weather_condition);
        textWeatherDetails = findViewById(R.id.text_weather_details);
        textWeatherHighLow = findViewById(R.id.text_weather_high_low);

        forecastChip1 = findViewById(R.id.forecast_chip_1);
        forecastChip2 = findViewById(R.id.forecast_chip_2);
        forecastChip3 = findViewById(R.id.forecast_chip_3);
        forecastChip4 = findViewById(R.id.forecast_chip_4);
        forecastChip5 = findViewById(R.id.forecast_chip_5);
        forecastChip6 = findViewById(R.id.forecast_chip_6);

        textForecastTime1 = findViewById(R.id.text_forecast_time_1);
        textForecastTime2 = findViewById(R.id.text_forecast_time_2);
        textForecastTime3 = findViewById(R.id.text_forecast_time_3);
        textForecastTime4 = findViewById(R.id.text_forecast_time_4);
        textForecastTime5 = findViewById(R.id.text_forecast_time_5);
        textForecastTime6 = findViewById(R.id.text_forecast_time_6);

        textForecastTemp1 = findViewById(R.id.text_forecast_temp_1);
        textForecastTemp2 = findViewById(R.id.text_forecast_temp_2);
        textForecastTemp3 = findViewById(R.id.text_forecast_temp_3);
        textForecastTemp4 = findViewById(R.id.text_forecast_temp_4);
        textForecastTemp5 = findViewById(R.id.text_forecast_temp_5);
        textForecastTemp6 = findViewById(R.id.text_forecast_temp_6);

        icForecast1 = findViewById(R.id.ic_forecast_1);
        icForecast2 = findViewById(R.id.ic_forecast_2);
        icForecast3 = findViewById(R.id.ic_forecast_3);
        icForecast4 = findViewById(R.id.ic_forecast_4);
        icForecast5 = findViewById(R.id.ic_forecast_5);
        icForecast6 = findViewById(R.id.ic_forecast_6);
    }

    private void setupAdapters() {
        float density = getResources().getDisplayMetrics().density;

        // 1. Super Mode Adapter
        superModeAdapter = new SuperModeAdapter(item -> item.launch(this));
        superModeAdapter.setOnMediaFocusedListener((item, view, position) -> lastFocusedSuperPosition = position);
        if (recyclerSuperMode != null) {
            LinearLayoutManager superLm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerSuperMode.setLayoutManager(superLm);
            recyclerSuperMode.setAdapter(superModeAdapter);
            recyclerSuperMode.setItemAnimator(null);
        }

        // 2. Apps Menu Adapter (Full 16:9 cards with Drag/Reorder on hold OK)
        appsMenuAdapter = new DockAdapter(new DockAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppEntry app) {
                if (isReordering) {
                    dropReorder();
                    return;
                }
                launchApp(app);
            }

            @Override
            public void onAppLongClick(AppEntry app, View view, int position) {
                if (isReordering) {
                    dropReorder();
                } else {
                    startReorder(position);
                }
            }

            @Override
            public void onAppFocused(AppEntry app, View view) {
                if (view != null && recyclerAppsMenu != null) {
                    int pos = recyclerAppsMenu.getChildAdapterPosition(view);
                    if (pos != RecyclerView.NO_POSITION) {
                        lastFocusedAppPosition = pos;
                    }
                }
                if (prefs.isAdaptiveColor()) {
                    handler.removeCallbacks(adaptiveColorRunnable);
                    handler.postDelayed(adaptiveColorRunnable, 600);
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerAppsMenu.setLayoutManager(layoutManager);
        recyclerAppsMenu.setAdapter(appsMenuAdapter);
        recyclerAppsMenu.setItemAnimator(null);

        weatherWidgetCard.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && mainScrollView != null) {
                mainScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void startReorder(int position) {
        if (position < 0 || position >= featuredApps.size()) return;
        isReordering = true;
        reorderPosition = position;
        reorderOriginalList = new ArrayList<>(featuredApps);
        appsMenuAdapter.setReordering(true, position);
        Toast.makeText(this, "Move Mode: Press ◄ / ► to move, OK to save, Back to cancel", Toast.LENGTH_SHORT).show();
        if (recyclerAppsMenu != null) {
            recyclerAppsMenu.smoothScrollToPosition(position);
        }
    }

    private void dropReorder() {
        if (!isReordering) return;
        isReordering = false;
        appsMenuAdapter.setReordering(false, -1);
        reorderOriginalList = null;

        // Persist new order to Preferences
        List<String> pinned = new ArrayList<>();
        for (AppEntry e : featuredApps) {
            pinned.add(e.getPackageName());
        }
        prefs.setPinnedPackages(pinned);
        Toast.makeText(this, "App position saved", Toast.LENGTH_SHORT).show();
    }

    private void cancelReorder() {
        if (!isReordering) return;
        isReordering = false;
        if (reorderOriginalList != null) {
            featuredApps.clear();
            featuredApps.addAll(reorderOriginalList);
            reorderOriginalList = null;
        }
        appsMenuAdapter.setReordering(false, -1);
        appsMenuAdapter.setItems(featuredApps);
        Toast.makeText(this, "Reorder cancelled", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        // 1. Inputs: Jump to HDMI 1
        btnCircleInputs.setOnClickListener(v -> {
            if (chipInputHdmi1 != null) {
                chipInputHdmi1.requestFocus();
            }
        });

        // 2. UI Settings: Flip Light / Dark mode directly on click; long press opens full settings
        btnCircleUiSettings.setOnClickListener(v -> toggleLightDarkMode());
        btnCircleUiSettings.setOnLongClickListener(v -> {
            showSettingsDialog();
            return true;
        });

        // 3. Wi-Fi
        btnCircleWifi.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_WIFI_SETTINGS));

        // 4. Bluetooth
        btnCircleBluetooth.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_BLUETOOTH_SETTINGS));

        // 5. Android TV Settings
        btnCircleTvSettings.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_SETTINGS));

        // Inputs row clicks
        chipInputHdmi1.setOnClickListener(v -> launchPassthroughInput("HDMI 1"));
        chipInputHdmi2.setOnClickListener(v -> launchPassthroughInput("HDMI 2"));
        chipInputHdmi3.setOnClickListener(v -> launchPassthroughInput("HDMI 3"));
        chipInputAv.setOnClickListener(v -> launchPassthroughInput("AV"));
        chipInputUsb.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));

        // Shortcuts
        chipShortcutAllApps.setOnClickListener(v -> showAllAppsDialog());
        chipShortcutUiSettings.setOnClickListener(v -> showSettingsDialog());
        chipShortcutTvSettings.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_SETTINGS));
        chipShortcutSearch.setOnClickListener(v -> {
            try {
                Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
                startActivity(searchIntent);
            } catch (Exception e) {
                try {
                    Intent assistIntent = new Intent(Intent.ACTION_ASSIST);
                    startActivity(assistIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Weather toggle Celsius / Fahrenheit
        weatherWidgetCard.setOnClickListener(v -> {
            prefs.setCelsius(!prefs.isCelsius());
            renderWeatherDisplay();
        });
    }

    private void updateLayoutMode() {
        boolean isSuper = AppPreferences.LAYOUT_SUPER.equals(prefs.getHomeLayout());
        float density = getResources().getDisplayMetrics().density;

        if (superModeContainer != null) {
            superModeContainer.setVisibility(isSuper ? View.VISIBLE : View.GONE);
        }

        // Space between Clock and Content (Airy breathing space in Minimal mode)
        if (spacerClockAppMenu != null) {
            ViewGroup.LayoutParams lp = spacerClockAppMenu.getLayoutParams();
            if (lp != null) {
                lp.height = (int) ((isSuper ? 14f : 40f) * density);
                spacerClockAppMenu.setLayoutParams(lp);
            }
        }

        if (isSuper) {
            TmdbRepository.fetchRecommendations(this, items -> {
                if (superModeAdapter != null && items != null && !items.isEmpty()) {
                    superModeAdapter.setItems(items);
                }
            });
        }
    }

    private void launchSafeSettings(String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Settings.ACTION_SETTINGS);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallback);
            } catch (Exception ex) {
                Toast.makeText(this, "Settings unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleLightDarkMode() {
        boolean isDark = !prefs.isDarkMode();
        prefs.setDarkMode(isDark);
        applyMaterial3Theme();
        Toast.makeText(this, isDark ? "Dark Theme" : "Light Theme", Toast.LENGTH_SHORT).show();
    }

    private void launchPassthroughInput(String name) {
        List<TvInputEntry> inputs = TvInputHelper.getAvailableInputs(this);
        for (TvInputEntry in : inputs) {
            String label = in.getLabel() != null ? in.getLabel().toString() : "";
            if (label.equalsIgnoreCase(name) || in.getId().toLowerCase().contains(name.toLowerCase().replace(" ", ""))) {
                TvInputHelper.launchInput(this, in);
                return;
            }
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("passthrough://" + name.replace(" ", "").toLowerCase()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Opening " + name + "...", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshWeather() {
        WeatherRepository.fetchWeather(this, data -> {
            currentWeatherData = data;
            renderWeatherDisplay();
        });
    }

    private void renderWeatherDisplay() {
        if (currentWeatherData == null) {
            currentWeatherData = WeatherRepository.getFallbackData(prefs.getCity());
        }

        boolean celsius = prefs.isCelsius();
        int cur = celsius ? currentWeatherData.currentTempC : cToF(currentWeatherData.currentTempC);
        int app = celsius ? currentWeatherData.apparentTempC : cToF(currentWeatherData.apparentTempC);
        int hi = celsius ? currentWeatherData.highC : cToF(currentWeatherData.highC);
        int lo = celsius ? currentWeatherData.lowC : cToF(currentWeatherData.lowC);
        String unit = celsius ? "°" : "°F";

        textWeatherTemp.setText(cur + unit);
        textWeatherCondition.setText(currentWeatherData.condition);
        textWeatherDetails.setText("Feels like " + app + unit + " · Humidity " + currentWeatherData.humidity + "% · Wind " + currentWeatherData.windKmh + " km/h");
        textWeatherHighLow.setText("High " + hi + unit + " · Low " + lo + unit);
        weatherIcon.setImageResource(currentWeatherData.iconRes);

        // 6 Hourly slots
        TextView[] times = {textForecastTime1, textForecastTime2, textForecastTime3, textForecastTime4, textForecastTime5, textForecastTime6};
        TextView[] temps = {textForecastTemp1, textForecastTemp2, textForecastTemp3, textForecastTemp4, textForecastTemp5, textForecastTemp6};
        ImageView[] icons = {icForecast1, icForecast2, icForecast3, icForecast4, icForecast5, icForecast6};

        List<WeatherRepository.HourlySlot> slots = currentWeatherData.hourlySlots;
        for (int i = 0; i < times.length; i++) {
            if (i < slots.size()) {
                WeatherRepository.HourlySlot s = slots.get(i);
                times[i].setText(s.timeLabel);
                int t = celsius ? s.tempC : cToF(s.tempC);
                temps[i].setText(t + unit);
                icons[i].setImageResource(s.iconRes);
            }
        }
    }

    private int cToF(int c) {
        return (int) Math.round((c * 9.0 / 5.0) + 32);
    }

    private void updateClockDisplay() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        boolean is24h = prefs.isClock24h();

        if (is24h) {
            SimpleDateFormat time24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textClockTime.setText(time24.format(now));
            textClockAmPm.setVisibility(View.GONE);
        } else {
            SimpleDateFormat time12 = new SimpleDateFormat("h:mm", Locale.getDefault());
            textClockTime.setText(time12.format(now));
            SimpleDateFormat amPmFormat = new SimpleDateFormat("a", Locale.getDefault());
            textClockAmPm.setText(amPmFormat.format(now));
            textClockAmPm.setVisibility(View.VISIBLE);
        }

        // Live Seconds in primary color
        SimpleDateFormat secFormat = new SimpleDateFormat(":ss", Locale.getDefault());
        textClockSeconds.setText(secFormat.format(now));

        // Full Date: Weekday, Day, Month, Year
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        textClockDate.setText(dateFormat.format(now));
    }

    private Drawable loadHighResIcon(PackageManager pm, ActivityInfo ai) {
        try {
            int iconRes = ai.getIconResource();
            if (iconRes != 0) {
                Resources res = pm.getResourcesForApplication(ai.applicationInfo);
                Drawable d = res.getDrawableForDensity(iconRes, DisplayMetrics.DENSITY_XXHIGH, null);
                if (d != null) return d;
            }
        } catch (Throwable ignored) {}
        try {
            return ai.loadIcon(pm);
        } catch (Throwable ignored) {
            return pm.getDefaultActivityIcon();
        }
    }

    private Drawable loadHighResBanner(PackageManager pm, ActivityInfo ai) {
        try {
            int bannerRes = ai.getBannerResource();
            if (bannerRes != 0) {
                Resources res = pm.getResourcesForApplication(ai.applicationInfo);
                Drawable d = res.getDrawableForDensity(bannerRes, DisplayMetrics.DENSITY_XXHIGH, null);
                if (d != null) return d;
            }
        } catch (Throwable ignored) {}
        try {
            return ai.loadBanner(pm);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        if (resolveInfos.isEmpty()) {
            Intent fallback = new Intent(Intent.ACTION_MAIN, null);
            fallback.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveInfos = pm.queryIntentActivities(fallback, 0);
        }

        allApps.clear();
        featuredApps.clear();

        List<String> pinned = prefs.getPinnedPackages();
        Set<String> hidden = prefs.getHiddenPackages();
        Map<String, AppEntry> appMap = new HashMap<>();

        for (ResolveInfo info : resolveInfos) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (hidden.contains(pkg)) continue;

            CharSequence label = info.loadLabel(pm);
            Drawable icon = loadHighResIcon(pm, info.activityInfo);
            Drawable banner = loadHighResBanner(pm, info.activityInfo);

            boolean isLeanback = true;
            AppEntry entry = new AppEntry(pkg, info.activityInfo.name, label, icon, banner, isLeanback);
            entry.setPinned(pinned.contains(pkg));
            allApps.add(entry);
            appMap.put(pkg, entry);
        }

        // Populate featuredApps preserving custom user order from pinned list
        for (String pkg : pinned) {
            AppEntry entry = appMap.get(pkg);
            if (entry != null && !featuredApps.contains(entry)) {
                featuredApps.add(entry);
            }
        }

        // If pinned list was empty or none matched, add first available apps
        if (featuredApps.isEmpty()) {
            for (int i = 0; i < Math.min(8, allApps.size()); i++) {
                featuredApps.add(allApps.get(i));
            }
        }

        // Sort allApps alphabetically for All Apps screen
        Collections.sort(allApps, (a, b) -> String.valueOf(a.getLabel()).compareToIgnoreCase(String.valueOf(b.getLabel())));

        appsMenuAdapter.setItems(featuredApps);
    }

    private void launchApp(AppEntry app) {
        try {
            startActivity(app.getLaunchIntent());
        } catch (Exception e) {
            try {
                Intent fallback = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                if (fallback != null) {
                    startActivity(fallback);
                } else {
                    Toast.makeText(this, "Unable to launch " + app.getLabel(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Launch error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAllAppsDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_m3_all_apps);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#141218")));
        }

        RecyclerView recycler = dialog.findViewById(R.id.dialog_recycler_all_apps);
        recycler.setLayoutManager(new GridLayoutManager(this, 5));

        final AppGridAdapter[] adapterHolder = new AppGridAdapter[1];
        AppGridAdapter adapter = new AppGridAdapter(new AppGridAdapter.OnAppActionListener() {
            @Override
            public void onAppClick(AppEntry app) {
                boolean isPinned = prefs.isPinned(app.getPackageName());
                String[] actions = {
                        "Open " + app.getLabel(),
                        isPinned ? "Remove from Home Selector" : "Add to Home Selector",
                        "App Info"
                };

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(app.getLabel())
                        .setItems(actions, (d, which) -> {
                            if (which == 0) {
                                dialog.dismiss();
                                launchApp(app);
                            } else if (which == 1) {
                                if (isPinned) {
                                    prefs.unpinPackage(app.getPackageName());
                                    Toast.makeText(MainActivity.this, "Removed from Home selector", Toast.LENGTH_SHORT).show();
                                } else {
                                    prefs.pinPackage(app.getPackageName());
                                    Toast.makeText(MainActivity.this, "Added to Home selector", Toast.LENGTH_SHORT).show();
                                }
                                if (adapterHolder[0] != null) {
                                    adapterHolder[0].setPinnedPackages(prefs.getPinnedPackages());
                                }
                                loadInstalledApps();
                            } else if (which == 2) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "App details unavailable", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }

            @Override
            public void onAppLongClick(AppEntry app, View view) {
                // Quick Toggle: Holding OK directly moves app to/from Home selector
                boolean isPinned = prefs.isPinned(app.getPackageName());
                if (isPinned) {
                    prefs.unpinPackage(app.getPackageName());
                    Toast.makeText(MainActivity.this, "Removed " + app.getLabel() + " from Home selector", Toast.LENGTH_SHORT).show();
                } else {
                    prefs.pinPackage(app.getPackageName());
                    Toast.makeText(MainActivity.this, "Added " + app.getLabel() + " to Home selector", Toast.LENGTH_SHORT).show();
                }
                if (adapterHolder[0] != null) {
                    adapterHolder[0].setPinnedPackages(prefs.getPinnedPackages());
                }
                loadInstalledApps();
            }
        });
        adapterHolder[0] = adapter;

        recycler.setAdapter(adapter);
        adapter.setItems(allApps);
        adapter.setPinnedPackages(prefs.getPinnedPackages());

        View btnClose = dialog.findViewById(R.id.btn_close_all_apps);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_m3_settings);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#141218")));
        }

        // 1. Home Layout: Minimal vs Super Mode
        M3Switch switchHomeLayout = dialog.findViewById(R.id.switch_home_layout);
        TextView textHomeLayoutLabel = dialog.findViewById(R.id.text_home_layout_label);
        boolean isSuper = AppPreferences.LAYOUT_SUPER.equals(prefs.getHomeLayout());
        if (switchHomeLayout != null) {
            switchHomeLayout.setChecked(isSuper, false);
            switchHomeLayout.setOnCheckedChangeListener((sw, isChecked) -> {
                String nextMode = isChecked ? AppPreferences.LAYOUT_SUPER : AppPreferences.LAYOUT_MINIMAL;
                prefs.setHomeLayout(nextMode);
                if (textHomeLayoutLabel != null) {
                    textHomeLayoutLabel.setText(isChecked ? "Super Mode (TMDB Recommendations)" : "Minimal Mode (Solid M3 Home)");
                }
                updateLayoutMode();
                Toast.makeText(this, isChecked ? "Super Mode Enabled" : "Minimal Mode Enabled", Toast.LENGTH_SHORT).show();
            });
        }
        dialog.findViewById(R.id.row_toggle_home_layout).setOnClickListener(v -> {
            if (switchHomeLayout != null) switchHomeLayout.toggle();
        });

        // 2. Dark Mode Switch
        M3Switch switchDark = dialog.findViewById(R.id.switch_dark_mode);
        if (switchDark != null) {
            switchDark.setChecked(prefs.isDarkMode(), false);
            switchDark.setOnCheckedChangeListener((sw, isChecked) -> {
                prefs.setDarkMode(isChecked);
                applyMaterial3Theme();
                Toast.makeText(this, isChecked ? "Dark Theme Active" : "Light Theme Active", Toast.LENGTH_SHORT).show();
            });
        }
        dialog.findViewById(R.id.row_toggle_dark_mode).setOnClickListener(v -> {
            if (switchDark != null) switchDark.toggle();
        });

        // 3. Dynamic Adaptive Color Switch
        M3Switch switchAdaptive = dialog.findViewById(R.id.switch_adaptive_color);
        if (switchAdaptive != null) {
            switchAdaptive.setChecked(prefs.isAdaptiveColor(), false);
            switchAdaptive.setOnCheckedChangeListener((sw, isChecked) -> {
                prefs.setAdaptiveColor(isChecked);
                if (!isChecked) {
                    applyMaterial3Theme();
                }
                Toast.makeText(this, isChecked ? "Adaptive Colors Enabled" : "Adaptive Colors Disabled", Toast.LENGTH_SHORT).show();
            });
        }
        dialog.findViewById(R.id.row_toggle_adaptive_color).setOnClickListener(v -> {
            if (switchAdaptive != null) switchAdaptive.toggle();
        });

        // 4. Reorder Info Row
        dialog.findViewById(R.id.row_reorder_info).setOnClickListener(v -> {
            Toast.makeText(this, "Focus any app on Home and hold OK to drag and change position", Toast.LENGTH_LONG).show();
        });

        // 5. Reset Home Apps Button
        dialog.findViewById(R.id.row_reset_home_apps).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Home Apps")
                    .setMessage("Restore default streaming applications to the Home carousel?")
                    .setPositiveButton("Reset", (d, which) -> {
                        prefs.setPinnedPackages(new ArrayList<>(java.util.Arrays.asList(
                                "com.google.android.youtube.tv",
                                "com.netflix.ninja",
                                "com.amazon.amazonvideo.livingroom",
                                "com.disney.disneyplus",
                                "org.videolan.vlc"
                        )));
                        loadInstalledApps();
                        Toast.makeText(this, "Home apps reset to default", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 6. 24H Clock Toggle
        M3Switch switchClock = dialog.findViewById(R.id.switch_24h_clock);
        if (switchClock != null) {
            switchClock.setChecked(prefs.isClock24h(), false);
            switchClock.setOnCheckedChangeListener((sw, isChecked) -> {
                prefs.setClock24h(isChecked);
                updateClockDisplay();
            });
        }
        dialog.findViewById(R.id.row_toggle_clock).setOnClickListener(v -> {
            if (switchClock != null) switchClock.toggle();
        });

        // 7. Weather Temperature Unit Toggle
        M3Switch switchWeather = dialog.findViewById(R.id.switch_weather_temp);
        TextView textWeatherUnit = dialog.findViewById(R.id.text_weather_unit_title);
        if (switchWeather != null) {
            switchWeather.setChecked(prefs.isCelsius(), false);
            switchWeather.setOnCheckedChangeListener((sw, isChecked) -> {
                prefs.setCelsius(isChecked);
                if (textWeatherUnit != null) {
                    textWeatherUnit.setText("Weather Unit: " + (isChecked ? "Celsius (°C)" : "Fahrenheit (°F)"));
                }
                renderWeatherDisplay();
            });
        }
        dialog.findViewById(R.id.row_toggle_weather_temp).setOnClickListener(v -> {
            if (switchWeather != null) switchWeather.toggle();
        });

        // 8. Weather City Picker
        TextView textCityLabel = dialog.findViewById(R.id.text_city_label);
        textCityLabel.setText("Weather City: " + prefs.getCity());
        dialog.findViewById(R.id.row_weather_city).setOnClickListener(v -> {
            showCityPickerDialog(dialog, textCityLabel);
        });

        // 9. Hardware & System Buttons
        dialog.findViewById(R.id.btn_open_system_settings).setOnClickListener(v -> {
            dialog.dismiss();
            launchSafeSettings(Settings.ACTION_SETTINGS);
        });

        dialog.findViewById(R.id.btn_open_wifi_settings).setOnClickListener(v -> {
            dialog.dismiss();
            launchSafeSettings(Settings.ACTION_WIFI_SETTINGS);
        });

        dialog.findViewById(R.id.btn_open_bluetooth_settings).setOnClickListener(v -> {
            dialog.dismiss();
            launchSafeSettings(Settings.ACTION_BLUETOOTH_SETTINGS);
        });

        dialog.findViewById(R.id.btn_close_settings).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showCityPickerDialog(Dialog parentDialog, TextView labelView) {
        String[] cities = {"New York", "London", "Tokyo", "Paris", "Berlin", "Mumbai", "Sydney", "Toronto", "Dubai", "Singapore"};
        new AlertDialog.Builder(this)
                .setTitle("Select Weather Location")
                .setItems(cities, (d, which) -> {
                    String selected = cities[which];
                    prefs.setCity(selected);
                    labelView.setText("Weather City: " + selected);
                    refreshWeather();
                    Toast.makeText(this, "Location set to " + selected, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void applyMaterial3Theme() {
        ThemeManager.ExpressivePalette p = ThemeManager.getCurrentPalette(this);
        applyPalette(p);
    }

    private void applyPalette(ThemeManager.ExpressivePalette p) {
        float density = getResources().getDisplayMetrics().density;

        // Root Background
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(p.bg);
        }

        // Header Clock
        if (textClockTime != null) textClockTime.setTextColor(p.onbg);
        if (textClockSeconds != null) textClockSeconds.setTextColor(p.pri);
        if (textClockAmPm != null) textClockAmPm.setTextColor(p.pri);
        if (textClockDate != null) textClockDate.setTextColor(p.osv);

        // Header Circular Buttons
        View[] circles = {btnCircleInputs, btnCircleUiSettings, btnCircleWifi, btnCircleBluetooth, btnCircleTvSettings};
        ImageView[] circleIcons = {icCircleInputs, icCircleUiSettings, icCircleWifi, icCircleBluetooth, icCircleTvSettings};
        for (int i = 0; i < circles.length; i++) {
            if (circles[i] != null) {
                GradientDrawable bgd = new GradientDrawable();
                bgd.setCornerRadius(20f * density);
                bgd.setColor(p.sch);
                circles[i].setBackground(bgd);
            }
            if (circleIcons[i] != null) {
                circleIcons[i].setColorFilter(p.onbg);
            }
        }

        // Super Mode Label
        if (labelSuperMode != null) {
            labelSuperMode.setTextColor(p.pri);
        }

        // Inputs & Shortcuts Pills
        View[] pills = {chipInputHdmi1, chipInputHdmi2, chipInputHdmi3, chipInputUsb, chipInputAv,
                chipShortcutAllApps, chipShortcutUiSettings, chipShortcutTvSettings, chipShortcutSearch};
        ImageView[] pillIcons = {icHdmi1, icHdmi2, icHdmi3, icUsb, icAv,
                icAllApps, icUiSettings, icTvSettings, icSearch};
        TextView[] pillTexts = {textHdmi1, textHdmi2, textHdmi3, textUsb, textAv,
                textAllApps, textUiSettings, textTvSettings, textSearch};

        for (int i = 0; i < pills.length; i++) {
            if (pills[i] != null) {
                GradientDrawable pgd = new GradientDrawable();
                pgd.setCornerRadius(17f * density);
                pgd.setColor(p.sch);
                pills[i].setBackground(pgd);
            }
            if (pillIcons[i] != null) {
                pillIcons[i].setColorFilter(p.onbg);
            }
            if (pillTexts[i] != null) {
                pillTexts[i].setTextColor(p.onbg);
            }
        }

        // Divider
        if (inputsDivider != null) {
            inputsDivider.setBackgroundColor(p.sc);
        }

        // Weather Widget
        if (weatherWidgetCard != null) {
            GradientDrawable wgd = new GradientDrawable();
            wgd.setCornerRadius(20f * density);
            wgd.setColor(p.scl);
            weatherWidgetCard.setBackground(wgd);
        }

        if (weatherBadgeContainer != null) {
            GradientDrawable bbg = new GradientDrawable();
            bbg.setCornerRadius(28f * density);
            bbg.setColor(p.tc);
            weatherBadgeContainer.setBackground(bbg);
        }

        if (weatherIcon != null) weatherIcon.setColorFilter(p.ontc);
        if (textWeatherTemp != null) textWeatherTemp.setTextColor(p.onbg);
        if (textWeatherCondition != null) textWeatherCondition.setTextColor(p.onbg);
        if (textWeatherDetails != null) textWeatherDetails.setTextColor(p.osv);
        if (textWeatherHighLow != null) textWeatherHighLow.setTextColor(p.osv);

        // 6 Hourly Chips
        View[] forecastChips = {forecastChip1, forecastChip2, forecastChip3, forecastChip4, forecastChip5, forecastChip6};
        TextView[] forecastTimes = {textForecastTime1, textForecastTime2, textForecastTime3, textForecastTime4, textForecastTime5, textForecastTime6};
        TextView[] forecastTemps = {textForecastTemp1, textForecastTemp2, textForecastTemp3, textForecastTemp4, textForecastTemp5, textForecastTemp6};
        ImageView[] forecastIcons = {icForecast1, icForecast2, icForecast3, icForecast4, icForecast5, icForecast6};

        for (int i = 0; i < forecastChips.length; i++) {
            if (forecastChips[i] != null) {
                GradientDrawable cgd = new GradientDrawable();
                cgd.setCornerRadius(16f * density);
                if (i == 0) {
                    cgd.setColor(p.pri);
                    forecastChips[i].setBackground(cgd);
                    if (forecastTimes[i] != null) forecastTimes[i].setTextColor(p.onpri);
                    if (forecastTemps[i] != null) forecastTemps[i].setTextColor(p.onpri);
                    if (forecastIcons[i] != null) forecastIcons[i].setColorFilter(p.onpri);
                } else {
                    cgd.setColor(p.sch);
                    forecastChips[i].setBackground(cgd);
                    if (forecastTimes[i] != null) forecastTimes[i].setTextColor(p.osv);
                    if (forecastTemps[i] != null) forecastTemps[i].setTextColor(p.onbg);
                    if (forecastIcons[i] != null) forecastIcons[i].setColorFilter(p.onbg);
                }
            }
        }
    }
}
