package io.climintv.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

/**
 * Handles market://launch?id=<package> deep-links from external remote services
 * such as Home Assistant android_tv_remote or ADB broadcasts.
 */
public class AppLaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            if ("market".equals(data.getScheme()) && "launch".equals(data.getHost())) {
                String pkg = data.getQueryParameter("id");
                if (pkg != null && !pkg.isEmpty()) {
                    launchPackage(pkg);
                }
            }
        }
        finish();
    }

    private void launchPackage(String pkg) {
        PackageManager pm = getPackageManager();
        Intent launch = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (launch == null) {
            launch = pm.getLaunchIntentForPackage(pkg);
        }
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        }
    }
}
