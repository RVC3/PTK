package ru.ppr.cppk.pd.utils.reader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;
import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeReader;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTask;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;
import ru.ppr.cppk.dataCarrier.PdToLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeData;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeDataStorage;
import ru.ppr.cppk.logic.coupon.ReadCouponFromBarcodeHandler;
import ru.ppr.cppk.logic.pd.PdHandler;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.repository.TariffRepository;
import rx.Observable;
import rx.Subscription;

/**
 * Данный класс производит считывание ПД со Штрихкода
 * Результатом работы данного класса будет являться список считанных билетов(т.к. на ШК всегда находится только 1 ПД,
 * то и размер списка равен 1)
 * Если данный класс возвращает null - ошибка считывания ШК, ШК поврежден
 * Если данный класс возвращает пустой список - считанные данные не корректны
 *
 * @author A.Ushakov
 */
public class ReadBarcodeData {

    public static final String TAG = Logger.makeLogTag(ReadBarcodeData.class);

    private final ReadBarcodeTaskFactory readBarcodeTaskFactory;
    private final PdHandler pdHandler;
    private final ReadCouponFromBarcodeHandler readCouponFromBarcodeHandler;
    private final Listener listener;
    private final AtomicBoolean readStarted;
    private final AtomicBoolean isWaitResult;
    private final PdControlBarcodeDataStorage pdControlBarcodeDataStorage;

    private Subscription readBarcodeSubscription;

    public ReadBarcodeData(@NonNull ReadBarcodeTaskFactory readBarcodeTaskFactory,
                           @NonNull PdHandler pdHandler,
                           @NonNull ReadCouponFromBarcodeHandler readCouponFromBarcodeHandler,
                           @NonNull Listener listener,
                           PdControlBarcodeDataStorage pdControlBarcodeDataStorage) {
        Logger.trace(TAG, "ReadBarcodeData: Конструктор, создаем объект");

        this.readBarcodeTaskFactory = readBarcodeTaskFactory;
        this.listener = listener;
        this.pdHandler = pdHandler;
        this.readCouponFromBarcodeHandler = readCouponFromBarcodeHandler;
        this.readStarted = new AtomicBoolean(false);
        this.isWaitResult = new AtomicBoolean(true);
        this.pdControlBarcodeDataStorage = pdControlBarcodeDataStorage;
    }

    /**
     * Запускает сканирование ШК
     */
    public void runPdRead() {
        Logger.trace(TAG, "RunPdRead: START");

        if (readStarted.getAndSet(true)) {
            return;
        }

        isWaitResult.set(true);

        ReadBarcodeTask readBarcodeTask = readBarcodeTaskFactory.create();

        readBarcodeSubscription = Observable
                .create(subscriber -> {
                    subscriber.add(new Subscription() {
                        private boolean unSubscribed;

                        @Override
                        public void unsubscribe() {
                            readBarcodeTask.cancel();
                            unSubscribed = true;
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return unSubscribed;
                        }
                    });

                    pdControlBarcodeDataStorage.clearBarcodeData();
                    BarcodeReader barcodeReader = readBarcodeTask.read();

                    if (barcodeReader instanceof PdBarcodeReader) {
                        List<PD> pdList = null;
                        PdBarcodeReader pdBarcodeReader = (PdBarcodeReader) barcodeReader;
                        ReadBarcodeResult<Pd> pdResult = pdBarcodeReader.readPd();

                        if (pdResult.isSuccess()) {
                            Pd pd = pdResult.getData();

                            if (pd instanceof PdWithoutPlace) {
                                Logger.trace(TAG, "Pd without place read");

                                PD legacyPd = new PdToLegacyMapper().toLegacyPd(pd, null, null);

                                if (legacyPd != null) {
                                    legacyPd.orderNumberPdOnCard = 0;

                                    if (legacyPd.versionPD != PdVersion.V64.getCode()) {
                                        final TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
                                        final Long tariffCodePD = legacyPd.tariffCodePD;
                                        final NsiVersionManager nsiVersionManager = Di.INSTANCE.nsiVersionManager();
                                        final int date = nsiVersionManager.getNsiVersionIdForDate(legacyPd.getSaleDate());
                                        final Tariff tariffToCodeIgnoreDeleteFlag = tariffRepository.getTariffToCodeIgnoreDeleteFlag(tariffCodePD, date);
                                        legacyPd.setTariff(tariffToCodeIgnoreDeleteFlag);
                                    }
                                    pdList = new ArrayList<>();
                                    pdList.add(legacyPd);
                                }
                            } else if (pd instanceof PdWithPlace) {
                                // Делаем список не null, чтобы не отобразился экран ошибки
                                pdList = new ArrayList<>();
                                Logger.trace(TAG, "Pd with place read");
                            }
                            //region Костыль для legacy
                            PdControlBarcodeData pdControlBarcodeData = new PdControlBarcodeData();
                            pdControlBarcodeData.setPd(pd);
                            pdControlBarcodeDataStorage.putBarcodeData(pdControlBarcodeData);
                            //endregion
                        }
                        if (pdList != null) {
                            // Обрабатываем считанные ПД
                            pdHandler.handle(pdList);
                        }
                        postResult(pdList, null);
                    } else if (barcodeReader instanceof CouponBarcodeReader) {
                        CouponBarcodeReader couponBarcodeReader = (CouponBarcodeReader) barcodeReader;
                        ReadBarcodeResult<Coupon> couponResult = couponBarcodeReader.readCoupon();

                        if (couponResult.isSuccess()) {
                            long couponReadEventId = readCouponFromBarcodeHandler.handle(couponResult.getData());
                            if (couponReadEventId != -1) {
                                postResult(null, couponReadEventId);
                            } else {
                                postResult(null, null);
                            }
                        } else {
                            postResult(null, null);
                        }
                    } else {
                        postResult(null, null);
                    }

                    subscriber.onCompleted();
                })
                .doOnError(throwable -> {
                    Logger.error(TAG, throwable);
                    postResult(null, null);
                })
                .subscribeOn(SchedulersCPPK.barcode())
                .subscribe(o -> readBarcodeSubscription = null);
    }

