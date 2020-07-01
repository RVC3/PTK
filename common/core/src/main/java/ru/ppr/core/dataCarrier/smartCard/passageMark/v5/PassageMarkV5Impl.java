package ru.ppr.core.dataCarrier.smartCard.passageMark.v5;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMark;
import ru.ppr.logger.Logger;

/**
 * Метка прохода v.5.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV5Impl extends BasePassageMark implements PassageMarkV5 {

    private static final String TAG = Logger.makeLogTag(PassageMarkV5Impl.class);

    /**
     * Флаг прохода по ПД №1
     */
    @PassageStatus
    private int passageStatusForPd1;
    /**
     * Флаг прохода по ПД №2
     */
    @PassageStatus
    private int passageStatusForPd2;
    /**
     * Направление прохода по ПД №1
     */
    @PassageType
    private int passageTypeForPd1;
    /**
     * Направление прохода по ПД №2
     */
    @PassageType
    private int passageTypeForPd2;
    /**
     * Показания аппаратного счетчика карты
     */
    private int hwCounterValue;
    /**
     * Количество секунд от момента оформления ПД №1 до момента последнего прохода через турникет по ПД №1
     */
    private int pd1PassageTime;
    /**
     * Количество секунд от момента оформления ПД №2 до момента последнего прохода через турникет по ПД №2
     */
    private int pd2PassageTime;
    /**
     * Номер турникета на станции, через который был совершен проход по ПД №1
     */
    private int pd1TurnstileNumber;
    /**
     * Номер турникета на станции, через который был совершен проход по ПД №1
     */
    private int pd2TurnstileNumber;

    public PassageMarkV5Impl() {
        super(PassageMarkVersion.V5, PassageMarkV5Structure.PASSAGE_MARK_SIZE);
    }

    @Override
    public int getPassageStatusForPd1() {
        return passageStatusForPd1;
    }

    @Override
    public void setPassageStatusForPd1(@PassageStatus int passageStatusForPd1) {
        this.passageStatusForPd1 = passageStatusForPd1;
    }

    @Override
    public int getPassageStatusForPd2() {
        return passageStatusForPd2;
    }

    @Override
    public void setPassageStatusForPd2(@PassageStatus int passageStatusForPd2) {
        this.passageStatusForPd2 = passageStatusForPd2;
    }

    @Override
    public int getPassageTypeForPd1() {
        return passageTypeForPd1;
    }

    @Override
    public void setPassageTypeForPd1(@PassageType int passageTypeForPd1) {
        this.passageTypeForPd1 = passageTypeForPd1;
    }

    @Override
    public int getPassageTypeForPd2() {
        return passageTypeForPd2;
    }

    @Override
    public void setPassageTypeForPd2(@PassageType int passageTypeForPd2) {
        this.passageTypeForPd2 = passageTypeForPd2;
    }

    @Override
    public int getHwCounterValue() {
        return hwCounterValue;
    }

    public void setHwCounterValue(int hwCounterValue) {
        this.hwCounterValue = hwCounterValue;
    }

    @Override
    public int getPd1PassageTime() {
        return pd1PassageTime;
    }

    @Override
    public void setPd1PassageTime(int pd1PassageTime) {
        this.pd1PassageTime = pd1PassageTime;
    }

    @Override
    public int getPd2PassageTime() {
        return pd2PassageTime;
    }

    @Override
    public void setPd2PassageTime(int pd2PassageTime) {
        this.pd2PassageTime = pd2PassageTime;
    }

    @Override
    public int getPd1TurnstileNumber() {
        return pd1TurnstileNumber;
    }

    @Override
    public void setPd1TurnstileNumber(int pd1TurnstileNumber) {
        this.pd1TurnstileNumber = pd1TurnstileNumber;
    }

    @Override
    public int getPd2TurnstileNumber() {
        return pd2TurnstileNumber;
    }

    @Override
    public void setPd2TurnstileNumber(int pd2TurnstileNumber) {
        this.pd2TurnstileNumber = pd2TurnstileNumber;
    }

    @Override
    public int getPdPassageTime(int pdIndex) {
        if (pdIndex == 0) {
            return pd1PassageTime;
        } else if (pdIndex == 1) {
            return pd2PassageTime;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
            return 0;
        }
    }

    public void setPdPassageTime(int pdPassageTime, int pdIndex) {
        if (pdIndex == 0) {
            this.pd1PassageTime = pdPassageTime;
        } else if (pdIndex == 1) {
            this.pd2PassageTime = pdPassageTime;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    @Override
    public int getPdTurnstileNumber(int pdIndex) {
        if (pdIndex == 0) {
            return pd1TurnstileNumber;
        } else if (pdIndex == 1) {
            return pd2TurnstileNumber;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
            return 0;
        }
    }

    public void setPdTurnstileNumber(int pdTurnstileNumber, int pdIndex) {
        if (pdIndex == 0) {
            this.pd1TurnstileNumber = pdTurnstileNumber;
        } else if (pdIndex == 1) {
            this.pd2TurnstileNumber = pdTurnstileNumber;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    @PassageStatus
    @Override
    public int getPassageStatusForPd(int pdIndex) {
        if (pdIndex == 0) {
            return passageStatusForPd1;
        } else if (pdIndex == 1) {
            return passageStatusForPd2;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
            return PASSAGE_STATUS_NO_EXISTS;
        }
    }

    public void setPassageStatusForPd(@PassageStatus int passageStatusForPd, int pdIndex) {
        if (pdIndex == 0) {
            this.passageStatusForPd1 = passageStatusForPd;
        } else if (pdIndex == 1) {
            this.passageStatusForPd2 = passageStatusForPd;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    @PassageType
    @Override
    public int getPassageTypeForPd(int pdIndex) {
        if (pdIndex == 0) {
            return passageTypeForPd1;
        } else if (pdIndex == 1) {
            return passageTypeForPd2;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
            return PASSAGE_TYPE_TO_STATION;
        }
    }

    public void setPassageTypeForPd(@PassageType int passageTypeForPd, int pdIndex) {
        if (pdIndex == 0) {
            this.passageTypeForPd1 = passageTypeForPd;
        } else if (pdIndex == 1) {
            this.passageTypeForPd2 = passageTypeForPd;
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    @Override
    public String toString() {
        return "PassageMarkV5Impl{" +
                "passageStatusForPd1=" + passageStatusForPd1 +
                ", passageStatusForPd2=" + passageStatusForPd2 +
                ", version=" + getVersion() +
                ", passageTypeForPd1=" + passageTypeForPd1 +
                ", size=" + getSize() +
                ", passageStationCode=" + getPassageStationCode() +
                ", passageTypeForPd2=" + passageTypeForPd2 +
                ", hwCounterValue=" + hwCounterValue +
                ", pd1PassageTime=" + pd1PassageTime +
                ", pd2PassageTime=" + pd2PassageTime +
                ", pd1TurnstileNumber=" + pd1TurnstileNumber +
                ", pd2TurnstileNumber=" + pd2TurnstileNumber +
                "} ";
    }
}
