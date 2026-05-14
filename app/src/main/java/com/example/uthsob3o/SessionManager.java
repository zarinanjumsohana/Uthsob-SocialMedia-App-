package com.example.uthsob3o;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "UthsobSession";
    private static final String KEY_UID = "uid";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save user session
    public void saveSession(String uid, String role, String name, String phone) {
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.apply();
    }

    // Get session data
    public String getUid() { return prefs.getString(KEY_UID, null); }
    public String getRole() { return prefs.getString(KEY_ROLE, null); }
    public String getName() { return prefs.getString(KEY_NAME, null); }
    public String getPhone() { return prefs.getString(KEY_PHONE, null); }
    public boolean isLoggedIn() { return prefs.getBoolean(KEY_LOGGED_IN, false); }

    // Clear session on logout
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}