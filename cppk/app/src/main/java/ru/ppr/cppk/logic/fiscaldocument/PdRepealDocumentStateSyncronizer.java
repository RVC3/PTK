package ru.ppr.cppk.logic.fiscaldocument;

import javax.inject.Inject;

import ru.ppr.cppk.logic.PdRepealDocument;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import rx.Single;

/**
 * Класс помощник для печати {@link PdRepealDocument} с синхронизацией чека.
 *
 * @author Aleksandr Brazhkin
 */
public class PdRepealDocumentStateSyncronizer extends DocumentStateSyncronizer<PdRepealDocument> {

    @Inject
    PdRepealDocumentStateSyncronizer(FiscalDocStateSynchronizer fiscalDocStateSynchronizer) {
        super(fiscalDocStateSynchronizer);
    }

    @Override
    protected Single<PdRepealDocument> print(PdRepealDocument document) {
        return document.print();
    }
}