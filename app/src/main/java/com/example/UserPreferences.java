package com.example;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AVATAR_URI = "user_avatar_uri";

    public static void saveProfile(Context context, String name, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .apply();
    }

    public static void saveAvatarUri(Context context, String uri) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_URI, uri).apply();
    }

    public static String getAvatarUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AVATAR_URI, "");
    }

    public static String getName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NAME, "");
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, "");
    }

    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_AVATAR_URI)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply();
    }

    public static void incrementPlayCount(Context context, long trackId) {
        SharedPreferences prefs = context.getSharedPreferences("song_play_counts", Context.MODE_PRIVATE);
        int current = prefs.getInt(String.valueOf(trackId), 0);
        prefs.edit().putInt(String.valueOf(trackId), current + 1).apply();
    }

    public static int getPlayCount(Context context, long trackId) {
        SharedPreferences prefs = context.getSharedPreferences("song_play_counts", Context.MODE_PRIVATE);
        return prefs.getInt(String.valueOf(trackId), 0);
    }
}
