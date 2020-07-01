package ru.ppr.chit.ui.widget.regbroken;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.api.auth.AuthorizationState;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.domain.model.local.AppRuntimeProperty;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class RegBrokenPresenter extends BaseMvpViewStatePresenter<RegBrokenView, RegBrokenViewState> {

    private boolean initialized;

    private final RegistrationInformant registrationInformant;
    private final AppRuntimeProperty appRuntimeProperty;
    private Disposable initializeDisposable = Disposables.disposed();

    @Inject
    RegBrokenPresenter(RegBrokenViewState regBrokenViewState,
                       RegistrationInformant registrationInformant,
                       AppRuntimeProperty appRuntimeProperty) {
        super(regBrokenViewState);
        this.registrationInformant = registrationInformant;
        this.appRuntimeProperty = appRuntimeProperty;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        initializeDisposable = registrationInformant
                .getAuthorizationStatePublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authorizationState -> {
                    view.setIndicatorVisible(authorizationState != AuthorizationState.AUTHORIZED);

                    if (checkBrokenRegistrationError(authorizationState)) {
                        view.showConnectionBrokenError();
                    }
                });
    }

    @Override
    public void destroy() {
        initializeDisposable.dispose();
        super.destroy();
    }

    // проверяет необходжимость показа ошибки авторизации на базовой станции
    private boolean checkBrokenRegistrationError(AuthorizationState authorizationState) {
        boolean hasBrokenRegistrationError = false;
        synchronized (appRuntimeProperty) {
            boolean isRegistrationBrokenShown = appRuntimeProperty.isRegistrationBrokenShown();
            // Показываем ошибку регистрации на БС только один раз
            if (!isRegistrationBrokenShown && authorizationState == AuthorizationState.AUTHORIZED_BROKEN) {
                appRuntimeProperty.setRegistrationBrokenShown(true);
                hasBrokenRegistrationError = true;
            } else
                // Если ошибка регистрации уже была показана, то сбрасываем этот флаг после первой же успешной регистрации
                if (isRegistrationBrokenShown && authorizationState == AuthorizationState.AUTHORIZED) {
                    appRuntimeProperty.setRegistrationBrokenShown(true);
                }
        }
        return hasBrokenRegistrationError;
    }

}
