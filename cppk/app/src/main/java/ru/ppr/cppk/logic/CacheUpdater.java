package ru.ppr.cppk.logic;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvType;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;

/**
 * Отвечает за обновления кеша для переданных систем.
 * Пока работает только с "окружениями" для оформления ПД.
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class CacheUpdater {

    private static final String TAG = Logger.makeLogTag(CacheUpdater.class);

    private final NsiVersionManager nsiVersionManager;
    private final PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private final PdSaleEnvFactory pdSaleEnvFactory;
    /**
     * Объект блокировки для запрета двух параллельных процессов кеширования
     */
    private final Lock lock = new ReentrantLock();

    @Inject
    CacheUpdater(NsiVersionManager nsiVersionManager,
                 PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
                 PdSaleEnvFactory pdSaleEnvFactory) {
        this.nsiVersionManager = nsiVersionManager;
        this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
        this.pdSaleEnvFactory = pdSaleEnvFactory;
    }

    /**
     * Запускает обновления кеша для всех "окружений" для оформления ПД.
     */
    public void updateCache() {
        try {
            Logger.trace(TAG, "updateCache start");
            lock.lock();
            Logger.trace(TAG, "pdSaleEnvForSinglePd start");
            updateCacheInternal(pdSaleEnvFactory.pdSaleEnvForSinglePd());
            Logger.trace(TAG, "pdSaleEnvForBaggage start");
            updateCacheInternal(pdSaleEnvFactory.pdSaleEnvForBaggage());
            Logger.trace(TAG, "pdSaleEnvForTariffsInfo start");
            updateCacheInternal(pdSaleEnvFactory.pdSaleEnvForTariffsInfo());
            Logger.trace(TAG, "pdSaleEnvForTransfer start");
            updateCacheInternal(pdSaleEnvFactory.pdSaleEnvForTransfer());
            Logger.trace(TAG, "updateCache success");
        } catch (Exception e) {
            Logger.error(TAG, "updateCache fail", e);
        } finally {
            Logger.trace(TAG, "updateCache end");
            lock.unlock();
        }
    }

    /**
     * Обновляет кеш для переданного "окружение" для оформления ПД.
     * Работа происходит в процессе кеширования:
     * В целом - это группа из 6 запросов к таблицы тарифов с большими списками в IN.
     * 6 запросов в группе - это
     * - 3 запроса на формирование списка станций отправления
     * - 3 запроса на формирование списка станций назначения
     * 3 запроса на список станций - это:
     * - 1 запрос для прямого маршрута A->C
     * - 1 запрос для части транзитного маршрута A->B
     * - 1 запрос для части транзитного маршрута B->C
     *
     * @param pdSaleEnv "окружение" для оформления ПД
     */
    private void updateCacheInternal(PdSaleEnv pdSaleEnv) {
        Logger.trace(TAG, "updateCacheInternal start");

        PdSaleRestrictionsParams pdSaleRestrictionsParams;
        if (pdSaleEnv.getType() == PdSaleEnvType.TRANSFER) {
            pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.createForTransfer(new Date(), nsiVersionManager.getCurrentNsiVersionId());
        } else {
            pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.create(new Date(), nsiVersionManager.getCurrentNsiVersionId());
        }

        pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);
        Logger.trace(TAG, "updateCacheInternal load all dep stations, pdSaleEnv");
        pdSaleEnv.depStationsLoader().loadAllStations(null, null, "");
        Logger.trace(TAG, "updateCacheInternal load all dest stations, pdSaleEnv");
        pdSaleEnv.destStationsLoader().loadAllStations(null, null, "");
        Logger.trace(TAG, "updateCacheInternal end");
    }

}
