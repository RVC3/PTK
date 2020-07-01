package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.localdb.model.BankOperationResult;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ipos.model.FinancialTransactionResult;

/**
 * Класс, выполняющий сборку {@link BankTransactionEvent} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class BankTransactionEventCreator {

    private final EventCreator eventCreator;
    private final LocalDaoSession localDaoSession;
    private final CommonSettingsStorage commonSettingsStorage;
    private final LocalDbTransaction localDbTransaction;

    private FinancialTransactionResult financialTransactionResult;
    private BankOperationType bankOperationType;
    private BankTransactionEvent.Status status;

    @Inject
    BankTransactionEventCreator(EventCreator eventCreator,
                                LocalDaoSession localDaoSession,
                                CommonSettingsStorage commonSettingsStorage,
                                LocalDbTransaction localDbTransaction) {
        this.eventCreator = eventCreator;
        this.localDaoSession = localDaoSession;
        this.commonSettingsStorage = commonSettingsStorage;
        this.localDbTransaction = localDbTransaction;
    }

    public BankTransactionEventCreator setFinancialTransactionResult(FinancialTransactionResult financialTransactionResult) {
        this.financialTransactionResult = financialTransactionResult;
        return this;
    }

    public BankTransactionEventCreator setBankOperationType(BankOperationType bankOperationType) {
        this.bankOperationType = bankOperationType;
        return this;
    }

    public BankTransactionEventCreator setStatus(BankTransactionEvent.Status status) {
        this.status = status;
        return this;
    }

    /**
     * Выполнят сборку {@link BankTransactionEvent} и запись его в БД.
     *
     * @return Сформированный {@link BankTransactionEvent}
     */
    @NonNull
    public BankTransactionEvent create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private BankTransactionEvent createInternal() {
        Preconditions.checkNotNull(financialTransactionResult);
        Preconditions.checkNotNull(bankOperationType);
        Preconditions.checkNotNull(status);

        // Пишем в БД Event
        Event event = eventCreator.create();

        ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        TerminalDay terminalDay = localDaoSession.getTerminalDayDao().getLastTerminalDay();
        MonthEvent monthEvent = localDaoSession.getMonthEventDao().getLastMonthEvent();

        Preconditions.checkNotNull(shiftEvent);
        Preconditions.checkNotNull(terminalDay);
        Preconditions.checkNotNull(monthEvent);

        BankTransactionEvent bankTransactionEvent = new BankTransactionEvent();

        //фискальные данные
        bankTransactionEvent.setTransactionId(financialTransactionResult.getId());
        bankTransactionEvent.setTransactionDateTime(financialTransactionResult.getTimeStamp());
        bankTransactionEvent.setOperationResult(financialTransactionResult.isApproved() ? BankOperationResult.Approved : BankOperationResult.Rejected);
        bankTransactionEvent.setTerminalNumber(financialTransactionResult.getTerminalId());
        bankTransactionEvent.setTotal(Decimals.divide(new BigDecimal(financialTransactionResult.getAmount()), Decimals.HUNDRED));
        bankTransactionEvent.setCardPan(financialTransactionResult.getCardPAN());
        bankTransactionEvent.setRrn(financialTransactionResult.getRRN());
        bankTransactionEvent.setMerchantId(financialTransactionResult.getMerchantId());
        bankTransactionEvent.setAuthorizationCode(financialTransactionResult.getAuthorizationId());
        bankTransactionEvent.setCardEmitentName(financialTransactionResult.getIssuerName());
        bankTransactionEvent.setCurrencyCode(financialTransactionResult.getCurrencyCode());
        bankTransactionEvent.setSmartCardApplicationName(financialTransactionResult.getApplicationName());
        bankTransactionEvent.setBankCheckNumber(financialTransactionResult.getInvoiceNumber());

        //все остальные данные
        bankTransactionEvent.setEventId(event.getId());
        bankTransactionEvent.setCashRegisterWorkingShiftId(shiftEvent.getId());
        bankTransactionEvent.setTerminalDayId(terminalDay.getId());
        bankTransactionEvent.setMonthId(monthEvent.getId());
        bankTransactionEvent.setOperationType(bankOperationType);
        bankTransactionEvent.setBankCode(commonSettingsStorage.get().getBankCode());
        bankTransactionEvent.setPointOfSaleNumber(null); // В будущем

        bankTransactionEvent.setStatus(status);

        // Пишем в БД BankTransactionEvent
        localDaoSession.getBankTransactionDao().insertOrThrow(bankTransactionEvent);
        return bankTransactionEvent;
    }

}
