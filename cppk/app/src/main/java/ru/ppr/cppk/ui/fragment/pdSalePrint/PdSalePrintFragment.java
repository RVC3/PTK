package ru.ppr.cppk.ui.fragment.pdSalePrint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.legacy.EdsException;
import ru.ppr.cppk.logic.BarcodeBuilder;
import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscaldocument.PdSaleDocumentStateSyncronizer;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран печати ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSalePrintFragment extends FragmentParent implements FragmentOnBackPressed {

    public static final String TAG = Logger.makeLogTag(PdSalePrintFragment.class);
    public static final String FRAGMENT_TAG = PdSalePrintFragment.class.getSimpleName();

    public static PdSalePrintFragment newInstance() {
        return new PdSalePrintFragment();
    }

    //region Di
    private PdSalePrintComponent component;
    @Inject
    EdsManager edsManager;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    @Inject
    PdSaleDocumentStateSyncronizer pdSaleDocumentStateSyncronizer;
    @Inject
    PdEncoderFactory pdEncoderFactory;
    @Inject
    BarcodeBuilder barcodeBuilder;
    @Inject
    TicketCategoryChecker ticketCategoryChecker;
    //endregion
    //region Views
    private SimpleLseView simpleLseView;
    //endregion
    //region Other
    private InteractionListener interactionListener;
    private boolean initialized = false;
    private boolean viewCreated = false;
    private DataSalePD dataSalePD;
    //endregion

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        component = DaggerPdSalePrintComponent.builder().appComponent(Dagger.appComponent()).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_sale_print, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);

        showPrintingPd();

        viewCreated = true;
        if (initialized) {
            onInitialize();
        }

        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    public void initialize(DataSalePD dataSalePD) {
        Logger.debug(TAG, "initialize() called with: dataSalePD = [" + dataSalePD + "] initialized == "+ initialized);
        Logger.error(TAG, "initialize: ", new Exception());
        if (!initialized) {
            this.initialized = true;
            this.dataSalePD = dataSalePD;
            if (viewCreated) {
                onInitialize();
            }
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        printPD();
    }

    private void printPD() {

        denyScreenLock();

        Logger.info(TAG, "Start print pd");

        // Проверяем наличие билетной ленты
        ticketTapeChecker.checkOrThrow()
                // Синхронизируем состояние последнего чека
                .andThen(pdSaleDocumentStateSyncronizer.syncBeforePrint())
                // Проверяем работоспособность sft
                .andThen(Completable
                        .fromCallable(() -> {
                            final SignDataResult signDataResult = edsManager.pingEds();

                            if (!signDataResult.isSuccessful()) {
                                throw new EdsException(EdsException.ERROR_SIGN_DATA);
                            }

                            return signDataResult;
                        })
                        .subscribeOn(SchedulersCPPK.eds()))
                .observeOn(SchedulersCPPK.printer())
                .andThen(new DocumentSalePd()
                        .setDataSalePD(dataSalePD)
                        // Создаем событие в БД со статусом PRE_PRINTING
                        .initCppkTicketSale()
                        // Печатем ПД
                        .flatMap(pdSaleDocumentStateSyncronizer::printWithSync))
                .observeOn(SchedulersCPPK.background())
                // Обновляем событие в БД до статуса CHECK_PRINTED
                .flatMap(DocumentSalePd::updateCPPKTicketSale)
                .doOnSuccess(documentSalePd -> dataSalePD.setSaleDateTime(documentSalePd.getSaleDateTime()))
                // Формируем подпись для данных ШК
                .flatMap(documentSalePd -> Single
                        .fromCallable(() -> {
                            Pd pd = barcodeBuilder.buildAsPd(dataSalePD);
                            PdEncoder pdEncoder = pdEncoderFactory.create(pd);
                            return pdEncoder.encodeWithoutEdsKeyNumber(pd);
                        })
                        .observeOn(SchedulersCPPK.eds())
                        .flatMap(unsignedRawData -> Single.fromCallable(() -> edsManager.signData(unsignedRawData, documentSalePd.getSaleDateTime())))
                        .observeOn(SchedulersCPPK.background())
                        .doOnSuccess(documentSalePd::setSignDataResult)
                        .map(signDataResult -> documentSalePd))
                .flatMap(documentSalePd -> {
                    if (ticketCategoryChecker.isTrainBaggageTicket(dataSalePD.getTicketType().getTicketCategory(getNsiDaoSession()).getCode())) {
                        // Если это багаж, пропускаем печать ШК
                        return Single.just(documentSalePd);
                    } else {
                        // Если это не багаж, печатаем ШК
                        return documentSalePd.printBarcode();
                    }
                })
                .doOnSuccess(documentSalePd -> dataSalePD.setTicketWritten(true))
                // Обновляем событие в БД до статуса COMPLETED
                .flatMap(DocumentSalePd::completeCppkTicketSale)
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(documentSalePd -> {
                    Logger.info(TAG, "Print pd success, id = " + documentSalePd.getSaleTicketId());
                    interactionListener.onPrintCompleted(documentSalePd.getSaleTicketId());
                }, throwable -> {
                    Logger.error(TAG, "Error print pd", throwable);

                    if (pdSaleDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
                        // http://agile.srvdev.ru/browse/CPPKPP-38173
                        // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                        // Сообщаем пользователю о необходимости аннулирования ПД.
                        Logger.info(TAG, "Команда печати завершилась с ошибкой, однако синхронизатор установил что чек лег на ФР и обновил статус для билета");
                        showPrintingFailedAndCheckInFr();
                    } else if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                        // Чтобы окно с ошибкой установки билетной ленты было показано раньше,
                        // чем фракмент с ошибкой печати, используется задержка.
                        // Если за 200 мс Активити стартовать не успеет, то самое страшное,
                        // что может случиться - это перед появлением активити установки
                        // билетной ленты моргнет фрагмент с ошибкой печати
                        Observable.timer(200, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(o -> showPrintingPdFailed());
                    } else if (throwable instanceof ShiftTimeOutException) {
                        showShiftTimeoutException();
                    } else if (throwable instanceof ShiftNotOpenedException) {
                        showIncorrectFrStateError();
                    } else if (throwable instanceof PrinterException) {
                        showPrintingPdFailed();
                    } else if (throwable instanceof IncorrectEKLZNumberException) {
                        showNeedActivateEKLZ();
                    } else if (throwable instanceof EdsException) {
                        // Это нормально https://aj.srvdev.ru/browse/CPPKPP-26425
                        EmergencyModeHelper.startEmergencyMode(throwable);
                    } else {
                        EmergencyModeHelper.startEmergencyMode(throwable);
                    }
                });
    }

    void showPrintingPd() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
        stateBuilder.setTextMessage(R.string.printing_pd);
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    void showNeedActivateEKLZ() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
        stateBuilder.setButton1(R.string.cancelOperation, v -> getActivity().finish());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    void showPrintingPdFailed() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.printing_pd_failed);
        stateBuilder.setButton1(R.string.terminal_cancel, v -> showReturnMoneyConfirmationDialog());
        stateBuilder.setButton2(R.string.terminal_retry, v -> {
            showPrintingPd();
            printPD();
        });
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    void showReturnMoneyConfirmationDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.dialog_cancel_message),
                getString(R.string.dialog_cancel_ok),
                getString(R.string.dialog_cancel_nope),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            interactionListener.onReturnMoneyRequired();
        });
    }

    void showPrintingFailedAndCheckInFr() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_sale_print_fail_cancel_required_msg);
        stateBuilder.setButton1(R.string.pd_sale_print_fail_cancel_required_back_btn, v -> interactionListener.onCancelSaleProcess());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    void showShiftTimeoutException() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.time_for_shift_ended);
        // В будущем: 10.06.2016 Нужно ли спрашивать про печать необязательных ведомостей?
        stateBuilder.setButton1(R.string.closeShift, v -> {
            Navigator.navigateToCloseShiftActivity(getActivity(), true, false);
        });
        simpleLseView.setState(stateBuilder.build());
    }

    void showIncorrectFrStateError() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.incorrect_fr_state);
        // В будущем: 10.06.2016 Нужно ли спрашивать про печать необязательных ведомостей?
        stateBuilder.setButton1(R.string.closeShift, v -> {
            Navigator.navigateToCloseShiftActivity(getActivity(), true, false);
        });
        simpleLseView.setState(stateBuilder.build());
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        allowScreenLock();
    }

    public interface InteractionListener {

        void onReturnMoneyRequired();

        void onPrintCompleted(long saleEventId);

        void onCancelSaleProcess();
    }
}
