package ru.ppr.chit.domain.model.nsi;

import java.util.Date;

/**
 * Версия НСИ
 *
 * @author Dmitry Nevolin
 */
public class Version {

    /**
     * Идентификатор версии НСИ
     */
    private int versionId;
    /**
     * Описание изменений
     */
    private String description;
    /**
     * Дата создания версии
     */
    private Date createdDateTime;
    /**
     * Дата запуска версии
     */
    private Date startingDateTime;
    /**
     * Статус версии НСИ
     */
    private Status status;
    /**
     * Флаг критичности обновления
     */
    private boolean isCriticalChange;

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getStartingDateTime() {
        return startingDateTime;
    }

    public void setStartingDateTime(Date startingDateTime) {
        this.startingDateTime = startingDateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean getIsCriticalChange() {
        return isCriticalChange;
    }

    public void setIsCriticalChange(boolean isCriticalChange) {
        this.isCriticalChange = isCriticalChange;
    }

    /**
     * Статус версии НСИ
     */
    public enum Status {

        /**
         * В работе
         */
        IN_WORK(0),
        /**
         * Готова к проверке
         */
        READY_TO_CHECK(1),
        /**
         * Готова к испытаниям
         */
        READY_TO_TEST(2),
        /**
         * Готова к распространению
         */
        READY_TO_DEPLOY(3);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status valueOf(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return null;
        }

    }

}
