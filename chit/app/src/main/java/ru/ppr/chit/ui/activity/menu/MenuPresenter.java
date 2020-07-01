package ru.ppr.chit.ui.activity.menu;

import javax.inject.Inject;

import ru.ppr.chit.BuildConfig;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class MenuPresenter extends BaseMvpViewStatePresenter<MenuView, MenuViewState> {

    private static final String TAG = Logger.makeLogTag(MenuPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final UiThread uiThread;
    //endregion
    //region Other
    private Navigator navigator;
    //endregion

    @Inject
    MenuPresenter(MenuViewState menuViewState,
                  UiThread uiThread) {
        super(menuViewState);
        this.uiThread = uiThread;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        uiThread.post(() -> view.setRootBtnVisibility(BuildConfig.DEBUG));
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onRootBtnClicked() {
        Logger.trace(TAG, "onRootBtnClicked");
        navigator.navigateToRoot();
    }

    void onWorkingStateBtnClicked() {
        Logger.trace(TAG, "onWorkingStateBtnClicked");
        navigator.navigateToWorkingState();
    }

    interface Navigator {

        void navigateToRoot();

        void navigateToWorkingState();

    }

}
