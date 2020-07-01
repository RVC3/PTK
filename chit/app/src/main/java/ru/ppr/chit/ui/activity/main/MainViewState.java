package ru.ppr.chit.ui.activity.main;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class MainViewState extends BaseMvpViewState<MainView> implements MainView {

    private boolean startBoardingBtnVisible;
    private boolean endBoardingBtnVisible;
    private boolean endTripServiceBtnVisible;
    private boolean passengerListBtnEnabled;
    private String errorMessage;

    @Inject
    MainViewState() {

    }

    @Override
    protected void onViewAttached(MainView view) {
        view.setStartBoardingBtnVisible(startBoardingBtnVisible);
        view.setEndBoardingBtnVisible(endBoardingBtnVisible);
        view.setEndTripServiceBtnVisible(endTripServiceBtnVisible);
        view.setPassengerListBtnEnabled(passengerListBtnEnabled);

        if (errorMessage != null) {
            showError(errorMessage);
            errorMessage = null;
        }
    }

    @Override
    protected void onViewDetached(MainView view) {

    }

    @Override
    public void setStartBoardingBtnVisible(boolean visible) {
        this.startBoardingBtnVisible = visible;
        forEachView(view -> view.setStartBoardingBtnVisible(this.startBoardingBtnVisible));
    }

    @Override
    public void setEndBoardingBtnVisible(boolean visible) {
        this.endBoardingBtnVisible = visible;
        forEachView(view -> view.setEndBoardingBtnVisible(this.endBoardingBtnVisible));
    }

    @Override
    public void setEndTripServiceBtnVisible(boolean visible) {
        this.endTripServiceBtnVisible = visible;
        forEachView(view -> view.setEndTripServiceBtnVisible(this.endTripServiceBtnVisible));
    }

    @Override
    public void setPassengerListBtnEnabled(boolean enabled) {
        this.passengerListBtnEnabled = enabled;
        forEachView(view -> view.setPassengerListBtnEnabled(this.passengerListBtnEnabled));
    }

    @Override
    public void showError(String message){
        forEachView(view -> view.showError(message));
        if (!hasView()){
            errorMessage = message;
        }
    }

}
