package ru.ppr.cppk.pd.utils.reader;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.cppk.Sounds.BeepPlayer;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.PassageMarkToLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.entity.PassageMark;
import ru.ppr.cppk.dataCarrier.rfid.RfidReaderFuture;
import ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes.Result;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.BscReader;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.WriteToCardResult;
import rx.Single;
import rx.Subscription;

/**
 * Данный клас выполняет считывание ПД с карт БСК, после чего проверяет ЭЦП для
 * считанных ПД.
 * <p>
 * В случае если не удалось считать какой-либо элемент ПД (сам ПД, подпись или метку прохода - Вернется вместо списака ПД null*
 * <p>
 * Если на карте метка прохода неизвестной версии или пустая, ПТК вместо нее запишет свою, без списания поездки, для случая если для данного физического типа карты такая запись разрешена.
 *
 * @author A.Ushakov
 * @deprecated Используется только при отображении результатов после записи ПД на карту.
 * Для оптимизации отсюда вырезаны все проверки валидности, т.к. в них нет смысла.
 */
@Deprecated
public class ReadRfidData {

    public static final String TAG = Logger.makeLogTag(ReadRfidData.class);

    private final FindCardTaskFactory findCardTaskFactory;
    private final BeepPlayer beepPlayer;
    private final Listener listener;
    private StartRead sr = null;
    private final AtomicBoolean readStarted;
    private final AtomicBoolean isWaitResult;
    Subscription readRfidSubscription = null;

    public ReadRfidData(@NonNull FindCardTaskFactory findCardTaskFactory,
                        @NonNull BeepPlayer beepPlayer,
                        @NonNull Listener listener) {
        this.findCardTaskFactory = findCardTaskFactory;
        Logger.trace(TAG, "ReadRfidData: Конструктор, создаем объект");
        this.beepPlayer = beepPlayer;
        this.listener = listener;
        readStarted = new AtomicBoolean(false);
        isWaitResult = new AtomicBoolean(true);
    }

    /**
     * Запускает считывание билетов с БСК
     */
    public void startRfidRead() {
        if (readStarted.getAndSet(true)) {
            return;
        }

        Logger.trace(TAG, "StartRfidRead: START");
        isWaitResult.set(true);
        if (sr != null)
            sr.stopListen();
        sr = new StartRead();
        sr.executeOnExecutor(SchedulersCPPK.backgroundExecutor());

        Logger.trace(TAG, "StartRfidRead: FINISH");
    }

    public void stop() {
        Logger.trace(TAG, "Stop: START");
        if (sr != null)
            sr.stopListen();
        postResult(null, null, false);
        Logger.trace(TAG, "Stop: FINISH");
    }

    /**
     * Публикует результат считывания с БСК
     *
     * @param pdList список билетов, либо null если произошла ошибка считывания
     */
    private void postResult(List<PD> pdList, BscInformation bscInformation, boolean needHandle) {
        Logger.trace(TAG, "PostResult: START");

        if (needHandle && isWaitResult.getAndSet(false)) {
            if (pdList != null && !pdList.isEmpty()) {
                //играем удачный рингтон
                beepPlayer.playSuccessBeep();
            } else {
                //играем неудачный рангтон
                beepPlayer.playFailBeep();
            }
            listener.readCompleted(pdList, bscInformation);
            readStarted.set(false);
        } else {
            if (isWaitResult.getAndSet(false)) {
                listener.readCompleted(pdList, bscInformation);
            }
            readStarted.set(false);
        }

        Logger.trace(TAG, "PostResult: FINISH");
    }

    private List<PD> readPdList(@NonNull BscReader readerBsc, BscInformation bscInformation,
                                @Nullable PassageMark passageMark) {
        Logger.trace(TAG, "ReadPdList: START");
        Result<List<PD>> readPdListResult = readerBsc.readPd(bscInformation, passageMark);
        List<PD> pdList = null;
        if (!readPdListResult.isError()) {
            Result<byte[]> ecpDataResult = readerBsc.readECP();
            if (!ecpDataResult.isError()) {
                pdList = readPdListResult.getResult();
                for (PD pd : pdList) {
                    pd.ecp = ecpDataResult.getResult();
                }
            } else {
                Logger.info(TAG, "ReadPdList: Error read ecp for pd - " + ecpDataResult.getTextError());
            }
        } else {
            Logger.info(TAG, "ReadPdList: Error read pd list from card: " + readPdListResult.getTextError());
        }
        Logger.trace(TAG, "ReadPdList: FINISH return " + ((pdList == null) ? null : pdList.size() + " ПД"));
        return pdList;
    }

    public class StartRead extends AsyncTask<Void, Void, List<PD>> {

        private BscInformation bscInformation = null;
        private PassageMark passageMark = null;

        private final RfidReaderFuture callable;

        public StartRead() {
            callable = RfidReaderFuture.createCallable(findCardTaskFactory);
        }

