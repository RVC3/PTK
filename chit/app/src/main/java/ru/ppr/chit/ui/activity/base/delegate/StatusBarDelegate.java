package ru.ppr.chit.ui.activity.base.delegate;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.ui.widget.statusbar.StatusBarAndroidView;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Делегат для добавления статус-бара на экран.
 *
 * @author Aleksandr Brazhkin
 */
public class StatusBarDelegate {

    private static final String MVP_STATUS_BAR_ID = "MVP_STATUS_BAR_ID";

    private final Activity activity;

    @Inject
    public StatusBarDelegate(Activity activity) {
        this.activity = activity;
    }

    public void init(@NonNull MvpDelegate parent) {
        StatusBarAndroidView statusBar = (StatusBarAndroidView) activity.findViewById(R.id.status_bar);
        statusBar.init(parent, MVP_STATUS_BAR_ID);
    }
}
