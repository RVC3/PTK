package ru.ppr.edssft.model;

import java.util.Date;

/**
 * Результат запроса информации по ключу подписи.
 *
 * @author Aleksandr Brazhkin
 */
public class GetKeyInfoResult {

    /**
     * Флаг успешности выполнения операции
     */
    private boolean successful;
    /**
     * Описание результата
     */
    private String description;
    /**
     * Номер ключа подписи
     */
    private long edsKeyNumber;
    /**
     * Id устройства создавшего подпись
     */
    private long deviceId;
    /**
     * Дата начала действия ключа ЭЦП
     */
    private Date effectiveDate;
    /**
     * Дата окончания действия ключа ЭЦП
     */
    private Date expireDate;
    /**
     * Дата отзыва ключа ЭЦП
     */
    private Date dateOfRevocation;

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Date getDateOfRevocation() {
        return dateOfRevocation;
    }

    public void setDateOfRevocation(Date dateOfRevocation) {
        this.dateOfRevocation = dateOfRevocation;
    }

    @Override
    public String toString() {
        return "GetKeyInfoResult{" +
                "successful=" + successful +
                ", description='" + description + '\'' +
                ", edsKeyNumber=" + edsKeyNumber +
                ", deviceId=" + deviceId +
                ", effectiveDate=" + effectiveDate +
                ", expireDate=" + expireDate +
                ", dateOfRevocation=" + dateOfRevocation +
                '}';
    }
}
