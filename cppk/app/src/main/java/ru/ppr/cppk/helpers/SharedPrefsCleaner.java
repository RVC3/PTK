package ru.ppr.cppk.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import ru.ppr.cppk.GlobalConstants;

/**
 * Класс помощник для очистки SharedPreferences от устаревших ключей.
 *
 * @author Aleksandr Brazhkin
 */
public class SharedPrefsCleaner {

    private Context mContext;

    public SharedPrefsCleaner(Context context) {
        mContext = context;
    }

    /**
     * Выполняет очистку SharedPreferences от устаревших ключей
     */
    public void deleteUnusedKeys() {
        SharedPreferences preferences = mContext.getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("SecurityCardUID");
        editor.remove("CurrentUserName");
        editor.remove("CurrentUserRole");
        editor.remove("UserId");
        editor.remove("sft_path");
        editor.apply();
    }
}
