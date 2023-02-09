package com.example.nativeandroidapp.ultil;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {
    public static SharedPreferences sharedPreferences;

    private static String sharePreName ="LOGIN" ;
    public static void setString(Context context, String key, String value) {
        sharedPreferences = context.getSharedPreferences(sharePreName, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(sharePreName, context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void deleteString(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(sharePreName, Context.MODE_PRIVATE);
        settings.edit().remove(key).commit();
    }

    public static void deleteAll(Context context) {
        SharedPreferences settings = context.getSharedPreferences(sharePreName, Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }


}
