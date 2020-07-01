package ru.ppr.ikkm.file.state.model;

import android.util.SparseArray;

import java.util.Collections;
import java.util.List;

/**
 * Настройки принетра
 * Created by Артем on 21.01.2016.
 */
public class PrinterSettings {
    private final long id;
    private final String model; // модель принетра
    private List<String> headerLines; // смена только при закрытой смене, 5 строк по 39 символов
    private String serialNumber; // серийный номер принтера
    private int shiftNumber; //счетчик для текущего номера смены
    private int checkNumber; //счетчик для хранения
    private SparseArray<Integer> vatTable; // таблица налоговых ставок
    private String inn;
    private String registerNumber; // в чеке печатается как РН
    private String eklzNumber;
    private long odometerValue;
    private long availableShifts;
    private long availableDocs;

    public PrinterSettings(long id, String model){
        this.id = id;
        this.model = model;
        headerLines = Collections.emptyList();
        serialNumber = "12345";
        shiftNumber = 0;
        checkNumber = 0;
        vatTable = new SparseArray<>(0);
        inn = "0123456789";
        registerNumber = "88888888";
        odometerValue = 0;
        availableShifts = 40000; // начальное значение
        availableDocs = 40000; // начальное значение
        eklzNumber = "77777";
    }

    public long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getEklzNumber() {
        return eklzNumber;
    }

    public void setEklzNumber(String eklzNumber) {
        this.eklzNumber = eklzNumber;
    }

    public long getOdometerValue() {
        return odometerValue;
    }

    public void setOdometerValue(long odometerValue) {
        this.odometerValue = odometerValue;
    }

    public long getAvailableShifts() {
        return availableShifts;
    }

    public void setAvailableShifts(long availableShifts) {
        this.availableShifts = availableShifts;
    }

    public long getAvailableDocs() {
        return availableDocs;
    }

    public void setAvailableDocs(long availableDocs) {
        this.availableDocs = availableDocs;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public int getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(int checkNumber) {
        this.checkNumber = checkNumber;
    }

    public List<String> getHeaderLines() {
        return headerLines;
    }

    public void setHeaderLines(List<String> headerLines) {
        this.headerLines = headerLines;
    }

    public SparseArray<Integer> getVatTable() {
        return vatTable;
    }

    public void setVatTable(SparseArray<Integer> vatTable) {
        this.vatTable = vatTable;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrinterSettings that = (PrinterSettings) o;

        if (id != that.id) return false;
        if (shiftNumber != that.shiftNumber) return false;
        if (checkNumber != that.checkNumber) return false;
        if (odometerValue != that.odometerValue) return false;
        if (availableShifts != that.availableShifts) return false;
        if (availableDocs != that.availableDocs) return false;
        if (!model.equals(that.model)) return false;
        if (!headerLines.equals(that.headerLines)) return false;
        if (!serialNumber.equals(that.serialNumber)) return false;
        if (!inn.equals(that.inn)) return false;
        if (!registerNumber.equals(that.registerNumber)) return false;
        return eklzNumber.equals(that.eklzNumber);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + model.hashCode();
        result = 31 * result + headerLines.hashCode();
        result = 31 * result + serialNumber.hashCode();
        result = 31 * result + shiftNumber;
        result = 31 * result + checkNumber;
        result = 31 * result + inn.hashCode();
        result = 31 * result + registerNumber.hashCode();
        result = 31 * result + eklzNumber.hashCode();
        result = 31 * result + (int) (odometerValue ^ (odometerValue >>> 32));
        result = 31 * result + (int) (availableShifts ^ (availableShifts >>> 32));
        result = 31 * result + (int) (availableDocs ^ (availableDocs >>> 32));
        return result;
    }
}
