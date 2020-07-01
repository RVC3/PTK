package ru.ppr.chit.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.inject.Inject;

import ru.ppr.chit.BuildConfig;
import ru.ppr.logger.Logger;

/**
 * Собирает ключ для входа в рут-меню.
 *
 * @author Dmitry Nevolin
 */
public class RootPasswordBuilder {

    private static final String TAG = Logger.makeLogTag(RootPasswordBuilder.class);

    private static final int FACTOR = 11;
    private static final int RELEASE_ROOT_KEY_LENGTH = 6;
    private static final String DEBUG_ROOT_KEY = "1684";

    @Inject
    RootPasswordBuilder() {

    }

    @Nullable
    public String buildReleaseRootPassword(long deviceId) {
        String rootKey = buildRootKey(deviceId);
        if (rootKey == null || rootKey.length() < RELEASE_ROOT_KEY_LENGTH) {
            return null;
        }
        // Пароль - последние 6 символов рут ключа
        return rootKey.substring(rootKey.length() - RELEASE_ROOT_KEY_LENGTH, rootKey.length());
    }

    @NonNull
    public String buildDebugRootPassword() {
        return DEBUG_ROOT_KEY;
    }

    @Nullable
    private String buildRootKey(long deviceId) {
        Calendar calendar = Calendar.getInstance();
        String versionName = BuildConfig.VERSION_NAME;

        try {
            int datePart = calendar.get(Calendar.DAY_OF_MONTH) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.YEAR) % 100;
            int versionNamePart = Integer.valueOf(versionName.substring(versionName.lastIndexOf('.') + 1)) + 1;
            long deviceIdPart = deviceId + 1;

            BigDecimal rootKey = BigDecimal.valueOf(datePart)
                    .multiply(BigDecimal.valueOf(versionNamePart))
                    .multiply(BigDecimal.valueOf(deviceIdPart))
                    .multiply(BigDecimal.valueOf(FACTOR));

            return rootKey.toPlainString();
        } catch (Throwable error) {
            Logger.error(TAG,
                    "Ошибка при построении рут ключа: " + calendar.getTime().getTime() +
                            " | " + versionName +
                            " | " + deviceId);
            Logger.error(TAG, error);

            return null;
        }
    }

}
