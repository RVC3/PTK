package ru.ppr.cppk.logic.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;

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
        return Single
                .create((SingleSubscriber<? super CardReader> singleSubscriber) -> {
                    FindCardTask findCardTask = findCardTaskFactory.create();
                    singleSubscriber.add(new Subscription() {

                        private boolean unSubscribed;

                        @Override
                        public void unsubscribe() {
                            findCardTask.cancel();
                            unSubscribed = true;
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return unSubscribed;
                        }

                    });
                    CardReader cardReader = findCardTask.find();
                    if (cardReader == null) {
                        // NPE, чтобы быть готовыми к RxJava2:
                        // RxJava 2.x no longer accepts null values and the following will yield NullPointerException immediately or as a signal to downstream.
                        singleSubscriber.onError(new NullPointerException());
                    } else {
                        singleSubscriber.onSuccess(cardReader);
                    }
                });
    }
}
