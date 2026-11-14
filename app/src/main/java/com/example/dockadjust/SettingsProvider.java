package com.example.dockadjust;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.content.pm.PackageManager;

/** Read-only bridge: the launcher process reads the module's private preference safely. */
public final class SettingsProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (getContext() == null || !"offset".equals(uri.getLastPathSegment())) return null;
        String[] callers = getContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
        boolean allowed = false;
        if (callers != null) {
            for (String caller : callers) {
                if ("com.android.launcher".equals(caller)) { allowed = true; break; }
            }
        }
        if (!allowed) throw new SecurityException("DockAdjust settings are only readable by the system launcher");
        float value = getContext().getSharedPreferences("dock_settings", 0).getFloat("offset_dp", 0f);
        MatrixCursor cursor = new MatrixCursor(new String[]{"offset_dp"});
        cursor.addRow(new Object[]{value});
        return cursor;
    }

    @Override public String getType(Uri uri) { return "vnd.android.cursor.item/vnd.dockadjust.offset"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("read only"); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("read only"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("read only"); }
}
