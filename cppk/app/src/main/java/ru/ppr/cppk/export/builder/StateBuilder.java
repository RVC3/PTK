package ru.ppr.cppk.export.builder;

import android.content.Context;
import android.content.pm.PackageManager;

import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.db.nsi.NsiDbOperations;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.model.State;
import ru.ppr.cppk.logic.NsiDataContractsVersionChecker;
import ru.ppr.cppk.logic.SecurityDataContractsVersionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.security.SecurityDaoSession;

/**
 * Билдит данные для файла State.bin
 *
 * @author Grigoriy Kashka
 */
public class StateBuilder {

    private static final String TAG = Logger.makeLogTag(StateBuilder.class);

    private int sftState;

    private final Context context;
    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final SecurityDaoSession securityDaoSession;
    private final PrivateSettings privateSettings;
    private final ApplicationInfo applicationInfo;
    private final SecurityDataContractsVersionChecker securityDataContractsVersionChecker;
    private final NsiDataContractsVersionChecker nsiDataContractsVersionChecker;
    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;


    public StateBuilder(Context context,
                        LocalDaoSession localDaoSession,
                        NsiVersionManager nsiVersionManager,
                        SecurityDaoSession securityDaoSession,
                        PrivateSettings privateSettings,
                        ApplicationInfo applicationInfo,
                        SecurityDataContractsVersionChecker securityDataContractsVersionChecker,
                        NsiDataContractsVersionChecker nsiDataContractsVersionChecker,
                        ServiceTicketControlEventRepository serviceTicketControlEventRepository) {
        this.context = context;
        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.securityDaoSession = securityDaoSession;
        this.privateSettings = privateSettings;
        this.applicationInfo = applicationInfo;
        this.securityDataContractsVersionChecker = securityDataContractsVersionChecker;
        this.nsiDataContractsVersionChecker = nsiDataContractsVersionChecker;
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
    }

    public StateBuilder setSftState(int sftState) {
        this.sftState = sftState;
        return this;
    }


    public State build() throws PackageManager.NameNotFoundException {

        boolean isNsiDbDataContractVersionValid = nsiDataContractsVersionChecker.isDataContractVersionValid();
        boolean isSecurityDbDataContractVersionValid = securityDataContractsVersionChecker.isDataContractVersionValid();

        State state = new State();

        state.terminalType = State.PtkType;
        state.id = String.valueOf(privateSettings.getTerminalNumber());
        state.softwareVersion = applicationInfo.getVersionName();
        state.dataContracts = BuildConfig.DATA_CONTRACTS_VERSION;
        state.rdsVersion = isNsiDbDataContractVersionValid ? nsiVersionManager.getMaxNsiVersionId() : -2;
        state.securityVersion = isSecurityDbDataContractVersionValid ? securityDaoSession.getSecurityDataVersionDao().getSecurityVersion() : null;
        state.sftInKeysVersion = NsiDbOperations.getOpenKeysPackageVersionString(context);
        state.ticketStopListItem = securityDaoSession.getSecurityStopListVersionDao().getTicketStopListItemVersion();
        state.ticketWhitelistItem = securityDaoSession.getSecurityStopListVersionDao().getTicketWhiteListItemVersion();
        state.smartCardStopListItem = securityDaoSession.getSecurityStopListVersionDao().getSmartCardStoplistItemVersion();
        state.ptkSftState = sftState;
        state.latestEventTimestamp = localDaoSession.getEventDao().getLastEventTimeStamp();
        state.lastTimestampsForEvent = new LastTimestampEventsBuilder(localDaoSession, serviceTicketControlEventRepository).build();
        state.lastTimestampsForSentEvent = new LastTimestampSentEventsBuilder(localDaoSession).build();

        return state;
    }
}
