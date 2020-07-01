package ru.ppr.cppk.ui.activity.transfersale;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.transfersale.interactor.TransferPdCostCalculator;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleData;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Dmitry Nevolin
 */
class TransferSalePresenter extends BaseMvpViewStatePresenter<TransferSaleView, TransferSaleViewState> {

    private static final String TAG = Logger.makeLogTag(TransferSalePresenter.class);

    private static final int TARGET_PAPER = 0;
    private static final int TARGET_CARD = 1;

    private Navigator navigator;

    private final NsiDaoSession nsiDaoSession;
    private final LocalDaoSession localDaoSession;
    private final TransferPdCostCalculator pdCostCalculator;
    private final TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker;
    private final TransferSaleData transferSaleData;

    private DataSalePD dataSalePD;
    private int tariffIndex = 0;
    private int currentTarget = TARGET_PAPER;

    @Inject
    TransferSalePresenter(@NonNull TransferSaleViewState transferSaleViewState,
                          @NonNull NsiDaoSession nsiDaoSession,
                          @NonNull LocalDaoSession localDaoSession,
                          @NonNull TransferPdCostCalculator pdCostCalculator,
                          @NonNull TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker,
                          @NonNull TransferSaleData transferSaleData) {
        super(transferSaleViewState);
        this.nsiDaoSession = nsiDaoSession;
        this.localDaoSession = localDaoSession;
        this.pdCostCalculator = pdCostCalculator;
        this.ticketStorageTypeToTicketTypeChecker = ticketStorageTypeToTicketTypeChecker;
        this.transferSaleData = transferSaleData;
    }

    @Override
    protected void onInitialize2() {
        navigator.navigateToPreparation();
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void writePd() {
        startWritePdWithPayment();
    }

    void printPd() {
        startPrintPdWithPayment();
    }

    void onPrintCompleted(long saleEventId) {
        navigateToSaleSuccess(saleEventId, SaleType.PRINT);
    }

    void onWriteCompleted(long newPdId) {
        navigateToSaleSuccess(newPdId, SaleType.SMART_CARD);
    }

    void onPrintPdOnWriteDenied() {
        navigator.navigateToPrintPdCheck(dataSalePD);
    }

    void onReturnMoneyRequired() {
        BankTransactionEvent bankTransactionEvent = dataSalePD.getBankTransactionEvent();
        if (bankTransactionEvent == null) {
            navigator.navigateToCancelCashPayment(calcSumForCashReturn());
        } else {
            navigator.navigateToCancelCardPayment(bankTransactionEvent.getId());
        }
    }

    void onCardPaymentFailed(@SuppressWarnings("unused") long bankTransactionEventId) {
        resetState();
        navigator.navigateToPreparation();
    }

    void onCardPaymentCompleted(long bankTransactionEventId) {
        dataSalePD.setBankTransactionEvent(localDaoSession.getBankTransactionDao().load(bankTransactionEventId));
        if (currentTarget == TARGET_CARD) {
            startWritePd();
        } else {
            startPrintPd();
        }
    }

    void onCancelCardPaymentRequired(long bankTransactionEventId) {
        navigator.navigateToCancelCardPayment(bankTransactionEventId);
    }

    void onCancelCardPaymentFinished() {
        resetState();
        navigator.navigateToPreparation();
    }

    void onCancelCashPaymentFinished() {
        resetState();
        navigator.navigateToPreparation();
    }

    void onCancelSaleProcess() {
        resetState();
        navigator.navigateToPreparation();
    }

    void onPreparationCanceled() {
        navigator.navigateBack();
    }

    private void navigateToSaleSuccess(long saleEventId, SaleType saleType) {
        BigDecimal cost = pdCostCalculator.getAllTicketsTotalCostValueWithDiscount();
        PdSaleSuccessParams pdSaleSuccessParams = new PdSaleSuccessParams();
        pdSaleSuccessParams.setNewPDId(saleEventId);
        pdSaleSuccessParams.setPdCost(cost);
        pdSaleSuccessParams.setSaleType(saleType);
        pdSaleSuccessParams.setHideDeliveryButton(transferSaleData.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD);
        navigator.navigateToSaleSuccess(pdSaleSuccessParams);
    }

    private void startPrintPdWithPayment() {
        currentTarget = TARGET_PAPER;
        dataSalePD = createDataSalePD();
        if (!ticketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, dataSalePD.getTicketType())) {
            //такого не должно было случиться, ищем ошибку логики наверху
            throw new IllegalStateException("Print pd disabled to TicketType = " + dataSalePD.getTicketType().toString());
        }

        if (dataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD) {
            BigDecimal cost = pdCostCalculator.getOneTicketTotalCostValueWithDiscount(tariffIndex);
            navigator.navigateToCardPayment(cost);
        } else {
            startPrintPd();
        }
    }

