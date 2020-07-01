package ru.ppr.chit.ui.widget.syncstatus;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает статус синхронизации
 *
 * @author Dmitry Nevolin
 */
public class SyncStatusAndroidView extends FrameLayout implements SyncStatusView {

    private MvpDelegate mvpDelegate;
    private SyncStatusComponent component;
    private SyncStatusPresenter presenter;

    public SyncStatusAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public SyncStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SyncStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SyncStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_sync_status, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerSyncStatusComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::syncStatusPresenter, SyncStatusPresenter.class);
        presenter.initialize();
    }

}
