package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

/**
 * Итоги смены
 *
 * @author Grigoriy Kashka
 */
public class ShiftClosureStatistics extends ClosureStatistics {
    /**
     * Всего количество событий за смену
     */
    public int totalEventsCount;

    /**
     * Номер первого документа за смену
     */
    public int firstDocumentNumber;

    /**
     * Номер последнего документа за смену
     */
    public int lastDocumentNumber;

    /**
     * Расход билетной ленты за текущую смену, миллиметры
     */
    public int currentTapeLengthInMillimeters;
}
