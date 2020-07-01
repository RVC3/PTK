package ru.ppr.cppk.ui.activity.pdSale;

import android.support.annotation.NonNull;
import android.util.Log;

import java.math.BigDecimal;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.TrainPdCostCalculator;
import ru.ppr.cppk.model.PdSaleData;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSalePresenter extends BaseMvpPresenter<PdSaleView> {

    private static final String TAG = Logger.makeLogTag(PdSalePresenter.class);

    private static final int TARGET_PAPER = 0;
    private static final int TARGET_CARD = 1;

    private InteractionListener mInteractionListener;

    private boolean mInitialized = false;
    private PdSaleParams mPdSaleParams;
    private LocalDaoSession mLocalDaoSession;
    private NsiDaoSession mNsiDaoSession;
    private final PdSaleData mPdSaleData = new PdSaleData();
    private DataSalePD mDataSalePD;
    private TrainPdCostCalculator mTrainPdCostCalculator;
    private TicketStorageTypeToTicketTypeChecker mTicketStorageTypeToTicketTypeChecker;
    //////////////////////////////////
    private int pdIndex = 0;
    private int tariffIndex = 0;
    private long previousPdSaleEventId = -1;
    private int currentTarget = TARGET_PAPER;

    public PdSalePresenter() {

    }

    void initialize(PdSaleParams pdSaleParams,
                    LocalDaoSession localDaoSession,
                    NsiDaoSession nsiDaoSession,
                    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker) {
        if (!mInitialized) {
            mInitialized = true;
            mPdSaleParams = pdSaleParams;
            mLocalDaoSession = localDaoSession;
            mNsiDaoSession = nsiDaoSession;
            mTicketStorageTypeToTicketTypeChecker = ticketStorageTypeToTicketTypeChecker;
            mTrainPdCostCalculator = new TrainPdCostCalculator(mPdSaleData);
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");

        // Получаем информацию о талоне ТППД
        if (mPdSaleParams.getCouponReadEventId() != -1) {
            CouponReadEvent couponReadEvent = mLocalDaoSession.getCouponReadEventDao().load(mPdSaleParams.getCouponReadEventId());
            mPdSaleData.setCouponReadEvent(couponReadEvent);
        }

        mInteractionListener.transferDataToChild(mPdSaleParams, mPdSaleData);
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.mInteractionListener = interactionListener;
    }

    void onCardPaymentFailed(long bankTransactionEventId) {
        resetState();
        mInteractionListener.navigateToPdSalePreparation();
    }

    void onCardPaymentCompleted(long bankTransactionEventId) {
        mDataSalePD.setBankTransactionEvent(mLocalDaoSession.getBankTransactionDao().load(bankTransactionEventId));
        if (currentTarget == TARGET_CARD) {
            startWritePd();
        } else {
            startPrintPd();
        }
    }

    void onCancelCardPaymentRequired(long bankTransactionEventId) {
        mInteractionListener.navigateToCancelCardPayment(bankTransactionEventId);
    }

    void onReturnMoneyRequired() {
        BankTransactionEvent bankTransactionEvent = mDataSalePD.getBankTransactionEvent();
        if (bankTransactionEvent == null) {
            mInteractionListener.navigateToCancelCashPayment(calcSumForCashReturn());
        } else {
            mInteractionListener.navigateToCancelCardPayment(bankTransactionEvent.getId());
        }
    }

    void onCancelCardPaymentFinished() {
        resetState();
        mInteractionListener.navigateToPdSalePreparation();
    }

    void onCancelCashPaymentFinished() {
        resetState();
        mInteractionListener.navigateToPdSalePreparation();
    }

    void writePd() {
        startWritePdWithPayment();
    }

    void printPd() {
        startPrintPdWithPayment();
    }

    void onPrintPdOnWriteDenied() {
        mInteractionListener.navigateToPrintPdCheck(mDataSalePD);
    }

    void onWriteCompleted(long newPdId, boolean isPrinted) {
        navigateToSaleSuccess(newPdId, isPrinted ? SaleType.PRINT : SaleType.SMART_CARD);
    }

    void onPrintCompleted(long saleEventId) {
        if (switchToNextTicket()) {
            previousPdSaleEventId = saleEventId;
            mInteractionListener.navigateToPrintSuccess();
        } else {
            navigateToSaleSuccess(saleEventId, SaleType.PRINT);
        }
    }

    private void navigateToSaleSuccess(long saleEventId, SaleType saleType) {
        BigDecimal cost = mTrainPdCostCalculator.getAllTicketsTotalCostValueWithDiscount();
        PdSaleSuccessParams pdSaleSuccessParams = new PdSaleSuccessParams();
        pdSaleSuccessParams.setNewPDId(saleEventId);
        pdSaleSuccessParams.setPdCost(cost);
        pdSaleSuccessParams.setSaleType(saleType);
        pdSaleSuccessParams.setHideDeliveryButton(mPdSaleData.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD);
        //
        Station departureStation = mPdSaleData.getTariffsThere().get(0).getStationDeparture(mNsiDaoSession);
        pdSaleSuccessParams.setDepartureStationCode(departureStation.getCode());
        int tariffsCount = mPdSaleData.getTariffsThere().size();
        Station destinationStation = mPdSaleData.getTariffsThere().get(tariffsCount - 1).getStationDestination(mNsiDaoSession);
        pdSaleSuccessParams.setDestinationStationCode(destinationStation.getCode());
        mInteractionListener.navigateToSaleSuccess(pdSaleSuccessParams);
    }

    void onPrintNextPdBtnClicked() {
        startPrintPdWithPayment();
    }

    void onCancelSaleProcess() {
        resetState();
        mInteractionListener.navigateToPdSalePreparation();
    }

    private void startPrintPdWithPayment() {
        currentTarget = TARGET_PAPER;
        mDataSalePD = createDataSalePD();
        if (!mTicketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, mDataSalePD.getTicketType())) {
            //такого не должно было случиться, ищем ошибку логики наверху
            throw new IllegalStateException("Print pd disabled to TicketType = " + mDataSalePD.getTicketType().toString());
        }

        if (mPdSaleData.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD) {
            BigDecimal cost = mTrainPdCostCalculator.getOneTicketTotalCostValueWithDiscount(tariffIndex);
            mInteractionListener.navigateToCardPayment(cost);
        } else {
            startPrintPd();
        }
    }

    private void startWritePdWithPayment() {
        currentTarget = TARGET_CARD;
        mDataSalePD = createDataSalePD();
        if (mPdSaleData.getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD) {
            BigDecimal cost = mTrainPdCostCalculator.getOneTicketTotalCostValueWithDiscount(tariffIndex);
            mInteractionListener.navigateToCardPayment(cost);
        } else {
            startWritePd();
        }
    }

    private void startPrintPd() {
        mInteractionListener.navigateToPrintPdCheck(mDataSalePD);
    }

    private void startWritePd() {
        if (mPdSaleData.getExemptions() == null) {
            mInteractionListener.navigateToWriteToBSC(mDataSalePD);
        } else {
            mInteractionListener.navigateToSellWithExemption(mDataSalePD);
        }
    }

    private Tariff getTariffByIndex(TicketWayType ticketWayType, int tariffIndex) {
        if (ticketWayType == TicketWayType.TwoWay) {
            // Обратного тарифа может и не быть
            if (mPdSaleData.getTariffsBack() != null && !mPdSaleData.getTariffsBack().isEmpty()) {
                return mPdSaleData.getTariffsBack().get(tariffIndex);
            } else {
                return null;
            }
        } else {
            return mPdSaleData.getTariffsThere().get(tariffIndex);
        }
    }

    private boolean switchToNextTicket() {
        int tariffsCount = mPdSaleData.getTariffsThere().size();
        int nextTariffIndex = tariffIndex + 1;
        if (nextTariffIndex < tariffsCount) {
            tariffIndex++;
            mDataSalePD = null;
            return true;
        }
        int pdCount = mPdSaleData.getPdCount();
        int nextPdIndex = pdIndex + 1;
        if (nextPdIndex < pdCount) {
            pdIndex++;
            tariffIndex = 0;
            mDataSalePD = null;
            return true;
        }
        return false;
    }

    private void resetState() {
        pdIndex = 0;
        tariffIndex = 0;
        mDataSalePD = null;
        previousPdSaleEventId = -1;
    }

    private BigDecimal calcSumForCashReturn() {
        BigDecimal forReturn = BigDecimal.ZERO;
        for (int i = pdIndex; i < mPdSaleData.getPdCount(); i++) {
            for (int j = (i == pdIndex) ? tariffIndex : 0; j < mPdSaleData.getTariffsThere().size(); j++) {
                forReturn = forReturn.add(mTrainPdCostCalculator.getOneTicketTotalCostValueWithDiscount(j));
            }
        }
        return forReturn;
    }

    private DataSalePD createDataSalePD() {
        DataSalePD dataSalePD = new DataSalePD();
        dataSalePD.setPaymentType(mPdSaleData.getPaymentType());
        if (mPdSaleData.getExemptions() != null) {
            ExemptionForEvent exemptionForEvent = mPdSaleData.getExemptions().get(tariffIndex).first;
            Exemption exemption = mPdSaleData.getExemptions().get(tariffIndex).second;
            dataSalePD.setExemptionForEvent(exemptionForEvent);
            AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEtt();
            additionalInfoForEtt.setIssueDateTime(exemptionForEvent.getIssueDate());
            dataSalePD.setAdditionalInfoForEttManualEntryDateIssue(additionalInfoForEtt);
            dataSalePD.setExemption(exemption);
        }
        dataSalePD.setAdditionalInfoForEttFromCard(mPdSaleData.getAdditionalInfoForEtt());
        dataSalePD.setDirection(mPdSaleData.getDirection());
        Tariff tariffThere = getTariffByIndex(TicketWayType.OneWay, tariffIndex);
        dataSalePD.setDepartureStation(tariffThere.getStationDeparture(mNsiDaoSession));
        dataSalePD.setDestinationStation(tariffThere.getStationDestination(mNsiDaoSession));
        dataSalePD.setTariffThere(tariffThere);
        Tariff tariffBack = getTariffByIndex(TicketWayType.TwoWay, tariffIndex);
        dataSalePD.setTariffBack(tariffBack);
        // Взимаем сбор только с первого ПД при оформлении транзита
        // http://agile.srvdev.ru/browse/CPPKPP-33911
        dataSalePD.setIncludeFee(mPdSaleData.isIncludeFee() && tariffIndex == 0);
        dataSalePD.setTariffPlan(mPdSaleData.getTariffPlan());
        dataSalePD.setTicketType(mPdSaleData.getTicketType());
        dataSalePD.setTrainCategory(mPdSaleData.getTariffPlan().getTrainCategory(mNsiDaoSession));
        dataSalePD.setProcessingFee(mPdSaleData.getProcessingFee());
        dataSalePD.setTicketCostValueWithoutDiscount(mTrainPdCostCalculator.getOneTicketCostValueWithoutDiscount(tariffIndex));
        dataSalePD.setETicketDataParams(mPdSaleData.geteTicketDataParams());
        dataSalePD.setCouponReadEvent(tariffIndex == 0 ? mPdSaleData.getCouponReadEvent() : null);
        // http://agile.srvdev.ru/browse/CPPKPP-42998
        // Задаем ParentTicketInfo и сохраняем ConnectionType для транзитных ПД
        if (tariffIndex > 0) {
            CPPKTicketSales cppkTicketSales = mLocalDaoSession.getCppkTicketSaleDao().load(previousPdSaleEventId);
            Event event = mLocalDaoSession.getEventDao().load(cppkTicketSales.getEventId());
            TicketSaleReturnEventBase ticketSaleReturnEventBase = mLocalDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
            TicketEventBase ticketEventBase = mLocalDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            Check check = mLocalDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());

            // Заполняем ParentTicketInfo
            ParentTicketInfo parentTicketInfo = new ParentTicketInfo();
            parentTicketInfo.setSaleDateTime(ticketEventBase.getSaledateTime());
            parentTicketInfo.setTicketNumber(check.getOrderNumber());
            parentTicketInfo.setWayType(ticketEventBase.getWayType());
            parentTicketInfo.setCashRegisterNumber(event.getDeviceId());
            // Устанавливаем данные о родительском билете
            dataSalePD.setParentTicketInfo(parentTicketInfo);
            // Устанавливаем тип связи с родительским билетом
            dataSalePD.setConnectionType(ConnectionType.TRANSIT);
        } else {
            dataSalePD.setConnectionType(null);
        }
        return dataSalePD;
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void transferDataToChild(PdSaleParams pdSaleParams, PdSaleData pdSaleData);

        void navigateToCardPayment(BigDecimal amount);

        void navigateToCancelCardPayment(long bankTransactionEventId);

        void navigateToPdSalePreparation();

        void navigateToCancelCashPayment(BigDecimal amount);

        void navigateToPrintPdCheck(DataSalePD dataSalePD);

        void navigateToWriteToBSC(DataSalePD dataSalePD);

        void navigateToSellWithExemption(DataSalePD dataSalePD);

        void navigateToSaleSuccess(PdSaleSuccessParams pdSaleSuccessParams);

        void navigateToPrintSuccess();
    }
}
