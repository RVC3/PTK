package ru.ppr.chit.bs.oauth2token;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.domain.repository.local.OAuth2TokenRepository;

/**
 * Менеджер токенов авторизации, предоставляет текущий токен,
 * замняет текущий токен на новый и оповещает подписчиков об изменении токена
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class OAuth2TokenManager {

    private final PublishSubject<OAuth2Token> oAuth2TokenPublisher = PublishSubject.create();
    private final OAuth2TokenRepository repository;

    @Inject
    OAuth2TokenManager(OAuth2TokenRepository repository) {
        this.repository = repository;
    }

    @Nullable
    public OAuth2Token load() {
        return repository.loadFirst();
    }

    public void save(@NonNull OAuth2Token token) {
        OAuth2Token savedToken = load();
        if (savedToken != null) {
            // токен должен быть только 1
            token.setId(savedToken.getId());
            repository.update(token);
        } else {
            repository.insert(token);
        }
        oAuth2TokenPublisher.onNext(token);
    }

    public void clear() {
        OAuth2Token savedToken = load();
        if (savedToken != null) {
            repository.delete(savedToken);
        }
    }

    public Observable<OAuth2Token> oAuth2TokenChanges() {
        return oAuth2TokenPublisher;
    }

}
