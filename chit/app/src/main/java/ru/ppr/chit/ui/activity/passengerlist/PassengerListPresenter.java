package ru.ppr.chit.ui.activity.passengerlist;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.ui.activity.passengerlist.interactor.PassengerListLoader;
import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class PassengerListPresenter extends BaseMvpViewStatePresenter<PassengerListView, PassengerListViewState> {

    private static final String TAG = Logger.makeLogTag(PassengerListPresenter.class);

    private static final int PAGE_LIMIT = 50;

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final PassengerListLoader passengerListLoader;
    //endregion
    //region Other
    private Navigator navigator;
    private Disposable loadPassengersDisposable = Disposables.disposed();
    //endregion

    // последнее загруженное условие фильтра
    private String mLastFilter;
    // кол-во загруженных записей
    private int mRecordCount;

    @Inject
    PassengerListPresenter(PassengerListViewState passengerListViewState, PassengerListLoader passengerListLoader) {
        super(passengerListViewState);
        this.passengerListLoader = passengerListLoader;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
    }

    void onPassengerItemClicked(PassengerInfo passengerInfo) {
        if (passengerInfo != null) {
            Logger.trace(TAG, "onPassengerItemClicked(passengerInfo): " + passengerInfo.getFio() + " [" + passengerInfo.getDocumentNumber() + "]");
            navigator.navigateToTicketControl(passengerInfo.getTicketId());
        }
    }

    void onQueryTextChanged(String queryText) {
        Logger.trace(TAG, "onQueryTextChanged(queryText): " + queryText);
        uploadList(queryText, 0, PAGE_LIMIT);
    }

    // Перегружает текущий список
    public void reloadList() {
        // В случае перегрузхки данных пытаемся загрузить на 1 запись больше, чтобы в случае перегрузки полностью загруженного списка сразу вычислился флаг hasMoreData
        uploadList(mLastFilter, 0, mRecordCount > PAGE_LIMIT ? mRecordCount + 1 : PAGE_LIMIT );
    }

    // Подгружает в список следующие PAGE_LIMIT записи
    public void uploadList(final int recordsOffset) {
        uploadList(mLastFilter, recordsOffset, PAGE_LIMIT);
    }

    // Загружает следующие recordsOffset + 1 записи, количество подгружаемых записей = recordsLimit
    private void uploadList(final @Nullable String filter, final int recordsOffset, final int recordsLimit) {
        loadPassengersDisposable.dispose();
        loadPassengersDisposable = Single
                .fromCallable(() -> passengerListLoader.load(filter, recordsOffset, recordsLimit))
                .subscribeOn(AppSchedulers.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passengers -> {
                    mLastFilter = filter;
                    mRecordCount = recordsOffset + passengers.size();
                    boolean hasMoreData = passengers.size() >= recordsLimit;

                    view.updatePassengers(passengers, recordsOffset, hasMoreData);
                }, throwable -> Logger.error(TAG, throwable));
    }

    @Override
    public void destroy() {
        super.destroy();
        loadPassengersDisposable.dispose();
    }

    interface Navigator {
        void navigateToTicketControl(long ticketId);
    }
}
