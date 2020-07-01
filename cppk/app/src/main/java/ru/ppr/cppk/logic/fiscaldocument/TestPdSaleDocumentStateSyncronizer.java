package ru.ppr.cppk.logic.fiscaldocument;

import javax.inject.Inject;

import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import rx.Single;

/**
 * Класс помощник для печати {@link DocumentTestPd} с синхронизацией чека.
 *
 * @author Grigoriy Kashka
 */
public class TestPdSaleDocumentStateSyncronizer extends DocumentStateSyncronizer<DocumentTestPd> {

    @Inject
    TestPdSaleDocumentStateSyncronizer(FiscalDocStateSynchronizer fiscalDocStateSynchronizer) {
        super(fiscalDocStateSynchronizer);
    }

    @Override
    protected Single<DocumentTestPd> print(DocumentTestPd document) {
        return document.print();
    }
}