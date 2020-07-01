package ru.ppr.cppk.systembar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ppr.cppk.R;

/**
 * @author Aleksandr Brazhkin
 */
public class BatteryLevelView extends FrameLayout {

    /**
     * Максимальная ширина цветного индикатора зарядки
     */
    public static int maxWidthChargingImage = 29;

    private boolean isWhiteThemeEnable = false;

    private boolean powerConnected = false;
    private int chargeLevel = 0;

    private View percentImage;
    private ImageView batteryImage;
    private TextView textView;

    public void setWhiteThemeEnable(boolean enable) {
        isWhiteThemeEnable = enable;
    }

    public BatteryLevelView(Context context) {
        this(context, null);
    }

    public BatteryLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_battery_level, this);

        percentImage = findViewById(R.id.percentImage);
        batteryImage = (ImageView) findViewById(R.id.batteryImage);
        textView = (TextView) findViewById(R.id.text);
    }


    void setPowerConnectedState(boolean powerConnected) {
        this.powerConnected = powerConnected;

        if (powerConnected) {
            batteryImage.setImageResource((isWhiteThemeEnable) ? R.drawable.system_bar_battery_white_charging : R.drawable.system_bar_battery_charging);
        } else {
            batteryImage.setImageResource((isWhiteThemeEnable) ? R.drawable.system_bar_battery_white : R.drawable.system_bar_battery);
        }

    }

    public void setChargeLevel(int chargeLevel) {

        this.chargeLevel = chargeLevel;
        String text = String.valueOf(chargeLevel) + "%";


        int color = R.color.statusBarBatteryGreen;
        if (chargeLevel <= 20)
            color = R.color.statusBarBatteryRed;
        else if (chargeLevel <= 50)
            color = R.color.statusBarBatteryYellow;

        percentImage.setBackgroundResource(color);

        ViewGroup.LayoutParams lp = percentImage.getLayoutParams();
        lp.width = Math.min(maxWidthChargingImage, maxWidthChargingImage * chargeLevel / 100);
        percentImage.setLayoutParams(lp);

        textView.setText(text);
        textView.setTextColor(getResources().getColor((isWhiteThemeEnable) ? R.color.statusBarTextColorWhite : R.color.statusBarTextColor));

    }

}