package ru.ppr.chit.ui.activity.passengerlist;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class PassengerListViewState extends BaseMvpViewState<PassengerListView> implements PassengerListView {

    @Inject
    PassengerListViewState() {
    }

    @Override
    protected void onViewAttached(PassengerListView view) {
        view.reloadPassengers();
    }

    @Override
    protected void onViewDetached(PassengerListView view) {

    }

    @Override
    public void reloadPassengers(){
        forEachView(view -> view.reloadPassengers());
    }

    @Override
    public void updatePassengers(List<PassengerInfo> passengers, int positionFrom, boolean hasMoreData){
        forEachView(view -> view.updatePassengers(passengers, positionFrom, hasMoreData));
    }
}