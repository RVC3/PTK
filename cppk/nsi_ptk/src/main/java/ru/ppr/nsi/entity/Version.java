package ru.ppr.nsi.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Версия НСИ.
 *
 * @author Aleksandr Brazhkin
 */
public class Version {

    /**
     * Статус версии
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            InWork,
            ReadyToCheck,
            ReadyToTest,
            ReadyToDeploy
    })
    public @interface Status {
    }

    /**
     * в работе
     */
    public static final int InWork = 0;
    /**
     * готова к проверке
     */
    public static final int ReadyToCheck = 1;
    /**
     * готова к испытаниям
     */
    public static final int ReadyToTest = 2;
    /**
     * готова к распространению
     */
    public static final int ReadyToDeploy = 3;


    /**
     * Список статусов доступных на тесте
     */
    @Status
    public static int[] testStatuses = {ReadyToTest, ReadyToDeploy};

    /**
     * Список релизных статусов.
     */
    @Status
    public static int[] releaseStatuses = {ReadyToDeploy};

    /**
     * версия БД
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
    private Date startedDateTime;
    /**
     * Статус версии БД (3-релиз)
     */
    @Status
    private int status;
    /**
     * Флаг критичности обновления
     */
    private boolean isCriticalChange;

    public Version() {
    }

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

    public Date getStartedDateTime() {
        return startedDateTime;
    }

    public void setStartedDateTime(Date startedDateTime) {
        this.startedDateTime = startedDateTime;
    }

    @Status
    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }

    public boolean getIsCriticalChange() {
        return isCriticalChange;
    }

    public void setIsCriticalChange(boolean isCriticalChange) {
        this.isCriticalChange = isCriticalChange;
    }
}
