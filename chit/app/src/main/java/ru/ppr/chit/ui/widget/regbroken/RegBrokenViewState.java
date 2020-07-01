package ru.ppr.chit.ui.widget.regbroken;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class RegBrokenViewState extends BaseMvpViewState<RegBrokenView> implements RegBrokenView {

    private boolean indicatorVisible;
    private boolean shownConnectionBrokenError;

    @Inject
    RegBrokenViewState() {

    }

    @Override
    protected void onViewAttached(RegBrokenView view) {
        view.setIndicatorVisible(indicatorVisible);
        if (shownConnectionBrokenError) {
            view.showConnectionBrokenError();
            shownConnectionBrokenError = false;
        }
    }

    @Override
    protected void onViewDetached(RegBrokenView view) {

    }

    @Override
    public void setIndicatorVisible(boolean visible) {
        this.indicatorVisible = visible;
        forEachView(view -> view.setIndicatorVisible(this.indicatorVisible));
    }

    @Override
    public void showConnectionBrokenError(){
        forEachView(view -> view.showConnectionBrokenError());
        if (!hasView()){
            shownConnectionBrokenError = true;
        }
    }

}
