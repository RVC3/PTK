package ru.ppr.cppk.repeal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
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
import ru.ppr.core.dataCarrier.pd.v64.PdV64Impl;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.rfid.RfidReaderFuture;
import ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes.Result;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReSign;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.event.model34.WritePdToBscError;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.BscReader;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.legacy.EdsException;
import ru.ppr.cppk.legacy.SmartCardBuilder;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.utils.CppkUtils;
import ru.ppr.cppk.utils.ecp.EcpDataCreator;
import ru.ppr.cppk.utils.ecp.SmartCardEcpDataCreator;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;
import rx.Completable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

/**
 * Экран записи билета заглушки во время аннулирования ПД, записанного на БСК.
 *
 * @author A.Ushakov
 */
public class DeletePdActivity extends SystemBarActivity {

    public static final String TAG = Logger.makeLogTag(DeletePdActivity.class);

    private static final int START = 0;
    private static final int FIND_CARD = 1;
    private static final int DELETE_PD = 2;
    private static final int DONE = 3;
    private static final int ERROR = 4;
    private static final int SIGN_DATA = 5;
    private static final int READ_CARD = 6;

    // EXTRAS
    private static final String EXTRA_CRYSTAL_NUMBER_CARD_FOR_DELETE = "EXTRA_CRYSTAL_NUMBER_CARD_FOR_DELETE";
    private static final String EXTRA_PD_NUMBER = "EXTRA_PD_NUMBER";

    public static Intent getCallingIntent(Context context, String uidCardForDelete, int pdNumber) {
        Intent intent = new Intent(context, DeletePdActivity.class);
        intent.putExtra(EXTRA_CRYSTAL_NUMBER_CARD_FOR_DELETE, uidCardForDelete);
        intent.putExtra(EXTRA_PD_NUMBER, pdNumber);
        return intent;
    }

    /**
     * Возвращает результат выполнения операции удаления ПД с карты.
     *
     * @param resultCode Код результата
     * @param intent     Данные результа
     * @return {@code true} если удаление завершено успешно, {@code false} иначе
     */
    public static boolean getResultFromIntent(int resultCode, @Nullable final Intent intent) {
        return resultCode == Activity.RESULT_OK;
    }

    // region Di
    private DeletePdComponent component;
    @Inject
    LocalDaoSession localDaoSession;
    @Inject
    FindCardTaskFactory findCardTaskFactory;
    @Inject
    PdEncoderFactory pdEncoderFactory;
    @Inject
    EdsManager edsManager;
    @Inject
    EventBuilder eventBuilder;
    // endregion
    // region Views
    private View readCardState;
    private View failState;
    private TextView statusOperationTextView;
    private TextView errorType;
    //endregion
    //region Other
    // endregion

