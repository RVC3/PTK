package ru.ppr.chit.ui.widget.statusbar;

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
import ru.ppr.chit.ui.widget.batterylevel.BatteryLevelAndroidView;
import ru.ppr.chit.ui.widget.clock.ClockAndroidView;
import ru.ppr.chit.ui.widget.networkstatus.NetworkStatusAndroidView;
import ru.ppr.chit.ui.widget.regbroken.RegBrokenAndroidView;
import ru.ppr.chit.ui.widget.syncstatus.SyncStatusAndroidView;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает статус-бар, содержит:
 * - текущее время
 * - признак поломанной регистрации
 * - статус сети
 * - статус синхронизации
 * - текущий уровень заряда батареи
 *
 * @author Dmitry Nevolin
 */
public class StatusBarAndroidView extends FrameLayout implements StatusBarView {

    // region Constants
    private static final String MVP_CLOCK_ID = "MVP_CLOCK_ID";
    private static final String MVP_REG_BROKEN_ID = "MVP_REG_BROKEN_ID";
    private static final String MVP_NETWORK_STATUS_ID = "MVP_NETWORK_STATUS_ID";
    private static final String MVP_SYNC_STATUS_CLOCK_ID = "MVP_SYNC_STATUS_CLOCK_ID";
    private static final String MVP_BATTERY_LEVEL_CLOCK_ID = "MVP_BATTERY_LEVEL_CLOCK_ID";
    // endregion
    // region Di
    private MvpDelegate mvpDelegate;
    private StatusBarComponent component;
    private StatusBarPresenter presenter;
    // endregion
    // region Views
    private ClockAndroidView clock;
    private RegBrokenAndroidView regBroken;
    private NetworkStatusAndroidView networkStatus;
    private SyncStatusAndroidView syncStatus;
    private BatteryLevelAndroidView batteryLevel;
    // endregion

    public StatusBarAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public StatusBarAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public StatusBarAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusBarAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_status_bar, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerStatusBarComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        clock = (ClockAndroidView) findViewById(R.id.clock);
        clock.init(mvpDelegate, MVP_CLOCK_ID);
        regBroken = (RegBrokenAndroidView) findViewById(R.id.reg_broken);
        regBroken.init(mvpDelegate, MVP_REG_BROKEN_ID);
        networkStatus = (NetworkStatusAndroidView) findViewById(R.id.network_status);
        networkStatus.init(mvpDelegate, MVP_NETWORK_STATUS_ID);
        syncStatus = (SyncStatusAndroidView) findViewById(R.id.sync_status);
        syncStatus.init(mvpDelegate, MVP_SYNC_STATUS_CLOCK_ID);
        batteryLevel = (BatteryLevelAndroidView) findViewById(R.id.battery_level);
        batteryLevel.init(mvpDelegate, MVP_BATTERY_LEVEL_CLOCK_ID);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::statusBarPresenter, StatusBarPresenter.class);
        presenter.initialize();
    }

}
