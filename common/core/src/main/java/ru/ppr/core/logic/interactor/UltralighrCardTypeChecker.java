package ru.ppr.core.logic.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.rfid.MifareCardType;

/**
 * Вилидирует соотсветствие физического типа карты ультралайт, типу карты, полученному из данных карты
 *
 * @author Grigoriy Kashka
 */
public class UltralighrCardTypeChecker {

    @Inject
    public UltralighrCardTypeChecker() {
    }

    /**
     * Проверить на соответствие типов
     *
     * @param bscType        - тип данных карты
     * @param mifareCardType - физический тип карты
     * @return - true в случае успеха
     */
    public boolean check(BscType bscType, MifareCardType mifareCardType) {
        switch (bscType) {
            case CPPK_COUNTER: {
                if (mifareCardType != MifareCardType.UltralightC) {
                    return false;
                }
                break;
            }
            case EV_1_CPPK_COUNTER: {
                if (mifareCardType != MifareCardType.UltralightEV1) {
                    return false;
                }
                break;
            }
        }
        return true;
    }
}