    /**
     * Отдаёт результат. Пользоваться им, чтобы гарантированно отдать 1 результат после чтения
     *
     * @param pdList            список считанных билетов
     * @param couponReadEventId id талона в базе
     */
    private void postResult(@Nullable List<PD> pdList,
                            @Nullable Long couponReadEventId) {
        Logger.trace(TAG, "PostResult: pdList=" + ((pdList == null) ? null : pdList.size() + "(ListSize)") +
                " OR " + ((couponReadEventId == null ? "none" : "existed") + " coupon") + " START");

        if (pdList != null) {
            if (isWaitResult.getAndSet(false)) {
                listener.onPdListRead(pdList);
            }
        } else if (couponReadEventId != null) {
            if (isWaitResult.getAndSet(false)) {
                listener.onCouponRead(couponReadEventId);
            }
        } else {
            if (isWaitResult.getAndSet(false)) {
                listener.onError();
            }
        }

        readStarted.set(false);

        Logger.trace(TAG, "PostResult() FINISH");
    }

    /**
     * Отменить процесс чтения (не вернет результата чтения)
     */
    public void cancel() {
        Logger.trace(TAG, "cancel() START");
        if (readBarcodeSubscription != null) {
            readBarcodeSubscription.unsubscribe();
            readBarcodeSubscription = null;
        }
        if (isWaitResult.getAndSet(false)) {
            listener.onCancelled();
        }
        readStarted.set(false);
        Logger.trace(TAG, "cancel() FINISH");
    }

    /**
     * Завершить процесс чтения (вернет результат чтения)
     */
    public void stop() {
        Logger.trace(TAG, "stop() START");

        if (readBarcodeSubscription != null) {
            readBarcodeSubscription.unsubscribe();
            readBarcodeSubscription = null;
        }

        postResult(null, null);

        Logger.trace(TAG, "stop() FINISH");
    }

    public interface Listener {
        /**
         * Вызывается после успешного считывания билетов
         *
         * @param pdList список считанных билетов
         */
        void onPdListRead(@NonNull List<PD> pdList);

        /**
         * Вызывается после успешного считывания талона
         *
         * @param couponReadEventId id талона в базе
         */
        void onCouponRead(long couponReadEventId);

        /**
         * Вызывается в случае ошибки чтения/обработки
         */
        void onError();

        /**
         * Операция чтения была прервана
         */
        void onCancelled();
    }

}
