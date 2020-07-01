package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода с номерами турникетов прохода по ПД №1 и ПД №2.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithTurnstileNumber extends PassageMark {

    /**
     * Возвращает номер турникета на станции, через который был совершен проход по ПД.
     *
     * @param pdIndex Номер ПД, с которым ассоциирован номер турникета
     * @return Номер турникета на станции, через который был совершен проход по ПД.
     */
    int getPdTurnstileNumber(int pdIndex);
}
