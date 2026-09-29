package com.kage.focus;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

public class KageBlockerService extends AccessibilityService {
    private static KageBlockerService instance;
    private SharedPreferences prefs;
    private WindowManager windowManager;
    private View blockerView;
    private String lastBlockedPackage = "";

    public static void requestRefresh() { if (instance != null) instance.refreshProtection(); }

    public static boolean isAccessibilityEnabled(Context context) {
        String enabled = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(enabled)) return false;
        String target = new ComponentName(context, KageBlockerService.class).flattenToString();
        for (String value : enabled.split(":")) {
            if (target.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        prefs = getSharedPreferences("kage_native_blocker", MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    | AccessibilityEvent.TYPE_WINDOWS_CHANGED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 50;
            setServiceInfo(info);
        }
        refreshProtection();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        refreshProtection();
        if (!isProtectionActive() || event == null || event.getPackageName() == null) {
            removeBlocker();
            return;
        }

        String packageName = event.getPackageName().toString();

        // Never cover KAGE Focus itself.
        if (packageName.equals(getPackageName())) {
            removeBlocker();
            return;
        }

        if (blockedPackages().contains(packageName)) {
            showBlocker(packageName);
        } else if (!packageName.equals(lastBlockedPackage)) {
            removeBlocker();
        }
    }

    private void refreshProtection() {
        if (prefs == null) {
            prefs = getSharedPreferences("kage_native_blocker", MODE_PRIVATE);
        }

        if (prefs.getBoolean("active", false)
                && System.currentTimeMillis() >= prefs.getLong("endAt", 0L)) {
            prefs.edit().putBoolean("active", false)
                    .remove("endAt").remove("packages").apply();
        }

        if (!isProtectionActive()) removeBlocker();
    }

    private boolean isProtectionActive() {
        return prefs != null
                && prefs.getBoolean("active", false)
                && System.currentTimeMillis() < prefs.getLong("endAt", 0L);
    }

    private Set<String> blockedPackages() {
        return new HashSet<>(prefs.getStringSet("packages", new HashSet<>()));
    }

    private void showBlocker(String packageName) {
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        if (blockerView != null && packageName.equals(lastBlockedPackage)) return;

        removeBlocker();
        lastBlockedPackage = packageName;

        TextView overlay = new TextView(this);
        overlay.setText("FOCUS SESSION ACTIVE\n\nThis app is blocked until your session ends.");
        overlay.setTextColor(Color.WHITE);
        overlay.setTextSize(18);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(48, 48, 48, 48);
        overlay.setBackgroundColor(Color.rgb(8, 8, 8));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;

        try {
            windowManager.addView(overlay, params);
            blockerView = overlay;
        } catch (Exception ignored) {}
    }

    private void removeBlocker() {
        lastBlockedPackage = "";
        if (blockerView != null && windowManager != null) {
            try { windowManager.removeView(blockerView); } catch (Exception ignored) {}
            blockerView = null;
        }
    }

    @Override public void onInterrupt() { removeBlocker(); }

    @Override public void onDestroy() {
        removeBlocker();
        if (instance == this) instance = null;
        super.onDestroy();
    }
}
