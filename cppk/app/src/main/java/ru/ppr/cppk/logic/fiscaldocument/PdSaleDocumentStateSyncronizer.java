package ru.ppr.cppk.logic.fiscaldocument;

import javax.inject.Inject;

import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import rx.Single;

/**
 * Класс помощник для печати {@link DocumentSalePd} с синхронизацией чека.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleDocumentStateSyncronizer extends DocumentStateSyncronizer<DocumentSalePd> {

    @Inject
    PdSaleDocumentStateSyncronizer(FiscalDocStateSynchronizer fiscalDocStateSynchronizer) {
        super(fiscalDocStateSynchronizer);
    }

    @Override
    protected Single<DocumentSalePd> print(DocumentSalePd document) {
        return document.print();
    }
}
