package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.helpers.CashierSessionInfo;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.logger.Logger;

/**
 * Класс, выполняющий сборку {@link CashRegisterEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class CashRegisterEventCreator {

    private static final String TAG = Logger.makeLogTag(CashRegisterEventCreator.class);

    private final CashierSessionInfo cashierSessionInfo;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    /**
     * Фискальный регистратор
     */
    private CashRegister cashRegister;

    @Inject
    CashRegisterEventCreator(CashierSessionInfo cashierSessionInfo,
                             LocalDaoSession localDaoSession,
                             LocalDbTransaction localDbTransaction) {
        this.cashierSessionInfo = cashierSessionInfo;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    /**
     * Задает фискальный регистратор
     *
     * @param cashRegister
     * @return
     */
    public CashRegisterEventCreator setCashRegister(@NonNull CashRegister cashRegister) {
        this.cashRegister = cashRegister;
        return this;
    }

    /**
     * Выполняет сборку {@link CashRegisterEvent} и запись его в БД.
     *
     * @return Сформированный {@link CashRegisterEvent}
     */
    @NonNull
    public CashRegisterEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private CashRegisterEvent createInternal() {

        CashRegisterEvent cashRegisterEvent;

        localDaoSession.beginTransaction();
        try {
            Preconditions.checkNotNull(cashRegister, "Cash register is null");
            localDaoSession.cashRegisterDao().insertOrThrow(cashRegister);

            Cashier cashier = cashierSessionInfo.getCurrentCashier();
            Preconditions.checkNotNull(cashier, "Cashier is null");
            localDaoSession.cashierDao().insertOrThrow(cashier);

            cashRegisterEvent = new CashRegisterEvent();
            cashRegisterEvent.setCashierId(cashier.getId());
            cashRegisterEvent.setCashRegisterId(cashRegister.getId());

            // Пишем в БД CashRegisterEvent
            localDaoSession.getCashRegisterEventDao().insertOrThrow(cashRegisterEvent);

            localDaoSession.setTransactionSuccessful();

        } catch (Exception e) {
            Logger.error(TAG, e);
            throw e;
        } finally {
            localDaoSession.endTransaction();
        }

        return cashRegisterEvent;
    }
}