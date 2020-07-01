package ru.ppr.chit.ui.activity.menu;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class MenuViewState extends BaseMvpViewState<MenuView> implements MenuView {

    private boolean rootBtnVisible;

    @Inject
    MenuViewState() {

    }

    @Override
    protected void onViewAttached(MenuView view) {
        view.setRootBtnVisibility(rootBtnVisible);
    }

    @Override
    protected void onViewDetached(MenuView view) {

    }

    @Override
    public void setRootBtnVisibility(boolean visible) {
        this.rootBtnVisible = visible;
        forEachView(view -> view.setRootBtnVisibility(this.rootBtnVisible));
    }

}
