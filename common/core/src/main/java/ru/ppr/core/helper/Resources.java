package ru.ppr.core.helper;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import javax.inject.Inject;

/**
 * Ресурсы приложения.
 * Обретка над {@link android.content.res.Resources},
 * чтобы отвязать {@link ru.ppr.core.ui.mvp.presenter.MvpPresenter} от контекста {@link Context}
 *
 * @author Aleksandr Brazhkin
 */
public class Resources {

    private final android.content.res.Resources androidResources;
    private final Context context;

    @Inject
    Resources(Context context) {
        this.androidResources = context.getResources();
        this.context = context;
    }

    @NonNull
    public String getString(@StringRes int id) throws android.content.res.Resources.NotFoundException {
        return androidResources.getString(id);
    }

    @NonNull
    public String getString(@StringRes int id, Object... formatArgs) throws android.content.res.Resources.NotFoundException {
        return androidResources.getString(id, formatArgs);
    }

    @ColorInt
    public int getColor(@ColorRes int id) throws android.content.res.Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return androidResources.getColor(id, context.getTheme());
        } else {
            return androidResources.getColor(id);
        }
    }
}