        @Override
        protected List<PD> doInBackground(Void... arg0) {

            Logger.info(TAG, "StartRead.doInBackground: START");

            List<PD> pdList = null;

            boolean isOut = false;

            Pair<BscReader, BscInformation> result = null;
            Future<Pair<BscReader, BscInformation>> submit;
            try {
                submit = SchedulersCPPK.rfidExecutorService().submit(callable);
                result = submit.get(DataCarrierReadSettings.RFID_FIND_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Logger.error(TAG, "StartRead.doInBackground: Ошибка RfidReaderFuture", e);
                callable.cancel();
                isOut = true;
            }

            if (!isOut) {
                bscInformation = result.second;

                //для реальных карт
                BscReader bscReader = result.first;
                if (bscReader == null || bscInformation == null) {
                    isOut = true;
                }

                if (!isOut) {

                    if (passageMark == null && bscInformation.getSmartCardTypeBsc().isMustHavePassageMark()) {
                        Logger.trace(TAG, "StartRead.doInBackground: START Read PassageMark...");
                        Result<PassageMark> passDataResult = bscReader.readPassageMark();
                        if (passDataResult.isError()) {
                            Logger.error(TAG, "StartRead.doInBackground: Error read passage mark");
                            //если мы сами можем восстанавливать метку на таких картах, тогда продолжим и вычитаем билеты
                            isOut = !bscInformation.getSmartCardTypeBsc().isCanRewritePassageMark();
                        } else
                            passageMark = passDataResult.getResult();
                        Logger.trace(TAG, "StartRead.doInBackground: FINISH Read PassageMark");
                    }

                    if (!isOut) {
                        // Не должно приводить к аварийному режиму, просто показываем экран с ошибкой
                        // см. http://agile.srvdev.ru/browse/CPPKPP-35253
                        if (!bscReader.canReadPd()) {
                            Logger.info(TAG, "StartRead.doInBackground: FINISH return:  null Reader not support read Pd");
                            return null;
                        }

                        if (bscReader.getMaxPdCount() > 0)
                            pdList = readPdList(bscReader, bscInformation, passageMark);
                        else
                            pdList = new ArrayList<>();
                        //если метка пустая и ПТК имеет право ее перезаписать
                        if (passageMark == null && bscInformation.getSmartCardTypeBsc().isCanRewritePassageMark()) {
                            //если на карте 1 билет
                            if (pdList != null && pdList.size() == 1 && pdList.get(0) != null) {
                                // создаем биледр для метки

                                PassageMarkV5Impl passageMarkV5 = new PassageMarkV5Impl();
                                //код станции ставим в 0 https://aj.srvdev.ru/browse/CPPKPP-29827
                                passageMarkV5.setPassageStationCode(0);
                                //255 говорит о том что списание было на ПТК
                                passageMarkV5.setPd1TurnstileNumber(PassageMark.GATE_NUMBER_FOR_PTK);
                                passageMarkV5.setPassageTypeForPd1(PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
                                //показание счетчика не трогаем, т.к, он обновляется при вычитывании метки на уровне ридера
                                passageMarkV5.setPassageStatusForPd1(PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
                                Date pdSaleDateTime = pdList.get(0).getSaleDate();
                                int pd1PassageTime = (int) TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - pdSaleDateTime.getTime()));
                                if (pd1PassageTime <= 0) {
                                    // http://agile.srvdev.ru/browse/CPPKPP-33731
                                    // Нужно сохранить хронологический порядок
                                    // Сделаем так, якобы этот проход был через 1 секунду после продажи
                                    Logger.trace(TAG, "Silent time modification: pd1PassageTime = " + pd1PassageTime + ", saleTime = " + pdSaleDateTime);
                                    pd1PassageTime = 1;
                                }
                                passageMarkV5.setPd1PassageTime(pd1PassageTime);

                                PassageMark legacyPassageMarkForWrite = new PassageMarkToLegacyMapper().toLegacyPassageMark(passageMarkV5);

                                //количество попыток записи метки прохода https://aj.srvdev.ru/browse/CPPKPP-30055
                                int maxCount = 3;
                                for (int i = 1; i <= maxCount; i++) {
                                    WriteToCardResult res = bscReader.writePassageMark(bscInformation.getCardUID(), legacyPassageMarkForWrite);
                                    if (res.isOk()) {
                                        Logger.info(TAG, "StartRead.doInBackground: Успешно обновили метку прохода с " + i + "-й попытки");
                                        passageMark = legacyPassageMarkForWrite;
                                        break;
                                    } else {
                                        Logger.info(TAG, "StartRead.doInBackground: Неудачная попытка записи метки №" + i);
                                        if (i < maxCount)
                                            continue;
                                        //если попытки записи исчерпаны и успеха небыло - прочитаем метку заново, потому что могла записаться только часть метки
                                        Result<PassageMark> readRes = bscReader.readPassageMark();
                                        //если прочитать не удалось - вернем null - тогда логика будет знать что карта битая
                                        passageMark = (readRes.isError()) ? null : readRes.getResult();
                                        Logger.error(TAG, "StartRead.doInBackground: Ошибка записи метки прохода на карту: " + res.toString());
                                    }
                                }

                                pdList.get(0).setPassageMark(passageMark);
                            }
                        }
                    }
                }
            }

            Logger.info(TAG, "StartRead.doInBackground: FINISH return: " + ((pdList == null) ? null : pdList.size() + " ПД"));

            return pdList;
        }

        @Override
        protected void onPostExecute(List<PD> result) {
            readRfidSubscription = Single
                    .create(singleSubscriber -> {
                        super.onPostExecute(result);
                        postResult(result, bscInformation, true);
                    })
                    .subscribeOn(SchedulersCPPK.background())
                    .subscribe(o -> readRfidSubscription = null);
        }

        public void stopListen() {
            if (readRfidSubscription != null) {
                readRfidSubscription.unsubscribe();
                readRfidSubscription = null;
            }

            callable.cancel();
        }
    }

    public interface Listener {
        /**
         * Вызывается после успешного считывания билетов
         *
         * @param pdList список считанных билетов
         */
        void readCompleted(@Nullable List<PD> pdList, @Nullable BscInformation bscInformation);
    }

}
