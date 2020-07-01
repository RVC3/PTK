package ru.ppr.cppk.helpers;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import ru.ppr.core.helper.Toaster;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.logger.Logger;

/**
 * Отображатель тостов для приложения по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class AppToaster implements Toaster {

    private static String TAG = Logger.makeLogTag(AppToaster.class);

    private final Context mContext;

    public AppToaster(Context context) {
        mContext = context;
    }

    @Override
    public void showToast(CharSequence text) {
        Logger.info(TAG, text == null ? "null" : text.toString());
        if (SharedPreferencesUtils.isErrorToastsEnabled(mContext)) {
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showToast(@StringRes int resId) {
        CharSequence text = mContext.getResources().getText(resId);
        showToast(text);
    }
}
