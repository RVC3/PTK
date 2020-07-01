package ru.ppr.security.entity;

/**
 * Элемент списка в стоп-листе карт.
 */
public class SmartCardStopListItem {

    private String crystalSerialNumber = null;
    private String outerNumber = null;
    /**
     * Код причины постановки в стоплист
     */
    private int reasonCode = 0;
    /**
     *  Критерий типизации стоп листов
     */
    private StopCriteriaType stopCriteriaType = null;

    public SmartCardStopListItem() {

    }

    public String getCrystalSerialNumber() {
        return crystalSerialNumber;
    }

    public void setCrystalSerialNumber(String serialNumber) {
        this.crystalSerialNumber = serialNumber;
    }

    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    public int getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public StopCriteriaType getStopCriteriaType() {
        return stopCriteriaType;
    }

    public void setStopCriteriaType(StopCriteriaType stopCriteriaType) {
        this.stopCriteriaType = stopCriteriaType;
    }
}
