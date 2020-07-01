package ru.ppr.cppk.logic.exemptionChecker.unit;

import android.support.annotation.Nullable;

import ru.ppr.cppk.model.SmartCardId;

/**
 * Проверка "Запрет на использование льготы с другой карты".
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardIdExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param smartCardId         Идентификационная информация смарт-карты.
     * @param exceptedSmartCardId Ожидаемая идентификационная информация смарт-карты.
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(@Nullable SmartCardId smartCardId,
                         @Nullable SmartCardId exceptedSmartCardId) {
        return exceptedSmartCardId == null || exceptedSmartCardId.equals(smartCardId);
    }
}
