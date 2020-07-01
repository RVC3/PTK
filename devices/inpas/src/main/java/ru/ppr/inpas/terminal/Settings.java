package ru.ppr.inpas.terminal;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Класс настроек, необходимых для реализации драйвера Inpas.
 */
public class Settings {

    private final SharedPreferences mPreferences;

    public Settings(@NonNull final Context context) {
        mPreferences = context.getSharedPreferences(Entities.INPAS_TERMINAL_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Метод для получения id последней транзакции.
     *
     * @return id последней транзакции.
     */
    public long getLastTransactionId() {
        return mPreferences.getLong(Entities.INPAS_TERMINAL_LAST_TRANSACTION_ID, Defaults.TRANSACTION_NO_ID);
    }

    /**
     * Метод для установления id последней транзакции.
     *
     * @param id id последней транзакции.
     */
    public void setLastTransactionId(final long id) {
        mPreferences.edit()
                .putLong(Entities.INPAS_TERMINAL_LAST_TRANSACTION_ID, id)
                .apply();
    }

    /**
     * Метод для получения статуса подтверждения последней транзакции.
     *
     * @return статус подтверждения последней транзакции.
     * {@code true} - транзакция подтверждена, иначе {@code false}.
     */
    public boolean getLastTransactionApprovedStatus() {
        return mPreferences.getBoolean(Entities.INPAS_TERMINAL_LAST_TRANSACTION_APPROVED_STATUS, Defaults.TRANSACTION_APPROVED_STATUS);
    }

    /**
     * Метод для установления статуса подтверждения последней транзакции.
     *
     * @param approved статус подтверждения последней транзакции.
     */
    public void setLastTransactionApprovedStatus(final boolean approved) {
        mPreferences.edit()
                .putBoolean(Entities.INPAS_TERMINAL_LAST_TRANSACTION_APPROVED_STATUS, approved)
                .apply();
    }

    /**
     * Класс, содержащий сущности для настроек драйвера Inpas.
     */
    private static final class Entities {
        public static final String INPAS_TERMINAL_PREFERENCES = "INPAS_TERMINAL_PREFERENCES";
        public static final String INPAS_TERMINAL_LAST_TRANSACTION_ID = "INPAS_TERMINAL_LAST_TRANSACTION_ID";
        public static final String INPAS_TERMINAL_LAST_TRANSACTION_APPROVED_STATUS = "INPAS_TERMINAL_LAST_TRANSACTION_APPROVED_STATUS";
    }

    /**
     * Класс, содержащий значения по умолчанию для настроек драйвера Inpas.
     */
    private static final class Defaults {
        private static final long TRANSACTION_NO_ID = -1L;
        private static final boolean TRANSACTION_APPROVED_STATUS = false;
    }

}