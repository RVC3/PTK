package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.nsi.entity.Carrier;

/**
 * Класс, выполняющий сборку {@link ParentTicketInfo} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class LegalEntityCreator {

    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private Carrier carrier;

    @Inject
    LegalEntityCreator(LocalDaoSession localDaoSession,
                       LocalDbTransaction localDbTransaction) {
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    public LegalEntityCreator setCarrier(Carrier carrier) {
        this.carrier = carrier;
        return this;
    }

    /**
     * Выполнят сборку {@link LegalEntity} и запись его в БД.
     *
     * @return Сформированный {@link LegalEntity}
     */
    @NonNull
    public LegalEntity create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private LegalEntity createInternal() {
        Preconditions.checkNotNull(carrier);

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setCode(carrier.getCode());
        legalEntity.setName(carrier.getName());
        legalEntity.setInn(carrier.getInn());

        // Пишем в БД LegalEntity
        localDaoSession.legalEntityDao().insertOrThrow(legalEntity);
        return legalEntity;
    }
}
