package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.entity.Version;
import ru.ppr.security.SecurityDaoSession;

/**
 * Класс, выполняющий сборку {@link UpdateEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class UpdateEventCreator {

    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;
    private final Provider<NsiVersionManager> nsiVersionManagerProvider;
    private final Provider<SecurityDaoSession> securityDaoSessionProvider;

    private UpdateEventType type;

    @Inject
    UpdateEventCreator(LocalDaoSession localDaoSession,
                       LocalDbTransaction localDbTransaction,
                       Provider<NsiVersionManager> nsiVersionManagerProvider,
                       Provider<SecurityDaoSession> securityDaoSessionProvider) {
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
        this.nsiVersionManagerProvider = nsiVersionManagerProvider;
        this.securityDaoSessionProvider = securityDaoSessionProvider;
    }

    public UpdateEventCreator setType(UpdateEventType type) {
        this.type = type;
        return this;
    }

    /**
     * Выполнят сборку {@link UpdateEvent} и запись его в БД.
     *
     * @return Сформированный {@link UpdateEvent}
     */
    @NonNull
    public UpdateEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private UpdateEvent createInternal() {
        Preconditions.checkNotNull(type);

        Date operationTime = new Date();

        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.setType(type);
        updateEvent.setOperationTime(operationTime);
        updateEvent.setVersion(getVersion(type, operationTime));
        updateEvent.setDataContractVersion(BuildConfig.DATA_CONTRACTS_VERSION);

        // Пишем в БД UpdateEvent
        localDaoSession.getUpdateEventDao().insertOrThrow(updateEvent);
        return updateEvent;
    }

    private String getVersion(@NonNull UpdateEventType type, @NonNull Date operationTime) {
        switch (type) {
            case SW:
                return getSwVersion();
            case NSI:
                return getNsiVersion();
            case SECURITY:
                return getSecurityVersion();
            case STOP_LISTS:
                return getStopListsVersion(operationTime);
            case ALL:
                return getFullSyncVersion(operationTime);
            default:
                throw new IllegalArgumentException("Unsupported update event type = " + type);
        }
    }

    private String getSwVersion() {
        return BuildConfig.VERSION_NAME;
    }

    private String getNsiVersion() {
        Version version = nsiVersionManagerProvider.get().getCurrentNsiVersion();
        return version == null ? null : String.valueOf(version.getVersionId());
    }

    private String getSecurityVersion() {
        return String.valueOf(securityDaoSessionProvider.get().getSecurityDataVersionDao().getSecurityVersion());
    }

    private String getStopListsVersion(@NonNull Date operationTime) {
        return formatDate(operationTime);
    }

    private String getFullSyncVersion(@NonNull Date operationTime) {
        return formatDate(operationTime);
    }

    private String formatDate(@NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(date);
    }
}
