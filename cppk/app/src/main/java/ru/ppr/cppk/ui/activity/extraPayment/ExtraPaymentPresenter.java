package ru.ppr.cppk.ui.activity.extraPayment;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentPresenter extends BaseMvpPresenter<ExtraPaymentView> {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private ExtraPaymentParams mExtraPaymentParams;
    private LocalDaoSession mLocalDaoSession;
    private DocumentNumberProvider mDocumentNumberProvider;
    private final DataSalePD mDataSalePd = new DataSalePD();

    public ExtraPaymentPresenter() {

    }

    void initialize(ExtraPaymentParams extraPaymentParams,
                    LocalDaoSession localDaoSession,
                    DocumentNumberProvider documentNumberProvider) {
        if (!mInitialized) {
            mInitialized = true;
            mExtraPaymentParams = extraPaymentParams;
            mLocalDaoSession = localDaoSession;
            mDocumentNumberProvider = documentNumberProvider;
            interactionListener.transferDataToChild(
                    mExtraPaymentParams,
                    mDataSalePd
            );
        }
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    /**
     * Возвращает объект DataSalePD.
     * Не делать так!
     * Сделано для совместимости со старым кодом.
     *
     * @return {@link DataSalePD}
     */
    DataSalePD getDataSalePd() {
        return mDataSalePd;
    }

    void onSellBtnClick() {
        Logger.trace(TAG, "onSellBtnClick");
        int checkNumber = mDocumentNumberProvider.getNextDocumentNumber();

        if (checkNumber == 0) {
            throw new IllegalArgumentException("Incorrect checkNumber");
        }

        if (!mDataSalePd.isMustTakeMoney() || mDataSalePd.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
            //выполняем печать билета
            mDataSalePd.setTicketWritten(false);
            interactionListener.navigateToPrintPd();
        } else {
            interactionListener.navigateToPayWithPos(SaleType.PRINT, mDataSalePd.getTotalCostValueWithDiscount().toPlainString());
        }
    }

    void onPaymentWithPosShouldBeCanceled(String transactionId) {
        Logger.trace(TAG, "onPaymentWithPosShouldBeCanceled, transactionId = " + transactionId);
        interactionListener.navigateToCancelCardPayment(Integer.valueOf(transactionId));
    }

    void onCardPaymentSuccess() {
        Logger.trace(TAG, "onCardPaymentSuccess");
        mDataSalePd.setBankTransactionEvent(mLocalDaoSession.getBankTransactionDao().getLastEventByType(BankOperationType.SALE));
        mDataSalePd.setTicketWritten(false);
        interactionListener.navigateToPrintPd();
    }

    void onPdSuccessfullyPrinted(long newPdId, BigDecimal pdCost) {
        Logger.trace(TAG, "onPdSuccessfullyPrinted, newPdId =" + newPdId + ", pdCost = " + pdCost.toPlainString());
        PdSaleSuccessParams pdSaleSuccessParams = new PdSaleSuccessParams();
        pdSaleSuccessParams.setNewPDId(newPdId);
        pdSaleSuccessParams.setPdCost(pdCost);
        pdSaleSuccessParams.setSaleType(SaleType.PRINT);
        pdSaleSuccessParams.setHideDeliveryButton(mDataSalePd.getBankTransactionEvent() != null);
        pdSaleSuccessParams.setDepartureStationCode(mDataSalePd.getDepartureStation().getCode());
        pdSaleSuccessParams.setDestinationStationCode(mDataSalePd.getDestinationStation().getCode());
        interactionListener.navigateToSellPdSuccess(pdSaleSuccessParams);
    }

    void onSellAnotherPdClick() {
        Logger.trace(TAG, "onSellAnotherPdClick");
        interactionListener.navigateToNewSale();
    }

    void onCancelSellProcess() {
        Logger.trace(TAG, "onCancelSellProcess");
        mDataSalePd.setBankTransactionEvent(null);
        mDataSalePd.setPDNumber(0);
        mDataSalePd.setSaleDateTime(null);
        mDataSalePd.setTicketWritten(false);
        interactionListener.navigateToExecution();
    }

    void onCardPaymentFailed() {
        Logger.trace(TAG, "onCardPaymentFailed");
        interactionListener.navigateToExecution();
    }

    void onCancelCardPaymentFailed() {
        Logger.trace(TAG, "onCancelCardPaymentFailed");
        interactionListener.navigateToPrintPd();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        /**
         * Успешное подтверждение посадки.
         */
        void transferDataToChild(ExtraPaymentParams extraPaymentParams, DataSalePD dataSalePd);

        void navigateToPrintPd();

        void navigateToPayWithPos(SaleType saleType, String price);

        void navigateToCancelCardPayment(int transactionId);

        void navigateToSellPdSuccess(PdSaleSuccessParams pdSaleSuccessParams);

        void navigateToNewSale();

        void navigateToExecution();
    }
}
