package ru.ppr.ingenico.core;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
class SharedPrefs {

    private static final String TAG = Logger.makeLogTag(SharedPrefs.class);

    private static class Entities {
        private static final String LAST_SALE_TRANSACTION_LOCAL_ID = "LAST_SALE_TRANSACTION_LOCAL_ID";
        private static final String LAST_SALE_TRANSACTION_EXTERNAL_ID = "LAST_SALE_TRANSACTION_EXTERNAL_ID";
        private static final String LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL = "LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL";
    }

    private final Context mContext;
    private final String mSharedPrefsFileName;
    private final SharedPreferences preferences;

    SharedPrefs(Context context, String sharedPrefsFileName) {
        this.mContext = context;
        this.mSharedPrefsFileName = sharedPrefsFileName;
        preferences = mContext.getSharedPreferences(mSharedPrefsFileName, Context.MODE_PRIVATE);
    }

    int getLastSaleTransactionLocalId() {
        return preferences.getInt(Entities.LAST_SALE_TRANSACTION_LOCAL_ID, -1);
    }

    void setLastSaleTransactionLocalId(int saleTransactionLocalId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Entities.LAST_SALE_TRANSACTION_LOCAL_ID, saleTransactionLocalId);
        if (!editor.commit()) {
            Logger.error(TAG, "setLastSaleTransactionLocalId failed, id = " + saleTransactionLocalId);
        }
    }

    int getLastSaleTransactionExternalId() {
        return preferences.getInt(Entities.LAST_SALE_TRANSACTION_EXTERNAL_ID, -1);
    }

    void setLastSaleTransactionExternalId(int saleTransactionExternalId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Entities.LAST_SALE_TRANSACTION_EXTERNAL_ID, saleTransactionExternalId);
        if (!editor.commit()) {
            Logger.error(TAG, "setLastSaleTransactionExternalId failed, id = " + saleTransactionExternalId);
        }
    }

    boolean isLastSaleTransactionKnownForExternal() {
        return preferences.getBoolean(Entities.LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL, false);
    }

    void setLastSaleTransactionKnownForExternal(boolean lastSaleTransactionKnownForExternal) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Entities.LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL, lastSaleTransactionKnownForExternal);
        if (!editor.commit()) {
            Logger.error(TAG, "setLastSaleTransactionKnownForExternal failed, lastSaleTransactionKnownForExternal = " + lastSaleTransactionKnownForExternal);
        }
    }
}
