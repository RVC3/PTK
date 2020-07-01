package ru.ppr.cppk.ui.fragment.fineSalePreparation;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.FineSaleData;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Aleksandr Brazhkin
 */
public class FineSalePreparationPresenter extends BaseMvpViewStatePresenter<FineSalePreparationView, FineSalePreparationViewState> {

    private static final String TAG = Logger.makeLogTag(FineSalePreparationPresenter.class);

    private InteractionListener interactionListener;

    private boolean mInitialized = false;
    private int mNsiVersion;
    private FineSaleData mFineSaleData;
    private UiThread mUiThread;
    private NsiDaoSession mNsiDaoSession;
    private PrinterManager mPrinterManager;
    private PrivateSettings privateSettings;
    private NsiVersionManager nsiVersionManager;
    private CriticalNsiChecker criticalNsiChecker;
    private FineRepository fineRepository;
    /**
     * Список штрафов
     */
    private List<Fine> mFines;

    public FineSalePreparationPresenter() {

    }

    @Override
    protected FineSalePreparationViewState provideViewState() {
        return new FineSalePreparationViewState();
    }

    void initialize(FineSaleData fineSaleData, UiThread uiThread, NsiDaoSession nsiDaoSession,
                    PrinterManager printerManager, PrivateSettings privateSettings, NsiVersionManager nsiVersionManager,
                    CriticalNsiChecker criticalNsiChecker, FineRepository fineRepository) {
        if (!mInitialized) {
            mInitialized = true;
            mFineSaleData = fineSaleData;
            mUiThread = uiThread;
            mNsiDaoSession = nsiDaoSession;
            mPrinterManager = printerManager;
            this.privateSettings = privateSettings;
            this.nsiVersionManager = nsiVersionManager;
            this.criticalNsiChecker = criticalNsiChecker;
            this.fineRepository = fineRepository;

            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");

        if (criticalNsiChecker.checkCriticalNsiCloseDialogShouldBeShown()) {
            if (criticalNsiChecker.checkCriticalNsiCloseShiftPermissions()) {
                view.setCriticalNsiCloseShiftDialogVisible(true);
            } else {
                view.setCriticalNsiBackDialogVisible(true);
            }

            return;
        }

        mNsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        mFines = loadFines();
        view.setFines(mFines);
        view.setNoFinesAvailableDialogVisible(mFines.isEmpty());

        mFineSaleData.setPaymentType(PaymentType.INDIVIDUAL_CASH);
        view.setSendETicketBtnVisible(mPrinterManager.getPrinter().isFederalLaw54Supported());
        // Устанавливаем видимость кнопок типа оплаты
        view.setPaymentTypeVisible(privateSettings.isPosEnabled());
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    void onFineSelected(int position) {
        Fine fine = mFines.get(position);
        mFineSaleData.setFine(fine);
        view.setRegion(fine.getRegion(mNsiDaoSession, mNsiVersion).getName());
        view.setCost(fine.getValue());
        view.setCostGroupVisible(true);
    }

    void onPaymentTypeChecked(PaymentType paymentType) {
        mFineSaleData.setPaymentType(paymentType);
    }

    void onSellBtnClick() {
        Logger.trace(TAG, "onSellBtnClick");
        view.setReallyWantFineDialogVisible(true, mFineSaleData.getFine().getName(), mFineSaleData.getFine().getValue());
    }

    void onSendETicketBtnClick() {
        Logger.trace(TAG, "onSendETicketBtnClick");
        interactionListener.onSendETicketBtnClick(mFineSaleData.getETicketDataParams());
    }

    void onETicketDataSelected(ETicketDataParams eTicketDataParams) {
        mFineSaleData.setETicketDataParams(eTicketDataParams);
    }

    void onRallyWantFineDialogPositiveClick() {
        Logger.trace(TAG, "onRallyWantFineDialogPositiveClick");
        interactionListener.onSellBtnClick();
    }

    void onRallyWantFineDialogDismiss() {
        view.setReallyWantFineDialogVisible(false, null, null);
    }

    void onNoFinesAvailableDialogDismiss() {
        view.setNoFinesAvailableDialogVisible(false);
        interactionListener.closeScreen();
    }

    void onCriticalNsiBackDialogRead() {
        interactionListener.closeScreen();
    }

    void onCriticalNsiCloseShiftDialogRead() {
        interactionListener.navigateToCloseShiftActivity();
    }

    private List<Fine> loadFines() {
        return fineRepository.loadAllWithCodes(mNsiVersion, privateSettings.getAllowedFineCodes());
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onSendETicketBtnClick(ETicketDataParams eTicketDataParams);

        void onSellBtnClick();

        void closeScreen();

        void navigateToCloseShiftActivity();
    }

}
