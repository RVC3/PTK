package ru.ppr.core.manager.eds;

import java.util.Date;

/**
 * Результат проверки подписи
 *
 * @author Grigoriy Kashka
 */
public class CheckSignResult {

    /**
     * Состояние после проверки
     */
    private CheckSignResultState state = CheckSignResultState.INVALID;
    /**
     * Идентификатор устрройства создавшего подпись
     */
    private long deviceId;
    /**
     * Описание ошибки
     */
    private String description;
    /**
     * Дата отзыва ключа ЭЦП
     */
    private Date dateOfRevocation;

    public CheckSignResultState getState() {
        return state;
    }

    public void setState(CheckSignResultState state) {
        this.state = state;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateOfRevocation() {
        return dateOfRevocation;
    }

    public void setDateOfRevocation(Date dateOfRevocation) {
        this.dateOfRevocation = dateOfRevocation;
    }

    @Override
    public String toString() {
        return "CheckSignResult{" +
                "state=" + state +
                ", deviceId=" + deviceId +
                ", description='" + description + '\'' +
                ", dateOfRevocation=" + dateOfRevocation +
                '}';
    }
}
