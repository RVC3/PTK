package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;

/**
 * Метка прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMark {
    /**
     * Возвращает версию метки прохода.
     *
     * @return Версия метки прохода.
     */
    PassageMarkVersion getVersion();

    /**
     * Возвращает размер метки прохода в байтах.
     *
     * @return Размер метки прохода в байтах.
     */
    int getSize();

    /**
     * Возвращает код станции прохода.
     *
     * @return Код станции прохода, в соответствии с НСИ
     */
    long getPassageStationCode();

    void setPassageStationCode(long passageStationCode);
}
