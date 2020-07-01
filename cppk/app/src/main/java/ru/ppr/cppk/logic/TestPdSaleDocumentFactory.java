package ru.ppr.cppk.logic;

import javax.inject.Inject;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.nsi.repository.StationRepository;

/**
 * Фабрика сущностей {@link DocumentTestPd}.
 *
 * @author Grigoriy Kashka
 */
public class TestPdSaleDocumentFactory {

    private final LocalDaoSession localDaoSession;
    private final ShiftManager shiftManager;
    private final EventBuilder eventBuilder;
    private final DocumentNumberProvider documentNumberProvider;
    private final PrivateSettings privateSettings;
    private final FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder;
    private final UserSessionInfo userSessionInfo;
    private final OperationFactory operationFactory;
    private final PaperUsageCounter paperUsageCounter;
    private final Holder<PrivateSettings> privateSettingsHolder;
    private final NsiVersionManager nsiVersionManager;
    private final StationRepository stationRepository;

    @Inject
    public TestPdSaleDocumentFactory(LocalDaoSession localDaoSession,
                                     ShiftManager shiftManager,
                                     EventBuilder eventBuilder,
                                     DocumentNumberProvider documentNumberProvider,
                                     PrivateSettings privateSettings,
                                     FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder,
                                     UserSessionInfo userSessionInfo,
                                     OperationFactory operationFactory,
                                     PaperUsageCounter paperUsageCounter,
                                     Holder<PrivateSettings> privateSettingsHolder,
                                     NsiVersionManager nsiVersionManager,
                                     StationRepository stationRepository) {
        this.localDaoSession = localDaoSession;
        this.shiftManager = shiftManager;
        this.eventBuilder = eventBuilder;
        this.documentNumberProvider = documentNumberProvider;
        this.privateSettings = privateSettings;
        this.fiscalHeaderParamsBuilder = fiscalHeaderParamsBuilder;
        this.userSessionInfo = userSessionInfo;
        this.operationFactory = operationFactory;
        this.paperUsageCounter = paperUsageCounter;
        this.privateSettingsHolder = privateSettingsHolder;
        this.nsiVersionManager = nsiVersionManager;
        this.stationRepository = stationRepository;
    }

    public DocumentTestPd create() {
        return new DocumentTestPd(localDaoSession,
                shiftManager,
                eventBuilder,
                documentNumberProvider,
                privateSettings,
                fiscalHeaderParamsBuilder,
                userSessionInfo,
                operationFactory,
                paperUsageCounter,
                privateSettingsHolder,
                nsiVersionManager,
                stationRepository);
    }
}
