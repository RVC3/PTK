package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

/**
 * Метка прохода с номерами турникетов входа/выхода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithTurnstileForInOutInTwo extends PassageMark {

    /**
     * Возвращает номер турникета на станции, через который был совершен вход.
     *
     * @return Номер турникета на станции, через который был совершен вход.
     */
    int getInTurnstileNumber();

    /**
     * Возвращает номер турникета на станции, через который был совершен выход.
     *
     * @return Номер турникета на станции, через который был совершен выход.
     */
    int getOutTurnstileNumber();
}
