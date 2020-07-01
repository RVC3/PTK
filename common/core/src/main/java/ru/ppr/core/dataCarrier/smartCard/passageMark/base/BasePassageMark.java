package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;

/**
 * Базовый класс для метки прохода.
 *
 * @author Aleksandr Brazhkin
 */
public class  BasePassageMark implements PassageMark {

    /**
     * Версия метки прохода
     */
    private final PassageMarkVersion version;
    /**
     * Размер метки прохода в байтах
     */
    private final int size;
    /**
     * Код станции прохода
     */
    private long passageStationCode;

    public BasePassageMark(PassageMarkVersion version, int size) {
        this.version = version;
        this.size = size;
    }

    @Override
    public PassageMarkVersion getVersion() {
        return version;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getPassageStationCode() {
        return passageStationCode;
    }

    @Override
    public void setPassageStationCode(long passageStationCode) {
        this.passageStationCode = passageStationCode;
    }
}
