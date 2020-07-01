package ru.ppr.ikkm.file.state.model;

import java.util.Date;
import java.util.List;

/**
 * Смена принтера
 * Created by Артем on 21.01.2016.
 */
public class Shift {
    private Long id = null;
    private int shiftNumber;
    private Date openShiftTime;
    private Date closeShiftTime;
    private ShiftState shiftState;
    private List<Check> checks;
    private Operator cashier; // при изменении оператора в рамках смены просто перезатираем его

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public Date getOpenShiftTime() {
        return openShiftTime;
    }

    public void setOpenShiftTime(Date openShiftTime) {
        this.openShiftTime = openShiftTime;
    }

    public Date getCloseShiftTime() {
        return closeShiftTime;
    }

    public void setCloseShiftTime(Date closeShiftTime) {
        this.closeShiftTime = closeShiftTime;
    }

    public ShiftState getShiftState() {
        return shiftState;
    }

    public void setShiftState(ShiftState shiftState) {
        this.shiftState = shiftState;
    }

    public List<Check> getChecks() {
        return checks;
    }

    public void setChecks(List<Check> checks) {
        this.checks = checks;
    }

    public Operator getCashier() {
        return cashier;
    }

    public void setCashier(Operator cashier) {
        this.cashier = cashier;
    }
}
