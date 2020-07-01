package ru.ppr.chit.bs.export;

import javax.inject.Inject;

import io.reactivex.Maybe;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.RegistrationState;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class BaseExporter {

    private static final String TAG = Logger.makeLogTag(BaseExporter.class);

    private final RegistrationInformant registrationInformant;
    private final WiFiManager wiFiManager;
    private final ApiManager apiManager;
    private final LocalDbManager localDbManager;
    private final NsiDbManager nsiDbManager;

    @Inject
    BaseExporter(RegistrationInformant registrationInformant,
                 WiFiManager wiFiManager,
                 ApiManager apiManager,
                 LocalDbManager localDbManager,
                 NsiDbManager nsiDbManager) {
        this.registrationInformant = registrationInformant;
        this.wiFiManager = wiFiManager;
        this.apiManager = apiManager;
        this.localDbManager = localDbManager;
        this.nsiDbManager = nsiDbManager;
    }

    Maybe<Boolean> canExchangeBehaviour() {
        return Maybe
                .fromCallable(() -> {
                    boolean wiFiPointAvailable = wiFiManager.isPointAvailable();
                    boolean apiAvailable = apiManager.isApiAvailable();
                    // Проверяем доступность локальной базы (мы можем быть в процессе восстановления)
                    boolean localDbConnected = localDbManager.connectionState().blockingFirst();
                    // Проверяем доступность НСИ (мы можем быть в процессе восстановления)
                    boolean nsiDbConnected = nsiDbManager.connectionState().blockingFirst();
                    Logger.info(TAG, "wiFiPointAvailable: " + wiFiPointAvailable);
                    Logger.info(TAG, "apiAvailable: " + apiAvailable);
                    Logger.info(TAG, "localDbConnected: " + localDbConnected);
                    Logger.info(TAG, "nsiDbConnected: " + nsiDbConnected);
                    // Дальше проверяем только при наличии соединения с локальной базой, т.к. данные проверки осуществляются с её помощью
                    if (localDbConnected) {
                        // Флаг, уведомляющий о том, что регистрация на БС была, и неважно в каком состоянии сейчас
                        boolean prepared = registrationInformant.getRegistrationState() != RegistrationState.NOT_PREPARED;
                        Logger.info(TAG, "prepared: " + prepared);

                        return wiFiPointAvailable && apiAvailable && nsiDbConnected && prepared;
                    }

                    return false;
                })
                .filter(Boolean.TRUE::equals);
    }

    RegistrationInformant getRegistrationInformant() {
        return registrationInformant;
    }

    WiFiManager getWiFiManager() {
        return wiFiManager;
    }

    ApiManager getApiManager() {
        return apiManager;
    }

}
