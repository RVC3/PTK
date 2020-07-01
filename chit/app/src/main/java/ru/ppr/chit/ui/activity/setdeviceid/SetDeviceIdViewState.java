package ru.ppr.chit.ui.activity.setdeviceid;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
public class SetDeviceIdViewState extends BaseMvpViewState<SetDeviceIdView> implements SetDeviceIdView {

    private boolean invalidDataErrorVisible;

    @Inject
    SetDeviceIdViewState() {
    }

    @Override
    protected void onViewAttached(SetDeviceIdView view) {
        view.setInvalidDataErrorVisible(this.invalidDataErrorVisible);
    }

    @Override
    protected void onViewDetached(SetDeviceIdView view) {

    }

    @Override
    public void setInvalidDataErrorVisible(boolean visible) {
        this.invalidDataErrorVisible = visible;
        forEachView(view -> view.setInvalidDataErrorVisible(this.invalidDataErrorVisible));
    }
}