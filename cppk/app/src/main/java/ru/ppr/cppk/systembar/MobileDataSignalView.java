package ru.ppr.cppk.systembar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.widget.ImageView;

import ru.ppr.cppk.R;
import ru.ppr.logger.Logger;

/**
 * Created by nevolin on 07.07.2016.
 */
public class MobileDataSignalView extends ImageView {

    static final String TAG = Logger.makeLogTag(MobileDataSignalView.class);

    public enum SignalStrength {

        NONE(R.drawable.signal_strength_none),
        ZERO(R.drawable.signal_strength_0),
        ONE(R.drawable.signal_strength_1),
        TWO(R.drawable.signal_strength_2),
        THREE(R.drawable.signal_strength_3),
        FOUR(R.drawable.signal_strength_4),
        FIVE(R.drawable.signal_strength_5),;

        private int imageResource;

        SignalStrength(int imageResource) {
            this.imageResource = imageResource;
        }

        public static SignalStrength fromSignalLevel(int signalLevel) {

            SignalStrength strength = ZERO;

            if (signalLevel == 0)
                strength = ZERO;
            else if (signalLevel == 1)
                strength = ONE;
            else if (signalLevel == 2)
                strength = TWO;
            else if (signalLevel == 3)
                strength = THREE;
            else if (signalLevel == 4)
                strength = FOUR;
            else if (signalLevel == 5)
                strength = FIVE;

            Logger.info(TAG, "fromSignalLevel(signalLevel=" + signalLevel + ") return SignalStrength=" + strength);

            return strength;
        }

    }

    private static SignalStrength lastSignalStrengthValue = SignalStrength.ZERO;

    public MobileDataSignalView(Context context) {
        super(context);

        init();
    }

    public MobileDataSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MobileDataSignalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MobileDataSignalView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setCurrentSignalStrength(lastSignalStrengthValue);
    }

    public void setCurrentSignalStrength(SignalStrength signalStrength) {
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        lastSignalStrengthValue = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT ? SignalStrength.NONE : signalStrength;

        setImageResource(signalStrength.imageResource);
    }

    public void refreshCurrentSignalStrength() {
        setCurrentSignalStrength(lastSignalStrengthValue);
    }

}
