package ru.ppr.cppk.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.cppk.model.FineSaleData;

/**
 * Фабрика сущностей {@link FineSaleDocument}.
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleDocumentFactory {

    private final Provider<FineSaleDocument> fineSaleDocumentProvider;

    @Inject
    FineSaleDocumentFactory(Provider<FineSaleDocument> fineSaleDocumentProvider) {
        this.fineSaleDocumentProvider = fineSaleDocumentProvider;
    }

    public FineSaleDocument create(FineSaleData fineSaleData) {
        FineSaleDocument fineSaleDocument = fineSaleDocumentProvider.get();
        fineSaleDocument.setFineSaleData(fineSaleData);
        return fineSaleDocument;
    }
}
