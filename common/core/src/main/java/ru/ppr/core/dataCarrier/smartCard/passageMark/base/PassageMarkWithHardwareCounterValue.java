package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода с показаниями аппаратного счетчика использования карты.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithHardwareCounterValue extends PassageMark {
    /**
     * Возвращает значение аппаратного счетчика карты при последнем входе через турникет.
     *
     * @return Показания аппаратного счетчика карты.
     */
    int getHwCounterValue();
}
