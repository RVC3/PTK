package ru.ppr.cppk.logic.interactor;

import javax.inject.Inject;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.fiscaldocument.TestPdSaleDocumentStateSyncronizer;
import rx.Completable;

/**
 * Класс помошник, умеющий печатать тестовый ПД
 *
 * @author Grigoriy Kashka
 */
public class PrintTestCheckInteractor {

    private final TestPdSaleDocumentStateSyncronizer testPdSaleDocumentStateSyncronizer;

    @Inject
    public PrintTestCheckInteractor(TestPdSaleDocumentStateSyncronizer testPdSaleDocumentStateSyncronizer) {
        this.testPdSaleDocumentStateSyncronizer = testPdSaleDocumentStateSyncronizer;
    }

    /**
     * Выполянет печать нового чека
     */
    public Completable print() {

        DocumentTestPd documentTestPd = Dagger.appComponent().testPdSaleDocumentFactory().create();

        return Completable
                // Создаем событие в БД со статусом CREATED
                .fromAction(documentTestPd::createTestSaleEvent)
                // Создаем событие в БД до статуса PRE_PRINTING
                .doOnCompleted(documentTestPd::updateStatusPrePrinting)
                // Печатем ПД
                .andThen(testPdSaleDocumentStateSyncronizer.printWithSync(documentTestPd))
                .toCompletable()
                // Обновляем событие в БД до статуса CHECK_PRINTED
                .doOnCompleted(documentTestPd::updateStatusPrinted)
                // Обновляем событие в БД до статуса COMPLETED
                .doOnCompleted(documentTestPd::updateStatusCompleted);
    }
}
