package ru.ppr.cppk.ui.fragment.extraPaymentPrint;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.v2.PdV2Impl;
import ru.ppr.core.helper.Toaster;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.EdsException;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscaldocument.PdSaleDocumentStateSyncronizer;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Фрагмент печати при продаже доплаты
 */
public class ExtraPaymentPrintFragment extends FragmentParent implements FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentPrintFragment.class);
    public static final String FRAGMENT_TAG = ExtraPaymentPrintFragment.class.getSimpleName();

    public interface OnInteractionListener {
        DataSalePD getDataSalePD();

        void startWorkWithPosTerminal(@Nullable SaleType saleType, String param);

        void printPdSuccess(long saleEventId, BigDecimal pdCost);

        void onCancelSellProcess();
    }

    //region Di
    private ExtraPaymentPrintComponent component;
    @Inject
    PosManager posManager;
    @Inject
    Toaster toaster;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    @Inject
    EdsManager edsManager;
    @Inject
    PdSaleDocumentStateSyncronizer pdSaleDocumentStateSyncronizer;
    //endregion
    //region Views
    private SimpleLseView simpleLseView;
    private View cashPaymentCancelView;
    private TextView cashPaymentCancelAmount;
    private Button cashPaymentCancelContinueBtn;
    //endregion
    //region Other
    private OnInteractionListener onInteractionListener;
    //endregion

    public static ExtraPaymentPrintFragment newInstance() {
        return new ExtraPaymentPrintFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnInteractionListener)
            onInteractionListener = (OnInteractionListener) activity;
        else
            throw new IllegalStateException(activity.toString() + " must implement " + OnInteractionListener.class.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerExtraPaymentPrintComponent.builder().appComponent(Dagger.appComponent()).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extra_payment_print, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        cashPaymentCancelView = view.findViewById(R.id.cashPaymentCancelView);
        cashPaymentCancelAmount = (TextView) view.findViewById(R.id.cashPaymentCancelAmount);
        cashPaymentCancelContinueBtn = (Button) view.findViewById(R.id.cashPaymentCancelContinueBtn);
        cashPaymentCancelContinueBtn.setOnClickListener(v -> {
            if (onInteractionListener != null) {
                onInteractionListener.onCancelSellProcess();
            }
        });

        printPD();

        return view;
    }

    private DataSalePD getData() {
        return onInteractionListener.getDataSalePD();
    }

    private void printPD() {
        Logger.info(TAG, "printPD() START");

        showPrintingPd();

        // Проверяем наличие билетной ленты
        ticketTapeChecker.checkOrThrow()
                // Синхронизируем состояние последнего чека
                .andThen(pdSaleDocumentStateSyncronizer.syncBeforePrint())
                // Проверяем работоспособность sft
                .andThen(Single
                        .fromCallable(() -> {
                            final SignDataResult signDataResult = Di.INSTANCE.getEdsManager().pingEds();

                            if (!signDataResult.isSuccessful()) {
                                throw new EdsException(EdsException.ERROR_SIGN_DATA);
                            }

                            return signDataResult;
                        })
                        .subscribeOn(SchedulersCPPK.eds()))
                .observeOn(SchedulersCPPK.printer())
                .flatMap(syncStateResult -> new DocumentSalePd()
                        .setDataSalePD(getData())
                        // Создаем событие в БД со статусом PRE_PRINTING
                        .initCppkTicketSale()
                        // Печатем ПД
                        .flatMap(pdSaleDocumentStateSyncronizer::printWithSync)
                        .observeOn(SchedulersCPPK.background())
                        // Обновляем событие в БД до статуса CHECK_PRINTED
                        .flatMap(DocumentSalePd::updateCPPKTicketSale)
                        .doOnSuccess(documentSalePd -> getData().setSaleDateTime(documentSalePd.getSaleDateTime()))
                        // Формируем подпись для данных ШК
                        .flatMap(documentSalePd -> Single.fromCallable(() -> {
                            ParentTicketInfo parentTicketInfo = getData().getParentTicketInfo();

                            PdV2Impl pdV2 = new PdV2Impl();
                            pdV2.setSaleDateTime(getData().getSaleDateTime());
                            pdV2.setOrderNumber(getData().getPDNumber());
                            pdV2.setTariffCode(getData().getTariffThere().getCode());
                            pdV2.setStartDayOffset(getData().getTerm());
                            pdV2.setPaymentType(getData().getPaymentType() == PaymentType.INDIVIDUAL_CASH ?
                                    PdWithPaymentType.PAYMENT_TYPE_CASH : PdWithPaymentType.PAYMENT_TYPE_CARD);
                            pdV2.setDirection(getData().getDirection() == TicketWayType.TwoWay ?
                                    PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                            pdV2.setSourceOrderNumber(parentTicketInfo.getTicketNumber());
                            pdV2.setSourceSaleDateTime(parentTicketInfo.getSaleDateTime());
                            pdV2.setSourceDeviceId(parentTicketInfo.getCashRegisterNumber());

                            PdEncoder pdEncoder = Dagger.appComponent().pdEncoderFactory().create(pdV2);
                            return pdEncoder.encodeWithoutEdsKeyNumber(pdV2);
                        })
                                .observeOn(SchedulersCPPK.eds())
                                .flatMap(unsignedRawData -> Single.fromCallable(() -> edsManager.signData(unsignedRawData, documentSalePd.getSaleDateTime())))
                                .observeOn(SchedulersCPPK.background())
                                .doOnSuccess(documentSalePd::setSignDataResult)
                                .map(signDataResult -> documentSalePd))
                        // Печатаем ШК
                        .flatMap(DocumentSalePd::printBarcode)
                        // Обновляем событие в БД до статуса COMPLETED
                        .flatMap(DocumentSalePd::completeCppkTicketSale)
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<DocumentSalePd>() {
                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, e);

                        if (pdSaleDocumentStateSyncronizer.isInFrButNotPrinted(e)) {
                            // http://agile.srvdev.ru/browse/CPPKPP-38173
                            // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                            // Сообщаем пользователю о необходимости аннулирования ПД.
                            Logger.info(TAG, "Команда печати завершилась с ошибкой, однако синхронизатор установил что чек лег на ФР и обновил статус для билета");
                            showPrintingFailedAndCheckInFr();
                        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                            Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                            // Чтобы окно с ошибкой установки билетной ленты было показано раньше,
                            // чем фракмент с ошибкой печати, используется задержка.
                            // Если за 200 мс Активити стартовать не успеет, то самое страшное,
                            // что может случиться - это перед появлением активити установки
                            // билетной ленты моргнет фрагмент с ошибкой печати
                            Observable.timer(200, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(o -> showPrintingPdFailed());
                        } else if (e instanceof PrinterException) {
                            showPrintingPdFailed();
                        } else if (e instanceof IncorrectEKLZNumberException) {
                            showNeedActivateEKLZ();
                        } else if (e instanceof EdsException) {
                            // это нормально https://aj.srvdev.ru/browse/CPPKPP-26425
                            EmergencyModeHelper.startEmergencyMode(e);
                        } else {
                            EmergencyModeHelper.startEmergencyMode(e);
                        }
                    }

                    @Override
                    public void onSuccess(DocumentSalePd documentSalePd) {
                        Logger.trace(TAG, "printPD() - onNext() - Print is successful");
                        onInteractionListener.printPdSuccess(documentSalePd.getSaleTicketId(), getData().getTotalCostValueWithDiscount());
                    }
                });
    }

    private void showPrintingFailedAndCheckInFr() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.extra_payment_print_fail_cancel_required_msg);
        stateBuilder.setButton1(R.string.extra_payment_print_fail_cancel_required_back_btn, v -> onInteractionListener.onCancelSellProcess());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    void showPrintingPd() {
        Logger.trace(TAG, "showPrintingPd");
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.printing_pd);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        cashPaymentCancelView.setVisibility(View.GONE);
    }

    void showNeedActivateEKLZ() {
        Logger.trace(TAG, "showNeedActivateEKLZ");
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
        stateBuilder.setButton1(R.string.cancelOperation, v -> {
            Logger.trace(TAG, "showNeedActivateEKLZ() - нажали кнопку 'Отменить'");
            getActivity().finish();
        });
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        cashPaymentCancelView.setVisibility(View.GONE);
    }

    void showPrintingPdFailed() {
        Logger.trace(TAG, "showPrintingPdFailed");
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.printing_pd_failed);
        stateBuilder.setButton1(R.string.terminal_cancel, v -> {
            Logger.trace(TAG, "showPrintingPdFailed() - нажали кнопку 'Отменить оплату'");
            showReturnMoneyConfirmationDialog();
        });
        stateBuilder.setButton2(R.string.terminal_retry, v -> {
            Logger.trace(TAG, "showPrintingPdFailed() - нажали кнопку 'Повторить'");
            printPD();
        });
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
        cashPaymentCancelView.setVisibility(View.GONE);
    }

    void showReturnMoneyConfirmationDialog() {
        Logger.trace(TAG, "showReturnMoneyConfirmationDialog");
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.dialog_cancel_message),
                getString(R.string.dialog_cancel_ok),
                getString(R.string.dialog_cancel_nope),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            showCancelPaymentView();
        });
    }

    void showCancelPaymentView() {
        Logger.trace(TAG, "showCancelPaymentView");
        if (getData().getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
            cashPaymentCancelAmount.setText(String.format(getString(R.string.rub_cent_as_single), getData().getTotalCostValueWithDiscount()));
            cashPaymentCancelView.setVisibility(View.VISIBLE);
            simpleLseView.hide();
        } else if (getData().getPaymentType() == PaymentType.INDIVIDUAL_BANK_CARD) {
            if (!posManager.isReady())
                toaster.showToast(R.string.terminal_is_busy);
            else {
                onInteractionListener.startWorkWithPosTerminal(null, String.valueOf(getData().getBankTransactionEvent().getId()));
            }
        }
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

}
