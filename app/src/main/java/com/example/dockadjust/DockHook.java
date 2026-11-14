package com.example.dockadjust;

import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Runtime hook for the ColorOS 17 OplusHotseat implementation. */
public final class DockHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.android.launcher";
    private static final String TARGET_CLASS = "com.android.launcher3.OplusHotseat";
    private static final Uri PREF_URI = Uri.parse("content://com.example.dockadjust.prefs/offset");
    private static volatile float offsetDp = 0f;
    private static volatile long lastRead = 0L;

    @Override public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) return;
        try {
            final Class<?> hotseat = XposedHelpers.findClass(TARGET_CLASS, lpparam.classLoader);
            XposedHelpers.findAndHookMethod(hotseat, "setTranslationY", float.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (!(param.thisObject instanceof View) || param.args == null || param.args.length != 1) return;
                    refreshOffset((View) param.thisObject);
                    float original = ((Float) param.args[0]).floatValue();
                    float density = ((View) param.thisObject).getResources().getDisplayMetrics().density;
                    param.args[0] = Float.valueOf(original + offsetDp * density);
                }
            });
            XposedBridge.log("DockAdjust: hooked " + TARGET_CLASS + ".setTranslationY(float)");
        } catch (Throwable t) {
            XposedBridge.log("DockAdjust: hook failed: " + t);
        }
    }

    private static void refreshOffset(View view) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastRead < 750L) return;
        synchronized (DockHook.class) {
            if (now - lastRead < 750L) return;
            Cursor c = null;
            try {
                c = view.getContext().getContentResolver().query(PREF_URI, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    int index = c.getColumnIndex("offset_dp");
                    if (index >= 0) offsetDp = c.getFloat(index);
                }
            } catch (Throwable t) {
                XposedBridge.log("DockAdjust: settings provider read failed: " + t);
            } finally {
                if (c != null) c.close();
                lastRead = now;
            }
        }
    }
}
