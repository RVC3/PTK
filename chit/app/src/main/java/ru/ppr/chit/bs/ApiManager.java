package ru.ppr.chit.bs;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.api.Api;
import ru.ppr.chit.api.ApiType;
import ru.ppr.chit.api.auth.OAuth2TokenStorage;
import ru.ppr.chit.api.di.ApiModule;
import ru.ppr.chit.api.di.DaggerApiComponent;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.domain.model.local.AuthInfo;

/**
 * Управляет состоянием API обмена данными с БС
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class ApiManager {

    private final OAuth2TokenStorage oAuth2TokenStorage;

    @Inject
    public ApiManager(OAuth2TokenStorage oAuth2TokenStorage) {
        this.oAuth2TokenStorage = oAuth2TokenStorage;
    }

    /**
     * Возвращает доступность API
     *
     * @return доступность API
     */
    public boolean isApiAvailable() {
        return Dagger.apiComponent() != null && api() != null;
    }

    public Api api() {
        return Dagger.apiComponent().api();
    }

    /**
     * Подготавливает QAuthProvider к новой сессии авторизации (удаляет старый токен из хранилища)
     * В этом случае система запросит токен авторизации у БС при первом обращении
     *
     */
    public void clearAuthToken(){
        oAuth2TokenStorage.clear();
    }
    /**
     * Обновляет текущий API
     *
     * @param type тип API
     * @param authInfo данные для первичной авторизации
     */
    public void updateApi(ApiType type, AuthInfo authInfo, long deviceId) {
        Dagger.setApiComponent(DaggerApiComponent.builder()
                .apiModule(new ApiModule(type, authInfo, oAuth2TokenStorage, deviceId))
                .build());
    }

}
