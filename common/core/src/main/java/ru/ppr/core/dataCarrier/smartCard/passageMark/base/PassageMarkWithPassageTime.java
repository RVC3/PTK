package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода со временем прохода по ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithPassageTime extends PassageMark {

    /**
     * Возвращает количество секунд от момента оформления ПД до момента последнего прохода через турникет по ПД.
     *
     * @param pdIndex Номер ПД, с которым ассоциировано время прохода
     * @return Количество секунд от момента оформления ПД до момента последнего прохода через турникет по ПД.
     */
    int getPdPassageTime(int pdIndex);
}
