package ru.ppr.cppk.logic;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.logger.Logger;

/**
 * Класс для проверки актауальности версии датаконтрактов НСИ.
 *
 * @author Aleksandr Brazhkin
 */
public class NsiDataContractsVersionChecker {

    private final LocalDaoSession localDaoSession;
    private final int actualVersion;
    private final UpdateEventRepository updateEventRepository;
    private static final String TAG = Logger.makeLogTag(NsiDataContractsVersionChecker.class);

    public NsiDataContractsVersionChecker(LocalDaoSession localDaoSession, int actualVersion) {
        this.localDaoSession = localDaoSession;
        this.actualVersion = actualVersion;
        this.updateEventRepository = Dagger.appComponent().updateEventRepository();
    }

    /**
     * Вернет флаг актуальности версии датаконтактов базы NSI
     *
     * @return {@code true} если версия датаконтактов для NSI Security актуальна, {@code false} иначе
     */
    public boolean isDataContractVersionValid() {
        UpdateEvent lastUpdateEvent = updateEventRepository.getLastUpdateEvent(UpdateEventType.NSI, false);
        if (lastUpdateEvent != null) Logger.trace(TAG, "lastUpdateEvent = " + (lastUpdateEvent != null) + ". getDataContractVersion = " + lastUpdateEvent.getDataContractVersion() + ". actualVersion = " + actualVersion);
        return (lastUpdateEvent != null && lastUpdateEvent.getDataContractVersion() == actualVersion);
    }
}
