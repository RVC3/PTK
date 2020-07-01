package ru.ppr.core.dataCarrier.smartCard.passageMark.v7;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMark;

/**
 * Метка прохода v.7.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV7Impl extends BasePassageMark implements PassageMarkV7 {

    /**
     * Номер турникета на станции, через который был совершен проход
     */
    private int turnstileNumber;
    /**
     * Дата и время последнего прохода, UTC.
     */
    private int passageTime;
    /**
     * Направление прохода
     */
    @PassageType
    private int passageType;
    /**
     * Показания счетчика использования карты
     */
    private int usageCounterValue;
    /**
     * Номер зоны
     */
    private int coverageAreaNumber;


    public PassageMarkV7Impl() {
        super(PassageMarkVersion.V7, PassageMarkV7Structure.PASSAGE_MARK_SIZE);
    }

    @Override
    public int getTurnstileNumber() {
        return turnstileNumber;
    }

    public void setTurnstileNumber(int turnstileNumber) {
        this.turnstileNumber = turnstileNumber;
    }

    @Override
    public int getPassageTime() {
        return passageTime;
    }

    public void setPassageTime(int passageTime) {
        this.passageTime = passageTime;
    }

    @PassageType
    @Override
    public int getPassageType() {
        return passageType;
    }

    public void setPassageType(@PassageType int passageType) {
        this.passageType = passageType;
    }

    @Override
    public int getUsageCounterValue() {
        return usageCounterValue;
    }

    @Override
    public void setUsageCounterValue(int usageCounterValue) {
        this.usageCounterValue = usageCounterValue;
    }

    @Override
    public int getCoverageAreaNumber() {
        return coverageAreaNumber;
    }

    public void setCoverageAreaNumber(int coverageAreaNumber) {
        this.coverageAreaNumber = coverageAreaNumber;
    }

    @Override
    public String toString() {
        return "PassageMarkV7Impl{" +
                "turnstileNumber=" + turnstileNumber +
                ", passageTime=" + passageTime +
                ", passageType=" + passageType +
                ", usageCounterValue=" + usageCounterValue +
                ", coverageAreaNumber=" + coverageAreaNumber +
                ", version=" + getVersion() +
                ", size=" + getSize() +
                ", passageStationCode=" + getPassageStationCode() +
                '}';
    }
}
