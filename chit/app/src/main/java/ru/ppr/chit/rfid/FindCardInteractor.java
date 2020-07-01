package ru.ppr.chit.rfid;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.ppr.core.dataCarrier.findcardtask.CardNotFoundException;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;

/**
 * Операция поиска смарт-карты.
 *
 * @author Aleksandr Brazhkin
 */
public class FindCardInteractor {

    private final FindCardTaskFactory findCardTaskFactory;

    @Inject
    FindCardInteractor(FindCardTaskFactory findCardTaskFactory) {
        this.findCardTaskFactory = findCardTaskFactory;
    }

    /**
     * Выполняет поиск карты
     */
    public Single<CardReader> findCard() {
        return Single.create(e -> {
            FindCardTask findCardTask = findCardTaskFactory.create();
            e.setDisposable(new Disposable() {

                private boolean disposed;

                @Override
                public void dispose() {
                    findCardTask.cancel();
                    disposed = true;
                }

                @Override
                public boolean isDisposed() {
                    return disposed;
                }
            });

            CardReader cardReader = findCardTask.find();

            if (cardReader == null) {
                if (!e.isDisposed()) {
                    e.onError(new CardNotFoundException());
                }
            } else {
                e.onSuccess(cardReader);
            }
        });
    }
}
