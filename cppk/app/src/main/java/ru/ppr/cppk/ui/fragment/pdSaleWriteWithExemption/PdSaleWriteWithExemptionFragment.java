package ru.ppr.cppk.ui.fragment.pdSaleWriteWithExemption;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.ETTData;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.rfid.RfidReaderFuture;
import ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes.Result;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReSign;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.BscInformationChecker;
import ru.ppr.cppk.legacy.BscReader;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.legacy.EdsException;
import ru.ppr.cppk.legacy.SmartCardBuilder;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.BarcodeBuilder;
import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.logic.exemptionChecker.unit.BeneficiaryCategoryExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.TicketStorageTypeExemptionChecker;
import ru.ppr.cppk.logic.fiscaldocument.PdSaleDocumentStateSyncronizer;
import ru.ppr.cppk.logic.interactor.ToLegacyPdListConverter;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.pd.check.write.WriteChecker;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.exception.ShiftNotOpenedException;
import ru.ppr.cppk.printer.rx.operation.base.OperationFactory;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.utils.ecp.BarcodeEcpDataCreator;
import ru.ppr.cppk.utils.ecp.EcpDataCreator;
import ru.ppr.cppk.utils.ecp.SmartCardEcpDataCreator;
import ru.ppr.cppk.utils.mapper.MapperFactory;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.exception.ShiftTimeOutException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.repository.ProhibitedTicketTypeForExemptionCategoryRepository;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.rfid.CardReadErrorType;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.security.entity.SmartCardStopListItem;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран записи ПД на карту с использованием льготы.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleWriteWithExemptionFragment extends FragmentParent implements FragmentOnBackPressed {

    public static final String TAG = Logger.makeLogTag(PdSaleWriteWithExemptionFragment.class);
    public static final String FRAGMENT_TAG = PdSaleWriteWithExemptionFragment.class.getSimpleName();

    //region Di
    private PdSaleWriteWithExemptionComponent component;
    @Inject
    EdsManager edsManager;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    @Inject
    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker;
    @Inject
    EventBuilder eventBuilder;
    @Inject
    PdEncoderFactory pdEncoderFactory;
    @Inject
    OperationFactory operationFactory;
    @Inject
    FindCardTaskFactory findCardTaskFactory;
    @Inject
    BarcodeBuilder barcodeBuilder;
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository;
    @Inject
    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;
    @Inject
    PdSaleDocumentStateSyncronizer pdSaleDocumentStateSyncronizer;
    @Inject
    PdVersionChecker pdVersionChecker;
    @Inject
    DeviceIdChecker deviceIdChecker;
    //endregion

    public static PdSaleWriteWithExemptionFragment newInstance() {
        return new PdSaleWriteWithExemptionFragment();
    }

    private InteractionListener mInteractionListener;
    private boolean fiscalTicketPrinted = false;
    private TempData tempData;
    private CPPKTicketReSign cppkTicketReSign;

    private View cancel_layout;
    private View cancel_layout_2;
    SimpleLseView simpleLseView;

    private WritePdToBscError error;
    private String stopListReason;
    /////////////////////////
    private boolean initialized = false;
    private boolean viewCreated = false;
    private DataSalePD dataSalePD;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerPdSaleWriteWithExemptionComponent.builder().appComponent(Dagger.appComponent()).build();
        component.inject(this);
        super.onCreate(savedInstanceState);

        cppkTicketReSign = new CPPKTicketReSign();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell_with_exemption, container, false);

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);
        cancel_layout = view.findViewById(R.id.cancel_layout);
        cancel_layout_2 = view.findViewById(R.id.cancel_layout_2);

        viewCreated = true;
        if (initialized) {
            onInitialize();
        }

        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    public void initialize(DataSalePD dataSalePD) {
        if (!initialized) {
            initialized = true;
            this.dataSalePD = dataSalePD;
            if (viewCreated) {
                onInitialize();
            }
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        startWriteWithExemption();
    }

    @NonNull
    private Observable<TempData> prepareDataAndPrintPdObservable() {
        return Single
                .fromCallable(() -> {
                    final SignDataResult signDataResult = edsManager.pingEds();

                    if (!signDataResult.isSuccessful()) {
                        throw new EdsException(EdsException.ERROR_SIGN_DATA);
                    }

                    return signDataResult;
                })
                .observeOn(SchedulersCPPK.background())
                .flatMapObservable(signDataResult -> findCardObservable())
                .doOnNext(tempData -> this.tempData = tempData)
                .flatMap(tempData -> readPdListObservable())
                .flatMap(tempData -> findPositionObservable())
                .flatMap(tempData -> prepareDataForWriteEventObservable())
                .flatMap(tempData -> printPdObservable())
                .subscribeOn(SchedulersCPPK.eds());
    }

    /**
     * Готовит дополнительные данные, которые понадобяться при записи события продажи.
     */
    @NonNull
    private Observable<TempData> prepareDataForWriteEventObservable() {
        return Observable.fromCallable(() -> {
            final DataSalePD data = dataSalePD;
            if (data == null) throw new WritePdException(WritePdToBscError.DATA_ERROR);

            if (tempData.pdIndex < 0) {
                // если билет все таки печатаем (на карте нет места или есть запрет на запиь на такую БСК), то надо записать smartCard с ParentTicketInfo
                SmartCard exemptionSmartCard = data.getExemptionForEvent().getSmartCardFromWhichWasReadAboutExemption();

                //тут нужно проверить, возможно стоит запрет на печать бумажного ПД
                int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();
                boolean isBlocked = !new TicketStorageTypeExemptionChecker(getNsiDaoSession())
                        .check(TicketStorageType.Paper,
                                data.getExemption(),
                                nsiVersion
                        );
                if (!isBlocked) {
                    ETTData ettData = tempData.bscInformation.getEttData();
                    String ettPassengerCategory = ettData == null ? null : ettData.getPassengerCategoryCipher();
                    isBlocked = !new BeneficiaryCategoryExemptionChecker(prohibitedTicketTypeForExemptionCategoryRepository)
                            .check(TicketStorageType.Paper,
                                    data.getTicketType().getCode(),
                                    ettPassengerCategory,
                                    nsiVersion);
                }
                if (isBlocked) {
                    /*
                     * Потому что если на карте ПД с невалидной ЭЦП, мы должны отобразить это, а не то что карта, якобы заполнена
                     */
                    // throw new WritePdException(WritePdToBscError.PRINT_PD_BLOCKED_FOR_EXEMPTION);
                    String prettyMessage = getString(R.string.print_paper_pd_banned_for_this_exemption, tempData.printingMessage);
                    throw new PrettyException(prettyMessage);
                } else {
                    //проверим можено ли печатать такие билеты
                    boolean canPrintPd = ticketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, data.getTicketType());
                    if (!canPrintPd) {
                        String prettyMessage = getString(R.string.print_paper_pd_banned_for_this_ticket_storage_type, tempData.printingMessage);
                        throw new PrettyException(prettyMessage);
                    }
                }

                if (tempData.pdList.size() > 0) {
                    ParentTicketInfo firstParentTicketInfo = MapperFactory
                            .createPdToParentTicketMapper().mapTo(tempData.pdList.get(0));
                    exemptionSmartCard.setPresentTicket1(firstParentTicketInfo);
                }
                if (tempData.pdList.size() > 1) {
                    ParentTicketInfo secondParentTicketInfo = MapperFactory
                            .createPdToParentTicketMapper().mapTo(tempData.pdList.get(1));

                    exemptionSmartCard.setPresentTicket2(secondParentTicketInfo);
                }
            }

            return tempData;
        });
    }

    private void startWriteWithExemption() {
        Observable
                .defer(() -> {
                    if (fiscalTicketPrinted) {
                        // чек распечатали, надо повторить только запись
                        return Observable.just(tempData);
                    } else {
                        // чек еще не напечатали, повторяем все сначала
                        return prepareDataAndPrintPdObservable();
                    }
                })
                .flatMap(tempData -> {
                    if (TicketStorageType.Paper.equals(tempData.typeForPd)) {
                        return Observable.just(tempData);
                    } else {
                        return writePdObservable();
                    }
                })
                .flatMap(tempData -> Observable
                        .fromCallable(() -> {
                            final DataSalePD data = dataSalePD;
                            if (data == null)
                                throw new WritePdException(WritePdToBscError.DATA_ERROR);
                            data.setTicketWritten(true);
                            if (error != null && error.getCode() > 0) {
                                // Если во время записи происходили ошибки которые нас интересуют, то их
                                // необходимо сохранить даже если запись на БСК всетаки завершилась успешно
                                data.setWriteError(error);
                            }
                            return tempData;
                        }))
                .flatMap(tempData -> tempData.documentSalePd.completeCppkTicketSale().map(documentSalePd -> tempData).flatMapObservable(Observable::just))
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tempData -> {
                            Logger.info(TAG, "Оформление билета завершено");
                            TicketStorageType type = tempData.typeForPd;
                            mInteractionListener.onWriteCompleted(tempData.documentSalePd.getSaleTicketId(), TicketStorageType.Paper.equals(type));
                        },
                        throwable -> {
                            Logger.error(TAG, throwable);

                            if (pdSaleDocumentStateSyncronizer.isInFrButNotPrinted(throwable)) {
                                // http://agile.srvdev.ru/browse/CPPKPP-38173
                                // Возникла ошибка при печати чека, в процессе синхронизации оказалось что чек лег на фискальник. Все события добавлены при синхронизации.
                                // Сообщаем пользователю о необходимости аннулирования ПД.
                                Logger.info(TAG, "Команда печати завершилась с ошибкой, однако синхронизатор установил что чек лег на ФР и обновил статус для билета");
                                showPrintingFailedAndCheckInFr();
                                return;
                            }

                            SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                            stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

                            if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                                stateBuilder.setTextMessage(R.string.printing_pd_failed);
                                stateBuilder.setButton1(R.string.repeat, defaultRepeatBtnClickListener);
                                stateBuilder.setButton2(R.string.cancelOperation, defaultCancelBtnClickListener);
                            } else if (throwable instanceof PrinterException) {

                                if (throwable instanceof ShiftTimeOutException) {
                                    stateBuilder.setTextMessage(R.string.time_for_shift_ended);
                                    stateBuilder.setButton1(R.string.closeShift, v -> {
                                        Navigator.navigateToCloseShiftActivity(getActivity(), true, false);
                                    });
                                } else if (throwable instanceof ShiftNotOpenedException) {
                                    stateBuilder.setTextMessage(R.string.incorrect_fr_state);
                                    stateBuilder.setButton1(R.string.closeShift, v -> {
                                        Navigator.navigateToCloseShiftActivity(getActivity(), true, false);
                                    });
                                } else {
                                    stateBuilder.setTextMessage(R.string.printing_pd_failed);
                                    stateBuilder.setButton1(R.string.repeat, defaultRepeatBtnClickListener);
                                    stateBuilder.setButton2(R.string.cancelOperation, defaultCancelBtnClickListener);
                                }

                            } else if (throwable instanceof IncorrectEKLZNumberException) {
                                stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
                                stateBuilder.setButton1(R.string.cancelOperation, v -> getActivity().finish());
                            } else if (throwable instanceof TimeoutException) {
                                Logger.error(TAG, "Card not found - timeout");
                                showWriteBscError(WritePdToBscError.CARD_NOT_FOUND, stateBuilder);
                            } else if (throwable instanceof WritePdException) {
                                Logger.error(TAG, "Error while sell pd with exemption - ", throwable);
                                WritePdException exception = (WritePdException) throwable;
                                showWriteBscError(exception.result, stateBuilder);
                            } else if (throwable instanceof PrettyException) {
                                Logger.error(TAG, "Error while sell pd with PrettyException - ", throwable);
                                stateBuilder.setTextMessage(throwable.getMessage());
                                stateBuilder.setButton1(R.string.repeat, defaultRepeatBtnClickListener);
                                stateBuilder.setButton2(R.string.cancelOperation, defaultCancelBtnClickListener);
                            } else if (throwable instanceof EdsException) {
                                Logger.error(TAG, "Ecp error when sign data", throwable);
                                // В будущем: 18.04.2016 Надо ли при каждом таком исключении переходить ваварийный режим, или в каких то ситуациях можно просто показать ошибку?
                                EmergencyModeHelper.startEmergencyMode(throwable);
                            } else {
                                EmergencyModeHelper.startEmergencyMode(throwable);
                            }

                            simpleLseView.setState(stateBuilder.build());
                            simpleLseView.show();
                        });
    }

    private void showPrintingFailedAndCheckInFr() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.pd_sale_write_with_exemption_fail_cancel_required_msg);
        stateBuilder.setButton1(R.string.pd_sale_write_with_exemption_fail_cancel_required_back_btn, v -> mInteractionListener.onCancelSaleProcess());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void showSearchCardState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);

        stateBuilder.setTextMessage(R.string.write_to_bsc_bring_card);

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private Observable<TempData> findCardObservable() {
        return Completable
                .fromAction(this::showSearchCardState)
                .observeOn(SchedulersCPPK.background())
                .andThen(Observable.fromCallable(() -> {
                    RfidReaderFuture callable = RfidReaderFuture.createCallable(findCardTaskFactory);
                    try {

                        Future<Pair<BscReader, BscInformation>> submit =
                                SchedulersCPPK.rfidExecutorService().submit(callable);
                        Pair<BscReader, BscInformation> result = submit
                                .get(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS);

                        BscReader reader = result.first;

                        if (reader == null) {
                            throw new WritePdException(WritePdToBscError.CARD_NOT_FOUND);
                        }

                        BscInformation bscInformation = result.second;

                        if (bscInformation == null) {
                            throw new WritePdException(WritePdToBscError.INCORRECT_BSC_INFO);
                        }

                        android.util.Pair<SmartCardStopListItem, String> stopItemResult = new BscInformationChecker(
                                bscInformation,
                                nsiVersionManager,
                                smartCardCancellationReasonRepository).getStopListItem(false);
                        if (stopItemResult != null) {
                            stopListReason = stopItemResult.second;
                            throw new WritePdException(WritePdToBscError.CARD_IN_STOP_LIST);
                        }
                        if (dataSalePD == null) {
                            throw new WritePdException(WritePdToBscError.DATA_ERROR);
                        }
                        if (!new SmartCardBuilder().setBscInformation(bscInformation).build().getCrystalSerialNumber()
                                .equals(dataSalePD.getExemptionForEvent()
                                        .getSmartCardFromWhichWasReadAboutExemption().getCrystalSerialNumber())) {
                            throw new WritePdException(WritePdToBscError.CARDS_NOT_MATCH);
                        }
                        //проверим возможность записи на данный тип БСК, не будем возвращать ошибку, если печать разрешена, т.к. там дальше нужно проверить валидность ЭЦП на билетах на карте.
                        boolean canWritePdToThisBsсType = ticketStorageTypeToTicketTypeChecker.check(bscInformation.getSmartCardTypeBsc(), dataSalePD.getTicketType());
                        if (!canWritePdToThisBsсType) {
                            //проверим возможность печати этого ПД
                            boolean canPrintPd = ticketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, dataSalePD.getTicketType());
                            if (!canPrintPd) {
                                Logger.error(TAG, "Error - write this TicketType to this TicketStorageType and print this TicketStorageType disabled by NSI");
                                throw new WritePdException(WritePdToBscError.WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE);
                            }
                        }

                        return new TempData(bscInformation, reader);
                    } finally {
                        callable.cancel();
                    }
                }))
                .timeout(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private Observable<TempData> writePdObservable() {
        return findCardObservable()
                .flatMap(observableResult -> {
                    final SmartCard smartCard = new SmartCardBuilder().setBscInformation(tempData.bscInformation).build();
                    if (smartCard.getCrystalSerialNumber()
                            .equals(new SmartCardBuilder().setBscInformation(tempData.bscInformation).build().getCrystalSerialNumber())) {
                        Logger.info(TAG, "Start write pd to card");
                        // передаем результат из аргументов метода,
                        // т.к. в нем хранится вся нужная информация
                        return writeToCardObservable();
                    } else {
                        Logger.error(TAG, "Не та карта, с которой читали билеты");
                        return Observable.error(new WritePdException(WritePdToBscError.CARDS_NOT_MATCH));
                    }
                });
    }

    private void showWriteCardState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);

        String textMessage = getString(R.string.write_to_bsc_bsc_pattern,
                getBSCTypeString(new SmartCardBuilder().setBscInformation(tempData.bscInformation).build().getType()),
                new SmartCardBuilder().setBscInformation(tempData.bscInformation).build().getOuterNumber())
                + "\n\n" + getString(R.string.write_to_bsc_writing_pd);

        stateBuilder.setTextMessage(textMessage);

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private Observable<TempData> writeToCardObservable() {
        return Completable
                .fromAction(this::showWriteCardState)
                .observeOn(SchedulersCPPK.background())
                .andThen(Observable.fromCallable(() -> {

                    BscReader bscReader = tempData.bscReader;

                    SignDataResult signDataResult = tempData.signDataResult;
                    Preconditions.checkNotNull(signDataResult);

                    ByteBuffer ecpDataBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                    ecpDataBuffer.putInt(EcpUtils.convertLongToInt(signDataResult.getEdsKeyNumber()));
                    byte[] ecpKeyData = ecpDataBuffer.array();
                    if (signDataResult.getSignature() == null) {
                        throw new WritePdException(WritePdToBscError.DATA_ERROR);
                    }

                    byte[] unsignedPd = tempData.unsignedPdData;
                    if (unsignedPd == null) {
                        throw new WritePdException(WritePdToBscError.DATA_ERROR);
                    }

                    byte[] fullPd = new byte[unsignedPd.length + ecpKeyData.length];
                    System.arraycopy(unsignedPd, 0, fullPd, 0, unsignedPd.length);
                    System.arraycopy(ecpKeyData, 0, fullPd, unsignedPd.length, ecpKeyData.length);

                    final WriteToCardResult writePdResult = bscReader.writePD(tempData.pdIndex, fullPd, tempData.needClearPdList);

                    if (!WriteToCardResult.SUCCESS.equals(writePdResult)) {
                        Logger.error(TAG, "Error write pd to card");
                        throw new WritePdException(WritePdToBscError.WRITE_ERROR);
                    }

                    final WriteToCardResult writeEcpResult = bscReader.writeEcp(signDataResult.getSignature(), tempData.bscInformation.getCardUID());

                    if (!WriteToCardResult.SUCCESS.equals(writeEcpResult)) {
                        Logger.error(TAG, "Error while write ECP");
                        throw new WritePdException(WritePdToBscError.WRITE_ERROR);
                    }

                    final DataSalePD data = dataSalePD;
                    if (data == null) {
                        throw new WritePdException(WritePdToBscError.DATA_ERROR);
                    }
                    // после записи установим флаг в true
                    data.setTicketWritten(true);

                    cppkTicketReSign.setEdsKeyNumber(signDataResult.getEdsKeyNumber());

                    saveCPPKTicketReSign();

                    return tempData;
                }))
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private void showReadCardState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);

        stateBuilder.setTextMessage(R.string.write_to_bsc_reading_bsc);

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private Observable<TempData> readPdListObservable() {
        return Completable
                .fromAction(this::showReadCardState)
                .observeOn(SchedulersCPPK.background())
                .andThen(Observable.fromCallable(() -> {

                    Logger.trace(TAG, "Start getPdListFromCard");
                    // http://agile.srvdev.ru/browse/CPPKPP-42972
                    // Определяем, является ли записанный билет ПД с местом.
                    // Если да, то в дальнейшем выводим сообщение, что карта заполнена, и выводим ПД на печать.
                    tempData.hasPdWithPlace = false;
                    List<PD> pdList = new ArrayList<>();
                    if (tempData.bscReader.getMaxPdCount() > 0) {
                        Result<List<PD>> listFromCard;
                        CardReader cardReader = tempData.bscReader.getCardReader();
                        if (cardReader instanceof ReadPdReader) {
                            ReadPdReader readPdReader = (ReadPdReader) cardReader;
                            ReadCardResult<List<Pd>> pdListResult = readPdReader.readPdList();
                            if (pdListResult.isSuccess()) {
                                List<Pd> pdListResultData = pdListResult.getData();
                                for (Pd pd : pdListResultData) {
                                    if (pd != null && pdVersionChecker.isPdWithPlace(pd.getVersion())) {
                                        tempData.hasPdWithPlace = true;
                                    }
                                }
                                List<PD> legacyPdList = new ToLegacyPdListConverter().convert(pdListResultData, tempData.bscInformation, null);
                                listFromCard = new Result<>(legacyPdList);
                            } else {
                                listFromCard = new Result<>(tempData.bscReader.map(pdListResult.getReadCardErrorType()), pdListResult.getDescription());
                            }
                        } else {
                            listFromCard = new Result<>(CardReadErrorType.OTHER, "readPd is not supported for " + cardReader.getClass().getSimpleName());
                        }

                        if (listFromCard.isError()) {
                            if (listFromCard.getErrorType() == CardReadErrorType.AUTHORIZATION) {
                                listFromCard = new Result<>(new ArrayList<>());
                                tempData.authorizationError = true;
                            } else {
                                Logger.error(TAG, "Error read pd list from card - " + listFromCard.getTextError());
                                throw new WritePdException(WritePdToBscError.READ_ERROR);
                            }
                        }
                        pdList = listFromCard.getResult();
                    }

                    // удаляем из списка билет-заглушку если он единственный, и неважно что там у него с ЭЦП
                    // см. http://agile.srvdev.ru/browse/CPPKPP-34952
                    if (pdList != null && pdList.size() == 1 && pdList.get(0).versionPD == PdVersion.V64.getCode()) {
                        pdList.clear();
                    }

                    if (pdList != null && pdList.size() > 0) {
                        Result<byte[]> ecpDataResult = tempData.bscReader.readECP();
                        if (!ecpDataResult.isError()) {
                            for (PD pd : pdList) {
                                pd.ecp = ecpDataResult.getResult();
                            }
                        } else if (ecpDataResult.getErrorType() != CardReadErrorType.AUTHORIZATION) {
                            Logger.info(TAG, "ReadPdList: Error read ecp for pd - " + ecpDataResult.getTextError());
                            throw new WritePdException(WritePdToBscError.READ_ERROR);
                        }
                    }

                    tempData.pdList = pdList;
                    return tempData;

                }))
                .doOnNext(tempData -> Dagger.appComponent().pdSignChecker().check(tempData.pdList))
                .observeOn(SchedulersCPPK.background())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Выполняет поиск места на карте для записи ПД.
     */
    private Observable<TempData> findPositionObservable() {
        return Observable.fromCallable(() -> {

            if (tempData.hasPdWithPlace) {
                Logger.warning(TAG, "findPositionObservable() На карте записан ПД с местом на два сектора!");
                tempData.pdIndex = -1;
                // Имитируем переполнение ("Отсутствие места для записи ПД")
                tempData.printingMessage = getString(R.string.bsc_overflow_message);
                // если печатаем билет, то smartCard установим в null,
                // чтобы не было информации о том, что хотели писать на карту
                dataSalePD.setSmartCard(null);
                return tempData;
            }

            final List<PD> pdFinalList = tempData.pdList;
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

                // На карте уже есть ПД (Логика по https://aj.srvdev.ru/browse/CPPKPP-26809)
                // На карте есть ПД с невалидной ЭЦП, если мы переподпишем его, по билету можно будет ездить, так нельзя.

                // https://aj.srvdev.ru/browse/CPPKPP-27959
                // Нельзя перезаписывать билет с невалидной ЭЦП, даже если сам ПД не валиден

                // Имитируем переполнение ("Отсутствие места для записи ПД")
                Logger.trace(TAG, "Rewriting PD with invalid ecp");
                tempData.pdIndex = -1;
                tempData.printingMessage = getString(R.string.printing_pd_pd_with_invalid_sign);
                // если печатаем билет, то smartCard установим в null,
                // чтобы не было информации о том, что хотели писать на карту
                dataSalePD.setSmartCard(null);
                Logger.trace(TAG, "Clearing smart card data");

                return tempData;
            }

            if (!v64tickets && pdWithRevokedSign) {
                // На карте есть ПД с отозванным ключом подписи, но сам ПД не занесен в белый лист (проверка на уровне PDSignChecker).
                // Его нельзя переподписывать
                // Имитируем переполнение ("Отсутствие места для записи ПД")
                tempData.pdIndex = -1;
                tempData.printingMessage = getString(R.string.printing_pd_pd_with_revoked_sign_key);
                // если печатаем билет, то smartCard установим в null,
                // чтобы не было информации о том, что хотели писать на карту
                dataSalePD.setSmartCard(null);
                Logger.trace(TAG, "Clearing smart card data");

                return tempData;
            }

            int cardCapacity = tempData.bscReader.getMaxPdCount();
            Logger.trace(TAG, "Card capacity = " + cardCapacity);

            boolean[] ticketsValidity = new boolean[cardCapacity];
            tempData.pdIndex = -1;

            boolean hasSeasonTicketOnDateInFullMode = false;

            // Проверим валидность всех билетов
            for (int i = 0; i < pdCount; i++) {
                WriteChecker writeChecker = new WriteChecker(pdFinalList.get(i));
                boolean ticketIsValid = ticketsValidity[i] = writeChecker.isValid();
                Logger.trace(TAG, "PD #" + i + " isValid = " + ticketIsValid);

                if (!ticketIsValid && tempData.pdIndex < 0) {
                    // Есть невалидный билет, будем затирать его
                    tempData.pdIndex = i;
                }
                if (pdFinalList.get(i) != null && pdVersionChecker.isSeasonTicketOnDates(PdVersion.getByCode(pdFinalList.get(i).versionPD))) {
                    Logger.warning(TAG, "readBSC() На карте записан абонемент на даты в полной форме!");
                    hasSeasonTicketOnDateInFullMode = true;
                }
                //нарушен порядок билетов на карте
                //http://agile.srvdev.ru/browse/CPPKPP-42381
                if (i != pdFinalList.get(i).orderNumberPdOnCard) {
                    Logger.warning(TAG, "findPositionObservable() Нарушен порядок билетов на карте!");
                    tempData.pdIndex = -1;
                    // Имитируем переполнение ("Отсутствие места для записи ПД")
                    tempData.printingMessage = getString(R.string.bsc_overflow_message);
                    // если печатаем билет, то smartCard установим в null,
                    // чтобы не было информации о том, что хотели писать на карту
                    dataSalePD.setSmartCard(null);
                    return tempData;
                }
            }

            //флаг попытки записи на ЭТТ с записанным абонементом на количество поездок в полной форме
            boolean isEttWithValidLongTicket = tempData.bscInformation.getSmartCardTypeBsc() == TicketStorageType.ETT && hasSeasonTicketOnDateInFullMode && ticketsValidity[0];
            boolean isEttWithInvalidLongTicket = tempData.bscInformation.getSmartCardTypeBsc() == TicketStorageType.ETT && hasSeasonTicketOnDateInFullMode && !ticketsValidity[0];
            tempData.needClearPdList = isEttWithInvalidLongTicket;
            if (isEttWithValidLongTicket || isEttWithInvalidLongTicket) {
                Logger.warning(TAG, "Попытка поиска свободного места на ЭТТ карте с абонементом на даты в полной форме! " +
                        "isEttWithValidLongTicket=" + isEttWithValidLongTicket + "; " +
                        "isEttWithInvalidLongTicket=" + isEttWithInvalidLongTicket + "; " +
                        "needCleanPdList=" + tempData.needClearPdList);
            }

            if (tempData.pdIndex < 0 && pdCount < cardCapacity && cardCapacity > 0 && !tempData.authorizationError) {
                if (isEttWithValidLongTicket) {
                    // На карте ЭТТ записан абонемент в полном формате
                    Logger.trace(TAG, "No place, ETT with valid long ticket");
                } else {
                    // Невалидных билетов нет, ошибки авторизации нет, но есть ещё свободное место, будем писать туда
                    tempData.pdIndex = pdFinalList.size();
                }
            }

            tempData.previousPD = null; // Предыдущий ПД. С помощью этой переменной мы ищем,
            // есть ли на карте ранее записанный ПД, который нужно переподписать

            tempData.printingMessage = null;

            //проверим нет ли запрета на запись на эту БСК, вставим здесь т.к. дальше есть такая тема что нужно показывать что на БСК есть ПД с невалидной ЭЦП
            if (tempData.pdIndex >= 0 && !ticketStorageTypeToTicketTypeChecker.check(tempData.bscInformation.getSmartCardTypeBsc(), dataSalePD.getTicketType())) {
                tempData.pdIndex = -1;
                tempData.printingMessage = getString(R.string.disabled_write_pd_to_this_bsk);
            }

            if (tempData.pdIndex < 0) {
                // если печатаем билет, то smartCard установим в null,
                // чтобы не было информации о том, что хотели писать на карту
                dataSalePD.setSmartCard(null);
                Logger.trace(TAG, "Clearing smart card data");
            } else {
                SmartCard smartCard = new SmartCardBuilder().setBscInformation(tempData.bscInformation).build();
                smartCard.setTrack(tempData.pdIndex);
                dataSalePD.setSmartCard(smartCard);
                // предыдущий пд нужен для создания подписи
                if (tempData.pdIndex == 0 && pdCount == 2) {
                    // Затираем первый
                    tempData.previousPD = pdFinalList.get(1);
                    Logger.trace(TAG, "Rewriting first PD");
                } else if (tempData.pdIndex == 1) {
                    // Затираем или просто дописываем второй
                    tempData.previousPD = pdFinalList.get(0);
                    Logger.trace(TAG, "Rewriting or writing second PD");
                } else {
                    Logger.trace(TAG, "Writing first PD");
                }
            }

            if (tempData.printingMessage == null) {
                tempData.printingMessage = getString(R.string.bsc_overflow_message);
            }

            return tempData;
        });
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
            Logger.info(PdSaleWriteWithExemptionFragment.class, "Some of fields are null: " +
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

    private void showPrintingState() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);

        String textMessage;
        if (tempData.pdIndex < 0) {
            // Нет места для записи
            textMessage = tempData.printingMessage + "\n\n" + getString(R.string.printing_pd);
        } else {
            textMessage = getString(R.string.write_to_bsc_bsc_pattern,
                    getBSCTypeString(new SmartCardBuilder().setBscInformation(tempData.bscInformation).build().getType()),
                    new SmartCardBuilder().setBscInformation(tempData.bscInformation).build().getOuterNumber()) + "\n\n" + getString(R.string.write_to_bsc_printing_ticket);
        }

        stateBuilder.setTextMessage(textMessage);

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    /**
     * Печатает ПД.
     */
    private Observable<TempData> printPdObservable() {
        return Completable
                .fromAction(this::showPrintingState)
                .andThen(ticketTapeChecker.checkOrThrow())
                .andThen(pdSaleDocumentStateSyncronizer.syncBeforePrint())
                .observeOn(SchedulersCPPK.printer())
                .andThen(new DocumentSalePd()
                        .setDataSalePD(dataSalePD)
                        .initCppkTicketSale()
                        .flatMap(pdSaleDocumentStateSyncronizer::printWithSync)
                        .flatMap(DocumentSalePd::updateCPPKTicketSale)
                        .doOnSuccess(documentSalePd -> {
                            fiscalTicketPrinted = true;
                            dataSalePD.setSaleDateTime(documentSalePd.getSaleDateTime());
                        })
                        .flatMap(documentSalePd -> {
                            EcpDataCreator ecpDataCreator;
                            final TicketStorageType type;
                            final byte[] unsignedPd;
                            if (tempData.pdIndex < 0) {
                                //печатаем чек
                                type = TicketStorageType.Paper;

                                Pd pd = barcodeBuilder.buildAsPd(dataSalePD);
                                PdEncoder pdEncoder = pdEncoderFactory.create(pd);
                                unsignedPd = pdEncoder.encodeWithoutEdsKeyNumber(pd);

                                ecpDataCreator = new BarcodeEcpDataCreator
                                        .Builder(unsignedPd)
                                        .build();
                            } else {
                                //пишем на карту
                                type = tempData.bscInformation.getSmartCardTypeBsc();

                                Pd pd;

                                switch (type) {
                                    case STR:
                                    case SKM:
                                    case SKMO:
                                    case TRK:
                                    case ETT:
                                        if (dataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
                                            PdV3Impl pdV3 = new PdV3Impl();
                                            pd = pdV3;
                                            pdV3.setSaleDateTime(dataSalePD.getSaleDateTime());
                                            pdV3.setOrderNumber(dataSalePD.getPDNumber());
                                            pdV3.setTariffCode(dataSalePD.getTariffThere().getCode());
                                            pdV3.setStartDayOffset(dataSalePD.getTerm());
                                            pdV3.setTicketType(dataSalePD.getExemptionForEvent() == null ?
                                                    PdWithTicketType.TICKET_TYPE_FULL : PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION);
                                            pdV3.setDirection(dataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                    PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                        } else {
                                            PdV11Impl pdV11 = new PdV11Impl();
                                            pd = pdV11;
                                            pdV11.setSaleDateTime(dataSalePD.getSaleDateTime());
                                            pdV11.setOrderNumber(dataSalePD.getPDNumber());
                                            pdV11.setTariffCode(dataSalePD.getTariffThere().getCode());
                                            pdV11.setStartDayOffset(dataSalePD.getTerm());
                                            pdV11.setTicketType(dataSalePD.getExemptionForEvent() == null ?
                                                    PdWithTicketType.TICKET_TYPE_FULL : PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION);
                                            pdV11.setDirection(dataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                    PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                        }
                                        break;


                                    case CPPK:
                                    case IPK:
                                        if (dataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH) {
                                            PdV5Impl pdV5 = new PdV5Impl();
                                            pd = pdV5;
                                            pdV5.setSaleDateTime(dataSalePD.getSaleDateTime());
                                            pdV5.setOrderNumber(dataSalePD.getPDNumber());
                                            pdV5.setTariffCode(dataSalePD.getTariffThere().getCode());
                                            pdV5.setStartDayOffset(dataSalePD.getTerm());
                                            pdV5.setExemptionCode(dataSalePD.getExemptionForEvent() == null ?
                                                    0 : dataSalePD.getExemptionForEvent().getExpressCode());
                                            pdV5.setDirection(dataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                    PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                        } else {
                                            PdV13Impl pdV13 = new PdV13Impl();
                                            pd = pdV13;
                                            pdV13.setSaleDateTime(dataSalePD.getSaleDateTime());
                                            pdV13.setOrderNumber(dataSalePD.getPDNumber());
                                            pdV13.setTariffCode(dataSalePD.getTariffThere().getCode());
                                            pdV13.setStartDayOffset(dataSalePD.getTerm());
                                            pdV13.setExemptionCode(dataSalePD.getExemptionForEvent() == null ?
                                                    0 : dataSalePD.getExemptionForEvent().getExpressCode());
                                            pdV13.setDirection(dataSalePD.getDirection() == TicketWayType.TwoWay ?
                                                    PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
                                        }
                                        break;

                                    default:
                                        throw new IllegalStateException("Incorrect ticket type - " + type.name());
                                }


                                PdEncoder pdEncoder = pdEncoderFactory.create(pd);
                                unsignedPd = pdEncoder.encodeWithoutEdsKeyNumber(pd);

                                SmartCardEcpDataCreator.Builder builder = new SmartCardEcpDataCreator
                                        .Builder(unsignedPd, tempData.bscInformation);
                                builder.setExistPd(tempData.previousPD);
                                ecpDataCreator = builder.build();

                                if (tempData.previousPD != null) {
                                    cppkTicketReSign.setTicketNumber(tempData.previousPD.numberPD == -1 ? null : tempData.previousPD.numberPD);
                                    cppkTicketReSign.setSaleDateTime(tempData.previousPD.saleDatetimePD);
                                    cppkTicketReSign.setTicketDeviceId(tempData.previousPD.deviceId == -1 ? null : String.valueOf(tempData.previousPD.deviceId));
                                    cppkTicketReSign.setReSignDateTime(new Date());
                                }
                            }
                            tempData.typeForPd = type;
                            tempData.unsignedPdData = unsignedPd;

                            byte[] dataForSign = ecpDataCreator.create();

                            return Single
                                    .fromCallable(() -> edsManager.signData(dataForSign, documentSalePd.getSaleDateTime()))
                                    .observeOn(SchedulersCPPK.background())
                                    .flatMap((SignDataResult signDataResult) -> {
                                        tempData.signDataResult = signDataResult;
                                        documentSalePd.setSignDataResult(signDataResult);
                                        return Single.just(documentSalePd);
                                    })
                                    .subscribeOn(SchedulersCPPK.eds());
                        })
                )
                .flatMap(documentSalePd -> {
                    if (tempData.pdIndex < 0) {
                        // после печати штрихкода установим isTicketWritten в true
                        return documentSalePd.printBarcode().doOnSuccess(documentSalePd1 -> dataSalePD.setTicketWritten(true));
                    } else {
                        return Single.just(documentSalePd);
                    }
                })
                .map(documentSalePd -> {
                    tempData.costPd = dataSalePD.getTicketCostValueWithDiscount();
                    tempData.documentSalePd = documentSalePd;
                    return tempData;
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(Observable::just);
    }

    @NonNull
    private String createMessage(WritePdToBscError result) {
        String message;

        switch (result) {
            case CARD_IN_STOP_LIST:
                message = getString(R.string.stop_list_message_with_reason, stopListReason);
                break;

            case CARD_NOT_FOUND:
                message = getString(R.string.card_not_found);
                break;

            case READ_ERROR:
                message = getString(R.string.write_to_bsc_error_read_card);
                break;

            case CARDS_NOT_MATCH:
                message = String.format(getString(R.string.write_to_bsc_card_not_match), getCardNumber());
                break;

            case WRITE_ERROR:
                message = getString(R.string.error_write_pd_to_card);
                break;

            case INCORRECT_CARD_TYPE:
                message = getString(R.string.write_to_bsc_pd_cant_be_written);
                break;

            case WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE:
                message = getString(R.string.write_to_bsc_disabled_write_to_ticket_storage_type);
                break;

            case DATA_ERROR:
            case UNKNOWN:
            default:
                message = getString(R.string.some_error);
        }

        return message;
    }

    @NonNull
    private String getCardNumber() {
        String number;
        DataSalePD dataSalePD = this.dataSalePD;
        if (dataSalePD != null) {
            number = dataSalePD.getExemptionForEvent()
                    .getSmartCardFromWhichWasReadAboutExemption().getOuterNumber();
        } else {
            number = "";
        }
        return number;
    }

    private void showWriteBscError(@NonNull WritePdToBscError result, SimpleLseView.State.Builder stateBuilder) {

        stateBuilder.setTextMessage(createMessage(result));

        if (fiscalTicketPrinted) {
            error = result;
        }

        switch (result) {

            case CARD_IN_STOP_LIST:
            case WRITE_AND_PRINT_PD_BLOCKED_FOR_TICKET_STORAGE_TYPE:
                stateBuilder.setButton1(R.string.cancelOperation, defaultCancelBtnClickListener);
                break;

            // зависит от стадии, если билет уже напечатали то, значит первое считывание прошло корректно,
            // а в момент записи поднесли не ту карту
            case INCORRECT_CARD_TYPE:
                // тут фискальный чек уже напечатан, надо повторять запись
            case WRITE_ERROR:
                // зависит от стадии, если билет уже напечатали то, значит первое считывание прошло корректно,
                // а в момент записи поднесли не ту карту
            case CARDS_NOT_MATCH:
                // если чек напечатан, то произошла ошибка чтения данных во время записи
            case READ_ERROR:
                // зависит от статуса чека
            case DATA_ERROR:
                // зависит от стадии, если билет уже напечатали то, значит первое считывание прошло
                // корректно, а в момент записи поднесли не ту карту
            case CARD_NOT_FOUND:
                // зависит от статуса чека
            case UNKNOWN:
            default:
                stateBuilder.setButton1(R.string.repeat, defaultRepeatBtnClickListener);
                stateBuilder.setButton2(R.string.cancelOperation, defaultCancelBtnClickListener);
        }
    }

    private View.OnClickListener defaultRepeatBtnClickListener = v -> startWriteWithExemption();

    private View.OnClickListener defaultCancelBtnClickListener = v -> {
        if (fiscalTicketPrinted && !WritePdToBscError.SUCCESS.equals(error) && tempData != null) {
            if (error != null && error.getCode() > 0) {
                printErrorWriteToBsc();
            } else {
                showCancelPaymentLayoutIfCheckWasPrinted();
            }
        } else {
            mInteractionListener.onReturnMoneyRequired();
        }
    };

    /**
     * Показывает сообщение о необходимости аннулирования ПД
     */
    private void showCancelPaymentLayoutIfCheckWasPrinted() {
        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.printing_pd_failed_need_cancel_warning);
        stateBuilder.setButton1(R.string.btnOk, v -> mInteractionListener.onCancelSaleProcess());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void printErrorWriteToBsc() {
        Completable
                .fromAction(() -> {
                    SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                    stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);

                    stateBuilder.setTextMessage(R.string.write_to_bsc_writing_bsc_failed_printing_msg);

                    simpleLseView.setState(stateBuilder.build());
                    simpleLseView.show();
                })
                .andThen(Completable.fromAction(() ->
                        getLocalDaoSession().getCppkTicketSaleDao().saveErrorForSaleEvent(tempData.documentSalePd.getSaleTicketId(), error)))
                .observeOn(SchedulersCPPK.printer())
                .andThen(operationFactory.getPrintLinesOperation()
                        .setTextLines(Collections.singletonList(getString(R.string.write_to_bsc_writing_bsc_failed).toUpperCase()))
                        .setAddSpaceAtTheEnd(true)
                        .call())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        aVoid -> {
                            showCancelPaymentLayoutIfCheckWasPrinted();
                        }, throwable -> {
                            Logger.error(TAG, throwable);

                            SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                            stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

                            stateBuilder.setTextMessage(R.string.write_to_bsc_writing_bsc_failed_printing_msg_failed);
                            stateBuilder.setButton1(R.string.repeat, v -> printErrorWriteToBsc());
                            stateBuilder.setButton2(R.string.cancelOperation, v -> showCancelPaymentLayoutIfCheckWasPrinted());

                            simpleLseView.setState(stateBuilder.build());
                            simpleLseView.show();
                        });
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    private static class TempData {

        final BscInformation bscInformation;
        final BscReader bscReader;
        List<PD> pdList;
        private int pdIndex;
        /**
         * Флаг необходимости стереть старые ПД с карты
         */
        private boolean needClearPdList;
        /**
         * Признак ошибки авторизации, нужен чтобы отличать ситуацию от ошибки авторизации от пустой карты.
         * Способа получше не придумал.
         */
        private boolean authorizationError;

        /**
         * Предыдущий ПД.
         * С помощью этой переменной мы ищем, есть ли на карте ранее записанный ПД, который нужно переподписать
         */
        private PD previousPD;

        TicketStorageType typeForPd; // тип, куда был записан билет
        private SignDataResult signDataResult;
        private byte[] unsignedPdData;
        private BigDecimal costPd;
        private DocumentSalePd documentSalePd;
        private String printingMessage;
        private boolean hasPdWithPlace;

        private TempData(BscInformation bscInformation, BscReader bscReader) {
            this.bscInformation = bscInformation;
            this.bscReader = bscReader;
        }
    }

    private class WritePdException extends Exception {
        final WritePdToBscError result;

        private WritePdException(WritePdToBscError result) {
            super("result = " + result);
            this.result = result;
        }
    }

    public interface InteractionListener {

        void onWriteCompleted(long newPdId, boolean isPrinted);

        void onCancelSaleProcess();

        void onReturnMoneyRequired();
    }
}
