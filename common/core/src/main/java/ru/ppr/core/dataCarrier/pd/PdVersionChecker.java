package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import javax.inject.Inject;

/**
 * Детектор вида ПД по версии.
 *
 * @author Aleksandr Brazhkin
 */
public class PdVersionChecker {

    @Inject
    PdVersionChecker() {

    }

    /**
     * Проверяет, является ли ПД абонементом на количество поездок.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если это абонемент на количество поездок, {@code false} иначе
     */
    public boolean isCountTripsSeasonTicket(@NonNull PdVersion pdVersion) {
        switch (pdVersion) {
            case V7:
            case V18:
            case V19:
            case V20:
            case V23:
            case V24:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверяет, является ли ПД комбинированным абонементом на количество поездок.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если это комбинированный абонемент на количество поездок, {@code false} иначе
     */
    public boolean isCombinedCountTripsSeasonTicket(@NonNull PdVersion pdVersion) {
        switch (pdVersion) {
            case V23:
            case V24:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверяет, является ли ПД абонементом на даты.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если это абонемент на даты, {@code false} иначе
     */
    public boolean isSeasonTicketOnDates(@NonNull PdVersion pdVersion) {
        switch (pdVersion) {
            case V6:
            case V25:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверяет, является ли ПД услугой.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если это услуга, {@code false} иначе
     */
    public boolean isServiceFeeTicket(@NonNull PdVersion pdVersion) {
        switch (pdVersion) {
            case V21:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверяет, является ли ПД билетом с местом.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если это ПД с местом, {@code false} иначе
     */
    public boolean isPdWithPlace(@NonNull PdVersion pdVersion) {
        switch (pdVersion) {
            case V9:
            case V10:
                return true;
            default:
                return false;
        }
    }

}
