package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.helpers.UserSessionInfo;

/**
 * Билдер сущности {@link LogEvent}.
 *
 * @author Aleksandr Brazhkin
 */
public class LogEventBuilder {

    private final UserSessionInfo userSessionInfo;

    private LogActionType logActionType;
    private String message;
    private LocalUser user;

    @Inject
    LogEventBuilder(UserSessionInfo userSessionInfo) {
        this.userSessionInfo = userSessionInfo;
    }

    public LogEventBuilder setLogActionType(LogActionType logActionType) {
        this.logActionType = logActionType;
        return this;
    }

    public LogEventBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public LogEventBuilder setUser(LocalUser user) {
        this.user = user;
        return this;
    }

    @NonNull
    public LogEvent build() {
        Preconditions.checkNotNull(logActionType);

        LogEvent logEvent = new LogEvent();
        logEvent.setCreationTimestamp(new Date());
        LocalUser user = this.user == null ? userSessionInfo.getCurrentUser() : this.user;
        logEvent.setUserName(user.getName());
        logEvent.setSecurityCardUid(user.getCardUid());
        logEvent.setActionType(logActionType);
        logEvent.setMessage(message == null ? logActionType.getDescription() : message);
        logEvent.setMessageType(logActionType.getMessageType());
        return logEvent;
    }
}
