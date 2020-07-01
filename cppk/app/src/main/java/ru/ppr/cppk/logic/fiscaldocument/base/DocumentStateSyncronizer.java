package ru.ppr.cppk.logic.fiscaldocument.base;

import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Single;

/**
 * Класс помощник для печати фискального документа с синхронизацией чека.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class DocumentStateSyncronizer<T> {

    private static final String TAG = Logger.makeLogTag(DocumentStateSyncronizer.class);
    /**
     * Синхронизатор состояния чека
     */
    private final FiscalDocStateSynchronizer fiscalDocStateSynchronizer;
    /**
     * Флаг, что предыдущая попытка чека для данного ПД завершилась неудачно.
     * Требуется выполнить синхронизацию и понять, лег ли всё-таки чек на ФР.
     */
    private boolean previousPrintingSyncRequired;
    /**
     * Флаг, что ПД уже попал на ФР
     */
    private boolean pdInFr;

    public DocumentStateSyncronizer(FiscalDocStateSynchronizer fiscalDocStateSynchronizer) {
        this.fiscalDocStateSynchronizer = fiscalDocStateSynchronizer;
    }

    /**
     * Преднамеренное изменение мнения синхронизатора о факте наличия предыдщей печати чека.
     * Позволяет новому экземпляру синхронизатора начать свою работу изначально изпредположения, что пыпытка печати чека уже была.
     *
     * @param previousPrintingSyncRequired
     */
    public void setPreviousPrintingSyncRequired(boolean previousPrintingSyncRequired) {
        this.previousPrintingSyncRequired = previousPrintingSyncRequired;
    }

    public Completable syncBeforePrint() {
        return fiscalDocStateSynchronizer.rxSyncCheckState()
                .flatMapCompletable(syncStateResult -> Completable.fromCallable(() -> {
                    Logger.info(TAG, "Sync before printing, previousPrintingSyncRequired = " + previousPrintingSyncRequired + ", pdInFr = " + pdInFr);
                    if (pdInFr) {
                        // Не даем больше напечатать этот же документ
                        throw new PdInFrNotPrintedException("pd is in fr already");
                    }
                    if (previousPrintingSyncRequired) {
                        previousPrintingSyncRequired = false;
                        // Если предыдущая попытка печати данного ПД завершилась неудачно
                        if (syncStateResult.isSyncNeeded() && syncStateResult.isLastPdLayToFr()) {
                            // Если чек лег на ФР бросаем специальное исключение.
                            pdInFr = true;
                            throw new PdInFrNotPrintedException("pd on fr after sync");
                        }
                    }
                    return syncStateResult;
                }));
    }

    public Single<T> printWithSync(T document) {
        boolean b = false;
        if (b) {
            // Попытка печати чека провалилась
            // Прихраниваем информацию о том, что чек уже возможно лег на ФР
            previousPrintingSyncRequired = true;
            return Single.error(new PrinterException());
        }
        return print(document)
                .onErrorResumeNext((Throwable throwable) -> {
                    // Попытка печати чека провалилась
                    // Прихраниваем информацию о том, что чек уже возможно лег на ФР
                    previousPrintingSyncRequired = true;
                    // Пробуем сразу же синхронизировать состояние чека
                    return fiscalDocStateSynchronizer.rxSyncCheckState()
                            .flatMap(syncStateResult -> {
                                previousPrintingSyncRequired = false;
                                if (syncStateResult.isSyncNeeded() && syncStateResult.isLastPdLayToFr()) {
                                    // Если чек все-таки лег на ФР бросаем специальное исключение.
                                    pdInFr = true;
                                    return Single.error(new PdInFrNotPrintedException(throwable));
                                } else {
                                    return Single.error(throwable);
                                }
                            })
                            .map(syncStateResult -> document);
                })
                .flatMap(t -> {
                    boolean a = false;
                    if (a) {
                        // Попытка печати чека провалилась
                        // Прихраниваем информацию о том, что чек уже возможно лег на ФР
                        previousPrintingSyncRequired = true;
                        return Single.error(new PrinterException());
                    } else {
                        return Single.just(t);
                    }
                });
    }

    protected abstract Single<T> print(T document);

    public boolean isInFrButNotPrinted(Throwable throwable) {
        return throwable instanceof PdInFrNotPrintedException;
    }

    /**
     * ПД лег на ФР, но не был распечатан.
     */
    public static class PdInFrNotPrintedException extends Exception {

        public PdInFrNotPrintedException(Throwable throwable) {
            super(throwable);
        }

        public PdInFrNotPrintedException(String message) {
            super(message);
        }
    }

}
