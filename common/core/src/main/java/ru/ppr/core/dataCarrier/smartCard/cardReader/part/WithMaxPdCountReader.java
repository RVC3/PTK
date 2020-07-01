package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

/**
 * Ридер, предоставляющий информацию о максимальном количестве ПД на карте.
 * Добавлен для обратной совместимости.
 *
 * @author Aleksandr Brazhkin
 */
@Deprecated
public interface WithMaxPdCountReader {

    /**
     * Возращает максимальное количество ПД на карте.
     *
     * @return Максимальное количество ПД на карте.
     */
    @Deprecated
    int getMaxPdCount();
}
