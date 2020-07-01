package ru.ppr.core.dataCarrier.pd.v22;

import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithEds;
import ru.ppr.core.dataCarrier.pd.base.PdWithExemption;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;

/**
 * ПД v.22.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdV22 extends PdWithoutPlace, PdWithExemption, PdWithEds, PdWithPaymentType, PdWithDirection {

    /**
     * Возвращает признак - требуется проверка входа на другой станции при выходе на текущей станции
     *
     * @return true если проверка необходима, false в противном случае
     */
    boolean isPassageToStationCheckRequired();

    /**
     * Задает признак - требуется проверка входа на другой станции при выходе на текущей станции
     *
     * @param passageToStationCheckRequired true если проверка необходима, false в противном случае
     */
    void setPassageToStationCheckRequired(boolean passageToStationCheckRequired);

    /**
     * Возвращает признак - требуется активация билета (с помощью считывания специального ШК на ИТ на станции отправления)
     *
     * @return true если проверка необходима, false в противном случае
     */
    boolean isActivationRequired();

    /**
     * Задает признак - требуется активация билета (с помощью считывания специального ШК на ИТ на станции отправления)
     *
     * @param activationRequired true если проверка необходима, false в противном случае
     */
    void setActivationRequired(boolean activationRequired);

    /**
     * Возвращает номер телефона
     *
     * @return целое число, до 12 цифр
     */
    long getPhoneNumber();

    /**
     * Задает номер телефона
     *
     * @param phoneNumber целое число, до 12 цифр
     */
    void setPhoneNumber(long phoneNumber);

}
