package io.yourtv.home;

import android.util.Log;

/** The app's log (adb logcat -s YourUI). Never logs keys. */
final class AppLog {
    private AppLog() {}

    static void w(String message) {
        Log.i("YourUI", message);
    }
}
