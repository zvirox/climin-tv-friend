package io.climintv.home.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import io.climintv.home.MainActivity;

/**
 * Intercepts the remote's Home button on OEM Android TV sets (e.g. Xiaomi Mi TV / TV Stick)
 * where the Home button emits KEYCODE_ALL_APPS (284).
 * Stock firmware routes that key exclusively to PatchWall/stock launcher; once disabled,
 * this AccessibilityService cleanly consumes the event and starts CliminTV Home.
 */
public class HomeKeyService extends AccessibilityService {
    private static final int KEYCODE_ALL_APPS = 284;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        // Intercept Xiaomi / OEM KEYCODE_ALL_APPS or Home key re-routing
        if (keyCode == KEYCODE_ALL_APPS || keyCode == KeyEvent.KEYCODE_HOME) {
            if (action == KeyEvent.ACTION_UP) {
                handler.post(this::openCliminHome);
                return true; // Consume event so OEM dead-end doesn't trigger
            }
            return true;
        }

        return super.onKeyEvent(event);
    }

    private void openCliminHome() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.setAction(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(homeIntent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No-op
    }

    @Override
    public void onInterrupt() {
        // No-op
    }
}
