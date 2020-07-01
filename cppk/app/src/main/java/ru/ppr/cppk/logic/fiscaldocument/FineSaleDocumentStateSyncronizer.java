package ru.ppr.cppk.logic.fiscaldocument;

import javax.inject.Inject;

import ru.ppr.cppk.logic.FineSaleDocument;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import rx.Single;

/**
 * Класс помощник для печати {@link FineSaleDocument} с синхронизацией чека.
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleDocumentStateSyncronizer extends DocumentStateSyncronizer<FineSaleDocument> {

    @Inject
    FineSaleDocumentStateSyncronizer(FiscalDocStateSynchronizer fiscalDocStateSynchronizer) {
        super(fiscalDocStateSynchronizer);
    }

    @Override
    protected Single<FineSaleDocument> print(FineSaleDocument document) {
        return document.print();
    }
}