    private void startWritePdWithPayment() {
        currentTarget = TARGET_CARD;
        dataSalePD = createDataSalePD();
        if (transferSaleData.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD) {
            BigDecimal cost = pdCostCalculator.getOneTicketTotalCostValueWithDiscount(tariffIndex);
            navigator.navigateToCardPayment(cost);
        } else {
            startWritePd();
        }
    }

    private void startPrintPd() {
        navigator.navigateToPrintPdCheck(dataSalePD);
    }

    private void startWritePd() {
        navigator.navigateToWriteToBSC(dataSalePD);
    }

    private DataSalePD createDataSalePD() {
        DataSalePD dataSalePD = new DataSalePD();
        dataSalePD.setPaymentType(transferSaleData.getPaymentType());
        dataSalePD.setDirection(transferSaleData.getDirection());
        Tariff tariffThere = getTariffByIndex(TicketWayType.OneWay, tariffIndex);
        dataSalePD.setDepartureStation(tariffThere.getStationDeparture(nsiDaoSession));
        dataSalePD.setDestinationStation(tariffThere.getStationDestination(nsiDaoSession));
        dataSalePD.setTariffThere(tariffThere);
        dataSalePD.setIncludeFee(transferSaleData.isIncludeFee());
        dataSalePD.setTariffPlan(transferSaleData.getTariffPlan());
        dataSalePD.setTicketType(transferSaleData.getTicketType());
        dataSalePD.setTrainCategory(transferSaleData.getTariffPlan().getTrainCategory(nsiDaoSession));
        dataSalePD.setProcessingFee(transferSaleData.getProcessingFee());
        dataSalePD.setTicketCostValueWithoutDiscount(pdCostCalculator.getOneTicketCostValueWithoutDiscount(tariffIndex));
        dataSalePD.setParentTicketInfo(transferSaleData.getParentTicketInfo());
        dataSalePD.setConnectionType(transferSaleData.getConnectionType());
        dataSalePD.setTerm(transferSaleData.getStartDayOffset());
        dataSalePD.setStartDate(transferSaleData.getStartDate());
        dataSalePD.setEndDate(transferSaleData.getEndDate());
        return dataSalePD;
    }

    private Tariff getTariffByIndex(TicketWayType ticketWayType, int tariffIndex) {
        if (ticketWayType == TicketWayType.TwoWay) {
            return transferSaleData.getTariffsBack().get(tariffIndex);
        } else {
            return transferSaleData.getTariffsThere().get(tariffIndex);
        }
    }

    private BigDecimal calcSumForCashReturn() {
        return pdCostCalculator.getOneTicketTotalCostValueWithDiscount(tariffIndex);
    }

    private void resetState() {
        tariffIndex = 0;
        dataSalePD = null;
    }

    /**
     * Интерфейс навигации.
     */
    public interface Navigator {

        void navigateToPreparation();

        void navigateToCancelCashPayment(BigDecimal amount);

        void navigateToCardPayment(BigDecimal amount);

        void navigateToCancelCardPayment(long bankTransactionEventId);

        void navigateToPrintPdCheck(@NonNull DataSalePD dataSalePD);

        void navigateToWriteToBSC(@NonNull DataSalePD dataSalePD);

        void navigateToSaleSuccess(PdSaleSuccessParams pdSaleSuccessParams);

        void navigateBack();
    }

}
