package ru.ppr.cppk.ui.fragment.pdSaleWrite;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;
import ru.ppr.core.dataCarrier.pd.v11.PdV11Impl;
import ru.ppr.core.dataCarrier.pd.v13.PdV13Impl;
import ru.ppr.core.dataCarrier.pd.v3.PdV3Impl;
import ru.ppr.core.dataCarrier.pd.v5.PdV5Impl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.logic.interactor.DeviceIdChecker;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.entity.PassageMark;
import ru.ppr.cppk.dataCarrier.rfid.RfidReaderFuture;
import ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes.Result;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReSign;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.BscInformationChecker;
import ru.ppr.cppk.legacy.BscReader;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.legacy.EdsException;
import ru.ppr.cppk.legacy.SmartCardBuilder;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.BarcodeBuilder;
import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.logic.fiscaldocument.PdSaleDocumentStateSyncronizer;
import ru.ppr.cppk.logic.interactor.ToLegacyPdListConverter;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.pd.check.write.WriteChecker;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.widget.LoggableViewFlipper;
import ru.ppr.cppk.utils.ecp.EcpDataCreator;
import ru.ppr.cppk.utils.ecp.SmartCardEcpDataCreator;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.rfid.CardReadErrorType;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.utils.CommonUtils;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Экран записи ПД на карту.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleWriteFragment extends FragmentParent implements FragmentOnBackPressed {

    public static final String TAG = Logger.makeLogTag(PdSaleWriteFragment.class);
    public static final String FRAGMENT_TAG = PdSaleWriteFragment.class.getSimpleName();

    private static final String ARG_FOR_TRANSFER = "ARG_FOR_TRANSFER";

    private enum OperationStatus {
        IN_PROCESS,
        FIND_PD,
        SEARCH_CARD,
        SIGN_DATA,
        READ_DATA_FROM_CARD,
        WRITE_PD
    }

    public static PdSaleWriteFragment newInstance(boolean forTransfer) {
        PdSaleWriteFragment pdSaleWriteFragment = new PdSaleWriteFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_FOR_TRANSFER, forTransfer);

        pdSaleWriteFragment.setArguments(bundle);

        return pdSaleWriteFragment;
    }

    //region Di
    private PdSaleWriteComponent component;
    @Inject
    EdsManager edsManager;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    @Inject
    EventBuilder eventBuilder;
    @Inject
    PdEncoderFactory pdEncoderFactory;
    @Inject
    OperationFactory operationFactory;
    @Inject
    PdSaleDocumentStateSyncronizer pdSaleDocumentStateSyncronizer;
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;
    @Inject
    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker;
    @Inject
    FindCardTaskFactory findCardTaskFactory;
    @Inject
    BarcodeBuilder barcodeBuilder;
    @Inject
    PdVersionChecker pdVersionChecker;
    @Inject
    DeviceIdChecker deviceIdChecker;
    //endregion

    /**
     * Di
     */
    private InteractionListener mInteractionListener;
    private DocumentSalePd documentSalePd;
    private boolean isPrinted = false; // флаг, сообщающий что ПД напечатан
    /**
     * Флаг, указывающий, что мы пишем трансфер
     */
    private boolean forTransfer = false;

    private long newPdId;

    /**
     * Предыдущий ПД.
     * С помощью этой переменной мы ищем, есть ли на карте ранее записанный ПД, который нужно переподписать
     */
    private PD previousPD;
    private BscInformation bscInformation;
    private SignDataResult signDataResult;
    private byte[] unsignedRawData;
    private int pdIndex;
    /**
     * Флаг необходимости стереть старые ПД с карты
     */
    private boolean needCleanPdList;
    // сюда сохраним ошибку, которая произошла во время записи на БСК, после печати фискального чека
    private WritePdToBscError error = null;
    private boolean cardTimeIsValid = true; // false означениет что время действия карты истекло
    private boolean canWritePdToThisBscType = true; // false означает, что существует запрет записи ПД на данный тип карты
    private CPPKTicketReSign cppkTicketReSign;

    private Screen screen;

    /////////////////////////
    private boolean initialized = false;
    private boolean viewCreated = false;
    private DataSalePD mDataSalePD;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerPdSaleWriteComponent.builder().appComponent(Dagger.appComponent()).build();
        component.inject(this);
        super.onCreate(savedInstanceState);

        cppkTicketReSign = new CPPKTicketReSign();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View screen = inflater.inflate(R.layout.fragment_write_to_bsc, container, false);

        screen.setFocusableInTouchMode(true);
        screen.requestFocus();

        this.screen = new Screen(screen);
        this.screen.showWaitForCard();

        viewCreated = true;
        if (initialized) {
            onInitialize();
        }

        return screen;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    public void initialize(@NonNull DataSalePD dataSalePD) {
        if (!initialized) {
            initialized = true;
            mDataSalePD = dataSalePD;
            if (viewCreated) {
                onInitialize();
            }
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        startWork();
    }

    private void startWork() {
        if (mDataSalePD.getExemptionForEvent() == null) {
            forTransfer = getArguments().getBoolean(ARG_FOR_TRANSFER);

            if (!isPrinted) {

                //В начале проверим работоспособность ЭЦП  потом запустим основной поток работы
                Single
                        .fromCallable(() -> {
                            final SignDataResult signDataResult = edsManager.pingEds();

                            if (!signDataResult.isSuccessful()) {
                                throw new EdsException(EdsException.ERROR_SIGN_DATA);
                            }

                            return signDataResult;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(SchedulersCPPK.eds())
                        .subscribe(
                                o -> readBSC(),
                                throwable -> {
                                    Logger.error(TAG, "Ecp error when sign data - " + throwable.getMessage());
                                    Navigator.navigateToSplashActivity(getActivity(), true);
                                });
            } else {
                new WriteBSC().execute();
            }
        } else {
            // Оформление с использованием льготы должно выполнятся на другом фрагменте
            throw new IllegalStateException("Sell with exemption should incorrect here");
        }
    }

    private void printTicketWithBarcode() {
        documentSalePd = new DocumentSalePd();

        ticketTapeChecker.checkOrThrow()
                .andThen(pdSaleDocumentStateSyncronizer.syncBeforePrint())
                .andThen(documentSalePd
                        .setDataSalePD(mDataSalePD)
                        .initCppkTicketSale()
                        .flatMap(pdSaleDocumentStateSyncronizer::printWithSync)
                        .flatMap(DocumentSalePd::updateCPPKTicketSale)
                        .flatMap(documentSalePd -> {
                            isPrinted = true;
                            mDataSalePD.setSaleDateTime(documentSalePd.getSaleDateTime());

                            Pd pd = barcodeBuilder.buildAsPd(mDataSalePD);
                            PdEncoder pdEncoder = pdEncoderFactory.create(pd);
                            byte[] unsignedRawData = pdEncoder.encodeWithoutEdsKeyNumber(pd);

                            return Single
                                    .fromCallable(() -> edsManager.signData(unsignedRawData, documentSalePd.getSaleDateTime()))
                                    .observeOn(SchedulersCPPK.background())
                                    .flatMap((SignDataResult signDataResult) -> {
                                        documentSalePd.setSignDataResult(signDataResult);
                                        return Single.just(documentSalePd);
                                    })
                                    .subscribeOn(SchedulersCPPK.eds());
                        })
                        .doOnSuccess(documentSalePd1 -> mDataSalePD.setTicketWritten(true)) // штрихкод напечатан, ставим флаг в true
                        .flatMap(DocumentSalePd::printBarcode)
                        .flatMap(DocumentSalePd::completeCppkTicketSale)
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        documentSalePd2 -> {
                            PdSaleWriteFragment.this.newPdId = documentSalePd.getSaleTicketId();
                            screen.showSellSuccess(true);
                        },
                        createErrorHandler());
    }

    private void printTicketForSmartCard() {
        documentSalePd = new DocumentSalePd();

        ticketTapeChecker.checkOrThrow()
                .andThen(pdSaleDocumentStateSyncronizer.syncBeforePrint())
                .andThen(documentSalePd
                        .setDataSalePD(mDataSalePD)
                        .initCppkTicketSale()
                        .flatMap(pdSaleDocumentStateSyncronizer::printWithSync)
                        .flatMap(DocumentSalePd::updateCPPKTicketSale)
                        .flatMapObservable(documentSalePd -> {
                            isPrinted = true;
                            mDataSalePD.setSaleDateTime(documentSalePd.getSaleDateTime());

                            TicketStorageType type = mDataSalePD.getSmartCard().getType();

                            Pd pd;

                            switch (type) {
                                case STR:
                                case SKM:
                                case SKMO:
                                case TRK:
                                case ETT:
                                    if (mDataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
                                        PdV3Impl pdV3 = new PdV3Impl();
                                        pd = pdV3;
                                        pdV3.setSaleDateTime(mDataSalePD.getSaleDateTime());
                                        pdV3.setOrderNumber(mDataSalePD.getPDNumber());
                                        pdV3.setTariffCode(mDataSalePD.getTariffThere().getCode());
                                        pdV3.setStartDayOffset(mDataSalePD.getTerm());
                                        pdV3.setTicketType(mDataSalePD.getExemptionForEvent() == null ?
                                                PdWithTicketType.TICKET_TYPE_FULL : PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION);
                                        pdV3.setDirection(mDataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                    } else {
                                        PdV11Impl pdV11 = new PdV11Impl();
                                        pd = pdV11;
                                        pdV11.setSaleDateTime(mDataSalePD.getSaleDateTime());
                                        pdV11.setOrderNumber(mDataSalePD.getPDNumber());
                                        pdV11.setTariffCode(mDataSalePD.getTariffThere().getCode());
                                        pdV11.setStartDayOffset(mDataSalePD.getTerm());
                                        pdV11.setTicketType(mDataSalePD.getExemptionForEvent() == null ?
                                                PdWithTicketType.TICKET_TYPE_FULL : PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION);
                                        pdV11.setDirection(mDataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                    }
                                    break;

                                case CPPK:
                                case IPK:
                                    if (mDataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
                                        PdV5Impl pdV5 = new PdV5Impl();
                                        pd = pdV5;
                                        pdV5.setSaleDateTime(mDataSalePD.getSaleDateTime());
                                        pdV5.setOrderNumber(mDataSalePD.getPDNumber());
                                        pdV5.setTariffCode(mDataSalePD.getTariffThere().getCode());
                                        pdV5.setStartDayOffset(mDataSalePD.getTerm());
                                        pdV5.setExemptionCode(mDataSalePD.getExemptionForEvent() == null ?
                                                0 : mDataSalePD.getExemptionForEvent().getExpressCode());
                                        pdV5.setDirection(mDataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                    } else {
                                        PdV13Impl pdV13 = new PdV13Impl();
                                        pd = pdV13;
                                        pdV13.setSaleDateTime(mDataSalePD.getSaleDateTime());
                                        pdV13.setOrderNumber(mDataSalePD.getPDNumber());
                                        pdV13.setTariffCode(mDataSalePD.getTariffThere().getCode());
                                        pdV13.setStartDayOffset(mDataSalePD.getTerm());
                                        pdV13.setExemptionCode(mDataSalePD.getExemptionForEvent() == null ?
                                                0 : mDataSalePD.getExemptionForEvent().getExpressCode());
                                        pdV13.setDirection(mDataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                    }
                                    break;

                                default:
                                    throw new IllegalStateException("Incorrect ticket type - " + type.name());
                            }


                            PdEncoder pdEncoder = pdEncoderFactory.create(pd);
                            unsignedRawData = pdEncoder.encodeWithoutEdsKeyNumber(pd);

                            EcpDataCreator ecpDataCreator = new SmartCardEcpDataCreator
                                    .Builder(unsignedRawData, bscInformation)
                                    .setExistPd(previousPD)
                                    .build();

                            //если мы можем взять данные из второго билета
                            if (previousPD != null) {
                                cppkTicketReSign.setTicketNumber(previousPD.numberPD == -1 ? null : previousPD.numberPD);
                                cppkTicketReSign.setSaleDateTime(previousPD.saleDatetimePD);
                                cppkTicketReSign.setTicketDeviceId(previousPD.deviceId == -1 ? null : String.valueOf(previousPD.deviceId));
                                cppkTicketReSign.setReSignDateTime(new Date());
                            }

                            byte[] dataForSign = ecpDataCreator.create();

                            return Observable
                                    .fromCallable(() -> edsManager.signData(dataForSign, documentSalePd.getSaleDateTime()))
                                    .observeOn(SchedulersCPPK.background())
                                    .flatMap((SignDataResult signDataResult) -> {
                                                PdSaleWriteFragment.this.signDataResult = signDataResult;
                                                documentSalePd.setSignDataResult(this.signDataResult);
                                                return Observable.just(documentSalePd);
                                            }
                                    )
                                    .subscribeOn(SchedulersCPPK.eds());
                        })
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        documentSalePd1 -> {
                            PdSaleWriteFragment.this.newPdId = documentSalePd.getSaleTicketId();
                            screen.showWaitForCard();
                            new WriteBSC().executeOnExecutor(SchedulersCPPK.backgroundExecutor());
                        },
                        createErrorHandler());
    }

    private Action1<Throwable> createErrorHandler() {
        return throwable -> {
            Logger.error(TAG, throwable);

            if (pdSaleDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
                // http://agile.srvdev.ru/browse/CPPKPP-38173
                // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                // Сообщаем пользователю о необходимости аннулирования ПД.
                Logger.info(TAG, "Команда печати завершилась с ошибкой, однако синхронизатор установил что чек лег на ФР и обновил статус для билета");
                screen.showPrintingFailedAndCheckInFr();
                return;
            }

            if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                Observable.timer(200, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            screen.showPrintingTicketFailed();
                        });
            } else if (throwable instanceof PrinterException) {

                if (throwable instanceof ShiftTimeOutException) {
                    screen.showShiftTimeoutException();
                } else if (throwable instanceof ShiftNotOpenedException) {
                    screen.showIncorrectShiftStateException();
                } else {
                    screen.showPrintingTicketFailed();
                }

            } else if (throwable instanceof IncorrectEKLZNumberException) {
                screen.showNeedActivateEKLZ();
            } else {
                EmergencyModeHelper.startEmergencyMode(throwable);
            }
        };
    }

    private String getBSCTypeString(TicketStorageType bscType) {
        String bscTypeString;

        if (bscType == TicketStorageType.CPPK) // mifare classic
            bscTypeString = getString(R.string.type_bsc);
        else if (bscType == TicketStorageType.CPPKCounter) // ultralight
            bscTypeString = getString(R.string.type_bsc);
        else if (bscType == TicketStorageType.SKM)
            bscTypeString = getString(R.string.type_scm);
        else if (bscType == TicketStorageType.SKMO)
            bscTypeString = getString(R.string.type_scmo);
        else if (bscType == TicketStorageType.IPK)
            bscTypeString = getString(R.string.type_ipk);
        else if (bscType == TicketStorageType.TRK)
            bscTypeString = getString(R.string.type_trk);
        else if (bscType == TicketStorageType.ETT)
            bscTypeString = getString(R.string.type_ett);
        else if (bscType == TicketStorageType.STR)
            bscTypeString = getString(R.string.type_str);
        else
            bscTypeString = getString(R.string.unknown_bsc_type);

        return bscTypeString;
    }

    /**
     * Сохраняет событие переподписи билета
     */
    private void saveCPPKTicketReSign() {
        if (cppkTicketReSign.getTicketNumber() == null ||
                cppkTicketReSign.getSaleDateTime() == null ||
                cppkTicketReSign.getTicketDeviceId() == null ||
                cppkTicketReSign.getEdsKeyNumber() == null ||
                cppkTicketReSign.getReSignDateTime() == null) {
            Logger.info(PdSaleWriteFragment.class, "Some of fields are null: " +
                    cppkTicketReSign.getTicketNumber() + ", " +
                    cppkTicketReSign.getSaleDateTime() + ", " +
                    cppkTicketReSign.getTicketDeviceId() + ", " +
                    cppkTicketReSign.getEdsKeyNumber() + ", " +
                    cppkTicketReSign.getReSignDateTime());

            return;
        }

        getLocalDaoSession().beginTransaction();
        try {
            // добавляем информацию о ПТК
            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
            if (stationDevice != null) {
                getLocalDaoSession().getStationDeviceDao().insertOrThrow(stationDevice);
            }
            Event event = eventBuilder
                    .setDeviceId(stationDevice.getId())
                    .build();
            getLocalDaoSession().getEventDao().insertOrThrow(event);

            cppkTicketReSign.setEventId(event.getId());
            getLocalDaoSession().getCppkTicketReSignDao().insertOrThrow(cppkTicketReSign);

            getLocalDaoSession().setTransactionSuccessful();
        } finally {
            getLocalDaoSession().endTransaction();
        }
    }

    private class Screen {

        private static final int WRITING = 0;
        private static final int WRITING_BUTTON = 1;
        private static final int WRITING_BUTTONS_2_LONG = 2;
        private static final int WRITING_BUTTONS_3_LONG = 3;
        private static final int WRITINGS_2 = 4;
        private static final int SHOW_REPEAL_MESSAGE = 6;

        LoggableViewFlipper layout;

        TextView writing_w;

        ImageView writing_button_icon;
        TextView writing_button_w;
        Button writing_button_b;

        ImageView writing_buttons_2_long_icon;
        TextView writing_buttons_2_long_w;
        Button writing_buttons_2_long_b_top;
        Button writing_buttons_2_long_b_bottom;

        ImageView writing_buttons_3_long_icon;
        TextView writing_buttons_3_long_w;
        Button writing_buttons_3_long_b_top;
        Button writing_buttons_3_long_b_mid;
        Button writing_buttons_3_long_b_bottom;

        TextView writings_2_w_top;
        TextView writings_2_w_bottom;

        TextView custom_cancel_full_amount;
        Button custom_cancel_ok;

        Screen(@NonNull View screen) {
            layout = (LoggableViewFlipper) screen.findViewById(R.id.layout);
            layout.setConcreteTag(PdSaleWriteFragment.class.getSimpleName());

            writing_w = (TextView) screen.findViewById(R.id.writing_w);

            writing_button_icon = (ImageView) screen.findViewById(R.id.writing_button_icon);
            writing_button_w = (TextView) screen.findViewById(R.id.writing_button_w);
            writing_button_b = (Button) screen.findViewById(R.id.writing_button_b);

            writing_buttons_2_long_icon = (ImageView) screen.findViewById(R.id.writing_buttons_2_long_icon);
            writing_buttons_2_long_w = (TextView) screen.findViewById(R.id.writing_buttons_2_long_w);
            writing_buttons_2_long_b_top = (Button) screen.findViewById(R.id.writing_buttons_2_long_b_top);
            writing_buttons_2_long_b_bottom = (Button) screen.findViewById(R.id.writing_buttons_2_long_b_bottom);

            writing_buttons_3_long_icon = (ImageView) screen.findViewById(R.id.writing_buttons_3_long_icon);
            writing_buttons_3_long_w = (TextView) screen.findViewById(R.id.writing_buttons_3_long_w);
            writing_buttons_3_long_b_top = (Button) screen.findViewById(R.id.writing_buttons_3_long_b_top);
            writing_buttons_3_long_b_mid = (Button) screen.findViewById(R.id.writing_buttons_3_long_b_mid);
            writing_buttons_3_long_b_bottom = (Button) screen.findViewById(R.id.writing_buttons_3_long_b_bottom);

            writings_2_w_top = (TextView) screen.findViewById(R.id.writings_2_w_top);
            writings_2_w_bottom = (TextView) screen.findViewById(R.id.writings_2_w_bottom);

            custom_cancel_full_amount = (TextView) screen.findViewById(R.id.custom_cancel_full_amount);
            custom_cancel_ok = (Button) screen.findViewById(R.id.custom_cancel_ok);

            View okBtn = screen.findViewById(R.id.cancel_button);
            okBtn.setOnClickListener(v1 -> {
                Logger.info(TAG, "Screen() - нажали на кнопку: " + ((Button) v1).getText());
                mInteractionListener.onCancelSaleProcess();
            });
        }

        void showWaitForCard() {
            writing_w.setText(R.string.write_to_bsc_bring_card);

            layout.setDisplayedChild(WRITING);
        }

        void showReadingBSC() {
            writing_w.setText(R.string.write_to_bsc_reading_bsc);

            layout.setDisplayedChild(WRITING);
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
                mInteractionListener.onReturnMoneyRequired();
            });
        }

        void showPrintingTicket() {
            final SmartCard smartCard = mDataSalePD.getSmartCard();
            if (smartCard != null) {
                // пишем на карту
                printTicketForSmartCard();
                writings_2_w_top.setText(getString(R.string.write_to_bsc_bsc_pattern,
                        getBSCTypeString(smartCard.getType()), smartCard.getOuterNumber()));

                writings_2_w_bottom.setText(R.string.write_to_bsc_printing_ticket);

                layout.setDisplayedChild(WRITINGS_2);
            } else {
                if (!cardTimeIsValid) {
                    writings_2_w_top.setText(R.string.write_to_bsc_card_is_expired);
                } else if (!canWritePdToThisBscType) {
                    writings_2_w_top.setText(R.string.write_to_bsc_disabled_write_to_ticket_storage_type);
                } else {
                    writings_2_w_top.setText(R.string.write_to_bsc_card_is_full);
                }
                writings_2_w_bottom.setText(R.string.printing_pd);

                layout.setDisplayedChild(WRITINGS_2);

                printTicketWithBarcode();
            }
        }

        void showTransferMustBeWrittenToCard() {
            writing_button_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            if (!cardTimeIsValid) {
                writing_button_w.setText(R.string.write_to_bsc_card_is_expired);
            } else if (!canWritePdToThisBscType) {
                writing_button_w.setText(R.string.write_to_bsc_disabled_write_to_ticket_storage_type);
            } else {
                writing_button_w.setText(R.string.write_to_bsc_card_is_full);
            }

            writing_button_b.setText(R.string.cancelOperation);
            writing_button_b.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTON);
        }

        void showWritingBSC() {
            SmartCard smartCard = mDataSalePD.getSmartCard();

            writings_2_w_top.setText(getString(R.string.write_to_bsc_bsc_pattern, getBSCTypeString(smartCard.getType()), smartCard.getOuterNumber()));

            if (mDataSalePD.getConnectionType() == ConnectionType.TRANSFER) {
                writings_2_w_bottom.setText(R.string.write_to_bsc_writing_transfer);
            } else {
                writings_2_w_bottom.setText(R.string.write_to_bsc_writing_pd);
            }

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showWritingBscFailedPrintingMsg() {
            writings_2_w_top.setText(getString(R.string.write_to_bsc_writing_bsc_failed_printing_msg));
            writings_2_w_bottom.setText("");
            layout.setDisplayedChild(WRITINGS_2);
        }

        void showSellSuccess(boolean isPrinting) {
            mInteractionListener.onWriteCompleted(newPdId, isPrinting);
        }

        void showWritingBSCFailed() {
            showWritingBSCFailed(getString(R.string.write_to_bsc_writing_bsc_failed));
        }

        void showWritingBSCFailed(String text) {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(text);

            if (isPrinted) {
                writing_buttons_2_long_b_top.setText(R.string.cancelOperation);
                writing_buttons_2_long_b_top.setOnClickListener(v -> {
                    printErrorWriteToBsc();
                });
            } else {
                writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
                writing_buttons_2_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());
            }

            writing_buttons_2_long_b_bottom.setText(R.string.repeat);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                showWaitForCard();
                startWork();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showNeedActivateEKLZ() {
            writing_button_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));
            writing_button_w.setText(R.string.printer_eklz_activation_required_msg);
            writing_button_b.setText(R.string.cancelOperation);
            writing_button_b.setOnClickListener(v -> getActivity().finish());

            layout.setDisplayedChild(WRITING_BUTTON);
        }

        void showWritingBscFailedPrintingMsgFailed() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.write_to_bsc_writing_bsc_failed_printing_msg_failed);

            writing_buttons_2_long_b_top.setText(R.string.cancelOperation);
            writing_buttons_2_long_b_top.setOnClickListener(v -> layout.setDisplayedChild(SHOW_REPEAL_MESSAGE));

            writing_buttons_2_long_b_bottom.setText(R.string.repeat);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> printErrorWriteToBsc());

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPrintingTicketFailed() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.write_to_bsc_printing_ticket_failed);

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            writing_buttons_2_long_b_bottom.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> showPrintingTicket());

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showIncorrectCardType() {
            writing_buttons_3_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_3_long_w.setText(R.string.write_to_bsc_pd_cant_be_written);

            writing_buttons_3_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_3_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            writing_buttons_3_long_b_mid.setText(R.string.repeat);
            writing_buttons_3_long_b_mid.setOnClickListener(v -> {
                showWaitForCard();
                readBSC();
            });

            writing_buttons_3_long_b_bottom.setVisibility(mDataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH ? View.VISIBLE : View.GONE);
            writing_buttons_3_long_b_bottom.setText(R.string.printPd);
            writing_buttons_3_long_b_bottom.setOnClickListener(v -> mInteractionListener.onPrintPdOnWriteDenied());

            layout.setDisplayedChild(WRITING_BUTTONS_3_LONG);
        }

        void showIncorrectCardTypeDisablePrint() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.write_to_bsc_pd_cant_be_written);

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            writing_buttons_2_long_b_bottom.setText(R.string.repeat);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                showWaitForCard();
                readBSC();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showTransferMustBeWrittenToPassengerBoundCard() {
            writing_buttons_2_long_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.write_to_bsc_transfer_must_be_written_to_passenger_bound_card);

            writing_buttons_2_long_b_top.setText(R.string.write_to_bsc_cancel_payment);
            writing_buttons_2_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            writing_buttons_2_long_b_bottom.setText(R.string.repeat);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                showWaitForCard();
                readBSC();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showCardNotFound() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.write_to_bsc_card_not_found);

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            writing_buttons_2_long_b_bottom.setText(R.string.repeat);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                showWaitForCard();
                readBSC();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showCardInStopList() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            android.util.Pair<SmartCardStopListItem, String> stopItemResult = new BscInformationChecker(
                    bscInformation,
                    nsiVersionManager,
                    smartCardCancellationReasonRepository).getStopListItem(false);

            if (stopItemResult != null) {
                writing_buttons_2_long_w.setText(getString(R.string.stop_list_message_with_reason, stopItemResult.second));
            }

            writing_buttons_2_long_b_top.setText(R.string.print_pd);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                mDataSalePD.setSmartCard(null);
                screen.showPrintingTicket();
            });

            writing_buttons_2_long_b_bottom.setText(R.string.cancelOperation);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showCardInStopListForTransfer() {
            writing_button_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            android.util.Pair<SmartCardStopListItem, String> stopItemResult = new BscInformationChecker(
                    bscInformation,
                    nsiVersionManager,
                    smartCardCancellationReasonRepository).getStopListItem(false);

            if (stopItemResult != null) {
                writing_button_w.setText(getString(R.string.stop_list_message_with_reason, stopItemResult.second));
            }

            writing_button_b.setText(R.string.cancelOperation);
            writing_button_b.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTON);
        }

        void showHasPDWithInvalidSign() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(getString(R.string.write_to_bsc_has_pd_with_invalid_sign));

            writing_buttons_2_long_b_top.setText(R.string.print_pd);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                mDataSalePD.setSmartCard(null);
                screen.showPrintingTicket();
            });

            writing_buttons_2_long_b_bottom.setText(R.string.cancelOperation);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showHasPDWithInvalidSignForTransfer() {
            writing_button_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            writing_button_w.setText(getString(R.string.write_to_bsc_has_pd_with_invalid_sign));

            writing_button_b.setText(R.string.cancelOperation);
            writing_button_b.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTON);
        }

        void showHasPDWithRevokedSignKey() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(getString(R.string.write_to_bsc_has_pd_with_revoked_sign_key));

            writing_buttons_2_long_b_top.setText(R.string.print_pd);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                mDataSalePD.setSmartCard(null);
                screen.showPrintingTicket();
            });

            writing_buttons_2_long_b_bottom.setText(R.string.cancelOperation);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showHasPDWithRevokedSignKeyForTransfer() {
            writing_button_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            writing_button_w.setText(getString(R.string.write_to_bsc_has_pd_with_revoked_sign_key));

            writing_button_b.setText(R.string.cancelOperation);
            writing_button_b.setOnClickListener(v -> showReturnMoneyConfirmationDialog());

            layout.setDisplayedChild(WRITING_BUTTON);
        }

        public void showShiftTimeoutException() {

            writing_button_icon.setImageDrawable(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.icon_failed, null));

            writing_button_w.setText(getString(R.string.time_for_shift_ended));
            writing_button_b.setText(getString(R.string.closeShift));
            writing_button_b.setOnClickListener(
                    v -> Navigator.navigateToCloseShiftActivity(getActivity(), true, false));
            layout.setDisplayedChild(WRITING_BUTTON);
        }

        public void showIncorrectShiftStateException() {

            writing_button_icon.setImageDrawable(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.icon_failed, null));

            writing_button_w.setText(getString(R.string.incorrect_fr_state));
            writing_button_b.setText(getString(R.string.closeShift));
            writing_button_b.setOnClickListener(
                    v -> Navigator.navigateToCloseShiftActivity(getActivity(), true, false));
            layout.setDisplayedChild(WRITING_BUTTON);
        }

        private void showPrintingFailedAndCheckInFr() {
            writing_button_icon.setImageDrawable(ActivityCompat.getDrawable(getActivity(), R.drawable.icon_failed));

            writing_button_w.setText(getString(R.string.pd_sale_write_fail_cancel_required_msg));

            writing_button_b.setText(R.string.pd_sale_write_fail_cancel_required_back_btn);
            writing_button_b.setOnClickListener(v -> mInteractionListener.onCancelSaleProcess());

            layout.setDisplayedChild(WRITING_BUTTON);
        }
    }

    /**
     * Сохраняет ошибку записи на бск в БД
     *
     * @param error
     */
    private void saveError(WritePdToBscError error) {

        if (error.getCode() > 0) {
            // в рамках отчета интересны ошибки с положительным кодом,
            // остальные ошибки нужны для отображения сообщений и не должны быть отражены в БД
            getLocalDaoSession().getCppkTicketSaleDao().saveErrorForSaleEvent(newPdId, error);
        }
    }

    private void printErrorWriteToBsc() {
        Completable
                .fromAction(() -> screen.showWritingBscFailedPrintingMsg())
                .observeOn(SchedulersCPPK.background())
                .andThen(Completable.fromAction(() -> saveError(error)))
                .observeOn(SchedulersCPPK.printer())
                .andThen(operationFactory.getPrintLinesOperation()
                        .setTextLines(Collections.singletonList(getString(R.string.write_to_bsc_writing_bsc_failed).toUpperCase()))
                        .setAddSpaceAtTheEnd(true)
                        .call())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    screen.layout.setDisplayedChild(Screen.SHOW_REPEAL_MESSAGE);
                }, e -> {
                    screen.showWritingBscFailedPrintingMsgFailed();
                });
    }

    private void readBSC() {
        Single
                .fromCallable(() -> {
                    Logger.info(TAG, "Start read BSC for prepare write pd");

                    if (isAdded()) {
                        getActivity().runOnUiThread(screen::showReadingBSC);
                    }

                    RfidReaderFuture callable = RfidReaderFuture.createCallable(findCardTaskFactory);
                    Future<Pair<BscReader, BscInformation>> submit;
                    try {
                        submit = SchedulersCPPK.rfidExecutorService().submit(callable);
                        return submit.get(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        callable.cancel();
                        return null;
                    }
                })
                .observeOn(SchedulersCPPK.rfid())
                .flatMap(result -> {
                    if (result == null) {
                        return Single.just(WritePdToBscError.CARD_NOT_FOUND);
                    }
                    bscInformation = result.second;
                    final BscReader tmpReader = result.first;

                    if (tmpReader == null) {
                        Logger.error(TAG, "Error create bsc reader");
                        return Single.just(WritePdToBscError.INCORRECT_CARD_TYPE);
                    }

                    if (bscInformation == null) {
                        Logger.error(TAG, "Error read service data for card");
                        return Single.just(WritePdToBscError.INCORRECT_BSC_INFO);
                    }

                    final TicketStorageType smartCardTypeBsc = bscInformation.getSmartCardTypeBsc();
                    if (smartCardTypeBsc == null || !smartCardTypeBsc.isEnabledWriteToBsc()) {
                        Logger.error(TAG, "Error - disabled ptk to write pd on this bsk type");
                        return Single.just(WritePdToBscError.INCORRECT_CARD_TYPE);
                    }

                    if (!tmpReader.canWritePd()) {
                        Logger.error(TAG, "Error - disabled ptk to write pd on this bsk type");
                        return Single.just(WritePdToBscError.INCORRECT_CARD_TYPE);
                    }

                    if (new BscInformationChecker(
                            bscInformation,
                            nsiVersionManager,
                            smartCardCancellationReasonRepository).isStopList(false)) {
                        Logger.error(TAG, "Error - card in stop list");
                        return Single.just(WritePdToBscError.CARD_IN_STOP_LIST);
                    }
                    // Только для трансфера!
                    // Проверим привязан ли к карте ФИО
                    if (forTransfer) {
                        if (mDataSalePD.getTicketType().isRequireBindedFio()) {
                            // Если вид ПД требует привязки к ФИО
                            Result<PassageMark> passDataResult = tmpReader.readPassageMark();
                            boolean isBoundToPassengerByPassageMark = !passDataResult.isError() && passDataResult.getResult() != null && passDataResult.getResult().getBoundToPassenger();
                            boolean isBoundToPassengerByBscInfo = bscInformation.isBoundToPassenger();
                            // Флаг привязки ФИО к карте может быть расположен как непосредственно на карте, так и в метке прохода,
                            // если ни в 1 из вариантов нет флага привязки ФИО, значит к карте ФИО не привязано,
                            // соответственно трансфер на данную карту писать нельзя
                            if (!isBoundToPassengerByPassageMark && !isBoundToPassengerByBscInfo) {
                                Logger.error(TAG, "Error - transfer must be written to passenger bound card");
                                return Single.just(WritePdToBscError.TRANSFER_MUST_BE_WRITTEN_TO_PASSENGER_BOUND_CARD);
                            }
                        }
                    }
                    //проверим возможность записи на данный тип БСК
                    canWritePdToThisBscType = ticketStorageTypeToTicketTypeChecker.check(bscInformation.getSmartCardTypeBsc(), mDataSalePD.getTicketType());
                    if (!canWritePdToThisBscType) {
                        //проверим возможность печати этого ПД
                        boolean canPrintPd = ticketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, mDataSalePD.getTicketType());
                        if (!canPrintPd) {
                            Logger.error(TAG, "Error - write this TicketType to this TicketStorageType and print this TicketStorageType disabled by NSI");
                            return Single.just(WritePdToBscError.WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE);
                        }
                    }

                    Logger.trace(TAG, "Start getPdListFromCard");
                    // http://agile.srvdev.ru/browse/CPPKPP-42972
                    // Определяем, является ли записанный билет ПД с местом.
                    // Если да, то в дальнейшем выводим сообщение, что карта заполнена, и выводим ПД на печать.
                    boolean hasPdWithPlace = false;
                    //http://agile.srvdev.ru/browse/CPPKPP-43054
                    //Флаг наличия абонемента на даты в полной форме
                    boolean hasSeasonTicketOnDateInFullMode = false;
                    List<PD> pdList = new ArrayList<>();
                    if (tmpReader.getMaxPdCount() > 0) {
                        Result<List<PD>> listFromCard;
                        CardReader cardReader = tmpReader.getCardReader();
                        if (cardReader instanceof ReadPdReader) {
                            ReadPdReader readPdReader = (ReadPdReader) cardReader;
                            ReadCardResult<List<Pd>> pdListResult = readPdReader.readPdList();
                            if (pdListResult.isSuccess()) {
                                List<Pd> pdListResultData = pdListResult.getData();
                                for (Pd pd : pdListResultData) {
                                    if (pd != null && pdVersionChecker.isPdWithPlace(pd.getVersion())) {
                                        Logger.warning(TAG, "readBSC() На карте записан ПД с местом на два сектора!");
                                        hasPdWithPlace = true;
                                    } else if (pd != null && pdVersionChecker.isSeasonTicketOnDates(pd.getVersion())) {
                                        Logger.warning(TAG, "readBSC() На карте записан абонемент на даты в полной форме!");
                                        hasSeasonTicketOnDateInFullMode = true;
                                    }
                                }
                                List<PD> legacyPdList = new ToLegacyPdListConverter().convert(pdListResultData, bscInformation, null);
                                listFromCard = new Result<>(legacyPdList);
                            } else {
                                listFromCard = new Result<>(tmpReader.map(pdListResult.getReadCardErrorType()), pdListResult.getDescription());
                            }
                        } else {
                            listFromCard = new Result<>(CardReadErrorType.OTHER, "readPd is not supported for " + cardReader.getClass().getSimpleName());
                        }

                        if (listFromCard.isError()) {
                            Logger.error(TAG, "Error read pd list from card - " + listFromCard.getTextError());
                            return Single.just(WritePdToBscError.READ_ERROR);
                        }
                        pdList = listFromCard.getResult();
                    }

                    // удаляем из списка билет-заглушку если он единственный, и неважно что там у него с ЭЦП
                    // см. http://agile.srvdev.ru/browse/CPPKPP-34952
                    if (pdList != null && pdList.size() == 1 && pdList.get(0).versionPD == PdVersion.V64.getCode()) {
                        pdList.clear();
                    }

                    if (pdList != null && pdList.size() > 0) {
                        Result<byte[]> ecpDataResult = tmpReader.readECP();
                        if (!ecpDataResult.isError()) {
                            for (PD pd : pdList) {
                                pd.ecp = ecpDataResult.getResult();
                            }
                        } else {
                            Logger.info(TAG, "ReadPdList: Error read ecp for pd - " + ecpDataResult.getTextError());
                            return Single.just(WritePdToBscError.READ_ERROR);
                        }
                    }

                    final List<PD> pdFinalList = pdList;

                    final boolean cardHasPdWithPlace = hasPdWithPlace;
                    final boolean cardHasSeasonTicketOnDateInFullMode = hasSeasonTicketOnDateInFullMode;
                    return Completable
                            .fromAction(() -> Dagger.appComponent().pdSignChecker().check(pdFinalList))
                            .andThen(Single.defer(() -> {
                                int pdCount = pdFinalList.size();
                                Logger.trace(TAG, "pdList size = " + pdFinalList.size());

                                boolean v64tickets = true; // Флаг, который показывает, все ли билеты на карте являются заглушками
                                boolean pdWithInvalidSign = false;  // Флаг, который показывает, есть ли на карте билеты с невалидной ЭЦП
                                boolean pdWithRevokedSign = false; // Флаг, который показывает, есть ли на карте билеты с отозванной ЭЦП
                                boolean pdWithInvalidDeviceId = false; // Флаг, который показывает, есть ли на карте билеты с невалидным deviceId
                                for (int i = 0; i < pdCount; i++) {
                                    if (!pdWithInvalidSign) {
                                        pdWithInvalidSign = pdFinalList.get(i).isInvalidEcp();
                                    }
                                    if (!pdWithRevokedSign) {
                                        pdWithRevokedSign = pdFinalList.get(i).isRevokedEcp();
                                    }
                                    if (!pdWithInvalidDeviceId) {
                                        pdWithInvalidDeviceId = !deviceIdChecker.isDeviceIdValid(pdFinalList.get(i).deviceId);
                                    }
                                    // http://agile.srvdev.ru/browse/CPPKPP-42002
                                    // Если на карте все билеты являются заглушками, то их можно перезаписывать вне зависимости от результата проверки ЭЦП
                                    if (pdFinalList.get(i).versionPD != PdVersion.V64.getCode()) {
                                        v64tickets = false;
                                    }
                                }

                                if (!v64tickets && (pdWithInvalidSign || pdWithInvalidDeviceId)) {
                                    // Если один из билетов содержит невалидный deviceId, то это значит,
                                    // что при получении данных в Infotecs произошла ошибка. Проверку ЭЦП нельзя считать корректной

                                    // https://aj.srvdev.ru/browse/CPPKPP-27959
                                    // Нельзя перезаписывать билет с невалидной ЭЦП, даже если сам ПД не валиден

                                    // На карте уже есть ПД (Логика по https://aj.srvdev.ru/browse/CPPKPP-26809)
                                    // На карте есть ПД с невалидной ЭЦП, если мы переподпишем его, по билету можно будет ездить, так нельзя.
                                    return Single.just(WritePdToBscError.PD_WITH_INVALID_SIGN);
                                }

                                if (!v64tickets && pdWithRevokedSign) {
                                    // На карте есть ПД с отозванным ключом подписи, но сам ПД не занесен в белый лист (проверка на уровне PDSignChecker).
                                    // Его нельзя переподписывать
                                    return Single.just(WritePdToBscError.PD_WITH_REVOKED_SIGN_KEY);
                                }

                                int cardCapacity = tmpReader.getMaxPdCount();

                                Logger.trace(TAG, "Card capacity = " + cardCapacity);

                                boolean[] ticketsValidity = new boolean[cardCapacity];
                                pdIndex = -1;

                                // Проверим валидность всех билетов
                                for (int i = 0; i < pdCount; i++) {
                                    WriteChecker writeChecker = new WriteChecker(pdFinalList.get(i));
                                    boolean ticketIsValid = ticketsValidity[i] = writeChecker.isValid();
                                    Logger.trace(TAG, "PD #" + i + " isValid = " + ticketIsValid);

                                    if (!ticketIsValid && pdIndex < 0) {
                                        // Есть невалидный билет, будем затирать его
                                        pdIndex = i;
                                    }
                                    //нарушен порядок билетов на карте
                                    //http://agile.srvdev.ru/browse/CPPKPP-42381
                                    if (i != pdFinalList.get(i).orderNumberPdOnCard) {
                                        Logger.warning(TAG, "findPositionObservable() Нарушен порядок билетов на карте!");
                                        return Single.just(WritePdToBscError.WRITE_ERROR);
                                    }
                                }

                                if (pdIndex < 0 && pdCount < cardCapacity && cardCapacity > 0) {
                                    // Невалидных билетов нет - есть ещё свободное место, будем писать туда
                                    pdIndex = pdFinalList.size();
                                }

                                previousPD = null; // Предыдущий ПД. С помощью этой переменной мы ищем,
                                // есть ли на карте ранее записанный ПД, который нужно переподписать

                                cardTimeIsValid = bscInformation.cardTimeIsValid();
                                //флаг попытки записи на ЭТТ с записанным абонементом на количество поездок в полной форме
                                boolean isEttWithValidLongTicket = smartCardTypeBsc == TicketStorageType.ETT && cardHasSeasonTicketOnDateInFullMode && ticketsValidity[0];
                                boolean isEttWithInvalidLongTicket = smartCardTypeBsc == TicketStorageType.ETT && cardHasSeasonTicketOnDateInFullMode && !ticketsValidity[0];
                                needCleanPdList = isEttWithInvalidLongTicket;
                                if (isEttWithValidLongTicket || isEttWithInvalidLongTicket) {
                                    Logger.warning(TAG, "Попытка поиска свободного места на ЭТТ карте с абонементом на даты в полной форме! " +
                                            "isEttWithValidLongTicket=" + isEttWithValidLongTicket + "; " +
                                            "isEttWithInvalidLongTicket=" + isEttWithInvalidLongTicket + "; " +
                                            "needCleanPdList=" + needCleanPdList);
                                }

                                if (pdIndex < 0 || !cardTimeIsValid || !canWritePdToThisBscType || cardHasPdWithPlace || isEttWithValidLongTicket) {
                                    if (forTransfer) {
                                        // для трансфера распознаём как ошибку, т.к. мы не можем
                                        // печатать бумажные трасферы
                                        Logger.error(TAG, "pdIndex < 0 (" + (pdIndex < 0) +
                                                ") || !cardTimeIsValid (" + !cardTimeIsValid +
                                                ") || !canWritePdToThisBsсType (" + !canWritePdToThisBscType + ")");
                                        return Single.just(WritePdToBscError.TRANSFER_MUST_BE_WRITTEN_TO_CARD);
                                    } else {
                                        // если печатаем билет, то smartCard установим в null,
                                        // чтобы не было информации о том, что хотели писать на карту
                                        mDataSalePD.setSmartCard(null);
                                        Logger.trace(TAG, "Clearing smart card data");
                                    }
                                } else {
                                    SmartCard smartCard = new SmartCardBuilder().setBscInformation(bscInformation).build();
                                    smartCard.setTrack(pdIndex);
                                    mDataSalePD.setSmartCard(smartCard);
                                    // предыдущий пд нужен для создания подписи
                                    if (pdIndex == 0 && pdCount == 2) {
                                        // Затираем первый
                                        previousPD = pdFinalList.get(1);
                                        Logger.trace(TAG, "Rewriting first PD");
                                    } else if (pdIndex == 1) {
                                        // Затираем или просто дописываем второй
                                        previousPD = pdFinalList.get(0);
                                        Logger.trace(TAG, "Rewriting or writing second PD");
                                    } else {
                                        Logger.trace(TAG, "Writing first PD");
                                    }
                                }

                                return Single.<WritePdToBscError>just(null);
                            }));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<WritePdToBscError>() {
                    @Override
                    public void onSuccess(WritePdToBscError value) {
                        if (isAdded()) {
                            // если ошибок не было, то приходит null
                            if (value != null) {
                                showResult(value);
                            } else {
                                screen.showPrintingTicket();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Logger.error(TAG, error);
                    }
                });
    }

    private class WriteBSC extends AsyncTask<Void, OperationStatus, WritePdToBscError> {

        @Override
        protected WritePdToBscError doInBackground(Void... params) {

            Logger.info(TAG, "Start write pd to smart card");

            if (isAdded())
                getActivity().runOnUiThread(screen::showReadingBSC);

            RfidReaderFuture callable = RfidReaderFuture.createCallable(findCardTaskFactory);
            Pair<BscReader, BscInformation> result;
            Future<Pair<BscReader, BscInformation>> submit;
            try {
                submit = SchedulersCPPK.rfidExecutorService().submit(callable);
                result = submit.get(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Logger.error(TAG, "Card not found");
                callable.cancel();
                return WritePdToBscError.CARD_NOT_FOUND;
            }

            BscReader bscReader = result.first;
            if (bscReader == null) {
                Logger.error(TAG, "Error create bsc reader");
                return WritePdToBscError.INCORRECT_CARD_TYPE;
            }

            if (result.second == null) {
                Logger.error(TAG, "Error read bsc information from card");
                return WritePdToBscError.INCORRECT_BSC_INFO;
            }

            if (!new SmartCardBuilder().setBscInformation(result.second).build().getCrystalSerialNumber()
                    .equals(new SmartCardBuilder().setBscInformation(bscInformation).build().getCrystalSerialNumber())) {
                return WritePdToBscError.CARDS_NOT_MATCH;
            }

            if (isAdded()) {
                getActivity().runOnUiThread(screen::showWritingBSC);
            }

            if (writeDataToCard(signDataResult.getSignature(), signDataResult.getEdsKeyNumber(),
                    unsignedRawData, pdIndex, bscInformation, bscReader, needCleanPdList)) {
                cppkTicketReSign.setEdsKeyNumber(signDataResult.getEdsKeyNumber());

                saveCPPKTicketReSign();

                return WritePdToBscError.SUCCESS;
            }
            return WritePdToBscError.WRITE_ERROR;
        }

        @Override
        protected void onPostExecute(WritePdToBscError writePdToBscError) {
            if (isAdded()) {
                error = writePdToBscError;
                switch (writePdToBscError) {
                    case SUCCESS:
                        screen.showSellSuccess(false);
                        break;
                    case CARDS_NOT_MATCH:
                        screen.showWritingBSCFailed(getString(R.string.write_to_bsc_card_not_match,
                                new SmartCardBuilder().setBscInformation(bscInformation).build().getOuterNumber()));
                        break;
                    default:
                        screen.showWritingBSCFailed();
                }
            }
        }
    }

    /**
     * Запись данных на карту
     *
     * @param ecp            - подпись
     * @param ecpKey         - ключ ЭЦП
     * @param pdData         - данные билета
     * @param pdPosition     - позиция билета
     * @param bscInformation - информация о карте
     * @param bscReader      - ридер
     * @param needCleanOldPd - флаг необходимости стереть старые билеты с карты перед записью
     * @return
     */
    private boolean writeDataToCard(byte[] ecp,
                                    long ecpKey,
                                    byte[] pdData,
                                    int pdPosition,
                                    BscInformation bscInformation,
                                    BscReader bscReader,
                                    boolean needCleanOldPd
    ) {
        if (pdData == null) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Pd is null, nothing to write");
            return false;
        }

        if (pdPosition < 0 || pdPosition > 1) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Incorrect position to write PD");
            return false;
        }

        if (bscReader == null) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "No reader to work with card");
            return false;
        }

        if (bscInformation == null) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Error while write pd to card - bsc information is null");
            return false;
        }

        Logger.info(TAG, "start write data");

        ByteBuffer ecpDataBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ecpDataBuffer.putInt(EcpUtils.convertLongToInt(ecpKey));
        byte[] ecpKeyData = ecpDataBuffer.array();
        if (ecp == null) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Ecp is null. Can not write pd without ECP. Return");
            return false;
        }

        Logger.info(TAG, "ECP - " + CommonUtils.bytesToHexWithoutSpaces(ecp));
        Logger.info(TAG, "ECP Key - " + CommonUtils.bytesToHexWithoutSpaces(ecpKeyData));

        byte[] fullPd = new byte[pdData.length + ecpKeyData.length];
        System.arraycopy(pdData, 0, fullPd, 0, pdData.length);
        System.arraycopy(ecpKeyData, 0, fullPd, pdData.length, ecpKeyData.length);

        final WriteToCardResult result = bscReader.writePD(pdPosition, fullPd, needCleanOldPd);

        if (!WriteToCardResult.SUCCESS.equals(result)) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Error while write PD - " + result);
            return false;
        }

        final WriteToCardResult writeEcpResult = bscReader.writeEcp(ecp, bscInformation.getCardUID());
        if (!WriteToCardResult.SUCCESS.equals(writeEcpResult)) {
            getActivity().runOnUiThread(screen::showWritingBSCFailed);
            Logger.error(TAG, "Error while write ECP - " + result);
            return false;
        }

        boolean completeResult;
        try {
            mDataSalePD.setTicketWritten(true);
            if (error != null && error.getCode() > 0) {
                // Если во время записи происходили ошибки которые нас интересуют, то их необходимо
                // сохранить даже если запись на БСК всетаки завершилась успешно
                mDataSalePD.setWriteError(error);
            }
            documentSalePd.completeCppkTicketSaleCommon();
            completeResult = true;
        } catch (Exception e) {
            Logger.error(TAG, "Error close sale event", e);
            // валим в аварийный режим
            getActivity().runOnUiThread(() ->
                    Navigator.navigateToSplashActivity(PdSaleWriteFragment.this.getActivity(), true));
            completeResult = false;
        }
        return completeResult;
    }

    private void showResult(WritePdToBscError result) {
        Logger.trace(TAG, "WritePdToBscError: " + result);
        switch (result) {
            case INCORRECT_CARD_TYPE:
                screen.showIncorrectCardType();
                break;

            case TRANSFER_MUST_BE_WRITTEN_TO_PASSENGER_BOUND_CARD:
                screen.showTransferMustBeWrittenToPassengerBoundCard();
                break;

            case WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE:
                screen.showIncorrectCardTypeDisablePrint();
                break;

            case TRANSFER_MUST_BE_WRITTEN_TO_CARD:
                screen.showTransferMustBeWrittenToCard();
                break;

            case CARD_IN_STOP_LIST:
                if (forTransfer) {
                    screen.showCardInStopListForTransfer();
                } else {
                    // Для трансфера нельзя показывать экраны с кнопкной "Распечатать ПД"
                    screen.showCardInStopList();
                }

                break;

            case PD_WITH_INVALID_SIGN:
                if (forTransfer) {
                    screen.showHasPDWithInvalidSignForTransfer();
                } else {
                    // Для трансфера нельзя показывать экраны с кнопкной "Распечатать ПД"
                    screen.showHasPDWithInvalidSign();
                }

                break;

            case PD_WITH_REVOKED_SIGN_KEY:
                if (forTransfer) {
                    screen.showHasPDWithRevokedSignKeyForTransfer();
                } else {
                    // Для трансфера нельзя показывать экраны с кнопкной "Распечатать ПД"
                    screen.showHasPDWithRevokedSignKey();
                }

                break;

            case CARD_NOT_FOUND:
            default:
                screen.showCardNotFound();
        }
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    public interface InteractionListener {

        void onPrintPdOnWriteDenied();

        void onWriteCompleted(long newPdId, boolean isPrinted);

        void onReturnMoneyRequired();

        void onCancelSaleProcess();
    }
}
