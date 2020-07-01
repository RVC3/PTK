package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода с показаниями счетчика использования карты.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithUsageCounterValue extends PassageMark {
    /**
     * Возвращает значение счетчика использования карты.
     *
     * @return Показания счетчика использования карты.
     */
    int getUsageCounterValue();


    void setUsageCounterValue(int usageCounterValue);
}
