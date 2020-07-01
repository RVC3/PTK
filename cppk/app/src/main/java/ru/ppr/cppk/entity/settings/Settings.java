package ru.ppr.cppk.entity.settings;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.utils.ArrayUtils;
import ru.ppr.logger.Logger;

/**
 * Базовый класс настроек ПТК
 * Created by Григорий on 16.03.2017.
 */
public class Settings {

    private static final String TAG = Logger.makeLogTag(Settings.class);

    /**
     * Структура данных для хранения настроек.
     */
    private final Map<String, String> mSettings = new HashMap<>();


    @NonNull
    public Map<String, String> getSettings() {
        return mSettings;
    }

    public void setSettings(@NonNull final Map<String, String> settings) {
        mSettings.putAll(settings);
    }


    /**
     * Метод, возвращающий long[] значение по ключу из внутреннего хранилища.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter    параметр настройки.
     * @param defaultValue значение по умолчанию для данной настройки.
     * @return значение для данной настройки.
     */
    protected long[] getLongArray(@NonNull final String parameter, final long[] defaultValue) {
        long[] longArrayValue = defaultValue;
        final String value = mSettings.get(parameter);

        if (!TextUtils.isEmpty(value)) {
            longArrayValue = ArrayUtils.splitToLongs(value);
        }

        return longArrayValue;
    }

    /**
     * Метод, возвращающий int значение по ключу из внутреннего хранилища.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter    параметр настройки.
     * @param defaultValue значение по умолчанию для данной настройки.
     * @return значение для данной настройки.
     */
    protected int getInt(@NonNull final String parameter, final int defaultValue) {
        int intValue = defaultValue;
        final String value = mSettings.get(parameter);

        if (!TextUtils.isEmpty(value)) {
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                Logger.error(TAG, ex);
            }
        }

        return intValue;
    }

    /**
     * Метод, возвращающий int значение по ключу из внутреннего хранилища.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter    параметр настройки.
     * @param defaultValue значение по умолчанию для данной настройки.
     * @return значение для данной настройки.
     */
    protected long getLong(@NonNull final String parameter, final long defaultValue) {
        long longValue = defaultValue;
        final String value = mSettings.get(parameter);

        if (!TextUtils.isEmpty(value)) {
            try {
                longValue = Long.parseLong(value);
            } catch (NumberFormatException ex) {
                Logger.error(TAG, ex);
            }
        }

        return longValue;
    }

    /**
     * Метод, возвращающий String значение по ключу из внутреннего хранилища.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter    параметр настройки.
     * @param defaultValue значение по умолчанию для данной настройки.
     * @return значение для данной настройки.
     */
    protected String getString(@NonNull final String parameter, @NonNull final String defaultValue) {
        String stringValue = defaultValue;
        final String value = mSettings.get(parameter);

        if (!TextUtils.isEmpty(value)) {
            stringValue = value;
        }

        return stringValue;
    }

    /**
     * Метод, возвращающий boolean значение по ключу из внутреннего хранилища.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter    параметр настройки.
     * @param defaultValue значение по умолчанию для данной настройки.
     * @return значение для данной настройки.
     */
    protected boolean getBoolean(@NonNull final String parameter, final boolean defaultValue) {
        boolean booleanValue = defaultValue;
        final String value = mSettings.get(parameter);

        if (!TextUtils.isEmpty(value)) {
            booleanValue = Boolean.parseBoolean(value);
        }

        return booleanValue;
    }
}
