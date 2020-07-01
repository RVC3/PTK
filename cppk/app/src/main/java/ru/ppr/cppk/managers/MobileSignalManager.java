package ru.ppr.cppk.managers;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.logger.Logger;

/**
 * Менеджер уровня сигнала мобильной сети.
 *
 * @author Aleksandr Brazhkin
 */
public class MobileSignalManager {

    private static final String TAG = Logger.makeLogTag(MobileSignalManager.class);

    private final Context context;
    private final TelephonyManager telephonyManager;
    private final List<Listener> listeners = new ArrayList<>();

    public MobileSignalManager(Context context) {
        Logger.trace(TAG, "MobileSignalManager initialized");
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void destroy() {
        throw new UnsupportedOperationException("TelephonyManager listener should be cleared");
//        Logger.trace(TAG, "BatteryManager destroyed");
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        /**
         * Gets the signal level from a <tt>SignalStrength</tt> as a value in the
         * range 0-4.  This info is hidden from the public API, so this method
         * obtains it via reflection.
         *
         * @return the signal level, or 0 if Google has broken the hack
         */
        int getSignalLevel(final SignalStrength signal) {
            try {
                final Method m = SignalStrength.class.getDeclaredMethod("getLevel", (Class[]) null);
                m.setAccessible(true);
                return (Integer) m.invoke(signal, (Object[]) null);
            } catch (Exception e) {
                Logger.error(TAG, "level-getSignalLevelReflection Error: Google Hate Developers " + e);
                Logger.error(TAG, e);
                return 0;
            }
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            final int signalLevel = getSignalLevel(signalStrength);
            Logger.trace(TAG, "signalLevel: " + signalLevel);

            for (Listener listener : listeners) {
                listener.onSignalLevelChanged(signalLevel);
            }
        }
    };

    public interface Listener {
        void onSignalLevelChanged(int signalLevel);
    }
}
