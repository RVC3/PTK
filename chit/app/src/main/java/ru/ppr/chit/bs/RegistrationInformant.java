package ru.ppr.chit.bs;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.ppr.chit.api.auth.AuthorizationState;
import ru.ppr.chit.bs.oauth2token.OAuth2TokenManager;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.AppRuntimeProperty;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;
import ru.ppr.chit.helpers.UiThread;

/**
 * Информирует о состоянии регистрации на БС.
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class RegistrationInformant {

    private final BehaviorSubject<RegistrationState> registrationStatePublisher = BehaviorSubject.create();
    private final BehaviorSubject<AuthorizationState> authorizationStatePublisher = BehaviorSubject.create();
    private final ExchangeEventRepository exchangeEventRepository;
    private final OAuth2TokenManager oAuth2TokenManager;
    private final ExchangeEventManager exchangeEventManager;
    private final AppRuntimeProperty appRuntimeProperty;
    private final UiThread uiThread;

    @Inject
    RegistrationInformant(ExchangeEventRepository exchangeEventRepository,
                          OAuth2TokenManager oAuth2TokenManager,
                          ExchangeEventManager exchangeEventManager,
                          AppRuntimeProperty appRuntimeProperty,
                          UiThread uiThread) {
        this.exchangeEventRepository = exchangeEventRepository;
        this.oAuth2TokenManager = oAuth2TokenManager;
        this.exchangeEventManager = exchangeEventManager;
        this.appRuntimeProperty = appRuntimeProperty;
        this.uiThread = uiThread;

        this.oAuth2TokenManager
                .oAuth2TokenChanges()
                .subscribe(oAuth2Token -> {
                    authorizationStatePublisher.onNext(getAuthorizationState(oAuth2Token));
                    registrationStatePublisher.onNext(getRegistrationState(oAuth2Token));
                });
        this.exchangeEventManager
                .completedExchangeEvents()
                .subscribe(exchangeEvent -> registrationStatePublisher.onNext(getRegistrationState()));
    }

    /**
     * Нельзя вызывать это в конструкторе, т.к. даггер инициализирует данный класс до иницилизации БД
     * соответственно метод getRegistrationState() бросит NPE т.к. лезет к базе
     */
    public void init() {
        final OAuth2Token token = oAuth2TokenManager.load();
        this.authorizationStatePublisher.onNext(getAuthorizationState(token));
        this.registrationStatePublisher.onNext(getRegistrationState(token));
    }

    /**
     * Возвращает признак готовности к работе
     * По сути означает наличие хотя бы одной успешной синхронизации с БС (есть база ключей и база нси)
     */
    private boolean isPrepared() {
        ExchangeEvent exchangeEvent = exchangeEventRepository.loadLastByTypeSetAndStatusSet(EnumSet.of(ExchangeEvent.Type.SYNC), EnumSet.of(ExchangeEvent.Status.SUCCESS));
        return exchangeEvent != null;
    }

    /**
     * Возвращает признак успешной регистрации на БС.
     * по сути наличие валидного авторизационного токена
     */
    private boolean isRegistered(OAuth2Token token) {
        return token != null && !token.isBroken();
    }

    /**
     * Возвращает признак наличия битой регистрации на БС (токен авторизации битый).
     */
    private boolean isRegisteredBroken(OAuth2Token token) {
        return token != null && token.isBroken();
    }

    private AuthorizationState getAuthorizationState(OAuth2Token token) {
        if (isRegistered(token)) {
            return AuthorizationState.AUTHORIZED;
        } else if (isRegisteredBroken(token)) {
            return AuthorizationState.AUTHORIZED_BROKEN;
        } else {
            return AuthorizationState.NOT_AUTHORIZED;
        }
    }

    private synchronized RegistrationState getRegistrationState(OAuth2Token token) {
        if (!isPrepared()) {
            return RegistrationState.NOT_PREPARED;
        } else if (isRegistered(token)) {
            return RegistrationState.REGISTERED;
        } else if (isRegisteredBroken(token)) {
            return RegistrationState.REGISTERED_BROKEN;
        } else {
            return RegistrationState.NOT_REGISTERED;
        }
    }

    // Всегда загружает актуальный токен из репозитория и определяет статус по нему
    public synchronized RegistrationState getRegistrationState() {
        return getRegistrationState(oAuth2TokenManager.load());
    }

    public Observable<RegistrationState> getRegistrationStatePublisher() {
        return registrationStatePublisher;
    }

    public Observable<AuthorizationState> getAuthorizationStatePublisher() {
        return authorizationStatePublisher;
    }

}
