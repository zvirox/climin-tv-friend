package io.climintv.home;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.climintv.home.adapter.AppGridAdapter;
import io.climintv.home.adapter.DockAdapter;
import io.climintv.home.model.AppEntry;
import io.climintv.home.model.TvInputEntry;
import io.climintv.home.theme.AdaptiveColorEngine;
import io.climintv.home.theme.ThemeManager;
import io.climintv.home.tif.TvInputHelper;
import io.climintv.home.util.AppPreferences;
import io.climintv.home.util.FocusAnimator;
import io.climintv.home.util.WeatherRepository;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AppPreferences prefs;

    // Root Layout
    private View rootLayout;

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

    // App Cards Carousel (Icon-Only, 340x200 px / 170x100 dp)
    private RecyclerView recyclerAppsMenu;
    private DockAdapter appsMenuAdapter;
    private final List<AppEntry> allApps = new ArrayList<>();
    private final List<AppEntry> featuredApps = new ArrayList<>();

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
    private ValueAnimator adaptiveThemeAnimator;

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
        setContentView(R.layout.activity_main);

        prefs = new AppPreferences(this);
        initViews();
        setupAdapters();
        setupListeners();
        loadInstalledApps();
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
        handler.post(clockRunnable);
        applyMaterial3Theme();
        refreshWeather();
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
        rootLayout = findViewById(R.id.root_layout);

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

        // App Carousel (Icon-Only wide rounded tiles)
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
        appsMenuAdapter = new DockAdapter(new DockAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(AppEntry app) {
                launchApp(app);
            }

            @Override
            public void onAppLongClick(AppEntry app, View view) {
                showAppContextDialog(app);
            }

            @Override
            public void onAppFocused(AppEntry app, View view) {
                if (prefs.isAdaptiveColor()) {
                    int seed = AdaptiveColorEngine.extractSeedColor(app.getPackageName(), app.getIcon());
                    ThemeManager.ExpressivePalette adaptivePalette =
                            AdaptiveColorEngine.generatePaletteFromSeed(seed, prefs.isDarkMode());
                    applyPalette(adaptivePalette);
                }
            }
        });

        // Center-centering smooth spring motion scroll layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
                int parentCenter = parent.getWidth() / 2;
                int childCenter = (child.getLeft() + child.getRight()) / 2;
                int dx = childCenter - parentCenter;
                if (dx != 0) {
                    parent.smoothScrollBy(dx, 0, new DecelerateInterpolator(1.8f));
                    return true;
                }
                return false;
            }
        };

        recyclerAppsMenu.setLayoutManager(layoutManager);
        recyclerAppsMenu.setAdapter(appsMenuAdapter);
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

        // 3. Wi-Fi (With OEM fallbacks)
        btnCircleWifi.setOnClickListener(v -> launchSafeSettings(Settings.ACTION_WIFI_SETTINGS));

        // 4. Bluetooth (With OEM fallbacks)
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

        for (ResolveInfo info : resolveInfos) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (hidden.contains(pkg)) continue;

            CharSequence label = info.loadLabel(pm);
            Drawable icon = null;
            Drawable banner = null;
            try {
                icon = info.activityInfo.loadIcon(pm);
                banner = info.activityInfo.loadBanner(pm);
            } catch (Exception ignored) {}

            boolean isLeanback = true;
            AppEntry entry = new AppEntry(pkg, info.activityInfo.name, label, icon, banner, isLeanback);
            entry.setPinned(pinned.contains(pkg));
            allApps.add(entry);

            if (entry.isPinned()) {
                featuredApps.add(entry);
            }
        }

        if (featuredApps.isEmpty()) {
            featuredApps.addAll(allApps);
        }

        Collections.sort(featuredApps, (a, b) -> String.valueOf(a.getLabel()).compareToIgnoreCase(String.valueOf(b.getLabel())));
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

    private void showAppContextDialog(AppEntry app) {
        String[] options = {
                app.isPinned() ? "Unpin from Favorites" : "Pin to Favorites",
                "App Info",
                "Hide Application"
        };

        new AlertDialog.Builder(this)
                .setTitle(app.getLabel())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (app.isPinned()) {
                            prefs.unpinPackage(app.getPackageName());
                        } else {
                            prefs.pinPackage(app.getPackageName());
                        }
                        loadInstalledApps();
                    } else if (which == 1) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + app.getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "App details unavailable", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 2) {
                        prefs.setHidden(app.getPackageName(), true);
                        loadInstalledApps();
                    }
                })
                .show();
    }

    private void showAllAppsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_m3_all_apps, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RecyclerView recycler = dialogView.findViewById(R.id.dialog_recycler_all_apps);
        recycler.setLayoutManager(new GridLayoutManager(this, 4));

        AppGridAdapter adapter = new AppGridAdapter(new AppGridAdapter.OnAppActionListener() {
            @Override
            public void onAppClick(AppEntry app) {
                dialog.dismiss();
                launchApp(app);
            }

            @Override
            public void onAppLongClick(AppEntry app, View view) {
                dialog.dismiss();
                showAppContextDialog(app);
            }
        });

        recycler.setAdapter(adapter);
        adapter.setItems(allApps);

        View btnClose = dialogView.findViewById(R.id.btn_close_all_apps);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_m3_settings, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 1. Dark Mode
        Switch switchDark = dialogView.findViewById(R.id.switch_dark_mode);
        switchDark.setChecked(prefs.isDarkMode());
        dialogView.findViewById(R.id.row_toggle_dark_mode).setOnClickListener(v -> {
            boolean next = !switchDark.isChecked();
            switchDark.setChecked(next);
            prefs.setDarkMode(next);
            applyMaterial3Theme();
        });

        // 2. Adaptive Color (Section 8)
        Switch switchAdaptive = dialogView.findViewById(R.id.switch_adaptive_color);
        switchAdaptive.setChecked(prefs.isAdaptiveColor());
        dialogView.findViewById(R.id.row_toggle_adaptive_color).setOnClickListener(v -> {
            boolean next = !switchAdaptive.isChecked();
            switchAdaptive.setChecked(next);
            prefs.setAdaptiveColor(next);
            if (!next) {
                applyMaterial3Theme();
            }
        });

        // 3. Home Layout (Minimal vs Super) (Section 9)
        TextView textLayoutLabel = dialogView.findViewById(R.id.text_home_layout_label);
        textLayoutLabel.setText("Home Layout: " + (AppPreferences.LAYOUT_SUPER.equals(prefs.getHomeLayout()) ? "Super Mode" : "Minimal Mode"));
        dialogView.findViewById(R.id.row_toggle_home_layout).setOnClickListener(v -> {
            boolean isSuper = AppPreferences.LAYOUT_SUPER.equals(prefs.getHomeLayout());
            String nextLayout = isSuper ? AppPreferences.LAYOUT_MINIMAL : AppPreferences.LAYOUT_SUPER;
            prefs.setHomeLayout(nextLayout);
            textLayoutLabel.setText("Home Layout: " + (AppPreferences.LAYOUT_SUPER.equals(nextLayout) ? "Super Mode" : "Minimal Mode"));
            Toast.makeText(this, "Layout set to " + (isSuper ? "Minimal" : "Super"), Toast.LENGTH_SHORT).show();
        });

        // 4. Weather City Picker
        TextView textCityLabel = dialogView.findViewById(R.id.text_city_label);
        textCityLabel.setText("Weather City: " + prefs.getCity());
        dialogView.findViewById(R.id.row_weather_city).setOnClickListener(v -> {
            showCityPickerDialog(dialog, textCityLabel);
        });

        // 5. 24H Clock Toggle
        Switch switchClock = dialogView.findViewById(R.id.switch_24h_clock);
        switchClock.setChecked(prefs.isClock24h());
        dialogView.findViewById(R.id.row_toggle_clock).setOnClickListener(v -> {
            boolean next = !switchClock.isChecked();
            switchClock.setChecked(next);
            prefs.setClock24h(next);
            updateClockDisplay();
        });

        // 6. Weather Unit Toggle
        Switch switchWeather = dialogView.findViewById(R.id.switch_weather_temp);
        switchWeather.setChecked(prefs.isCelsius());
        dialogView.findViewById(R.id.row_toggle_weather_temp).setOnClickListener(v -> {
            boolean next = !switchWeather.isChecked();
            switchWeather.setChecked(next);
            prefs.setCelsius(next);
            renderWeatherDisplay();
        });

        dialogView.findViewById(R.id.btn_open_system_settings).setOnClickListener(v -> {
            dialog.dismiss();
            launchSafeSettings(Settings.ACTION_SETTINGS);
        });

        dialogView.findViewById(R.id.btn_close_settings).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showCityPickerDialog(AlertDialog parentDialog, TextView labelView) {
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
                bgd.setCornerRadius(24f * density);
                bgd.setColor(p.sch);
                circles[i].setBackground(bgd);
            }
            if (circleIcons[i] != null) {
                circleIcons[i].setColorFilter(p.onbg);
            }
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
                pgd.setCornerRadius(18f * density);
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

        if (appsMenuAdapter != null) {
            appsMenuAdapter.notifyDataSetChanged();
        }
    }
}
