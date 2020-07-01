package ru.ppr.cppk.logic;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;

/**
 * Класс для проверки актауальности версии датаконтрактов Security.
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityDataContractsVersionChecker {

    private final LocalDaoSession localDaoSession;
    private final int actualVersion;
    private final UpdateEventRepository updateEventRepository;

    public SecurityDataContractsVersionChecker(LocalDaoSession localDaoSession, int actualVersion) {
        this.localDaoSession = localDaoSession;
        this.actualVersion = actualVersion;
        this.updateEventRepository = Dagger.appComponent().updateEventRepository();
    }

    /**
     * Вернет флаг актуальности версии датаконтактов базы Security
     *
     * @return {@code true} если версия датаконтактов для БД Security актуальна, {@code false} иначе
     */
    public boolean isDataContractVersionValid() {
        UpdateEvent lastUpdateEvent = updateEventRepository.getLastUpdateEvent(UpdateEventType.SECURITY, false);
        return (lastUpdateEvent != null && lastUpdateEvent.getDataContractVersion() == actualVersion);
    }
}
