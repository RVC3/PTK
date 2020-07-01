package ru.ppr.chit.ui.activity.setuser;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class SetUserViewState extends BaseMvpViewState<SetUserView> implements SetUserView {

    private boolean userNameEmptyErrorVisible;
    private String errorMessage;

    @Inject
    SetUserViewState() {

    }

    @Override
    protected void onViewAttached(SetUserView view) {
        // В будущем иначе тоасты показываются постоянно после 1 показа, когда будет нормальное отображение ошибок вернуть на место
//        view.setUserNameEmptyErrorVisible(userNameEmptyErrorVisible);
        if (errorMessage != null) {
            showError(errorMessage);
            errorMessage = null;
        }
    }

    @Override
    public void showError(String message){
        forEachView(view -> view.showError(message));
        if (!hasView()){
            errorMessage = message;
        }
    }

    @Override
    protected void onViewDetached(SetUserView view) {

    }

    @Override
    public void setUserNameEmptyErrorVisible(boolean visible) {
        this.userNameEmptyErrorVisible = visible;
        forEachView(view -> view.setUserNameEmptyErrorVisible(this.userNameEmptyErrorVisible));
    }

}
