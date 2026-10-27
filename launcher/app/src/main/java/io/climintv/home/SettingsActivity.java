package io.climintv.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Climin UI's settings (the app's icon, "All Apps", or the gear on the home screen): two glass panels on the home
 * screen's backdrop, as on Apple TV. The menu on the left picks what the right panel shows (Home Settings, Greetings,
 * Theme, Screensaver, Shelf, Top Buttons, Remote Buttons, Sleep Timer, About), and the focused row in either panel is
 * white.
 * The mode switch still runs in the main process (HomeMode), but this screen no longer offers it.
 */
public class SettingsActivity extends Activity {
    private SettingsView view;
    private DreamView preview;   // "Screensaver": the screensaver right here, without changing the system setting

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (openFireHome(getIntent())) return;
        Watchdog.install(this);
        Ui.init(this);
        SleepTimer.listen(this);   // :home may start here, from the launcher icon
        SleepTimer.check(this);
        view = new SettingsView(this);
        setContentView(view);
    }

    /**
     * Fire TV: Amazon keeps the Home button, so the app's icon opens the home screen (FireHomeActivity) directly;
     * the gear on the home screen opens these settings (no launcher category).
     */
    private boolean openFireHome(Intent intent) {
        if (!HomeMode.AMAZON || intent == null || !(intent.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                || intent.hasCategory(Intent.CATEGORY_LAUNCHER)) || intent.getBooleanExtra("screensaver", false)) {
            return false;
        }
        startActivity(new Intent(this, FireHomeActivity.class));
        finish();
        return true;
    }

    @Override protected void onResume() {
        super.onResume();
        if (view == null) return;
        view.resume();
        // From ADB: am start -n io.climintv.home/.SettingsActivity --ez screensaver true (straight to the preview).
        if (preview == null && getIntent().getBooleanExtra("screensaver", false)) {
            getIntent().removeExtra("screensaver");
            view.pause();
            showPreview();
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (openFireHome(intent)) return;
        setIntent(intent);
    }

    @Override protected void onPause() {
        closePreview();     // before pausing the view: closing the preview resumes it (view.resume)
        if (view != null) view.pause();
        super.onPause();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (preview != null) {
            // Like the real screensaver: arrows, OK or Back close it (other keys go on to the system).
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_BACK:
                    if (event.getAction() == KeyEvent.ACTION_UP) closePreview();
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        if (view.onKey(event)) return true;
        return super.dispatchKeyEvent(event);
    }

    void showPreview() {
        preview = new DreamView(this);
        setContentView(preview);
        preview.start();
    }

    private void closePreview() {
        if (preview == null) return;
        preview.stop();
        preview = null;
        setContentView(view);
        view.resume();
    }

    static final class SettingsView extends View implements Choreographer.FrameCallback {
        private static final int HOME_SETTINGS = 1, REMOTE_KEYS = 2, PICKER = 3, SHELF_EDIT = 4,
                POWER = 5, GREETINGS = 6, SCREENSAVER = 7, ABOUT = 8, THEME = 9;
        /** The left menu, in its order. */
        private static final int RAIL_HOME_SETTINGS = 0, RAIL_GREETINGS = 1, RAIL_THEME = 2, RAIL_SCREENSAVER = 3,
                RAIL_SHELF = 4, RAIL_TOP = 5, RAIL_REMOTE = 6, RAIL_POWER = 7, RAIL_ABOUT = 8, RAILS = 9;
        private static final int Z_RAIL = 0, Z_PANE = 1;
        private static final float LIST_ROW_H = 64, LIST_ROW_GAP = 6;
        private static final int PICK_VISIBLE = 8;
        private static final float LIST_X = 640, LIST_R = 1800, LIST_TOP = 236;
        /** The two glass panels: the menu on the left, what it holds on the right. */
        private static final float RAIL_X = 80, RAIL_R = 560, PANE_X = 600, PANE_R = 1840, PANEL_TOP = 120,
                PANEL_BOTTOM = 960, PANEL_RADIUS = 30;
        private static final float RAIL_ROW_H = 68, RAIL_ROW_GAP = 4, RAIL_ROW_X = 104, RAIL_ROW_R = 536, RAIL_ROW_TOP = 252;
        /** The right panel's heading baseline (its rows start at LIST_TOP). */
        private static final float PANE_HEAD = 190;
        private static final int INK = 0xFF1C1C1E, SMOKE = 0xFF111113;

        private final SettingsActivity activity;
        private final Scene scene = new Scene();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Spring[] pop = new Spring[12], railPop = new Spring[RAILS];
        private final List<String> appPkgs = new ArrayList<>(), appLabels = new ArrayList<>();
        // The list picker (the app of a remote button): what it shows, which one is in use, where we are.
        private final List<String> pickLabels = new ArrayList<>();
        // The shelf editor (also the top buttons' one, with editingTop): what it can hold (selected ones first, in
        // order) and the selection.
        private final List<String[]> shelfRows = new ArrayList<>();
        private final List<String> shelfOrder = new ArrayList<>();
        private boolean editingTop;
        private int pickFor, pickCurrent, pickScroll;   // pickFor: the remote button (index + 1)
        private static final int PICK_CITY = 0, PICK_LANGUAGE = -1, PICK_SLEEP = -2, PICK_PLACE = -3;
        /** Sleep Timer's pickers (PICK_SLEEP and below): their choices as {value, name}, and the value now. */
        private final List<String[]> choices = new ArrayList<>();
        private String choiceNow = "";
        private final List<String[]> cities = new ArrayList<>();
        private String cityStatus = "";
        private final RectF tmp = new RectF(), rowRect = new RectF(), focusBox = new RectF(), railBox = new RectF(),
                railRect = new RectF(), paneRect = new RectF(), screenRect = new RectF();
        private JSONObject state = new JSONObject();
        private int screen = HOME_SETTINGS, focus, railFocus, zone = Z_RAIL;
        private boolean running, okDown, backDown, wasBusy;
        private StaticLayout whyLayout;     // the panel's reason for a menu item this TV can't use
        private String whyReason;
        private Toast whyToast;   // why the focused option is unavailable
        private String[] whyRows;  // each row's reason, worked out once per screen (null: again on the next frame)
        private int whyScreen = -1, whyScroll = -1;
        private int sleepLeft;     // the sleep timer's minutes left, read with the state, not on every frame
        private int whyState = -1;
        private final Spoken spoken;
        private long lastNanos;
        private int w, h;
        private float k = 1;
        private boolean night;
        /** The look in use and its colors: the same values the panels used to hardcode. */
        private Look look = Look.current();
        private Palette pal = look.palette;
        private Bitmap backdrop;    // the home screen's photo, softly blurred behind the glass
        private boolean backdropTried;
        private int[] glassDay, glassNight;   // the same photo as the panels' glass reads it (lightened by day, darker at night)
        private int glassFor = -1;
        private Shader sky;
        private final Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        private final Paint fill = new Paint();
        private Paint brand, title, subtitle, version, rowLabel, rowValue, rowSub, railLabel, white, shadow, aboutName;
        private final TextPaint whyPaint = new TextPaint(), aboutPaint = new TextPaint();
        private StaticLayout aboutLayout;

        private final Runnable poll = new Runnable() {
            @Override public void run() {
                if (!running) return;
                state = HomeMode.state(getContext());
                sleepLeft = SleepTimer.minutesLeft(getContext());
                night = HomeData.get(getContext()).isNight(System.currentTimeMillis());
                boolean nowBusy = !busyText().isEmpty();
                // What can change the reasons away from a key: a switch (or the watchdog's revert) and the overlay's check.
                int whyNow = (freeActive() ? 1 : 0) | (Capabilities.refused(getContext(), Capabilities.OVERLAY) ? 2 : 0);
                if (whyNow != whyState) whyRows = null;
                whyState = whyNow;
                // A switch just ended: TalkBack reads how it went (the newest notice).
                JSONArray notices = state.optJSONArray("notices");
                if (wasBusy && !nowBusy && notices != null && notices.length() > 0) {
                    JSONObject newest = notices.optJSONObject(0);
                    if (newest != null) announceForAccessibility(newest.optString("text"));
                }
                wasBusy = nowBusy;
                invalidate();
                handler.postDelayed(this, busy() ? 500 : 2000);
            }
        };

        SettingsView(SettingsActivity a) {
            super(a);
            activity = a;
            for (int i = 0; i < pop.length; i++) pop[i] = new Spring(0.28f, 0.85f, 0);
            for (int i = 0; i < railPop.length; i++) railPop[i] = new Spring(0.28f, 0.85f, 0);
            HomeData.Weather wx = HomeData.get(a).weather;
            Shaders.sky(scene.palette, System.currentTimeMillis(), wx.sunrise, wx.sunset);
            scene.setMotion(0.5f, 0.2f);
            scene.applyPalette();
            setFocusable(true);
            setContentDescription("Climin UI");
            // TalkBack: the menu (ids 1 to 9), then one item per row of the panel on the right.
            spoken = new Spoken(this, new Spoken.Source() {
                @Override public void items(List<Integer> out) {
                    for (int i = 1; i <= RAILS; i++) out.add(i);
                    if (railUnavailable(railFocus) == null) {
                        for (int i = 1; i <= rowCount(); i++) out.add(RAILS + i);
                    }
                }
                @Override public String text(int id) {
                    if (id >= 1 && id <= RAILS) return railLabels()[id - 1];
                    int row = id - RAILS - 1;
                    return row >= 0 && row < rowCount() ? rowText(row) : "";
                }
                @Override public boolean bounds(int id, Rect out) {
                    if (id >= 1 && id <= RAILS) {
                        railRowBox(id - 1).round(out);
                        return true;
                    }
                    int row = id - RAILS - 1;
                    if (row < 0 || row >= rowCount()) return false;
                    rowBox(row).round(out);
                    return true;
                }
                @Override public int focused() { return zone == Z_RAIL ? railFocus + 1 : RAILS + focus + 1; }
                @Override public void focus(int id) {
                    if (id >= 1 && id <= RAILS) {
                        zone = Z_RAIL;
                        selectRail(id - 1);
                    } else if (id > RAILS) {
                        zone = Z_PANE;
                        focus = Math.max(0, Math.min(id - RAILS - 1, rowCount() - 1));
                    }
                    invalidate();
                }
                @Override public void click(int id) { activate(); }
            });
        }

        @Override public AccessibilityNodeProvider getAccessibilityNodeProvider() {
            return spoken;
        }

        /** A row as TalkBack reads it: its name and its value, as on screen, and why where it's unavailable. */
        private String rowText(int row) {
            String why = unavailable(row);
            return why == null ? rowValueText(row) : rowValueText(row) + ", " + Ui.tr("Unavailable") + ". " + why;
        }

        private String rowValueText(int row) {
            switch (screen) {
                case GREETINGS:
                    return Ui.tr("Coming soon.");
                case SCREENSAVER:
                    return Ui.tr("Screensaver") + ", " + Ui.tr("Preview");
                case THEME:
                    return row == 0 ? Ui.tr("Mode") + ", " + modeName() : Ui.tr("Style") + ", " + look.name;
                case HOME_SETTINGS:
                case REMOTE_KEYS:
                case POWER: {
                    boolean keys = screen == REMOTE_KEYS;
                    String[] labels = listLabels(), values = listValues();
                    int at = keys ? row : pickScroll + row;
                    return at < labels.length ? labels[at] + ", " + values[at] : "";
                }
                case PICKER: {
                    int at = pickScroll + row;
                    if (at >= pickLabels.size()) return "";
                    return pickLabels.get(at) + (at == pickCurrent ? Ui.tr(", selected") : "")
                            + ", " + Ui.trf("{i} of {n}", "i", at + 1, "n", pickLabels.size());
                }
                case SHELF_EDIT: {
                    int at = pickScroll + row;
                    if (at >= shelfRows.size()) return "";
                    String[] app = shelfRows.get(at);
                    int place = shelfOrder.indexOf(app[0]);
                    if (editingTop) {
                        return app[1] + ", " + (place >= 0 ? Ui.trf("in the top buttons, place {n}", "n", place + 1)
                                : Ui.tr("not in the top buttons"));
                    }
                    return app[1] + ", " + (place >= 0 ? Ui.trf("on the shelf, place {n}", "n", place + 1)
                            : Ui.tr("not on the shelf"));
                }
                default:
                    return "";
            }
        }

        private RectF rowBox(int row) {
            float top = LIST_TOP + 20 + row * (LIST_ROW_H + LIST_ROW_GAP);
            focusBox.set(LIST_X * k, top * k, LIST_R * k, (top + LIST_ROW_H) * k);
            return focusBox;
        }

        /** A menu item on the left: its row, full width of the rail's panel. */
        private RectF railRowBox(int rail) {
            float top = RAIL_ROW_TOP + rail * (RAIL_ROW_H + RAIL_ROW_GAP);
            railBox.set(RAIL_ROW_X * k, top * k, RAIL_ROW_R * k, (top + RAIL_ROW_H) * k);
            return railBox;
        }

        void resume() {
            if (running) return;
            running = true;
            whyRows = null;
            night = HomeData.get(getContext()).isNight(System.currentTimeMillis());
            lastNanos = 0;
            handler.post(poll);
            Choreographer.getInstance().postFrameCallback(this);
            spoken.readAgain();
            loadBackdrop();
            Photos.importInbox(getContext(), this::invalidate);   // photos a computer just sent
        }

        void pause() {
            running = false;
            handler.removeCallbacks(poll);
            Choreographer.getInstance().removeFrameCallback(this);
        }

        @Override public void doFrame(long frameTimeNanos) {
            if (!running) return;
            boolean moving = busy();
            for (Spring s : pop) moving |= !s.settled();
            for (Spring s : railPop) moving |= !s.settled();
            if (moving) invalidate();
            Choreographer.getInstance().postFrameCallback(this);
        }

        @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            w = width;
            h = height;
            k = w / 1920f;
            scene.setSize(w, h);
            screenRect.set(0, 0, w, h);
            railRect.set(RAIL_X * k, PANEL_TOP * k, RAIL_R * k, PANEL_BOTTOM * k);
            paneRect.set(PANE_X * k, PANEL_TOP * k, PANE_R * k, PANEL_BOTTOM * k);
            Symbols.warm(getContext(), "chevron_right", 22 * k);
            Symbols.warm(getContext(), "check", 26 * k);
            title = Ui.text(Ui.displaySemibold, 36 * k, 0xFFFFFFFF);
            subtitle = Ui.text(Ui.regular, 23 * k, 0x99FFFFFF);
            subtitle.setTextAlign(Paint.Align.LEFT);
            version = Ui.text(Ui.regular, 20 * k, 0x66FFFFFF);
            version.setTextAlign(Paint.Align.RIGHT);
            rowLabel = Ui.text(Ui.medium, 29 * k, 0xF2FFFFFF);
            rowValue = Ui.text(Ui.regular, 27 * k, 0x8CFFFFFF);
            rowValue.setTextAlign(Paint.Align.RIGHT);
            rowSub = Ui.text(Ui.regular, 23 * k, 0x8CFFFFFF);
            railLabel = Ui.text(Ui.medium, 28 * k, 0xD9FFFFFF);
            brand = Ui.text(Ui.displaySemibold, 28 * k, 0xF2FFFFFF);
            aboutName = Ui.text(Ui.displaySemibold, 46 * k, 0xFFFFFFFF);
            whyPaint.setTextSize(23 * k);
            whyPaint.setAntiAlias(true);
            aboutPaint.setTextSize(23 * k);
            aboutPaint.setAntiAlias(true);
            String about = Ui.tr("Climin UI is a home screen for Android TV: no ads, your own photos, the weather, and a screensaver.");
            aboutLayout = StaticLayout.Builder.obtain(about, 0, about.length(), aboutPaint, Math.round(1020 * k))
                    .setLineSpacing(0, 1.2f).build();
            white = new Paint(Paint.ANTI_ALIAS_FLAG);
            white.setColor(0xFFF5F5F7);
            shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadow.setMaskFilter(new BlurMaskFilter(22 * k, BlurMaskFilter.Blur.NORMAL));
        }

        private boolean busy() {
            return !busyText().isEmpty();
        }

        private String busyText() {
            long at = state.optLong("busyAt"), now = SystemClock.elapsedRealtime();
            boolean stale = now < at || now - at > 10 * 60_000L;
            return stale ? "" : state.optString("busy");
        }

        private boolean freeActive() {
            return HomeMode.FREE.equals(state.optString("mode")) && !state.optBoolean("homeReverted");
        }

        private int rowCount() {
            switch (screen) {
                case GREETINGS: return 0;
                case HOME_SETTINGS: return Math.min(SETTINGS_VISIBLE, settingLabels().length);
                case PICKER: return Math.min(PICK_VISIBLE, pickLabels.size());
                case SHELF_EDIT: return Math.min(PICK_VISIBLE, shelfRows.size());
                case POWER: return powerRows().length;
                case SCREENSAVER: return 1;
                case ABOUT: return 0;
                case THEME: return 2;
                default: return Prefs.KEYS.length;
            }
        }

        @Override protected void onDraw(Canvas c) {
            if (w == 0) return;
            applyTone();
            if (glassFor != (night ? 1 : 0)) applyGlass();
            long now = System.nanoTime();
            float dt = lastNanos == 0 ? 1 / 60f : Math.min(0.05f, (now - lastNanos) / 1e9f);
            lastNanos = now;
            for (int i = 0; i < pop.length; i++) {
                pop[i].target = zone == Z_PANE && i == focus && i < rowCount() ? 1 : 0;
                pop[i].step(dt);
            }
            for (int i = 0; i < railPop.length; i++) {
                railPop[i].target = zone == Z_RAIL && i == railFocus ? 1 : 0;
                railPop[i].step(dt);
            }
            drawBackdrop(c);
            drawPanels(c);
            drawRail(c);

            String why = railUnavailable(railFocus);
            if (why != null) drawUnavailable(c, why);
            else if (screen == GREETINGS) drawGreetings(c);
            else if (screen == ABOUT) drawAbout(c);
            else if (screen == THEME) drawTheme(c);
            else if (screen == PICKER) drawPicker(c);
            else if (screen == SHELF_EDIT) drawShelfEditor(c);
            else if (screen == SCREENSAVER) drawActionPane(c);
            else drawList(c);
        }

        /** The home screen's photo behind the glass (softly blurred), or its light/night gradient without one. */
        private void drawBackdrop(Canvas c) {
            if (backdrop != null) {
                c.drawBitmap(backdrop, null, screenRect, photoPaint);
                photoPaint.setColor(night ? 0x9E000000 : 0x66FFFFFF);
                c.drawRect(screenRect, photoPaint);
                photoPaint.setColor(0xFFFFFFFF);
                return;
            }
            if (sky == null) sky = Ui.gradient(h, night ? 0xFF1C1C1E : 0xFFF5F5F7, night ? 0xFF0B0B0D : 0xFFE2E2E7);
            fill.setShader(sky);
            c.drawRect(screenRect, fill);
            fill.setShader(null);
        }

        /** The two glass panels: the menu on the left, what it holds on the right. */
        private void drawPanels(Canvas c) {
            float radius = PANEL_RADIUS * k;
            shadow.setColor(col(pal.panelShadow));
            tmp.set(railRect);
            tmp.offset(0, 16 * k);
            c.drawRoundRect(tmp, radius, radius, shadow);
            tmp.set(paneRect);
            tmp.offset(0, 16 * k);
            c.drawRoundRect(tmp, radius, radius, shadow);
            Surfaces.panel(c, scene, railRect, radius, col(pal.surface), tone(0.24f, 0.12f), tone(0.03f, 0.08f),
                    tone(26, 30) * k, tone(20, 26) * k, tone(0.6f, 0.9f), 1, night ? 1 : 0);
            Surfaces.panel(c, scene, paneRect, radius, col(pal.surface), tone(0.20f, 0.10f), tone(0.03f, 0.08f),
                    tone(26, 30) * k, tone(20, 26) * k, tone(0.6f, 0.9f), 1, night ? 1 : 0);
        }

        /** The menu: the wordmark and version, then one row per screen. */
        private void drawRail(Canvas c) {
            float x = RAIL_ROW_X * k + 24 * k;
            c.drawText("Climin UI", x, 178 * k, brand);
            String number = versionNumber(getContext());
            c.drawText(number.isEmpty() ? "" : "v" + number, RAIL_ROW_R * k - 24 * k, 178 * k, version);
            fill.setColor(col(pal.rowFill));
            c.drawRect(x, 202 * k, RAIL_ROW_R * k - 24 * k, 203 * k, fill);
            String[] labels = railLabels();
            for (int i = 0; i < RAILS; i++) {
                RectF r = railRowBox(i);
                boolean chosen = i == railFocus;
                float f = Ui.clamp(railPop[i].value);
                if (chosen && f > 0.01f) {
                    float s = 1 + 0.02f * f, rr = 16 * k * s;
                    tmp.set(r.centerX() - r.width() * s / 2, r.centerY() - r.height() * s / 2,
                            r.centerX() + r.width() * s / 2, r.centerY() + r.height() * s / 2);
                    tmp.offset(0, 8 * k * f);
                    shadow.setColor(Ui.alpha(0xFF000000, tone(0.28f, 0.34f) * f));
                    c.drawRoundRect(tmp, rr, rr, shadow);
                    tmp.offset(0, -8 * k * f);
                    white.setAlpha(Math.round(255 * f));
                    c.drawRoundRect(tmp, rr, rr, white);
                    r = tmp;
                } else if (chosen) {
                    fill.setColor(col(pal.rowFillAlt));
                    c.drawRoundRect(r, 16 * k, 16 * k, fill);
                }
                boolean off = railUnavailable(i) != null;
                railLabel.setColor(off ? inkFade(0x73, f) : blend(inkBase(0xD9), INK, f));
                c.drawText(labels[i], x, r.centerY() + Ui.halfCap(railLabel), railLabel);
            }
        }

        /**
         * The photo behind the glass: the home screen's current backdrop, read from the launcher's image cache and
         * softened the way its shelf glass is. Without one, the panel's own gradient shows instead.
         */
        private void loadBackdrop() {
            if (backdropTried) return;
            backdropTried = true;
            new Thread(() -> {
                Bitmap b = homeBackdrop(getContext());
                if (b == null) return;
                // The panels' glass refracts the same photo, at the size the home screen's shelf glass reads.
                Bitmap small = Bitmap.createScaledBitmap(b, Backdrop.GW, Backdrop.GH, true);
                int[] pixels = new int[Backdrop.GW * Backdrop.GH];
                small.getPixels(pixels, 0, Backdrop.GW, 0, 0, Backdrop.GW, Backdrop.GH);
                small.recycle();
                handler.post(() -> {
                    backdrop = b;
                    glassDay = whiten(pixels.clone(), 0.55f);
                    glassNight = darken(pixels, 0.56f);
                    glassFor = -1;   // the glass swaps with the theme
                    invalidate();
                });
            }, "settings-backdrop").start();
        }

        private static Bitmap homeBackdrop(Context c) {
            File[] files = new File(c.getCacheDir(), "recs").listFiles((d, name) -> !name.endsWith(".part"));
            if (files == null || files.length == 0) return null;
            File newest = null;
            for (File f : files) if (newest == null || f.lastModified() > newest.lastModified()) newest = f;
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(newest.getPath(), o);
            if (o.outWidth <= 0 || o.outHeight <= 0) return null;
            int sample = 1;
            while (o.outWidth / (sample * 2) >= 720) sample *= 2;
            o.inJustDecodeBounds = false;
            o.inSampleSize = sample;
            Bitmap src = BitmapFactory.decodeFile(newest.getPath(), o);
            if (src == null) return null;
            int width = src.getWidth(), height = Math.max(1, Math.round(width * 9f / 16f));
            Bitmap crop = height < src.getHeight()
                    ? Bitmap.createBitmap(src, 0, (src.getHeight() - height) / 2, width, height) : src;
            return Ui.downscale(crop, Backdrop.FW, Backdrop.FH);
        }

        /** A menu item's name, as on the left and as TalkBack reads it. */
        private String[] railLabels() {
            return new String[] {Ui.tr("Home Settings"), Ui.tr("Greetings"), Ui.tr("Theme"), Ui.tr("Screensaver"),
                    Ui.tr("Shelf"), Ui.tr("Top Buttons"), Ui.tr("Remote Buttons"), Ui.tr("Sleep Timer"),
                    Ui.tr("About")};
        }

        private void drawPaneTitle(Canvas c, String text) {
            c.drawText(text, LIST_X * k + 30 * k, PANE_HEAD * k, title);
        }

        /** What the panels' glass shows: the photo lightened (by day) or darkened (at night). */
        private void applyGlass() {
            int wanted = night ? 1 : 0;
            glassFor = wanted;
            if (glassDay != null) {
                scene.setGlassImage(night ? glassNight : glassDay);
                return;
            }
            // No photo: the glass reads the background's own gray, not Scene's aurora (which isn't on screen).
            int[] neutral = new int[Backdrop.GW * Backdrop.GH];
            java.util.Arrays.fill(neutral, col(pal.neutral));
            scene.setGlassImage(neutral);
        }

        private static int[] whiten(int[] px, float amount) {
            float keep = 1 - amount, base = 255 * amount;
            for (int i = 0; i < px.length; i++) {
                int p = px[i];
                px[i] = 0xFF000000 | (Math.round(((p >> 16) & 255) * keep + base) << 16)
                        | (Math.round(((p >> 8) & 255) * keep + base) << 8) | Math.round((p & 255) * keep + base);
            }
            return px;
        }

        private static int[] darken(int[] px, float amount) {
            float keep = 1 - amount, base = 11 * amount;
            for (int i = 0; i < px.length; i++) {
                int p = px[i];
                px[i] = 0xFF000000 | (Math.round(((p >> 16) & 255) * keep + base) << 16)
                        | (Math.round(((p >> 8) & 255) * keep + base) << 8) | Math.round((p & 255) * keep + base + 2 * amount);
            }
            return px;
        }

        /** A menu item this TV can't use (Remote Buttons, Sleep Timer): the panel says why, instead of rows. */
        private void drawUnavailable(Canvas c, String why) {
            drawPaneTitle(c, railLabels()[railFocus]);
            if (whyLayout == null || whyReason == null || !whyReason.equals(why)) {
                whyReason = why;
                whyLayout = StaticLayout.Builder.obtain(why, 0, why.length(), whyPaint, Math.round(1080 * k))
                        .setMaxLines(3).setEllipsize(TextUtils.TruncateAt.END).build();
            }
            c.save();
            c.translate(LIST_X * k + 30 * k, (LIST_TOP + 20) * k);
            whyLayout.draw(c);
            c.restore();
        }

        /** Greetings: not built yet; the empty panel says so instead of showing nothing. */
        private void drawGreetings(Canvas c) {
            drawPaneTitle(c, Ui.tr("Greetings"));
            c.drawText(Ui.tr("Coming soon."), LIST_X * k + 30 * k, (LIST_TOP + 64) * k, subtitle);
        }

        /** Theme: the light/night mode (moved out of Home Settings) and the look of the whole launcher. */
        private void drawTheme(Canvas c) {
            drawPaneTitle(c, Ui.tr("Theme"));
            String[] labels = {Ui.tr("Mode"), Ui.tr("Style")};
            String[] values = {modeName(), look.name};
            for (int i = 0; i < labels.length; i++) {
                float y = (LIST_TOP + 20 + i * (LIST_ROW_H + LIST_ROW_GAP)) * k;
                rowRect.set(LIST_X * k, y, LIST_R * k, y + LIST_ROW_H * k);
                float f = drawRowBackground(c, i);
                rowLabel.setColor(blend(inkBase(0xF2), INK, f));
                rowValue.setColor(inkValue(f));
                float base = rowRect.centerY() + Ui.halfCap(rowLabel);
                c.drawText(labels[i], rowRect.left + 30 * k, base, rowLabel);
                c.drawText(values[i], rowRect.right - 30 * k, base, rowValue);
            }
            c.drawText(focus == 0 ? Ui.tr("OK or the arrows change the value.")
                    : Ui.tr("Applies to the whole launcher."), LIST_X * k + 30 * k, (PANEL_BOTTOM - 60) * k, subtitle);
        }

        /** Screensaver: one action, with a chevron. */
        private void drawActionPane(Canvas c) {
            drawPaneTitle(c, Ui.tr("Screensaver"));
            float y = (LIST_TOP + 20) * k;
            rowRect.set(LIST_X * k, y, LIST_R * k, y + LIST_ROW_H * k);
            float f = drawRowBackground(c, 0);
            rowLabel.setColor(blend(inkBase(0xF2), INK, f));
            c.drawText(Ui.tr("Preview"), rowRect.left + 30 * k, rowRect.centerY() + Ui.halfCap(rowLabel), rowLabel);
            float cw = Symbols.width(getContext(), "chevron_right", 22 * k);
            Symbols.draw(getContext(), c, "chevron_right", rowRect.right - 30 * k - cw / 2, rowRect.centerY(), 22 * k,
                    blend(inkBase(0x66), inkBase(0x99), f));
        }

        /** List row: no background; focused, white with a shadow (the Apple TV focus). Returns how focused it is. */
        private float drawRowBackground(Canvas c, int i) {
            float f = Ui.clamp(pop[i].value);
            if (f > 0.01f) {
                float s = 1 + 0.02f * f;
                float cx = rowRect.centerX(), cy = rowRect.centerY();
                tmp.set(cx - rowRect.width() * s / 2, cy - rowRect.height() * s / 2, cx + rowRect.width() * s / 2, cy + rowRect.height() * s / 2);
                float r = 18 * k * s;
                tmp.offset(0, 10 * k * f);
                shadow.setColor(Ui.alpha(0xFF000000, 0.4f * f));
                c.drawRoundRect(tmp, r, r, shadow);
                tmp.offset(0, -10 * k * f);
                white.setAlpha(Math.round(255 * f));
                c.drawRoundRect(tmp, r, r, white);
                rowRect.set(tmp);
            }
            return f;
        }

        /** About: what Climin UI is, and the version, with no rows to act on. */
        private void drawAbout(Canvas c) {
            drawPaneTitle(c, Ui.tr("About"));
            float x = LIST_X * k + 30 * k;
            c.drawText("Climin UI", x, (LIST_TOP + 56) * k, aboutName);
            c.drawText(Ui.trf("Version {version}", "version", versionNumber(getContext())), x,
                    (LIST_TOP + 104) * k, subtitle);
            fill.setColor(col(pal.rowFill));
            c.drawRect(x, (LIST_TOP + 132) * k, LIST_R * k - 30 * k, (LIST_TOP + 133) * k, fill);
            c.save();
            c.translate(x, (LIST_TOP + 168) * k);
            aboutLayout.draw(c);
            c.restore();
        }

        private static int blend(int a, int b, float f) {
            int aa = a >>> 24, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
            int ba = b >>> 24, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
            return (Math.round(aa + (ba - aa) * f) << 24) | (Math.round(ar + (br - ar) * f) << 16)
                    | (Math.round(ag + (bg - ag) * f) << 8) | Math.round(ab + (bb - ab) * f);
        }

        /** Just the number, next to the wordmark. */
        private static String versionNumber(Context c) {
            try {
                return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                return "";
            }
        }

        /** A palette slot at the panels' day or night value (they swap, they do not blend). */
        private int col(Palette.Pair c) { return c.at(night ? 1 : 0); }

        private float tone(float day, float dark) { return night ? dark : day; }

        private int tone(int day, int dark) { return night ? dark : day; }

        /** A text tint at `alpha` of the ink the panels need: black by day, white at night (the focused row is always ink). */
        private int inkBase(int alpha) { return (alpha << 24) | col(pal.inkBase); }

        /** A faded label: white on the smoked glass at night, ink on the light glass by day; ink once the row is focused. */
        private int inkFade(int alpha, float f) { return blend(inkBase(alpha), (alpha << 24) | 0x1C1C1E, f); }

        /** A row's value, right-aligned: dimmer than its label, and ink once the row is focused. */
        private int inkValue(float f) { return inkFade(0xB3, f); }

        /** The wordmark, headings, values and hints follow the panels' light (day) or smoked (night) glass. */
        private void applyTone() {
            title.setColor(inkBase(0xFF));
            subtitle.setColor(inkBase(0xA6));
            version.setColor(inkBase(0x73));
            brand.setColor(inkBase(0xFF));
            aboutName.setColor(inkBase(0xFF));
            aboutPaint.setColor(inkBase(0xA6));
            rowLabel.setColor(inkBase(0xF2));
            rowValue.setColor(inkBase(0xB3));
            rowSub.setColor(inkBase(0x8C));
            railLabel.setColor(inkBase(0xD9));
            whyPaint.setColor(inkBase(0xA6));
            white.setColor(col(pal.white));
        }

        // MARK: - The menu on the left

        /** What the menu's item needs from the TV (Capabilities.NEED_…), or null. */
        private String railNeed(int rail) {
            return rail == RAIL_REMOTE ? Capabilities.NEED_REMOTE_BUTTONS : rail == RAIL_POWER ? Capabilities.NEED_ADB : null;
        }

        private String railUnavailable(int rail) {
            String need = railNeed(rail);
            return need == null ? null : Capabilities.missing(getContext(), need);
        }

        /** The panel a menu item shows. */
        private int paneFor(int rail) {
            switch (rail) {
                case RAIL_GREETINGS: return GREETINGS;
                case RAIL_THEME: return THEME;
                case RAIL_SCREENSAVER: return SCREENSAVER;
                case RAIL_SHELF:
                case RAIL_TOP: return SHELF_EDIT;
                case RAIL_REMOTE: return REMOTE_KEYS;
                case RAIL_POWER: return POWER;
                case RAIL_ABOUT: return ABOUT;
                default: return HOME_SETTINGS;
            }
        }

        /**
         * The menu moved to `rail`: the panel on the right now shows what it holds. The shelf editors and the remote
         * buttons ask the TV for their rows, so they are built only when the menu enters them.
         */
        private void selectRail(int rail) {
            railFocus = Math.max(0, Math.min(rail, RAILS - 1));
            focus = 0;
            pickScroll = 0;
            whyRows = null;
            if (railFocus == RAIL_SHELF) {
                if (screen != SHELF_EDIT || editingTop) openShelfEditor(false);
            } else if (railFocus == RAIL_TOP) {
                if (screen != SHELF_EDIT || !editingTop) openShelfEditor(true);
            } else if (railFocus == RAIL_REMOTE) {
                if (screen != REMOTE_KEYS) {
                    HomeMode.ensureRemoteService(getContext(), 0);   // the buttons work from other apps through this
                    loadApps();
                }
                screen = REMOTE_KEYS;
            } else {
                screen = paneFor(railFocus);
            }
            for (int i = 0; i < pop.length; i++) pop[i].snap(0);
            invalidate();
        }

        /** Down/right/OK on the menu: the panel takes the focus (its first row). */
        private void enterPane() {
            String why = railUnavailable(railFocus);
            if (why != null) {
                if (whyToast != null) whyToast.cancel();
                whyToast = Toast.makeText(getContext(), why, Toast.LENGTH_LONG);
                whyToast.show();
                return;
            }
            if (rowCount() <= 0) return;
            zone = Z_PANE;
            focus = Math.min(focus, rowCount() - 1);
            invalidate();
            spoken.focusMoved();
        }

        /** Back in the panel: the menu takes the focus again. */
        private void leavePane() {
            zone = Z_RAIL;
            invalidate();
            spoken.focusMoved();
        }

        boolean onKey(KeyEvent e) {
            boolean down = e.getAction() == KeyEvent.ACTION_DOWN;
            switch (e.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (down) {
                        if (zone == Z_RAIL) {
                            if (railFocus > 0) selectRail(railFocus - 1);
                        }
                        else if (focus > 0) focus--;
                        else if (scrolling() && pickScroll > 0) pickScroll--;
                    }
                    invalidate();
                    spoken.focusMoved();
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (down) {
                        if (zone == Z_RAIL) {
                            if (railFocus < RAILS - 1) selectRail(railFocus + 1);
                        }
                        else if (focus < rowCount() - 1) focus++;
                        else if (scrolling() && pickScroll + rowCount() < listSize()) pickScroll++;
                    }
                    invalidate();
                    spoken.focusMoved();
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    boolean right = e.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT;
                    if (down && zone == Z_RAIL) {
                        if (right) enterPane();
                    } else if (down && screen == SHELF_EDIT) {
                        moveShelfApp(right ? 1 : -1);
                    } else if (down && (screen == HOME_SETTINGS || screen == REMOTE_KEYS || screen == POWER
                            || screen == THEME)) {
                        adjust(right ? 1 : -1);
                    } else if (down && !right) {
                        leavePane();   // panels with nothing to step back through: ← returns to the menu
                    }
                    whyRows = null;   // a value or the rows' order changed: what each row needs may have too
                    invalidate();
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (down) {
                        if (e.getRepeatCount() == 0) okDown = true;
                    } else {
                        if (okDown && !e.isCanceled() && spoken.keyClick()) {
                            activate();
                            whyRows = null;   // the same after OK
                        }
                        okDown = false;
                    }
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    if (zone == Z_RAIL && screen != PICKER) return false;   // leaves the settings
                    if (down) {
                        if (e.getRepeatCount() == 0) backDown = true;
                    } else {
                        if (backDown && !e.isCanceled()) {
                            if (screen == PICKER && pickFor <= PICK_SLEEP) {
                                focus = powerFocus(pickFor == PICK_SLEEP ? P_SLEEP : P_WHERE);
                                pickScroll = 0;   // the picker's scroll isn't this short list's
                                screen = POWER;
                            } else if (screen == PICKER && (pickFor == PICK_CITY || pickFor == PICK_LANGUAGE)) {
                                focus = pickFor == PICK_CITY ? CITY_ROW : 0;
                                pickScroll = 0;   // both rows are on the list's first screen
                                screen = HOME_SETTINGS;
                            } else if (screen == PICKER) {
                                focus = pickFor - 1;
                                screen = REMOTE_KEYS;
                            } else {
                                zone = Z_RAIL;   // the menu
                            }
                        }
                        backDown = false;
                    }
                    invalidate();
                    spoken.focusMoved();
                    return true;
                default:
                    return false;
            }
        }

        // MARK: - Home Settings and Remote Buttons

        private static final String[] UNITS = {"auto", "C", "F"}, THEMES = {"auto", "light", "dark"};
        /** Home Settings: language, temperature, my photos, sounds, weather, city, forecast, quotes, then one row per coin. */
        private static final int PHOTOS_ROW = 2, CITY_ROW = 5, FORECAST_ROW = 6, QUOTES_ROW = 7, COIN_ROWS = 8;
        /** Home Settings rows on screen at once (what fits above the hints at 1080p); a longer list scrolls. */
        private static final int SETTINGS_VISIBLE = 8;

        private boolean dollar() {
            return "BRL".equals(Place.currency());
        }

        private String[] settingLabels() {
            List<String> l = new ArrayList<>();
            l.add(Ui.tr("Language"));
            l.add(Ui.tr("Temperature"));
            l.add(Ui.tr("My Photos"));
            l.add(Ui.tr("Navigation sounds"));
            l.add(Ui.tr("Weather widget"));
            l.add(Ui.tr("City"));
            l.add(Ui.tr("Forecast"));
            l.add(Ui.tr("Quotes widget"));
            l.add("Bitcoin");
            l.add("Ethereum");
            l.add("Solana");
            if (dollar()) l.add(Ui.tr("Dollar"));
            return l.toArray(new String[0]);
        }

        private String[] settingValues() {
            String[] labels = settingLabels();
            String[] v = new String[labels.length];
            String lang = Prefs.language();
            v[0] = lang.equals("auto") ? Ui.tr("Automatic") : Ui.languageName(lang);
            String unit = Prefs.temperature();
            v[1] = unit.equals("C") ? "°C" : unit.equals("F") ? "°F" : Ui.tr("unit|Automatic");
            int photos = Photos.files(getContext()).size();
            v[PHOTOS_ROW] = photos == 0 ? Ui.tr("photos|None")
                    : Prefs.photos() ? Ui.plural("plural|one:{n} photo|other:{n} photos", photos) : Ui.tr("Off");
            v[3] = onOff(Prefs.sounds());
            v[4] = onOff(Prefs.weather());
            v[CITY_ROW] = !cityStatus.isEmpty() ? cityStatus : Prefs.city().isEmpty() ? "—" : Prefs.city();
            v[FORECAST_ROW] = Prefs.forecast().equals("days") ? Ui.tr("Next days") : Ui.tr("Next hours");
            v[QUOTES_ROW] = onOff(Prefs.quotes());
            // No quotes widget, nowhere to show prices: the coins read as off (and stay locked) until it is back on.
            for (int i = COIN_ROWS; i < v.length; i++) v[i] = onOff(Prefs.quotes() && Prefs.coin(i - COIN_ROWS));
            return v;
        }

        /** The light/night mode, as the Theme pane and TalkBack show it. */
        private String modeName() {
            String theme = Prefs.theme();
            return theme.equals("light") ? Ui.tr("Light") : theme.equals("dark") ? Ui.tr("Dark") : Ui.tr("Automatic");
        }

        /** "auto", then English and every language with a catalogue. */
        private static String[] languageChoices() {
            String[] all = Ui.languages();
            String[] v = new String[all.length + 1];
            v[0] = "auto";
            System.arraycopy(all, 0, v, 1, all.length);
            return v;
        }

        /**
         * What the focused screen's row `i` needs from the TV (Capabilities.NEED_…), or null. A new option that needs
         * something adds its line here; drawing, keys and TalkBack then treat it as unavailable where the TV lacks it.
         */
        private String need(int i) {
            switch (screen) {
                case POWER: {
                    int id = powerRows()[i];
                    return id == P_WHERE ? Capabilities.NEED_OVERLAY : null;
                }
                case SHELF_EDIT: {   // only to add it: one already among the buttons can still move or go
                    String key = shelfRows.get(pickScroll + i)[0];
                    return editingTop && HomeApps.TOP_LIVE.equals(key) && !shelfOrder.contains(key) ? Capabilities.NEED_LIVE_TV : null;
                }
                default:
                    return null;
            }
        }

        /** Why row `i` can't be used here, for the person to read; null where it works. */
        private String unavailable(int i) {
            int n = rowCount();
            if (i < 0 || i >= n) return null;
            if (whyRows == null || whyRows.length != n || whyScreen != screen || whyScroll != pickScroll) {
                whyRows = new String[n];
                whyScreen = screen;
                whyScroll = pickScroll;
                for (int r = 0; r < n; r++) {
                    String need = need(r);
                    whyRows[r] = screen == HOME_SETTINGS && pickScroll + r >= COIN_ROWS && !Prefs.quotes()
                            ? Ui.tr("Turn on the Quotes widget to pick its coins.")
                            : need == null ? null : Capabilities.missing(getContext(), need);
                }
            }
            return whyRows[i];
        }

        /** OK on an unavailable row: a toast says why; false if it isn't unavailable. */
        private boolean tellWhy() {
            String why = unavailable(focus);
            if (whyToast != null) whyToast.cancel();
            whyToast = why == null ? null : Toast.makeText(getContext(), why, Toast.LENGTH_LONG);
            if (whyToast != null) whyToast.show();
            return why != null;
        }

        /**
         * Sleep Timer's rows, by what they set: the timer, whether it shows on screen and (where it shows over other
         * apps) where. Values are picked from lists (OK), as on the TV's own settings; ← → step them.
         */
        private static final int P_SLEEP = 0, P_SHOW = 1, P_WHERE = 2;

        private int[] powerRows() {
            return Prefs.sleepShown() ? new int[] {P_SLEEP, P_SHOW, P_WHERE} : new int[] {P_SLEEP, P_SHOW};
        }

        private int powerRow() {
            int[] rows = powerRows();
            return rows[Math.min(focus, rows.length - 1)];
        }

        private int powerFocus(int id) {
            int[] rows = powerRows();
            for (int i = 0; i < rows.length; i++) if (rows[i] == id) return i;
            return 0;
        }

        private String[] powerLabels() {
            int[] rows = powerRows();
            String[] out = new String[rows.length];
            for (int i = 0; i < rows.length; i++) {
                switch (rows[i]) {
                    case P_SLEEP: out[i] = Ui.tr("Sleep Timer"); break;
                    case P_SHOW: out[i] = Ui.tr("Show Time Left"); break;
                    default: out[i] = Ui.tr("Position");
                }
            }
            return out;
        }

        private String[] powerValues() {
            int[] rows = powerRows();
            String[] out = new String[rows.length];
            for (int i = 0; i < rows.length; i++) {
                switch (rows[i]) {
                    case P_SLEEP: out[i] = sleepLeft == 0 ? Ui.tr("Off") : Ui.trf("{time} left", "time", SleepTimer.duration(sleepLeft)); break;
                    case P_SHOW: out[i] = onOff(Prefs.sleepShown()); break;
                    default: out[i] = SleepOverlay.placeName(Prefs.sleepPlace());
                }
            }
            return out;
        }

        /** OK on a Sleep Timer row: its list of choices, or (on a switch) the other value. */
        private void activatePower() {
            Context c = getContext();
            List<String[]> list = new ArrayList<>();
            switch (powerRow()) {
                case P_SLEEP:
                    list.add(new String[] {"0", Ui.tr("Off")});
                    for (int m : new int[] {5, 10, 15, 20, 30, 45, 60, 90, 120, 180, 240}) {
                        list.add(new String[] {String.valueOf(m), SleepTimer.duration(m)});
                    }
                    int left = SleepTimer.minutesLeft(c);
                    openChoices(PICK_SLEEP, list, String.valueOf(left));
                    return;
                case P_WHERE:
                    for (int i = 0; i < SleepOverlay.PLACES; i++) list.add(new String[] {String.valueOf(i), SleepOverlay.placeName(i)});
                    openChoices(PICK_PLACE, list, String.valueOf(Prefs.sleepPlace()));
                    return;
                default:
                    adjust(1);
            }
        }

        private void openChoices(int which, List<String[]> list, String now) {
            choices.clear();
            choices.addAll(list);
            choiceNow = now;
            openPicker(which);
        }

        /** OK in a Sleep Timer picker: applied, and back to its row. */
        private void pickPower() {
            Context c = getContext();
            String[] choice = choices.get(pickScroll + focus);
            int row = pickFor == PICK_SLEEP ? P_SLEEP : P_WHERE;
            if (pickFor == PICK_SLEEP) {
                SleepTimer.set(c, Integer.parseInt(choice[0]));
                sleepLeft = SleepTimer.minutesLeft(c);
            } else {
                Prefs.setSleepPlace(Integer.parseInt(choice[0]));
                SleepOverlay.update(c);
            }
            pickScroll = 0;
            screen = POWER;
            focus = powerFocus(row);
        }

        private String[] listLabels() {
            return screen == REMOTE_KEYS ? keyLabels() : screen == POWER ? powerLabels() : settingLabels();
        }

        private String[] listValues() {
            return screen == REMOTE_KEYS ? keyValues() : screen == POWER ? powerValues() : settingValues();
        }

        private static String onOff(boolean on) {
            return on ? Ui.tr("On") : Ui.tr("Off");
        }

        private String[] keyLabels() {
            if (RemoteKeys.HISENSE) return new String[] {Ui.tr("Prime Video button"), Ui.tr("YouTube button"),
                    Ui.tr("Disney+ button"), Ui.tr("All Apps button"), Ui.tr("Settings button"), Ui.tr("Profile button")};
            if (RemoteKeys.STEM_NETFLIX) return new String[] {Ui.tr("Netflix button")};
            if (RemoteKeys.KTC) return new String[] {Ui.tr("Tabii button")};
            if (RemoteKeys.XIAOMI) return new String[] {Ui.tr("Mi button"), Ui.tr("Okko button"), Ui.tr("VK button"),
                    Ui.tr("Rutube button"), Ui.tr("KION button"), Ui.tr("Wink button"), Ui.tr("IVI button")};
            if (RemoteKeys.SONY) return new String[] {Ui.tr("BRAVIA CORE button"),
                    Ui.tr("Netflix button"), Ui.tr("Disney+ button"),
                    Ui.tr("Prime Video button"), Ui.tr("YouTube button"),
                    Ui.tr("Crunchyroll button"), Ui.tr("Google Play button")};
            return new String[] {Ui.tr("Netflix button"), Ui.tr("YouTube button"),
                    Ui.tr("Prime Video button"), Ui.tr("TCL Channel button")};
        }

        private String[] keyValues() {
            String[] v = new String[Prefs.KEYS.length];
            for (int i = 0; i < v.length; i++) {
                int at = appPkgs.indexOf(Prefs.keyApp(Prefs.KEYS[i]));
                v[i] = at >= 0 ? appLabels.get(at) : Ui.tr("Not installed");
            }
            return v;
        }

        /** Installed TV apps, by name, after "Nothing": what a remote button can open. */
        private void loadApps() {
            List<String[]> found = new ArrayList<>();
            for (String[] app : HomeApps.launchable(getContext())) {
                if (!app[0].equals(getContext().getPackageName())) found.add(app);
            }
            Collections.sort(found, (a, b) -> a[1].toLowerCase(Locale.ROOT).compareTo(b[1].toLowerCase(Locale.ROOT)));
            appPkgs.clear();
            appLabels.clear();
            appPkgs.add("");
            // On Sony and Xiaomi, "nothing" leaves the key to the TV, which opens its usual app.
            appLabels.add(RemoteKeys.SONY ? Ui.tr("Sony default") : RemoteKeys.XIAOMI ? Ui.tr("Xiaomi default") : Ui.tr("Nothing"));
            // On Xiaomi the Mi button is the remote's Home button: it can open this home screen.
            if (RemoteKeys.XIAOMI) {
                appPkgs.add(HomeMode.PKG);
                appLabels.add("Climin UI");
            }
            if (RemoteKeys.HISENSE) {
                appPkgs.add(Prefs.ALL_APPS);
                appLabels.add(Ui.tr("All Apps"));
            }
            for (String[] f : found) {
                appPkgs.add(f[0]);
                appLabels.add(f[1]);
            }
        }

        /** Changes the focused row's value one step (OK and → go forward, ← goes back). */
        private void adjust(int dir) {
            if (unavailable(focus) != null) return;
            if (screen == THEME) {
                if (focus == 0) {
                    int at = java.util.Arrays.asList(THEMES).indexOf(Prefs.theme());
                    Prefs.setTheme(THEMES[(at + dir + THEMES.length) % THEMES.length]);
                    SleepOverlay.update(getContext());   // the pill's glass follows
                    HomeData.get(getContext()).settingsChanged();
                } else if (Look.ALL.length > 1) {
                    Look next = Look.ALL[(Look.indexOf(look) + dir + Look.ALL.length) % Look.ALL.length];
                    Prefs.setLook(next.key);
                    look = next;
                    pal = look.palette;
                    applyTone();
                    glassFor = -1;      // the panels' glass follows the look
                    applyGlass();
                }
                return;
            }
            if (screen == POWER) {
                int row = powerRow();
                if (row == P_SLEEP) {
                    SleepTimer.step(getContext(), dir);
                    sleepLeft = SleepTimer.minutesLeft(getContext());
                } else if (row == P_SHOW) {
                    Prefs.setSleepShown(!Prefs.sleepShown());
                    SleepOverlay.update(getContext());
                } else if (row == P_WHERE) {
                    Prefs.setSleepPlace((Prefs.sleepPlace() + dir + SleepOverlay.PLACES) % SleepOverlay.PLACES);
                    SleepOverlay.update(getContext());
                }
                focus = powerFocus(row);   // rows come and go with the switches above them
                return;
            }
            if (screen == REMOTE_KEYS) {
                int n = appPkgs.size();
                int at = appPkgs.indexOf(Prefs.keyApp(Prefs.KEYS[focus]));
                int next = at < 0 ? (dir > 0 ? 0 : n - 1) : (at + dir + n) % n;
                Prefs.setKeyApp(Prefs.KEYS[focus], appPkgs.get(next));
                return;
            }
            int row = pickScroll + focus;
            switch (row) {
                case 0: break;   // OK opens the language picker.
                case 1: {
                    int at = java.util.Arrays.asList(UNITS).indexOf(Prefs.temperature());
                    Prefs.setTemperature(UNITS[(at + dir + UNITS.length) % UNITS.length]);
                    break;
                }
                case PHOTOS_ROW:
                    if (Photos.files(getContext()).isEmpty()) return;
                    Prefs.setPhotos(!Prefs.photos());
                    return;
                case 3: Prefs.setSounds(!Prefs.sounds()); break;
                case 4: Prefs.setWeather(!Prefs.weather()); break;
                case CITY_ROW: return;
                case FORECAST_ROW:   // both are in memory: nothing to fetch
                    Prefs.setForecast(Prefs.forecast().equals("days") ? "hours" : "days");
                    return;
                case QUOTES_ROW: Prefs.setQuotes(!Prefs.quotes()); break;
                default:
                    if (!Prefs.quotes()) return;
                    Prefs.setCoin(row - COIN_ROWS, !Prefs.coin(row - COIN_ROWS));
                    break;
            }
            HomeData.get(getContext()).settingsChanged();
        }

        private void askCity() {
            if (Ui.tr("Searching…").equals(cityStatus)) return;
            EditText field = new EditText(activity);
            field.setSingleLine(true);
            field.setHint(Ui.tr("Type the city's name"));
            field.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle(Ui.tr("City"))
                    .setView(field)
                    .setPositiveButton(Ui.tr("Search"), (d, which) -> searchCity(field.getText().toString()))
                    .setNegativeButton(Ui.tr("Cancel"), null)
                    .create();
            field.setOnEditorActionListener((v, action, event) -> {
                dialog.dismiss();
                searchCity(field.getText().toString());
                return true;
            });
            dialog.show();
        }

        // MARK: - My Photos (sent from a computer: tools/photos.py)

        /** OK on My Photos: asks, then deletes them all. */
        private void askRemovePhotos() {
            int n = Photos.files(getContext()).size();
            if (n == 0) return;
            AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle(Ui.plural("plural|one:Remove {n} photo?|other:Remove {n} photos?", n))
                    .setPositiveButton(Ui.tr("Remove"), (d, which) -> {
                        Photos.removeAll(getContext());
                        invalidate();
                        spoken.focusMoved();
                    })
                    .setNegativeButton(Ui.tr("Cancel"), null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();   // one stray OK keeps them
        }

        private void searchCity(String name) {
            if (name.trim().isEmpty()) return;
            cityStatus = Ui.tr("Searching…");
            invalidate();
            new Thread(() -> {
                List<String[]> found;
                String error;
                try {
                    found = HomeData.findCity(name);
                    error = found.isEmpty() ? Ui.tr("No city with that name.") : "";
                } catch (Exception e) {
                    found = new ArrayList<>();
                    error = Ui.trf("Couldn't search now: {error}", "error", e.getMessage());
                    AppLog.w("city search: " + e.getMessage());
                }
                List<String[]> result = found;
                String message = error;
                handler.post(() -> {
                    cityStatus = message;
                    if (!result.isEmpty() && screen == HOME_SETTINGS) {
                        cities.clear();
                        cities.addAll(result);
                        openPicker(PICK_CITY);
                        spoken.focusMoved();
                    }
                    invalidate();
                    if (!message.isEmpty()) handler.postDelayed(() -> { cityStatus = ""; invalidate(); }, 4000);
                });
            }).start();
        }

        private boolean scrolling() {
            return screen == PICKER || screen == SHELF_EDIT || screen == HOME_SETTINGS;
        }

        private int listSize() {
            return screen == SHELF_EDIT ? shelfRows.size() : screen == HOME_SETTINGS ? settingLabels().length : pickLabels.size();
        }

        // MARK: - Shelf editor

        private void openShelfEditor(boolean top) {
            editingTop = top;
            shelfOrder.clear();
            if (top) {
                shelfOrder.addAll(HomeApps.topButtonKeys());
                rebuildShelfRows(HomeApps.topCandidates());
            } else {
                // Only the apps the shelf shows: one that was uninstalled would hold a place (and count toward the
                // limit) without a row to take it off.
                PackageManager pm = getContext().getPackageManager();
                for (String pkg : HomeApps.shelfPackages(getContext())) if (HomeApps.launchIntent(pm, pkg) != null) shelfOrder.add(pkg);
                rebuildShelfRows(HomeApps.shelfCandidates(getContext()));
            }
            pickScroll = 0;
            focus = 0;
            screen = SHELF_EDIT;
        }

        /** Selected apps first, in shelf order, then the rest in the order they came. */
        private void rebuildShelfRows(List<String[]> candidates) {
            shelfRows.clear();
            for (String pkg : shelfOrder) {
                for (String[] cand : candidates) if (cand[0].equals(pkg)) { shelfRows.add(cand); break; }
            }
            for (String[] cand : candidates) if (!shelfOrder.contains(cand[0])) shelfRows.add(cand);
        }

        /** Same rows after a change: keeps the focus on the app that was focused. */
        private void refreshShelfRows(String focusedPkg) {
            List<String[]> all = new ArrayList<>(shelfRows);
            rebuildShelfRows(all);
            for (int i = 0; i < shelfRows.size(); i++) {
                if (!shelfRows.get(i)[0].equals(focusedPkg)) continue;
                int visible = rowCount();
                if (i < pickScroll) pickScroll = i;
                else if (i >= pickScroll + visible) pickScroll = i - visible + 1;
                focus = i - pickScroll;
                break;
            }
            if (editingTop) Prefs.setTopButtons(shelfOrder); else Prefs.setShelf(shelfOrder);
        }

        /** OK: puts the app on the shelf (at the end) or takes it off. */
        private void toggleShelfApp() {
            String pkg = shelfRows.get(pickScroll + focus)[0];
            if (shelfOrder.contains(pkg)) shelfOrder.remove(pkg);
            else if (editingTop || shelfOrder.size() < HomeApps.maxShelf(getContext())) shelfOrder.add(pkg);   // the top buttons: no limit
            else return;
            refreshShelfRows(pkg);
        }

        /** ← → : moves a shelf app one place earlier or later. */
        private void moveShelfApp(int dir) {
            String pkg = shelfRows.get(pickScroll + focus)[0];
            int at = shelfOrder.indexOf(pkg), to = at + dir;
            if (at < 0 || to < 0 || to >= shelfOrder.size()) return;
            shelfOrder.remove(at);
            shelfOrder.add(to, pkg);
            refreshShelfRows(pkg);
        }

        private void drawShelfEditor(Canvas c) {
            drawPaneTitle(c, editingTop ? Ui.tr("Top Buttons") : Ui.tr("Shelf"));
            int rows = rowCount();
            for (int i = 0; i < rows; i++) {
                String[] app = shelfRows.get(pickScroll + i);
                int place = shelfOrder.indexOf(app[0]);
                float y = (LIST_TOP + 20 + i * (LIST_ROW_H + LIST_ROW_GAP)) * k;
                rowRect.set(LIST_X * k, y, LIST_R * k, y + LIST_ROW_H * k);
                float f = drawRowBackground(c, i);
                boolean off = unavailable(i) != null;
                rowLabel.setColor(off ? inkFade(0x73, f)
                        : blend(place >= 0 ? inkBase(0xF2) : inkBase(0x80), INK, f));
                rowValue.setColor(off ? inkFade(0x59, f) : inkValue(f));
                float base = rowRect.centerY() + Ui.halfCap(rowLabel);
                c.drawText(TextUtils.ellipsize(app[1], new TextPaint(rowLabel), 520 * k, TextUtils.TruncateAt.END).toString(), rowRect.left + 30 * k, base, rowLabel);
                float right = rowRect.right - 30 * k;
                if (place >= 0 && !off) {
                    // On the shelf: a check, as in the pickers, after its place.
                    float cw = Symbols.width(getContext(), "check", 26 * k);
                    Symbols.draw(getContext(), c, "check", right - cw / 2, rowRect.centerY(), 26 * k,
                            blend(col(pal.valueInk), INK, f));
                    right -= cw + 16 * k;
                }
                c.drawText(off ? Ui.tr("Unavailable") : place >= 0 ? "#" + (place + 1) : Ui.tr("shelf|Off"), right, base, rowValue);
            }
            float hy = (PANEL_BOTTOM - 60) * k;
            if (!editingTop && HomeApps.lowRam(getContext())) {
                c.drawText(Ui.tr("This TV has little memory, so the shelf holds fewer apps."), LIST_X * k + 30 * k, hy, subtitle);
                hy += 36 * k;
            }
            c.drawText(editingTop ? Ui.tr("OK adds or removes a button. ← → change the order.")
                    : HomeApps.maxShelf(getContext()) == Integer.MAX_VALUE ? Ui.tr("OK adds or removes an app. ← → change the order.")
                    : Ui.trf("OK adds or removes (up to {n}). ← → change the order.", "n", HomeApps.maxShelf(getContext())),
                    LIST_X * k + 30 * k, hy, subtitle);
        }

        private void openPicker(int which) {
            pickFor = which;
            pickLabels.clear();
            if (which == PICK_CITY) {
                for (String[] city : cities) pickLabels.add(city[0]);
                pickCurrent = -1;
            } else if (which == PICK_LANGUAGE) {
                String[] choices = languageChoices();
                for (String code : choices) {
                    pickLabels.add(code.equals("auto") ? Ui.tr("Automatic") : Ui.languageName(code));
                }
                pickCurrent = java.util.Arrays.asList(choices).indexOf(Prefs.language());
            } else if (which <= PICK_SLEEP) {
                pickCurrent = -1;
                for (int i = 0; i < choices.size(); i++) {
                    pickLabels.add(choices.get(i)[1]);
                    if (choices.get(i)[0].equals(choiceNow)) pickCurrent = i;
                }
            } else {
                pickLabels.addAll(appLabels);
                pickCurrent = appPkgs.indexOf(Prefs.keyApp(Prefs.KEYS[which - 1]));
            }
            int visible = Math.min(PICK_VISIBLE, pickLabels.size());
            int at = Math.max(0, pickCurrent);
            pickScroll = Math.max(0, Math.min(at - 3, pickLabels.size() - visible));
            focus = at - pickScroll;
            screen = PICKER;
        }

        /** A long list (the apps): nine rows at a time, the one in use checked. */
        private void drawPicker(Canvas c) {
            String name = pickFor == PICK_CITY ? Ui.tr("City")
                    : pickFor == PICK_LANGUAGE ? Ui.tr("Language")
                    : pickFor == PICK_SLEEP ? Ui.tr("Sleep Timer") : pickFor == PICK_PLACE ? Ui.tr("Position")
                    : keyLabels()[pickFor - 1];
            drawPaneTitle(c, name);
            int rows = rowCount();
            for (int i = 0; i < rows; i++) {
                int at = pickScroll + i;
                float y = (LIST_TOP + 20 + i * (LIST_ROW_H + LIST_ROW_GAP)) * k;
                rowRect.set(LIST_X * k, y, LIST_R * k, y + LIST_ROW_H * k);
                float f = drawRowBackground(c, i);
                boolean off = unavailable(i) != null;
                rowLabel.setColor(off ? inkFade(0x73, f) : blend(inkBase(0xF2), INK, f));
                c.drawText(pickLabels.get(at), rowRect.left + 30 * k, rowRect.centerY() + Ui.halfCap(rowLabel), rowLabel);
                if (off) {
                    rowValue.setColor(inkFade(0x59, f));
                    c.drawText(Ui.tr("Unavailable"), rowRect.right - 30 * k, rowRect.centerY() + Ui.halfCap(rowLabel), rowValue);
                } else if (at == pickCurrent) {
                    float cw = Symbols.width(getContext(), "check", 26 * k);
                    Symbols.draw(getContext(), c, "check", rowRect.right - 30 * k - cw / 2, rowRect.centerY(), 26 * k,
                            blend(col(pal.valueInk), INK, f));
                }
            }
            float hy = (PANEL_BOTTOM - 60) * k;
            c.drawText((pickScroll + focus + 1) + " / " + pickLabels.size() + "  ·  " + Ui.tr("OK picks, Back cancels."),
                    LIST_X * k + 30 * k, hy, subtitle);
        }

        private void drawList(Canvas c) {
            boolean keys = screen == REMOTE_KEYS;
            String[] labels = listLabels();
            String[] values = listValues();
            drawPaneTitle(c, keys ? Ui.tr("Remote Buttons") : screen == POWER ? Ui.tr("Sleep Timer") : Ui.tr("Home Settings"));
            float pitch = LIST_ROW_H + LIST_ROW_GAP;
            int rows = keys ? labels.length : rowCount(), from = keys ? 0 : pickScroll;
            for (int i = 0; i < rows; i++) {
                float y = (LIST_TOP + 20 + i * pitch) * k;
                rowRect.set(LIST_X * k, y, LIST_R * k, y + LIST_ROW_H * k);
                float f = drawRowBackground(c, i);
                rowLabel.setColor(blend(inkBase(0xF2), INK, f));
                boolean locked = unavailable(i) != null;
                if (locked) values[from + i] = Ui.tr("Unavailable");
                rowValue.setColor(locked ? inkFade(0x59, f) : inkValue(f));
                if (locked) rowLabel.setColor(inkFade(0x73, f));
                float base = rowRect.centerY() + Ui.halfCap(rowLabel);
                c.drawText(labels[from + i], rowRect.left + 30 * k, base, rowLabel);
                c.drawText(values[from + i], rowRect.right - 30 * k, base, rowValue);
            }
            float hy = (PANEL_BOTTOM - 60) * k;
            // A list longer than the screen says where we are in it, as the app picker does. Photos say where they come
            // from; on the Sleep Timer screen, the timer and Position open a list on OK.
            String where = rows < labels.length ? (from + focus + 1) + " / " + labels.length + "  ·  " : "";
            c.drawText(where + (screen == HOME_SETTINGS && from + focus == PHOTOS_ROW ? (Photos.files(getContext()).isEmpty()
                    ? Ui.tr("Send them from a computer: see “Your own photos” in the README.")
                    : Ui.tr("OK removes them; the arrows turn them on or off."))
                    : screen == POWER && powerRow() != P_SHOW && unavailable(focus) == null ? Ui.tr("OK opens the list. ← → change the value.")
                    : Ui.tr("OK or the arrows change the value.")), LIST_X * k + 30 * k, hy, subtitle);
            if (keys) {
                c.drawText(RemoteKeys.VESTEL ? Ui.tr("YouTube, Prime Video and Google Play: the TV itself controls those buttons.")
                        : RemoteKeys.KTC ? Ui.tr("Netflix, YouTube and Prime Video: the TV itself controls those buttons.")
                        : RemoteKeys.SONY ? Ui.tr("Sony default: the button opens what it always did.")
                        : RemoteKeys.FORMOVIE ? Ui.tr("The other buttons: the TV itself controls them.")
                        : RemoteKeys.XIAOMI ? Ui.tr("Xiaomi default: the button opens what it always did.")
                        : RemoteKeys.HISENSE ? Ui.tr("Netflix: the TV itself controls that button.")
                        : RemoteKeys.supported() ? Ui.tr("Disney and Globoplay: the TV itself controls those buttons.")
                        : Ui.tr("TCL remote buttons: on this TV they work as always."), LIST_X * k + 30 * k, hy + 36 * k, subtitle);
            }
        }

        private void activate() {
            Context c = getContext();
            if (zone == Z_RAIL) {   // OK on the menu: into the panel it holds
                enterPane();
                return;
            }
            if (tellWhy()) return;
            if (screen == PICKER && pickFor == PICK_CITY) {
                String[] city = cities.get(pickScroll + focus);
                Prefs.setCity(city[1], city[2], city[3]);
                HomeData.get(c).settingsChanged();
                focus = CITY_ROW;
                pickScroll = 0;
                screen = HOME_SETTINGS;
            } else if (screen == PICKER && pickFor <= PICK_SLEEP) {
                pickPower();
            } else if (screen == PICKER && pickFor == PICK_LANGUAGE) {
                Prefs.setLanguage(languageChoices()[pickScroll + focus]);
                SleepOverlay.update(c);   // the pill speaks it too
                focus = 0;
                pickScroll = 0;
                screen = HOME_SETTINGS;
            } else if (screen == PICKER) {
                int at = pickScroll + focus;
                Prefs.setKeyApp(Prefs.KEYS[pickFor - 1], appPkgs.get(at));
                focus = pickFor - 1;
                screen = REMOTE_KEYS;
            } else if (screen == SHELF_EDIT) {
                toggleShelfApp();
                invalidate();
                return;
            } else if (screen == REMOTE_KEYS) {
                openPicker(focus + 1);
            } else if (screen == HOME_SETTINGS && pickScroll + focus == 0) {
                openPicker(PICK_LANGUAGE);
            } else if (screen == HOME_SETTINGS && pickScroll + focus == CITY_ROW) {
                askCity();
                return;
            } else if (screen == HOME_SETTINGS && pickScroll + focus == PHOTOS_ROW) {
                askRemovePhotos();
                return;
            } else if (screen == POWER) {
                activatePower();
            } else if (screen == HOME_SETTINGS || screen == THEME) {
                adjust(1);
                invalidate();
                return;
            } else if (screen == SCREENSAVER) {
                pause();
                activity.showPreview();
                return;
            }
            for (Spring s : pop) s.snap(0);
            invalidate();
            spoken.focusMoved();
        }
    }
}
