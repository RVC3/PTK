package ru.ppr.cppk.export.model;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.model.CashRegister;
import ru.ppr.cppk.sync.kpp.model.Cashier;

/**
 * Created by григорий on 14.07.2016.
 */
public class PtkShiftSummary {

    public long deviceId;

    public int shiftNumber;

    public Date openDate;

    public Cashier cashier;

    public CashRegister cashRegister;

    public boolean isOpened;

    public SalesSum cashSum;

    public SalesSum cashlessSum;

    @Override
    public String toString() {
        return "PtkShiftSummary{" +
                "deviceId=" + deviceId +
                ", shiftNumber=" + shiftNumber +
                ", openDate=" + openDate +
                ", cashier=" + cashier +
                ", cashRegister=" + cashRegister +
                ", isOpened=" + isOpened +
                ", cashSum=" + cashSum +
                ", cashlessSum=" + cashlessSum +
                '}';
    }
}
