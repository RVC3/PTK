package ru.ppr.core.dataCarrier.smartCard.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMark;
import ru.ppr.utils.DateFormatOperations;

/**
 * Кошелек для тройки.
 * @author isedoi
 */
public class TroykaWalletImpl extends BasePassageMark implements MetroWallet {
    private BigDecimal unitsAmount;
    private long endDateTime;
    private boolean validFormatData;
    private String endDateTimeFormatted;
    private int daysEnd;//дней с 31.12.2018.
    private final Calendar calendar = GregorianCalendar.getInstance();

    TroykaWalletImpl() {
        super(PassageMarkVersion.V0, TroykaWalletStructure.SECTOR_SIZE);
    }

    void setUnits(double units) {
        unitsAmount = new BigDecimal(units);
        correctUnits();
    }

    @Override
    public boolean isValidUnitsData() {
        return validFormatData;
    }

    /**
     * Корректируем копейки
     */
    private void correctUnits() {
        unitsAmount = unitsAmount.divide(new BigDecimal(100), RoundingMode.HALF_UP);
    }

    void setFormatData(int codeFormat, int extendNumFormat){
        validFormatData = codeFormat == TroykaWalletStructure.CODING_FORMAT_VALUE && extendNumFormat == TroykaWalletStructure.EXTEND_NUM_FORMAT_VALUE;
    }

    void setDaysEnd(int daysEnd) {
        this.daysEnd = daysEnd;
        convertEndDateTime();
    }

    private void convertEndDateTime() {
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        endDateTime = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(daysEnd);
        calendar.setTimeInMillis(endDateTime);
        endDateTimeFormatted = DateFormatOperations.getDateddMMyyyyHHmm(calendar.getTime());
    }

    @Override
    public BigDecimal getUnitsLeft() {
        return unitsAmount;
    }

    @Override
    public long getDateTimeEnd() {
        return endDateTime;
    }

    @Override
    public String getDateTimeEndFormat() {
        return endDateTimeFormatted;
    }

    @Override
    public String toString() {
        return "TroykaWalletImpl{" +
                "unitsAmount=" + unitsAmount +
                ", endDateTime=" + endDateTime +
                ", validFormatData=" + validFormatData +
                ", endDateTimeFormatted='" + endDateTimeFormatted + '\'' +
                ", daysEnd=" + daysEnd +
                '}';
    }
}
