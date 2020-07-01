package ru.ppr.chit.ui.widget.clock;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class ClockViewState extends BaseMvpViewState<ClockView> implements ClockView {

    private Date date = new Date();

    @Inject
    ClockViewState() {

    }

    @Override
    protected void onViewAttached(ClockView view) {
        view.setDate(date);
    }

    @Override
    protected void onViewDetached(ClockView view) {

    }

    @Override
    public void setDate(Date date) {
        this.date = date;
        forEachView(view -> view.setDate(this.date));
    }

}
