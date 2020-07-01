package ru.ppr.ipos.stub;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.logger.Logger;

/**
 * Хранилище данных для {@link StubTerminal}
 *
 * @author Aleksandr Brazhkin
 */
class SharedPrefs {

    private static final String TAG = Logger.makeLogTag(SharedPrefs.class);

    private static class Entities {
        private static final String LAST_TRANSACTION_ID = "LAST_TRANSACTION_ID";
    }

    private final Context mContext;
    private final String mSharedPrefsFileName;
    private final SharedPreferences preferences;

    SharedPrefs(Context context, String sharedPrefsFileName) {
        this.mContext = context;
        this.mSharedPrefsFileName = sharedPrefsFileName;
        preferences = mContext.getSharedPreferences(mSharedPrefsFileName, Context.MODE_PRIVATE);
    }

    void setLastTransactionId(int lastTransactionId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Entities.LAST_TRANSACTION_ID, lastTransactionId);
        if (!editor.commit()) {
            Logger.error(TAG, "setLastTransactionId failed, id = " + lastTransactionId);
        }
    }

    int getLastTransactionId() {
        return preferences.getInt(Entities.LAST_TRANSACTION_ID, 0);
    }
}
