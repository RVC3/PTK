package ru.ppr.cppk.logic;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.logic.pdRepeal.PdRepealData;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * Фабрика сущностей {@link PdRepealDocument}.
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealDocumentFactory {

    private final FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder;
    private final DocumentNumberProvider documentNumberProvider;
    private final OperationFactory operationFactory;
    private final LocalDaoSession localDaoSession;
    private final NsiDaoSession nsiDaoSession;
    private final EventBuilder eventBuilder;
    private final PaperUsageCounter paperUsageCounter;
    private final TariffRepository tariffRepository;

    @Inject
    PdRepealDocumentFactory(
            FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder,
            DocumentNumberProvider documentNumberProvider,
            OperationFactory operationFactory,
            LocalDaoSession localDaoSession,
            NsiDaoSession nsiDaoSession,
            EventBuilder eventBuilder,
            PaperUsageCounter paperUsageCounter,
            TariffRepository tariffRepository) {
        this.fiscalHeaderParamsBuilder = fiscalHeaderParamsBuilder;
        this.documentNumberProvider = documentNumberProvider;
        this.operationFactory = operationFactory;
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
        this.eventBuilder = eventBuilder;
        this.paperUsageCounter = paperUsageCounter;
        this.tariffRepository = tariffRepository;
    }

    public PdRepealDocument create(PdRepealData pdRepealData) {
        return new PdRepealDocument(pdRepealData,
                fiscalHeaderParamsBuilder,
                documentNumberProvider,
                operationFactory, localDaoSession,
                nsiDaoSession,
                eventBuilder,
                paperUsageCounter,
                tariffRepository);
    }
}
