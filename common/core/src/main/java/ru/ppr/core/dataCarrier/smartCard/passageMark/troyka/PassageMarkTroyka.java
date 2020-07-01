package ru.ppr.core.dataCarrier.smartCard.passageMark.troyka;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Метка прохода для тройки и стрелки.
 *
 * @author isedoi
 */
public interface PassageMarkTroyka extends PassageMark {
    /**
     * Код станции выхода
     * @return
     */
    int getOutComeStation();

    /**
     * Валидность имитовставки
     * @return
     */
    boolean isValidImitovstavka();

    /**
     * Тип поезда
     * @return
     */
    int getTrainType();
    /**
     * Тип билета
     * @return
     */
    int getTicketType();
    /**
     * Тип метки
     * @return
     */
    int getPassMarkType();
    /**
     * Номер турникета прохода
     * @return
     */
    int getTurniketNumber();

    /**
     * Время прохода турникета в формате dd.MM.yyyy HH:mm
     * @return
     */
    String getIntersectionTimeFormatted();

    /**
     * Время прохода турникета
     * @return
     */
    long getIntersectionLongTime();

    /**
     *  Проверяем станцию назначения на 11111   (станции назначения нет)
     */
    boolean isCheckExitStation();
}
