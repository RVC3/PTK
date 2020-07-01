package ru.ppr.chit.ui.widget.batterylevel;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает информацию о текущем уровне заряда батареи и статус зарядки
 *
 * @author Dmitry Nevolin
 */
public class BatteryLevelAndroidView extends FrameLayout implements BatteryLevelView {

    // region Constants
    /**
     * Максимальная ширина цветного индикатора зарядки
     */
    public static final int PERCENT_IMAGE_MAX_WIDTH = 29;
    public static final int CHARGE_LEVEL_LOW = 20;
    public static final int CHARGE_LEVEL_MEDIUM = 50;
    public static final int CHARGE_LEVEL_MAX = 100;
    // endregion
    // region Di
    private MvpDelegate mvpDelegate;
    private BatteryLevelComponent component;
    private BatteryLevelPresenter presenter;
    // endregion
    // region Views
    private View percentImage;
    private ImageView batteryImage;
    private TextView percentText;
    // endregion

    public BatteryLevelAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public BatteryLevelAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public BatteryLevelAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BatteryLevelAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_battery_level, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerBatteryLevelComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        percentImage = findViewById(R.id.bl_percent_image);
        batteryImage = (ImageView) findViewById(R.id.bl_battery_image);
        percentText = (TextView) findViewById(R.id.bl_percent_text);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::batteryLevelPresenter, BatteryLevelPresenter.class);
        presenter.initialize();
    }

    @Override
    public void setCharging(boolean charging) {
        batteryImage.setImageResource(charging ? R.drawable.battery_level_battery_charging : R.drawable.battery_level_battery);
    }

    @Override
    public void setChargeLevel(int chargeLevel) {
        percentText.setText(getResources().getString(R.string.battery_level_percent, chargeLevel));
        int percentImageColor = R.color.batteryLevelBatteryGreen;
        if (chargeLevel <= CHARGE_LEVEL_LOW) {
            percentImageColor = R.color.batteryLevelBatteryRed;
        } else if (chargeLevel <= CHARGE_LEVEL_MEDIUM) {
            percentImageColor = R.color.batteryLevelBatteryYellow;
        }
        percentImage.setBackgroundResource(percentImageColor);
        ViewGroup.LayoutParams percentImageLayoutParams = percentImage.getLayoutParams();
        percentImageLayoutParams.width = Math.min(PERCENT_IMAGE_MAX_WIDTH, PERCENT_IMAGE_MAX_WIDTH * chargeLevel / CHARGE_LEVEL_MAX);
        percentImage.setLayoutParams(percentImageLayoutParams);
    }

}
