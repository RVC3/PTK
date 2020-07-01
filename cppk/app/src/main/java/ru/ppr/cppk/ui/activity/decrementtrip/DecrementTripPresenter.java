package ru.ppr.cppk.ui.activity.decrementtrip;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkV4V5V8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageTime;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Impl;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.interactor.FindCardInteractor;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripData;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripError;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripResult;
import ru.ppr.cppk.ui.activity.decrementtrip.sharedstorage.DecrementTripDataStorage;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV23V24HwCounter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.utils.CommonUtils;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;


/**
 * @author Aleksandr Brazhkin
 */
class DecrementTripPresenter extends BaseMvpViewStatePresenter<DecrementTripView, DecrementTripViewState> {

    private static final String TAG = Logger.makeLogTag(DecrementTripPresenter.class);

    private static final int TIMER_VALUE = 4;
    /**
     * Номер турникета на станции, через который был совершен проход по ПД. На ПТК записывается 255. При продаже - 0.
     */
    public static final int GATE_NUMBER_FOR_PTK = 255; // В будущем: 06.11.2017 Убрать отсюда

    // Common fields start
    private boolean initialized = false;
    // Common fields end
    private final FindCardInteractor findCardInteractor;
    private final UiThread uiThread;
    private final PdVersionChecker pdVersionChecker;
    private final DecrementTripDataStorage decrementTripDataStorage;
    private final DecrementTripParams decrementTripParams;
    private Navigator navigator;
    private Subscription timerSubscription = Subscriptions.unsubscribed();
    private Subscription readCardSubscription = Subscriptions.unsubscribed();
    /**
     * Флаг, что информация считана и осуществлен переход на другой экран
     */
    private boolean bscResultRead = false;

    @Inject
    DecrementTripPresenter(DecrementTripViewState decrementTripViewState,
                           FindCardInteractor findCardInteractor,
                           UiThread uiThread,
                           PdVersionChecker pdVersionChecker,
                           DecrementTripDataStorage decrementTripDataStorage,
                           DecrementTripParams decrementTripParams) {
        super(decrementTripViewState);
        this.pdVersionChecker = pdVersionChecker;
        this.decrementTripDataStorage = decrementTripDataStorage;
        this.findCardInteractor = findCardInteractor;
        this.uiThread = uiThread;
        this.decrementTripParams = decrementTripParams;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        startReadCard();
    }

    void onRepeatBtnClicked() {
        Logger.trace(TAG, "onRepeatBtnClicked");
        startReadCard();
    }

    void onCancelBtnClicked() {
        Logger.trace(TAG, "onCancelBtnClicked");
        navigator.navigateToPreviousScreen(new DecrementTripResult(false));
    }

    void onRfidBtnClicked() {
        Logger.trace(TAG, "onRfidBtnClicked");
        if (readCardSubscription.isUnsubscribed()) {
            startReadCard();
        }
    }

