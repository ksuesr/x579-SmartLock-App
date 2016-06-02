package byeonghoon.x579.smartlock.cardapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Byeonghoon on 31-May-16.
 */
public class SessionStorage {

    public static void set(Context c, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putString(key, value).apply();
    }

    public static String get(Context c, String key, String default_value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getString(key, default_value);
    }

    public static boolean exists(Context c, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.contains("card[" + id + "].key");
    }

    public static boolean exists(Context c, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.contains(key);
    }

    public static void expire(Context c, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().remove(key).apply();
    }
}
