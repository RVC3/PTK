package ru.ppr.ikkm.file.state.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Артем on 24.05.2016.
 */
public class ShiftInfo {

    private final int shiftNumber;
    private final BigDecimal sumForShift;
    private final Date closeTime;

    private ShiftInfo(Builder builder) {
        shiftNumber = builder.shiftNumber;
        sumForShift = builder.summForShift;
        closeTime = builder.closeTime;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public BigDecimal getSumForShift() {
        return sumForShift;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public static final class Builder {
        private int shiftNumber;
        private BigDecimal summForShift;
        private Date closeTime;

        public Builder() {
        }

        public Builder shiftNumber(int val) {
            shiftNumber = val;
            return this;
        }

        public Builder summForShift(BigDecimal val) {
            summForShift = val;
            return this;
        }

        public Builder closeTime(Date val) {
            closeTime = val;
            return this;
        }

        public ShiftInfo build() {
            return new ShiftInfo(this);
        }
    }
}