    private void startReadCard() {
        Logger.trace(TAG, "startWriteCard");
        if (bscResultRead) {
            Logger.trace(TAG, "Result is already read, skip reading");
            return;
        }
        if (!readCardSubscription.isUnsubscribed()) {
            throw new IllegalStateException("Operation is already running");
        }
        readCardSubscription = Completable
                .fromAction(() -> {
                    decrementTripDataStorage.clearData();
                    view.setState(DecrementTripView.State.SEARCH_CARD);
                    startTimer();
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .andThen(findCardInteractor.findCard())
                .onErrorResumeNext(throwable -> {
                    Logger.error(TAG, throwable);
                    return Single.error(new DecrementTripException(DecrementTripError.CARD_NOT_FOUND));
                })
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    stopTimer();
                    view.setState(DecrementTripView.State.READ_CARD);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(cardReader -> Single.fromCallable(() -> readCard(cardReader)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    view.setState(DecrementTripView.State.PROCESSING_DATA);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(readBscResult -> Single.fromCallable(() -> {
                    if (handleResult(readBscResult)) {
                        return readBscResult;
                    } else {
                        throw new DecrementTripException(readBscResult.decrementTripError);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onBscResultRead, throwable -> {
                    Logger.error(TAG, throwable);
                    if (throwable instanceof DecrementTripException) {
                        DecrementTripError error = ((DecrementTripException) throwable).decrementTripError;
                        if (error == DecrementTripError.CARD_NOT_FOUND) {
                            view.setState(DecrementTripView.State.SEARCH_CARD_ERROR);
                            return;
                        }
                    }
                    view.setState(DecrementTripView.State.UNKNOWN_ERROR);
                });
    }

    private void stopReadCard() {
        Logger.trace(TAG, "stopReadCard");
        readCardSubscription.unsubscribe();
    }

    @NonNull
    private ReadBscResult readCard(CardReader cardReader) {
        Logger.trace(TAG, "readCard, cardReader = " + cardReader);
        if (cardReader instanceof CppkNumberOfTripsReader) {
            return decrementTrip((CppkNumberOfTripsReader) cardReader);
        } else {
            return new ReadBscResult();
        }
    }

    private ReadBscResult decrementTrip(CppkNumberOfTripsReader cardReader) {
        ReadBscResult readBscResult = new ReadBscResult();
        if (!Arrays.equals(cardReader.getCardInfo().getCardUid(), decrementTripParams.getCardUid())) {
            // Попытка списание поездки с другой карты
            Logger.info(TAG, "Required card uid = "
                    + CommonUtils.bytesToHexWithSpaces(decrementTripParams.getCardUid())
                    + "founded card uid = "
                    + CommonUtils.bytesToHexWithSpaces(cardReader.getCardInfo().getCardUid()));
            readBscResult.decrementTripError = DecrementTripError.OTHER_CARD;
            return readBscResult;
        }

        ReadCardResult<Integer> firstReadCounterResult = cardReader.readHardwareCounter(decrementTripParams.getPdIndex());
        if (!firstReadCounterResult.isSuccess()) {
            // Ошибка при предварительном считывании показаний счетчика
            Logger.trace(TAG, "First read counter error = " + firstReadCounterResult);
            readBscResult.decrementTripError = DecrementTripError.COUNTER_VALUE_NOT_READ;
            return readBscResult;
        }

        // Значение счетчика при контроле ПД
        int initialHwCounterValue = decrementTripParams.getInitialHwCounterValue();
        // Значение счетчика, которое мы стремимся получить
        int targetHwCounterValue;

        Logger.trace(TAG, "Pd version: " + decrementTripParams.getPdVersion());

        // Флаг необходимости перезаписи метки
        // По умолчанию метка перезаписывается всегда
        boolean overridePassageMark = true;

        PdVersion pdVersion = PdVersion.getByCode(decrementTripParams.getPdVersion());
        Preconditions.checkNotNull(pdVersion);

        if (pdVersionChecker.isCombinedCountTripsSeasonTicket(pdVersion)) {
            // Если это комбинированный абонемент

            // http://agile.srvdev.ru/browse/CPPKPP-42106
            // http://agile.srvdev.ru/browse/CPPKPP-42009

            PdV23V24HwCounter pdV23V24HwCounter = new PdV23V24HwCounter(initialHwCounterValue);

            // Значение счетчика всех поездов при контроле ПД
            int initialTripsTotalCounter = pdV23V24HwCounter.getTripsTotalCounter();
            // Значение счетчика 7000-х поездов при контроле ПД
            int initialTrips7000Counter = pdV23V24HwCounter.getTrips7000Counter();

            Logger.trace(TAG, "initialTripsTotalCounter = " + initialTripsTotalCounter);
            Logger.trace(TAG, "initialTrips7000Counter = " + initialTrips7000Counter);

            // Значение счетчика всех поездок, которое мы стремимся получить
            int targetTripsTotalCounter;
            boolean passage6000Valid = decrementTripParams.isPassage6000Valid();
            Logger.trace(TAG, "passage6000Valid = " + passage6000Valid);
            if (passage6000Valid) {
                // Если есть вадидный проход на поезд 6000
                if (decrementTripParams.getTrainCategory() == TrainCategoryPrefix.PASSENGER) {
                    // Если мы пытаемся списать поездку на пассажирский поезд
                    Logger.info(TAG, "Invalid params. Trip could not be decremented for passenger train, because passage mark is valid");
                    readBscResult.decrementTripError = DecrementTripError.INVALID_PARAMS;
                    return readBscResult;
                }
                // Общий счетчик не трогаем
                targetTripsTotalCounter = initialTripsTotalCounter;
                // Сбрасываем флаг обновления метки прохода
                // Если есть валидный проход на поезд 6000, то при списании поездки на поезд 7000
                // метку нужно сохранить, чтобы не потерять время реального прохода на станцию.
                overridePassageMark = false;
            } else {
                // Если нет вадидного прохода на поезд 6000
                // Увеличиваем общий счетчик на 1
                targetTripsTotalCounter = initialTripsTotalCounter + 1;
            }

            // Значение счетчика 7000-х поездов, которое мы стремимся получить
            int targetTrips7000Counter;

            if (decrementTripParams.getTrainCategory() == TrainCategoryPrefix.EXPRESS) {
                if (initialTrips7000Counter % 2 == 0) {
                    // Счетчик 7000-х поездок имеет четное значение
                    // Значит, был вход через 7000-ый турникет, увеличиваем этот счетчик на 2.
                    targetTrips7000Counter = initialTrips7000Counter + 2;
                } else {
                    // Счетчик 7000-х поездок имеет нечетное значение
                    // Значит, не было входа через 7000-ый турникет, увеличиваем этот счетчик на 1.
                    targetTrips7000Counter = initialTrips7000Counter + 1;
                }
            } else {
                if (initialTrips7000Counter % 2 == 0) {
                    // Счетчик 7000-х поездок имеет четное значение
                    // При списании поездки на поезд 6000 нужно довести счетчик 7000 до нечетного значения,
                    // иначе потом ПО решит, что последний проход был по 7000
                    // Увеличиваем счетчик на 1.
                    targetTrips7000Counter = initialTrips7000Counter + 1;
                } else {
                    // Счетчик 7000-х поездок имеет нечетное значение
                    // Всё нормально. При списании поездки на поезд 6000 ничего не меняем.
                    targetTrips7000Counter = initialTrips7000Counter;
                }
            }
            // Защищаемся от некорректных значений
            targetTripsTotalCounter = Math.max(Math.min(targetTripsTotalCounter, 0x7F), 0);
            targetTrips7000Counter = Math.max(Math.min(targetTrips7000Counter, 0xFF), 0);

            Logger.trace(TAG, "targetTripsTotalCounter = " + targetTripsTotalCounter);
            Logger.trace(TAG, "targetTrips7000Counter = " + targetTrips7000Counter);

            // Обновляем счетчики
            pdV23V24HwCounter.setTrips7000Counter(targetTrips7000Counter);
            pdV23V24HwCounter.setTripsTotalCounter(targetTripsTotalCounter);

            // Значение счетчика, которое мы стремимся получить
            targetHwCounterValue = pdV23V24HwCounter.getHwCounterValue();
        } else {
            // Значение счетчика, которое мы стремимся получить
            targetHwCounterValue = initialHwCounterValue + 1;
        }

        Logger.trace(TAG, "initialHwCounterValue = " + initialHwCounterValue);
        Logger.trace(TAG, "targetHwCounterValue = " + targetHwCounterValue);

        // Текущее значение счетчика
        int currentHwCounterValue = firstReadCounterResult.getData();

        if (currentHwCounterValue == initialHwCounterValue) {
            // Показания счетчика имеют ожидаемое значение
            // Выполняем увеличение, для этого вычисляем разницу между ожидаемым и исходным значением
            int incrementValue = targetHwCounterValue - initialHwCounterValue;
            Logger.trace(TAG, "incrementValue = " + incrementValue);

            WriteCardResult incrementCounterResult = cardReader.incrementHardwareCounter(decrementTripParams.getPdIndex(), incrementValue);
            if (!incrementCounterResult.isSuccess()) {
                // Ошибка при инкременте показаний счетчика
                Logger.trace(TAG, "Increment counter error = " + incrementCounterResult);
                readBscResult.decrementTripError = DecrementTripError.COUNTER_VALUE_NOT_READ;
                return readBscResult;
            }

            ReadCardResult<Integer> secondReadCounterResult = cardReader.readHardwareCounter(decrementTripParams.getPdIndex());
            if (!secondReadCounterResult.isSuccess()) {
                // Ошибка при считывании новых показаний счетчика
                Logger.trace(TAG, "Second read counter error = " + secondReadCounterResult);
                readBscResult.decrementTripError = DecrementTripError.COUNTER_VALUE_NOT_READ;
                return readBscResult;
            }
            // Значение счетчика после инкремента
            int counterValueAfterIncrement = secondReadCounterResult.getData();

            if (counterValueAfterIncrement != targetHwCounterValue) {
                // Странное значение показаний счетчика после инкремента
                // Не усугбляем ситуацию новыми изменениями, завершаем списание ошибкой
                Logger.info(TAG, "Invalid counter value after increment = " + counterValueAfterIncrement + ", target = " + targetHwCounterValue);
                readBscResult.decrementTripError = DecrementTripError.INVALID_COUNTER_VALUE;
                return readBscResult;
            }
        } else {
            // Показания счетчика изменились с момента контроля
            // Внимательно обрабатываем данный момент
            if (currentHwCounterValue == targetHwCounterValue) {
                Logger.trace(TAG, "Target counter value already reached = " + targetHwCounterValue);
                // Значение счетчика уже увеличилось
                // Полагаем, что это сделали мы на предыдущей попытке списания поездки, которая завершилась неудачно
                // Завершаем операцию, будто все прошло хорошо
            } else {
                // Произошло странное изменение показаний счетчика с момента контроля
                // Не усугбляем ситуацию новыми изменениями, завершаем списание ошибкой
                Logger.info(TAG, "Invalid counter value at start = " + currentHwCounterValue + ", target = " + targetHwCounterValue);
                readBscResult.decrementTripError = DecrementTripError.INVALID_COUNTER_VALUE;
                return readBscResult;
            }
        }

        ReadCardResult<PassageMark> readOldPassageMarkResult = cardReader.readPassageMark();
        if (!readOldPassageMarkResult.isSuccess()) {
            // Ошибка при считывании старой метки прохода
            Logger.trace(TAG, "Read old passage mark error = " + readOldPassageMarkResult);
            readBscResult.decrementTripError = DecrementTripError.PASSAGE_MARK_NOT_READ;
            return readBscResult;
        }

        Logger.trace(TAG, "overridePassageMark: " + overridePassageMark);

        if (!overridePassageMark) {
            // Если ненужно переписывать метку прохода, считаем операцию завершенной
            // Списание поездки произведено успешно
            readBscResult.passageMark = readOldPassageMarkResult.getData();
            readBscResult.newHwCounterValue = targetHwCounterValue;
            return readBscResult;
        }

        // Вполне возможно, что мы уже записали метку прохода на предущей попытке, но не смогли её прочитать
        // Для упрощения реализации просто запишем метку прохода ещё раз
        PassageMark oldPassageMark = readOldPassageMarkResult.getData();

        PassageMark targetPassageMark;
        Logger.trace(TAG, "Old passage mark = " + oldPassageMark);
        if (oldPassageMark instanceof PassageMarkV4 || oldPassageMark instanceof PassageMarkV8) {
            // Значение счетчика использования карты в метке прохода, которое мы стремимся получить
            // Важно опираться на значение из входных данных, т.к. вполне возможно,
            // что счетчик в метке был увеличен на предущей попытке
            int targetPmUsageCounterValue = decrementTripParams.getPassageMarkUsageCounterValue() + 1;
            if (oldPassageMark instanceof PassageMarkV4) {
                targetPassageMark = buildPassageMarkV4((PassageMarkV4) oldPassageMark, targetPmUsageCounterValue);
            } else {
                if (cardReader instanceof CppkNumberOfTripsOnePdReader) {
                    // http://agile.srvdev.ru/browse/CPPKPP-41000
                    // Специфичное формирование метки V8 для карты с одним ПД V7
                    // В данном случае поле метки V8 "Счетчик использования карты" интерпретируется как
                    // "Последнее показание счетчика при проходе", т.е. как поле метки V5
                    // Считается, что такая ситуация может возникать только в тестовых условиях
                    targetPassageMark = buildPassageMarkV8((PassageMarkV8) oldPassageMark, targetHwCounterValue);
                } else {
                    targetPassageMark = buildPassageMarkV8((PassageMarkV8) oldPassageMark, targetPmUsageCounterValue);
                }
            }
        } else if (oldPassageMark instanceof PassageMarkV5) {
            targetPassageMark = buildPassageMarkV5((PassageMarkV5) oldPassageMark, targetHwCounterValue);
        } else {
            if (oldPassageMark == null) {
                // На карте не оказалось метки прохода
                // Такого быть не должно
                // Метка автоматически пишется при контроле, если её нет
                Logger.trace(TAG, "Old passage is null");
            } else {
                // На карте записана метка прохода неподдерживаемой версии
                // Не перезаписываем её
                Logger.info(TAG, "Unsupported passage mark version = " + oldPassageMark.getVersion());
            }
            readBscResult.decrementTripError = DecrementTripError.INVALID_PASSAGE_MARK;
            return readBscResult;
        }
        Logger.trace(TAG, "New passage mark = " + targetPassageMark);

        WriteCardResult writePassageMarkResult = cardReader.writePassageMark(targetPassageMark);

        if (!writePassageMarkResult.isSuccess()) {
            // Ошибка при записи новой метки прохода
            Logger.trace(TAG, "Write new passage mark error = " + writePassageMarkResult);
            readBscResult.decrementTripError = DecrementTripError.PASSAGE_MARK_NOT_WRITTEN;
            return readBscResult;
        }

        ReadCardResult<PassageMark> readNewPassageMarkResult = cardReader.readPassageMark();
        if (!readNewPassageMarkResult.isSuccess() || readNewPassageMarkResult.getData() == null) {
            // Ошибка при считывании новой метки прохода
            Logger.trace(TAG, "Read new passage mark error = " + readNewPassageMarkResult);
            readBscResult.decrementTripError = DecrementTripError.PASSAGE_MARK_NOT_READ;
            return readBscResult;
        }

        // Списание поездки произведено успешно
        readBscResult.passageMark = readNewPassageMarkResult.getData();
        readBscResult.newHwCounterValue = targetHwCounterValue;
        return readBscResult;
    }

    private PassageMark buildPassageMarkV4(PassageMarkV4 oldPassageMark, int targetUsageCounterValue) {
        PassageMarkV4Impl newPassageMark = new PassageMarkV4Impl();
        newPassageMark.setUsageCounterValue(targetUsageCounterValue);
        fillPassageMark(oldPassageMark, newPassageMark);
        return newPassageMark;
    }

    private PassageMark buildPassageMarkV8(PassageMarkV8 oldPassageMark, int targetUsageCounterValue) {
        PassageMarkV8Impl newPassageMark = new PassageMarkV8Impl();
        newPassageMark.setUsageCounterValue(targetUsageCounterValue);
        newPassageMark.setBoundToPassenger(oldPassageMark.isBoundToPassenger());
        fillPassageMark(oldPassageMark, newPassageMark);
        return newPassageMark;
    }

    private PassageMark buildPassageMarkV5(PassageMarkV5 oldPassageMark, int targetHwCounterValue) {
        PassageMarkV5Impl newPassageMark = new PassageMarkV5Impl();
        newPassageMark.setHwCounterValue(targetHwCounterValue);
        fillPassageMark(oldPassageMark, newPassageMark);
        return newPassageMark;
    }

    private void fillPassageMark(PassageMarkV4V5V8 oldPassageMark, PassageMarkV4V5V8 newPassageMark) {
        newPassageMark.setPassageStationCode(getPassageStationCodeForPassageMark());
        setPassageStatusForPd(oldPassageMark, newPassageMark, decrementTripParams.getPdIndex());
        setPdTurnstileNumber(oldPassageMark, newPassageMark, decrementTripParams.getPdIndex());
        setPassageTypeForPd(oldPassageMark, newPassageMark, decrementTripParams.getPdIndex());
        setPdPassageTime(oldPassageMark, newPassageMark, decrementTripParams.getPdIndex());
    }

    private void setPassageStatusForPd(PassageMarkV4V5V8 oldPassageMark, PassageMarkV4V5V8 newPassageMark, int pdIndex) {
        if (pdIndex == 0) {
            newPassageMark.setPassageStatusForPd1(getPassageStatusForPassageMark());
            newPassageMark.setPassageStatusForPd2(oldPassageMark.getPassageStatusForPd2());
        } else if (pdIndex == 1) {
            newPassageMark.setPassageStatusForPd2(getPassageStatusForPassageMark());
            newPassageMark.setPassageStatusForPd1(oldPassageMark.getPassageStatusForPd1());
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    private void setPdTurnstileNumber(PassageMarkV4V5V8 oldPassageMark, PassageMarkV4V5V8 newPassageMark, int pdIndex) {
        if (pdIndex == 0) {
            newPassageMark.setPd1TurnstileNumber(getTurnstileNumberForPassageMark());
            newPassageMark.setPd2TurnstileNumber(oldPassageMark.getPd2TurnstileNumber());
        } else if (pdIndex == 1) {
            newPassageMark.setPd2TurnstileNumber(getTurnstileNumberForPassageMark());
            newPassageMark.setPd1TurnstileNumber(oldPassageMark.getPd1TurnstileNumber());
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    private void setPassageTypeForPd(PassageMarkV4V5V8 oldPassageMark, PassageMarkV4V5V8 newPassageMark, int pdIndex) {
        if (pdIndex == 0) {
            newPassageMark.setPassageTypeForPd1(getPassageTypeForPassageMark());
            newPassageMark.setPassageTypeForPd2(oldPassageMark.getPassageTypeForPd2());
        } else if (pdIndex == 1) {
            newPassageMark.setPassageTypeForPd2(getPassageTypeForPassageMark());
            newPassageMark.setPassageTypeForPd1(oldPassageMark.getPassageTypeForPd1());
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    private void setPdPassageTime(PassageMarkV4V5V8 oldPassageMark, PassageMarkV4V5V8 newPassageMark, int pdIndex) {
        if (pdIndex == 0) {
            newPassageMark.setPd1PassageTime(getPassageTimeForPassageMark(oldPassageMark));
            newPassageMark.setPd2PassageTime(oldPassageMark.getPd2PassageTime());
        } else if (pdIndex == 1) {
            newPassageMark.setPd2PassageTime(getPassageTimeForPassageMark(oldPassageMark));
            newPassageMark.setPd1PassageTime(oldPassageMark.getPd1PassageTime());
        } else {
            Logger.error(TAG, "pdIndex = " + pdIndex + " + out of bounds");
        }
    }

    private int getPassageStationCodeForPassageMark() {
        // Если метка ставится на ПТК, тогда пишем в станцию 0
        // https://aj.srvdev.ru/browse/CPPKPP-29827
        return 0;
    }

    private int getPassageStatusForPassageMark() {
        return PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
    }

    private int getTurnstileNumberForPassageMark() {
        // Нужно установить номер турникета 255, это будет означать что поездку списали на ПТК
        return GATE_NUMBER_FOR_PTK;
    }

    private int getPassageTypeForPassageMark() {
        // Нужно установить направление прохода в 0
        // https://aj.srvdev.ru/browse/CPPKPP-31509
        return PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION;
    }

    private int getPassageTimeForPassageMark(PassageMarkWithPassageTime oldPassageMark) {
        int oldPassageTime = oldPassageMark.getPdPassageTime(decrementTripParams.getPdIndex());
        int newPassageTime = (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - decrementTripParams.getPdSaleDate().getTime()));
        if (newPassageTime <= 0) {
            // http://agile.srvdev.ru/browse/CPPKPP-33731
            // Нужно сохранить хронологический порядок
            // Сделаем так, якобы этот проход был через 1 секунду после продажи
            Logger.info(TAG, "Silent time modification: newPassageTime = " + newPassageTime + ", saleTime = " + decrementTripParams.getPdSaleDate());
            newPassageTime = 1;
        }
        if (newPassageTime <= oldPassageTime) {
            // Нужно сохранить хронологический порядок
            // Сделаем так, якобы этот проход был через 1 секунду после предыдущего прохода
            Logger.info(TAG, "Silent time modification: newPassageTime = " + newPassageTime + ", oldPassageTime = " + oldPassageTime);
            newPassageTime = oldPassageTime + 1;
        }
        return newPassageTime;
    }

    private boolean handleResult(ReadBscResult readBscResult) {
        if (readBscResult.decrementTripError == null) {
            DecrementTripData decrementTripData = new DecrementTripData(readBscResult.passageMark, readBscResult.newHwCounterValue);
            decrementTripDataStorage.putData(decrementTripData);
            return true;
        } else {
            return false;
        }
    }

    private void onBscResultRead(ReadBscResult readBscResult) {
        Logger.trace(TAG, "onBscResultRead");
        bscResultRead = true;
        navigator.navigateToPreviousScreen(new DecrementTripResult(true));
    }

    private void startTimer() {
        Logger.trace(TAG, "startTimer");
        if (!timerSubscription.isUnsubscribed()) {
            throw new IllegalStateException("Operation is already running");
        }
        timerSubscription = Observable
                .interval(0, 1, TimeUnit.SECONDS, SchedulersCPPK.background())
                .take(TIMER_VALUE + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        stopReadCard();
                        view.setState(DecrementTripView.State.SEARCH_CARD_ERROR);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, e);
                    }

                    @Override
                    public void onNext(Long second) {
                        view.setTimerValue((int) (TIMER_VALUE - second));
                    }
                });
    }

    private void stopTimer() {
        Logger.trace(TAG, "stopTimer");
        timerSubscription.unsubscribe();
    }

    @Override
    public void destroy() {
        timerSubscription.unsubscribe();
        readCardSubscription.unsubscribe();
        super.destroy();
    }

    void onScreenClosed() {
        Logger.trace(TAG, "onScreenClosed");
        if (!readCardSubscription.isUnsubscribed()) {
            stopReadCard();
            stopTimer();
            uiThread.post(() -> view.setState(DecrementTripView.State.UNKNOWN_ERROR));
        }
    }

    private static class ReadBscResult {
        DecrementTripError decrementTripError;
        PassageMark passageMark;
        int newHwCounterValue;
    }

    interface Navigator {
        void navigateToPreviousScreen(DecrementTripResult decrementTripResult);
    }

    class DecrementTripException extends Exception {

        final DecrementTripError decrementTripError;

        DecrementTripException(DecrementTripError decrementTripError) {
            super(decrementTripError.toString());
            this.decrementTripError = decrementTripError;
        }
    }
}
