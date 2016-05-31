package byeonghoon.x579.smartlock.cardapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Byeonghoon on 26-May-16.
 */
public class AccountStorage {
    private static final String PREF_ACCOUNT_NUMBER = "SmartLockCardApp-account-number";
    private static final String DEFAULT_ACCOUNT_NUMBER = "00000000";
    private static final String TAG = "AccountStorage";
    private static final Object sAccountLock = new Object();

    //TODO: add encryption to account secret

    public static void SetAccount(Context c, String s, int id) {
        synchronized(sAccountLock) {
            Log.i(TAG, "Setting account number: " + s);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_ACCOUNT_NUMBER + "-" + id, s).commit();
        }
    }

    public static String GetAccount(Context c, int id) {
        synchronized (sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            String account = prefs.getString(PREF_ACCOUNT_NUMBER + "-" + id, DEFAULT_ACCOUNT_NUMBER);
            return account;
        }
    }
}