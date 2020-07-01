package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

public class LogEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Время события
     */
    private Date creationTimestamp;
    /**
     * Имя пользователя
     */
    private String userName;
    /**
     * Тип действия
     */
    private LogActionType actionType;
    /**
     * Тип сообщения
     */
    private LogMessageType messageType;
    /**
     * UID карты авторизации
     */
    private String securityCardUid;
    /**
     * Текст сообщения
     */
    private String message;

    public LogEvent() {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LogActionType getActionType() {
        return actionType;
    }

    public void setActionType(LogActionType actionType) {
        this.actionType = actionType;
    }

    public LogMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(LogMessageType messageType) {
        this.messageType = messageType;
    }

    public String getSecurityCardUid() {
        return securityCardUid;
    }

    public void setSecurityCardUid(String securityCardUid) {
        this.securityCardUid = securityCardUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