    private String crystalNumber;
    private int pdNum;
    private Date repealTimestamp;
    private final CPPKTicketReSign cppkTicketReSign = new CPPKTicketReSign();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerDeletePdComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_data_from_bsc_fragent);
        ////////////////////////////////
        crystalNumber = getIntent().getStringExtra(EXTRA_CRYSTAL_NUMBER_CARD_FOR_DELETE);
        pdNum = getIntent().getIntExtra(EXTRA_PD_NUMBER, -1);
        ////////////////////////////////
        readCardState = findViewById(R.id.readCardState);
        failState = findViewById(R.id.failState);
        statusOperationTextView = (TextView) findViewById(R.id.delete_pd_fragment_status_delete);
        errorType = (TextView) findViewById(R.id.error_type);
        findViewById(R.id.retry).setOnClickListener(v -> {
            Logger.info(TAG, "onRepeatBtnClicked");
            readCardState.setVisibility(View.VISIBLE);
            failState.setVisibility(View.GONE);
            performDelete(crystalNumber);
        });
        findViewById(R.id.cancel).setOnClickListener(v -> {
            Logger.info(TAG, "onCancelBtnClicked");
            returnResult(false);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        performDelete(crystalNumber);
    }

    /**
     * Запускает удаление ПД с БСК.
     *
     * @param uidCard Uid карты, с которой нужно удалить ПД
     */
    private void performDelete(@Nullable String uidCard) {

        if (uidCard == null || uidCard.isEmpty()) {
            Logger.info(TAG, "Uid card for delete pd is null. pd is not delete");
            returnResult(false);
        } else {
            startDelete(uidCard);
        }
    }

    /**
     * ПРоизводит запись билета и ЭЦП на карту
     *
     * @param ecp             сформированная эцп
     * @param ecpKey          ключ эцп
     * @param pdData          данные билета
     * @param pdPosition      позиция для записи билета
     * @param cardUID         ид карты на которую записываем
     * @param bscReader       ридер
     * @param needCleanPdList флаг необходимости затереть старые ПД перед записью
     * @return
     */
    private WriteToCardResult writeDataToCard(@NonNull byte[] ecp,
                                              long ecpKey,
                                              @NonNull byte[] pdData,
                                              int pdPosition,
                                              byte[] cardUID,
                                              BscReader bscReader,
                                              boolean needCleanPdList) {

        ByteBuffer ecpDataBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ecpDataBuffer.putInt(EcpUtils.convertLongToInt(ecpKey));

        byte[] ecpKeyData = ecpDataBuffer.array();

        Logger.info(TAG, "ecp - " + CommonUtils.bytesToHexWithSpaces(ecp));
        Logger.info(TAG, "ecpKey - " + CommonUtils.bytesToHexWithSpaces(ecpKeyData));
        Logger.info(TAG, "pdData - " + CommonUtils.bytesToHexWithSpaces(pdData));
        Logger.info(TAG, "pdPosition - " + pdPosition);
        Logger.info(TAG, "uidCard - " + CommonUtils.bytesToHexWithSpaces(cardUID));

        byte[] fullPd = new byte[pdData.length + ecpKeyData.length];
        System.arraycopy(pdData, 0, fullPd, 0, pdData.length);
        System.arraycopy(ecpKeyData, 0, fullPd, pdData.length, ecpKeyData.length);

        WriteToCardResult resultWritePd = bscReader.writePD(pdPosition, fullPd, needCleanPdList);
        // result.isError всегда == false
        if (!WriteToCardResult.SUCCESS.equals(resultWritePd)) {
            Logger.info(TAG, "Error while write PD - " + resultWritePd.toString());
            return resultWritePd;
        }
        resultWritePd = bscReader.writeEcp(ecp, cardUID);
        if (!WriteToCardResult.SUCCESS.equals(resultWritePd)) {
            Logger.info(TAG, "Error while write ecp - " + resultWritePd.toString());
        }

        return resultWritePd;
    }

    /**
     * Возвращает результат в вызывающую активити
     * Если запись билета заглушки завершена корректо, то возвращает код RESULT_OK
     * Иначе возвращает код RESULT_CANCELED
     *
     * @param deleted {@code true} если заглушка была успешно записана, {@code false} - иначе
     */
    private void returnResult(boolean deleted) {
        if (deleted) {
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
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
            Logger.info(TAG, "Some of fields are null: " +
                    cppkTicketReSign.getTicketNumber() + ", " +
                    cppkTicketReSign.getSaleDateTime() + ", " +
                    cppkTicketReSign.getTicketDeviceId() + ", " +
                    cppkTicketReSign.getEdsKeyNumber() + ", " +
                    cppkTicketReSign.getReSignDateTime());

            return;
        }

        localDaoSession.beginTransaction();
        try {
            // добавляем информацию о ПТК
            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
            if (stationDevice != null) {
                localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
            }
            Event event = eventBuilder
                    .setDeviceId(stationDevice.getId())
                    .build();
            localDaoSession.getEventDao().insertOrThrow(event);

            cppkTicketReSign.setEventId(event.getId());
            localDaoSession.getCppkTicketReSignDao().insertOrThrow(cppkTicketReSign);

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }
    }

    @Override
    public void onBackPressed() {
        /*NOP*/
    }

    @Override
    public void onClickSettings() {
        /* NOP */
    }

    private void startDelete(@NonNull final String crystalNumberCardForDeletePd) {
        Logger.info(TAG, "StartDelete: START");
        BehaviorSubject<Integer> integerBehaviorSubject = BehaviorSubject.create(START);
        integerBehaviorSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {

                            String message;
                            switch (integer) {

                                case START:
                                    message = getString(R.string.start_repeal);
                                    break;

                                case FIND_CARD:
                                    message = getString(R.string.find_bsc);
                                    break;

                                case DELETE_PD:
                                    message = getString(R.string.deletePdFromCard);
                                    break;

                                case SIGN_DATA:
                                    message = getString(R.string.sign_data);
                                    break;
                                case ERROR:
                                default:
                                    message = getString(R.string.unknown_error);
                            }
                            statusOperationTextView.setText(message);
                        },
                        throwable -> {


                            String message;
                            String errorType;
                            if (throwable instanceof DeletePdException) {
                                DeletePdException exception = (DeletePdException) throwable;
                                message = exception.deleteError.name();
                                if (exception.deleteError.equals(WritePdToBscError.CARD_NOT_FOUND)) {
                                    errorType = getString(R.string.write_to_bsc_card_not_found);
                                } else {
                                    errorType = getString(R.string.write_to_bsc_deleting_bsc_failed);
                                }
                            } else {
                                message = throwable.getMessage();
                                errorType = getString(R.string.unknown_error);
                            }
                            Logger.info(TAG, "StartDelete: Error delete pd from card - " + message);

                            this.errorType.setText(errorType);
                            failState.setVisibility(View.VISIBLE);
                            readCardState.setVisibility(View.GONE);
                        },
                        () -> {
                            Logger.info(TAG, "StartDelete: Delete pd done success");
                            returnResult(true);
                        });

        Logger.info(TAG, "StartDelete: Start delete pd from card with crystal number - " + crystalNumberCardForDeletePd);

        Single
                .fromCallable(() -> {
                    final SignDataResult signDataResult = edsManager.pingEds();

                    if (!signDataResult.isSuccessful()) {
                        throw new EdsException(EdsException.ERROR_SIGN_DATA);
                    }

                    return signDataResult;
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(o -> Single.fromCallable(() -> {
                    Logger.info(TAG, "StartDelete: Find card");
                    integerBehaviorSubject.onNext(FIND_CARD);
                    RfidReaderFuture callable = RfidReaderFuture.createCallable(findCardTaskFactory);

                    Future<Pair<BscReader, BscInformation>> submit;
                    Pair<BscReader, BscInformation> result;

                    try {
                        submit = SchedulersCPPK.rfidExecutorService().submit(callable);
                        result = submit.get(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        Logger.info(TAG, "StartDelete: Card not found");
                        Logger.error(TAG, "FUTURE EXCEPTION", e);
                        callable.cancel();
                        throw new DeletePdException(WritePdToBscError.CARD_NOT_FOUND);
                    }

                    BscReader reader = result.first;

                    if (reader == null) {
                        Logger.info(TAG, "StartDelete: Error - BscReader is null");
                        throw new DeletePdException(WritePdToBscError.CARD_NOT_FOUND);
                    }

                    BscInformation bscInformation = result.second;

                    if (bscInformation == null) {
                        Logger.info(TAG, "StartDelete: Error -  BscInformation is null");
                        throw new DeletePdException(WritePdToBscError.READ_ERROR);
                    }

                    String currentCrystalNumber = new SmartCardBuilder().setBscInformation(bscInformation).build().getCrystalSerialNumber();
                    if (!CppkUtils.equalsRfidCrystalNumber(crystalNumberCardForDeletePd, currentCrystalNumber)) {
                        Logger.info(TAG, "StartDelete: UID current card (" + crystalNumberCardForDeletePd + ") not equals " +
                                "with uid card (" + crystalNumberCardForDeletePd + ") which was wrote pd");
                        throw new DeletePdException(WritePdToBscError.CARDS_NOT_MATCH);
                    }

                    List<PD> pdList = new ArrayList<>();
                    if (reader.getMaxPdCount() > 0) {
                        Result<List<PD>> pdListOnCard = reader.readPd(bscInformation, null);
                        if (pdListOnCard.isError()) {
                            Logger.info(TAG, "StartDelete: Error read pd list from card - " + pdListOnCard.getTextError());
                            throw new DeletePdException(WritePdToBscError.READ_ERROR);
                        }
                        pdList = pdListOnCard.getResult();
                    }

                    //сформируем данные для билета-заглушки
                    repealTimestamp = new Date();


                    PdV64Impl pdV64 = new PdV64Impl();
                    pdV64.setSaleDateTime(repealTimestamp);

                    PdEncoder pdEncoder = pdEncoderFactory.create(pdV64);
                    byte[] capPdData = pdEncoder.encodeWithoutEdsKeyNumber(pdV64);

                    Logger.info(TAG, "StartDelete: Cap pd data - " + CommonUtils.bytesToHexWithSpaces(capPdData));

                    // Т.к. на карте могут быть 2 билета, то билет которой остается на карте должен
                    // участвовать в аннулирование, поэтому попробуем его найти.
                    // Т.к. мы знаем порядковый номер аннулируемого билета, то по другому номеру
                    // попробуем получить билет с карты, который будет участвовать в подписи

                    PD oldPd = null;
                    int oldPdNumber;
                    if (pdNum == 0) {
                        oldPdNumber = 1;
                    } else {
                        oldPdNumber = 0;
                    }

                    for (PD pd : pdList) {
                        if (pd.orderNumberPdOnCard == oldPdNumber) {
                            oldPd = pd;
                            break;
                        }
                    }

                    //формируем данные для подписи
                    EcpDataCreator ecpDataCreator = new SmartCardEcpDataCreator.Builder(capPdData, bscInformation)
                            .setExistPd(oldPd).build();
                    byte[] signData = ecpDataCreator.create();
                    if (signData == null) {
                        Logger.info(TAG, "StartDelete: Error create data for sign");
                        throw new DeletePdException(WritePdToBscError.DATA_ERROR);
                    }

                    final List<PD> oldPdHolder = new ArrayList<>();
                    oldPdHolder.add(oldPd);

                    return Completable
                            .fromAction(() -> Dagger.appComponent().pdSignChecker().check(oldPdHolder))
                            .doOnCompleted(() -> {
                                PD tmp = oldPdHolder.get(0);

                                //если мы можем взять данные из второго билета
                                if (tmp != null) {
                                    cppkTicketReSign.setTicketNumber(tmp.numberPD == -1 ? null : tmp.numberPD);
                                    cppkTicketReSign.setSaleDateTime(tmp.saleDatetimePD);
                                    cppkTicketReSign.setTicketDeviceId(tmp.deviceId == -1 ? null : String.valueOf(tmp.deviceId));
                                    cppkTicketReSign.setReSignDateTime(repealTimestamp);
                                }

                                Logger.info(TAG, "StartDelete: Data for sign - " + CommonUtils.bytesToHexWithSpaces(signData));
                            })
                            .andThen(Single.just(new DeletePdData(bscInformation, reader, signData, capPdData, pdNum, false)));
                }))
                .flatMap(deletePdDataSingle -> deletePdDataSingle)
                .observeOn(SchedulersCPPK.eds())
                .flatMap(deletePdData -> {
                    Logger.info(TAG, "StartDelete: Start sign data");
                    integerBehaviorSubject.onNext(SIGN_DATA);
                    return Single
                            .fromCallable(() -> edsManager.signData(deletePdData.dataForSign, new Date()))
                            .observeOn(SchedulersCPPK.background())
                            .map((SignDataResult signDataResult) -> {
                                deletePdData.signDataResult = signDataResult;
                                return deletePdData;
                            })
                            .subscribeOn(SchedulersCPPK.eds());
                })
                .observeOn(SchedulersCPPK.background())
                .flatMap(deletePdData -> Single.fromCallable(() -> {

                    Logger.info(TAG, "StartDelete: Data sign with result - " + deletePdData.signDataResult.isSuccessful());
                    if (!deletePdData.signDataResult.isSuccessful()) {
                        Logger.info(TAG, "StartDelete: Error sign data");
                        throw new DeletePdException(WritePdToBscError.DATA_ERROR);
                    }

                    cppkTicketReSign.setEdsKeyNumber(deletePdData.signDataResult.getEdsKeyNumber());

                    Logger.info(TAG, "StartDelete: Start delete pd from card");
                    integerBehaviorSubject.onNext(DELETE_PD);
                    return writeDataToCard(deletePdData.signDataResult.getSignature(),
                            deletePdData.signDataResult.getEdsKeyNumber(),
                            deletePdData.dataForWrite,
                            deletePdData.positionForCapPd,
                            deletePdData.bscInformation.getCardUID(),
                            deletePdData.bscReader,
                            deletePdData.needCleanPdList);
                }))
                .flatMap(writeToCardResult1 -> {
                    if (!writeToCardResult1.isOk()) {
                        return Single.error(new DeletePdException(WritePdToBscError.WRITE_ERROR));
                    }

                    Logger.info(TAG, "saveCPPKTicketReSign()");

                    saveCPPKTicketReSign();

                    return Single.just(new Object());
                })
                .subscribeOn(SchedulersCPPK.eds())
                .subscribe(
                        o -> integerBehaviorSubject.onCompleted(),
                        integerBehaviorSubject::onError);
    }

    private static class DeletePdException extends Exception {

        private final WritePdToBscError deleteError;

        private DeletePdException(WritePdToBscError deleteError) {
            this.deleteError = deleteError;
        }
    }

    private static class DeletePdData {
        final BscInformation bscInformation;
        final BscReader bscReader;
        final byte[] dataForSign;
        final byte[] dataForWrite;
        private final int positionForCapPd;
        private final boolean needCleanPdList;
        SignDataResult signDataResult;

        private DeletePdData(BscInformation bscInformation, BscReader bscReader, byte[] dataForSign,
                             byte[] dataForWrite, int positionForCapPd, boolean needCleanPdList) {
            this.bscInformation = bscInformation;
            this.bscReader = bscReader;
            this.dataForSign = dataForSign;
            this.dataForWrite = dataForWrite;
            this.positionForCapPd = positionForCapPd;
            this.needCleanPdList = needCleanPdList;
        }
    }

}
