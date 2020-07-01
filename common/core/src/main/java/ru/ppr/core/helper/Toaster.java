package ru.ppr.core.helper;

import android.support.annotation.StringRes;

/**
 * Отображатель тостов.
 *
 * @author Aleksandr Brazhkin
 */
public interface Toaster {
    void showToast(CharSequence text);

    void showToast(@StringRes int resId);
}
