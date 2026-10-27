package io.climintv.home;

import android.util.Log;

/** The app's log (adb logcat -s CliminUI). Never logs keys. */
final class AppLog {
    private AppLog() {}

    static void w(String message) {
        Log.i("CliminUI", message);
    }
}
