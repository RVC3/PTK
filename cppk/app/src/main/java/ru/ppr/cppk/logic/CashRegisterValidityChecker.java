package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import ru.ppr.cppk.localdb.model.CashRegister;

/**
 * Валидатор информации о фискальном регистраторе.
 *
 * @author Aleksandr Brazhkin
 */
public class CashRegisterValidityChecker {

    @Inject
    public CashRegisterValidityChecker() {

    }

    /**
     * Проверяет валидность информации о фискальном регистраторе
     *
     * @param cashRegister Информации о фискальном регистраторе
     * @return {@code true} если информация валидна, {@code false} иначе
     */
    public boolean isValid(@NonNull CashRegister cashRegister) {
        if (TextUtils.isEmpty(cashRegister.getEKLZNumber()) && TextUtils.isEmpty(cashRegister.getFNSerial())) {
            // Если не указаны ни номер ЭКЛЗ, ни номер ФН
            return false;
        }
        if (TextUtils.isEmpty(cashRegister.getINN())) {
            // Если не указан ИНН
            return false;
        }
        if (TextUtils.isEmpty(cashRegister.getModel())) {
            // Если не указана модель
            return false;
        }
        if (TextUtils.isEmpty(cashRegister.getSerialNumber())) {
            // Если не указан серийный номер
            return false;
        }
        return true;
    }
}
