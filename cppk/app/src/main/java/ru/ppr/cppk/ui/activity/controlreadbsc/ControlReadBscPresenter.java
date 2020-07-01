package ru.ppr.cppk.ui.activity.controlreadbsc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmNoPdPlaceReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmoNoPdPlaceReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaTroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardPdData;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.ReadCardInformationInteractor;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.ReadIntegratedSingleTicketInteractor;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.PassageMarkToLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardDataStorage;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.interactor.FindCardInteractor;
import ru.ppr.cppk.logic.interactor.ReadLegacyBscInformationInteractor;
import ru.ppr.cppk.logic.interactor.ToLegacyPdListConverter;
import ru.ppr.cppk.logic.pd.PdHandler;
import ru.ppr.cppk.logic.servicedatacontrol.ServiceCardDataHandler;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.ui.activity.controlreadbsc.helper.IncrementPmHwUsageCounterChecker;
import ru.ppr.cppk.ui.activity.controlreadbsc.helper.PassageMarkForFirstPassageBuilder;
import ru.ppr.cppk.ui.activity.controlreadbsc.helper.PmUsageCounterUpdater;
import ru.ppr.cppk.ui.activity.controlreadbsc.interactor.CanRewritePassageMarkChecker;
import ru.ppr.cppk.ui.activity.controlreadbsc.interactor.ReadPassageMarkInteractor;
import ru.ppr.cppk.ui.activity.controlreadbsc.interactor.ReadWalletInteractor;
import ru.ppr.cppk.ui.activity.controlreadbsc.interactor.ShouldHavePassageMarkChecker;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.security.entity.PermissionDvc;
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
class ControlReadBscPresenter extends BaseMvpViewStatePresenter<ControlReadBscView, ControlReadBscViewState> {

    private static final String TAG = Logger.makeLogTag(ControlReadBscPresenter.class);

    private static final int TIMER_VALUE = (int) (DataCarrierReadSettings.RFID_FIND_TIME / 1000);

    // Common fields start
    private boolean initialized = false;
    // Common fields end
    private final ReadPassageMarkInteractor readPassageMarkInteractor;
    private final ReadWalletInteractor walletInteractor;
    private final CanRewritePassageMarkChecker canRewritePassageMarkChecker;
    private final ShouldHavePassageMarkChecker shouldHavePassageMarkChecker;
    private final FindCardInteractor findCardInteractor;
    private final ReadLegacyBscInformationInteractor readLegacyBscInformationInteractor;
    private final UiThread uiThread;
    private final PermissionChecker permissionChecker;
    private final PrivateSettings privateSettings;
    private final PassageMarkForFirstPassageBuilder passageMarkForFirstPassageBuilder;
    private final ReadCardInformationInteractor readCardInformationInteractor;
    private final ReadIntegratedSingleTicketInteractor readIntegratedSingleTicketInteractor;
    private final ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage;
    private final PdControlCardDataStorage pdControlCardDataStorage;
    private final ServiceCardDataHandler serviceCardDataHandler;
    private final ServiceDataDecoderFactory serviceDataDecoderFactory;
    private final CoverageAreaListDecoderFactory coverageAreaListDecoderFactory;
    private final PdHandler pdHandler;
    private final IncrementPmHwUsageCounterChecker incrementPmHwUsageCounterChecker;
    private final PmUsageCounterUpdater pmUsageCounterUpdater;
    private final ControlReadBscParams controlReadBscParams;
    private Navigator navigator;
    private Subscription timerSubscription = Subscriptions.unsubscribed();
    private Subscription readCardSubscription = Subscriptions.unsubscribed();
    /**
     * Флаг, что информация считана и осуществлен переход на другой экран
     */
    private boolean bscResultRead = false;

    @Inject
    ControlReadBscPresenter(
            ControlReadBscViewState viewState,
            ReadPassageMarkInteractor readPassageMarkInteractor,
            CanRewritePassageMarkChecker canRewritePassageMarkChecker,
            ShouldHavePassageMarkChecker shouldHavePassageMarkChecker,
            FindCardInteractor findCardInteractor,
            ReadLegacyBscInformationInteractor readLegacyBscInformationInteractor,
            UiThread uiThread,
            PermissionChecker permissionChecker,
            PrivateSettings privateSettings,
            PassageMarkForFirstPassageBuilder passageMarkForFirstPassageBuilder,
            ReadCardInformationInteractor readCardInformationInteractor,
            ReadIntegratedSingleTicketInteractor readIntegratedSingleTicketInteractor,
            ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage,
            PdControlCardDataStorage pdControlCardDataStorage,
            ServiceCardDataHandler serviceCardDataHandler,
            ServiceDataDecoderFactory serviceDataDecoderFactory,
            CoverageAreaListDecoderFactory coverageAreaListDecoderFactory,
            PdHandler pdHandler,
            IncrementPmHwUsageCounterChecker incrementPmHwUsageCounterChecker,
            PmUsageCounterUpdater pmUsageCounterUpdater,
            ControlReadBscParams controlReadBscParams,
            ReadWalletInteractor walletInteractor) {
        super(viewState);
        this.readPassageMarkInteractor = readPassageMarkInteractor;
        this.canRewritePassageMarkChecker = canRewritePassageMarkChecker;
        this.shouldHavePassageMarkChecker = shouldHavePassageMarkChecker;
        this.findCardInteractor = findCardInteractor;
        this.readLegacyBscInformationInteractor = readLegacyBscInformationInteractor;
        this.uiThread = uiThread;
        this.permissionChecker = permissionChecker;
        this.privateSettings = privateSettings;
        this.passageMarkForFirstPassageBuilder = passageMarkForFirstPassageBuilder;
        this.readCardInformationInteractor = readCardInformationInteractor;
        this.serviceTicketControlCardDataStorage = serviceTicketControlCardDataStorage;
        this.readIntegratedSingleTicketInteractor = readIntegratedSingleTicketInteractor;
        this.pdControlCardDataStorage = pdControlCardDataStorage;
        this.serviceCardDataHandler = serviceCardDataHandler;
        this.serviceDataDecoderFactory = serviceDataDecoderFactory;
        this.coverageAreaListDecoderFactory = coverageAreaListDecoderFactory;
        this.pdHandler = pdHandler;
        this.incrementPmHwUsageCounterChecker = incrementPmHwUsageCounterChecker;
        this.pmUsageCounterUpdater = pmUsageCounterUpdater;
        this.controlReadBscParams = controlReadBscParams;
        this.walletInteractor = walletInteractor;
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
        view.setSaleNewPdBtnVisible(privateSettings.isSaleEnabled() && permissionChecker.checkPermission(PermissionDvc.SalePd));
    }

    void onRepeatBtnClicked() {
        Logger.trace(TAG, "onRepeatBtnClicked");
        startReadCard();
    }

    void onSaleNewPdBtnClicked() {
        Logger.trace(TAG, "onSaleNewPdBtnClicked");
        view.showSaleNewPdConfirmDialog();
    }

    void onRfidBtnClicked() {
        Logger.trace(TAG, "onRfidBtnClicked");
        if (readCardSubscription.isUnsubscribed()) {
            startReadCard();
        }
    }

    void onSaleNewPdDialogYesBtnClicked() {
        Logger.trace(TAG, "onSaleNewPdDialogYesBtnClicked");
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        navigator.navigateToPdSaleActivity(pdSaleParams);
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
                    serviceTicketControlCardDataStorage.clearCardData();
                    pdControlCardDataStorage.clearCardData();
                    view.setState(ControlReadBscView.State.SEARCH_CARD);
                    startTimer();
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .andThen(findCardInteractor.findCard())
                .onErrorResumeNext(throwable -> {
                    Logger.error(TAG, throwable);
                    Logger.error(TAG, "CardNotFoundException");
                    return Single.error(new CardNotFoundException());
                })
                .observeOn(SchedulersCPPK.rfid())
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    stopTimer();
                    view.setState(ControlReadBscView.State.READ_CARD);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(cardReader -> Single.fromCallable(() -> readCard(cardReader)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    view.setState(ControlReadBscView.State.PROCESSING_DATA);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(readBscResult -> Single.fromCallable(() -> {
                    if (handleResult(readBscResult)) {
                        return readBscResult;
                    } else {
                        throw new Exception();
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onBscResultRead, throwable -> {
                    Logger.error(TAG, throwable);
                    Logger.error(TAG, "CardNotFoundException 2");
                    if (throwable instanceof CardNotFoundException) {
                        view.setState(ControlReadBscView.State.SEARCH_CARD_ERROR);
                    } else {
                        view.setState(ControlReadBscView.State.UNKNOWN_ERROR);
                    }
                });
    }

    private void stopReadCard() {
        Logger.trace(TAG, "stopReadCard");
        readCardSubscription.unsubscribe();
    }

    @NonNull
    private ReadBscResult readCard(CardReader cardReader) {
        Logger.trace(TAG, "readCard, cardReader = " + cardReader);
        if (cardReader instanceof StrelkaTroykaReader) {
            Logger.trace(TAG, "cardReader is StrelkaReader");
            return readCardStrelka((StrelkaTroykaReader) cardReader);
        } else if (cardReader instanceof TroykaReader) {
            Logger.trace(TAG, "cardReader is TroykaReader");
            return readCardTroyka((TroykaReader) cardReader);
        } else if (cardReader instanceof ReadPdReader) {
            Logger.trace(TAG, "cardReader is ReadPdReader");
            return readCardWithPd((ReadPdReader) cardReader);
        } else if (cardReader instanceof SkmNoPdPlaceReader) {
            Logger.trace(TAG, "cardReader is SkmNoPdPlaceReader");
            return readSkmSkmoWithoutPd(cardReader);
        } else if (cardReader instanceof SkmoNoPdPlaceReader) {
            Logger.trace(TAG, "cardReader is SkmoNoPdPlaceReader");
            return readSkmSkmoWithoutPd(cardReader);
        } else if (cardReader instanceof ServiceCardReader) {
            Logger.trace(TAG, "cardReader is ServiceCardReader");
            return readCardWithServiceData((ServiceCardReader) cardReader);
        } else {
            Logger.trace(TAG, "return ReadBscResult");
            return new ReadBscResult();
        }
    }

    private ReadBscResult readSkmSkmoWithoutPd(CardReader cardReader) {
        Logger.trace(TAG, "readSkmSkmoWithoutPd");
        ReadBscResult readBscResult = new ReadBscResult();

        readBscResult.legacyBscInformation = readLegacyBscInformationInteractor.read(cardReader);

        if (readBscResult.legacyBscInformation == null) {
            Logger.trace(TAG, "readSkmSkmoWithoutPd, legacyBscInformation is null");
            return readBscResult;
        }

        ReadCardResult<CardInformation> cardInformationReadCardResult = readCardInformationInteractor.readCardInformation(cardReader);
        Logger.trace(TAG, "readSkmSkmoWithoutPd: cardInformationReadCardResult = " + cardInformationReadCardResult);
        if (cardInformationReadCardResult.isSuccess()) {
            readBscResult.cardInformation = cardInformationReadCardResult.getData();
        } else {
            return readBscResult;
        }

        if (readBscResult.cardInformation == null) {
            return readBscResult;
        }

        readBscResult.legacyPdList = new ArrayList<>();
        readBscResult.pdControlCardData = handlePdListResult(readBscResult);
        Logger.trace(TAG, "readSkmSkmoWithoutPd: pdList is read");

        return readBscResult;
    }

    /**
     * Чтение карт Стрелка
     * @param cardReader
     * @return
     */
    private ReadBscResult readCardStrelka(StrelkaTroykaReader cardReader) {
        Logger.trace(TAG, "readCardWithPd");
        ReadBscResult readBscResult = new ReadBscResult();

        // Читаем информацию о БСК
        readBscResult.legacyBscInformation = readLegacyBscInformationInteractor.read(cardReader);

        if (readBscResult.legacyBscInformation == null) {
            Logger.trace(TAG, "readCardWithPd, legacyBscInformation is null");
            return readBscResult;
        }

        // Читаем информацию о карте
        ReadCardResult<CardInformation> cardInformationReadCardResult = readCardInformationInteractor.readCardInformation(cardReader);
        Logger.trace(TAG, "readCardWithPd: cardInformationReadCardResult = " + cardInformationReadCardResult);
        if (cardInformationReadCardResult.isSuccess()) {
            readBscResult.cardInformation = cardInformationReadCardResult.getData();
        } else {
            return readBscResult;
        }

        // Читаем комплексные и единые билеты
        ReadCardResult<CardPdData> readIntegratedSingleTicket = readIntegratedSingleTicketInteractor.readInformation(cardReader);
        if (readIntegratedSingleTicket != null && readIntegratedSingleTicket.isSuccess()) {
            readBscResult.cardPdData = readIntegratedSingleTicket.getData();
        }

        // Читаем метку прохода
        PassageMark passageMark = readPassageMark(cardReader);
        if(passageMark != null)
            readBscResult.passageMark = passageMark;
        Logger.trace(TAG, "readCardWithPd, passage mark: " + readBscResult.passageMark);


        //читаем данные кошелька
        MetroWallet metroWallet = readWalletData(cardReader);
        if(metroWallet != null)
            readBscResult.troykaWallet = metroWallet;

        // Читаем список ПД
        readBscResult.pdList = readPdList(cardReader);
        if (readBscResult.pdList == null) {
            Logger.trace(TAG, "readCardWithPd: pdList is null");
            return readBscResult;
        } else {
            List<PD> legacyPdList = new ToLegacyPdListConverter().convert(readBscResult.pdList, readBscResult.legacyBscInformation, null);
            readBscResult.legacyPdList = new ArrayList<>(legacyPdList);
        }

        // Читаем ЭЦП
        if (!readEds(cardReader, readBscResult.legacyPdList)) {
            Logger.trace(TAG, "readCardWithPd: readEds failed");
            return readBscResult;
        }

        if (readBscResult.passageMark != null) {
            // Добавляем информацию о метке прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }
        if (readBscResult.legacyPdList.isEmpty()) {
            // Если нет ПД, завершаем работу
            // Нет необходимости читать метку и счетчики, выполнять проверку ПД
            readBscResult.pdControlCardData = handlePdListResult(readBscResult);
            Logger.trace(TAG, "readCardWithPd: empty pdList is read");
            return readBscResult;
        }

        PdControlCardData pdControlCardData = handlePdListResult(readBscResult);

        if (readBscResult.passageMark != null) {
            if (controlReadBscParams.isIncrementPmHwUsageCounter() &&
                    incrementPmHwUsageCounterChecker.isIncrementRequired(readBscResult.legacyPdList)) {
                Logger.trace(TAG, "readCardWithPd: usage counter increment required");
                // Производим увеличение счетчика использования карты
                readBscResult.passageMark = pmUsageCounterUpdater.incrementUsageCounter(cardReader, readBscResult.passageMark);
                Logger.trace(TAG, "readCardWithPd, incrementUsageCounter result: " + readBscResult.passageMark);
                if (readBscResult.passageMark == null) {
                    // Если произошла ошибка при увеличении счетчика использования карты, завершаем работу
                    return readBscResult;
                }
            }

            // Обновляем метку прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }

        readBscResult.pdControlCardData = pdControlCardData;
        Logger.trace(TAG, "readCardWithPd: pdList is read");

        return readBscResult;
    }

    /**
     * Чтение карт тройка
     * @param cardReader
     * @return
     */
    private ReadBscResult readCardTroyka(TroykaReader cardReader) {
        Logger.trace(TAG, "readCardWithPd");
        ReadBscResult readBscResult = new ReadBscResult();

        // Читаем информацию о БСК
        readBscResult.legacyBscInformation = readLegacyBscInformationInteractor.read(cardReader);

        if (readBscResult.legacyBscInformation == null) {
            Logger.trace(TAG, "readCardWithPd, legacyBscInformation is null");
            return readBscResult;
        }

        // Читаем информацию о карте
        ReadCardResult<CardInformation> cardInformationReadCardResult = readCardInformationInteractor.readCardInformation(cardReader);
        Logger.trace(TAG, "readCardWithPd: cardInformationReadCardResult = " + cardInformationReadCardResult);
        if (cardInformationReadCardResult.isSuccess()) {
            readBscResult.cardInformation = cardInformationReadCardResult.getData();
        } else {
            return readBscResult;
        }

        // Читаем комплексные и единые билеты
        ReadCardResult<CardPdData> readIntegratedSingleTicket = readIntegratedSingleTicketInteractor.readInformation(cardReader);
        if (readIntegratedSingleTicket != null && readIntegratedSingleTicket.isSuccess()) {
            readBscResult.cardPdData = readIntegratedSingleTicket.getData();
        }

        // Читаем метку прохода
        PassageMark passageMark = readPassageMark(cardReader);
        if(passageMark != null)
            readBscResult.passageMark = passageMark;
        Logger.trace(TAG, "readCardWithPd, passage mark: " + readBscResult.passageMark);


        //читаем данные кошелька
        MetroWallet metroWallet = readWalletData(cardReader);
        if(metroWallet != null)
            readBscResult.troykaWallet = metroWallet;

        // Читаем список ПД
        readBscResult.pdList = readPdList(cardReader);
        if (readBscResult.pdList == null) {
            Logger.trace(TAG, "readCardWithPd: pdList is null");
            return readBscResult;
        } else {
            List<PD> legacyPdList = new ToLegacyPdListConverter().convert(readBscResult.pdList, readBscResult.legacyBscInformation, null);
            readBscResult.legacyPdList = new ArrayList<>(legacyPdList);
        }

        // Читаем ЭЦП
        if (!readEds(cardReader, readBscResult.legacyPdList)) {
            Logger.trace(TAG, "readCardWithPd: readEds failed");
            return readBscResult;
        }

        if (readBscResult.passageMark != null) {
            // Добавляем информацию о метке прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }
        if (readBscResult.legacyPdList.isEmpty()) {
            // Если нет ПД, завершаем работу
            // Нет необходимости читать метку и счетчики, выполнять проверку ПД
            readBscResult.pdControlCardData = handlePdListResult(readBscResult);
            Logger.trace(TAG, "readCardWithPd: empty pdList is read");
            return readBscResult;
        }

        PdControlCardData pdControlCardData = handlePdListResult(readBscResult);

        if (readBscResult.passageMark != null) {
            if (controlReadBscParams.isIncrementPmHwUsageCounter() &&
                    incrementPmHwUsageCounterChecker.isIncrementRequired(readBscResult.legacyPdList)) {
                Logger.trace(TAG, "readCardWithPd: usage counter increment required");
                // Производим увеличение счетчика использования карты
                readBscResult.passageMark = pmUsageCounterUpdater.incrementUsageCounter(cardReader, readBscResult.passageMark);
                Logger.trace(TAG, "readCardWithPd, incrementUsageCounter result: " + readBscResult.passageMark);
                if (readBscResult.passageMark == null) {
                    // Если произошла ошибка при увеличении счетчика использования карты, завершаем работу
                    return readBscResult;
                }
            }

            // Обновляем метку прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }

        readBscResult.pdControlCardData = pdControlCardData;
        Logger.trace(TAG, "readCardWithPd: pdList is read");

        return readBscResult;
    }

    private MetroWallet readWalletData(CardReader cardReader) {
        return walletInteractor.readWalletData(cardReader).getData();
    }

    /**
     * Чтение карт с ПД
     * @param cardReader
     * @return
     */
    private ReadBscResult readCardWithPd(ReadPdReader cardReader) {
        Logger.trace(TAG, "readCardWithPd");
        ReadBscResult readBscResult = new ReadBscResult();

        // Читаем информацию о БСК
        readBscResult.legacyBscInformation = readLegacyBscInformationInteractor.read(cardReader);

        if (readBscResult.legacyBscInformation == null) {
            Logger.trace(TAG, "readCardWithPd, legacyBscInformation is null");
            return readBscResult;
        }

        // Читаем информацию о карте
        ReadCardResult<CardInformation> cardInformationReadCardResult = readCardInformationInteractor.readCardInformation(cardReader);
        Logger.trace(TAG, "readCardWithPd: cardInformationReadCardResult = " + cardInformationReadCardResult);
        if (cardInformationReadCardResult.isSuccess()) {
            readBscResult.cardInformation = cardInformationReadCardResult.getData();
        } else {
            return readBscResult;
        }

        if (readBscResult.cardInformation == null) {
            return readBscResult;
        }

        // Читаем список ПД
        readBscResult.pdList = readPdList(cardReader);
        if (readBscResult.pdList == null) {
            Logger.trace(TAG, "readCardWithPd: pdList is null");
            return readBscResult;
        } else {
            List<PD> legacyPdList = new ToLegacyPdListConverter().convert(readBscResult.pdList, readBscResult.legacyBscInformation, null);
            readBscResult.legacyPdList = new ArrayList<>(legacyPdList);
        }

        // Читаем ЭЦП
        if (!readEds(cardReader, readBscResult.legacyPdList)) {
            Logger.trace(TAG, "readCardWithPd: readEds failed");
            return readBscResult;
        }

        if (readBscResult.legacyPdList.isEmpty()) {
            // Если нет ПД, завершаем работу
            // Нет необходимости читать метку и счетчики, выполнять проверку ПД
            readBscResult.pdControlCardData = handlePdListResult(readBscResult);
            Logger.trace(TAG, "readCardWithPd: empty pdList is read");
            return readBscResult;
        }

        // Читаем метку прохода
        readBscResult.passageMark = readPassageMark(cardReader);
        Logger.trace(TAG, "readCardWithPd, passage mark: " + readBscResult.passageMark);

        if (readBscResult.passageMark == null) {
            // Если не удалось прочитать метку
            if (shouldHavePassageMarkChecker.shouldHavePassageMark(cardReader)) {
                // Если карта должна иметь метку прохода с точки зрения логики
                Logger.trace(TAG, "readCardWithPd: cardReader should have passage mark");
                if (canRewritePassageMarkChecker.canPassageMarkBeRewritten(cardReader)) {
                    // Если ПТК имеет право ее перезаписать (восстановить)
                    Logger.trace(TAG, "readCardWithPd: passage mark can be rewritten");
                    // Создаем новую метку прохода
                    PassageMark passageMarkForWrite = passageMarkForFirstPassageBuilder.build(cardReader, readBscResult.legacyPdList);
                    if (passageMarkForWrite == null) {
                        // Не удалось собрать метку прохода для записи
                        return readBscResult;
                    }
                    readBscResult.passageMark = overridePassageMark(cardReader, passageMarkForWrite);
                    Logger.trace(TAG, "readCardWithPd, passage mark: " + readBscResult.passageMark);
                    if (readBscResult.passageMark == null) {
                        // Если после попытки восстановления метки её так и не появилось, завершаем работу
                        return readBscResult;
                    }
                } else {
                    // Если нет прав на перезапись метки, завершаем работу
                    return readBscResult;
                }
            }
        }

        if (readBscResult.passageMark != null) {
            // Добавляем информацию о метке прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }

        if (cardReader instanceof CppkNumberOfTripsReader) {
            // Читаем значения хардварных счетчиков
            CppkNumberOfTripsReader cppkNumberOfTripsReader = (CppkNumberOfTripsReader) cardReader;
            List<Integer> hwCounters = new ArrayList<>();
            int legacyPdIndex = 0;
            for (int i = 0; i < readBscResult.pdList.size(); i++) {
                if (readBscResult.pdList.get(i) == null) {
                    // Если билета в данной позиции нет, не считываем для него показания счетчика
                    continue;
                }
                ReadCardResult<Integer> readHwCounterResult = cppkNumberOfTripsReader.readHardwareCounter(i);
                Logger.trace(TAG, "readCardWithPd, readHwCounterResult: " + readHwCounterResult);
                if (readHwCounterResult.isSuccess()) {
                    hwCounters.add(readHwCounterResult.getData());
                    readBscResult.legacyPdList.get(legacyPdIndex).setHwCounterValue(readHwCounterResult.getData());
                } else {
                    hwCounters.add(null);
                }
                legacyPdIndex++;
            }
            readBscResult.hwCounters = hwCounters;
        }

        Logger.trace(TAG, "readCardWithPd, handling data");
        // Выполняем обработку считанного результата (валидация, проверка ЭЦП)
        PdControlCardData pdControlCardData = handlePdListResult(readBscResult);

        if (readBscResult.passageMark != null) {
            if (controlReadBscParams.isIncrementPmHwUsageCounter() &&
                    incrementPmHwUsageCounterChecker.isIncrementRequired(readBscResult.legacyPdList)) {
                Logger.trace(TAG, "readCardWithPd: usage counter increment required");
                // Производим увеличение счетчика использования карты
                readBscResult.passageMark = pmUsageCounterUpdater.incrementUsageCounter(cardReader, readBscResult.passageMark);
                Logger.trace(TAG, "readCardWithPd, incrementUsageCounter result: " + readBscResult.passageMark);
                if (readBscResult.passageMark == null) {
                    // Если произошла ошибка при увеличении счетчика использования карты, завершаем работу
                    return readBscResult;
                }
            }

            // Обновляем метку прохода в ПД
            ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(readBscResult.passageMark);
            for (PD legacyPd : readBscResult.legacyPdList) {
                legacyPd.setPassageMark(legacyPassageMark);
            }
        }

        readBscResult.pdControlCardData = pdControlCardData;
        Logger.trace(TAG, "readCardWithPd: pdList is read");

        return readBscResult;
    }


    private ReadBscResult readCardWithServiceData(ServiceCardReader cardReader) {
        Logger.trace(TAG, "readCardWithServiceData");
        ReadBscResult readBscResult = new ReadBscResult();

        ReadCardResult<CardInformation> cardInformationReadCardResult = readCardInformationInteractor.readCardInformation(cardReader);
        Logger.trace(TAG, "readCardWithServiceData, cardInformationReadCardResult: " + cardInformationReadCardResult);
        if (cardInformationReadCardResult.isSuccess()) {
            readBscResult.cardInformation = cardInformationReadCardResult.getData();
        } else {
            return readBscResult;
        }

        if (readBscResult.cardInformation == null) {
            return readBscResult;
        }

        ReadCardResult<byte[]> rawServiceDataReadCardResult = cardReader.readRawServiceData();
        Logger.trace(TAG, "readCardWithServiceData, rawServiceDataReadCardResult: " + rawServiceDataReadCardResult);
        if (rawServiceDataReadCardResult.isSuccess()) {
            ServiceDataDecoder serviceDataDecoder = serviceDataDecoderFactory.create(rawServiceDataReadCardResult.getData());
            ServiceData serviceData = serviceDataDecoder.decode(rawServiceDataReadCardResult.getData());
            readBscResult.rawServiceData = rawServiceDataReadCardResult.getData();
            readBscResult.serviceData = serviceData;
        } else {
            return readBscResult;
        }

        if (readBscResult.serviceData == null) {
            return readBscResult;
        }

        ReadCardResult<byte[]> rawCoverageAreaListReadCardResult = cardReader.readRawCoverageAreaList();
        Logger.trace(TAG, "readCardWithServiceData, rawCoverageAreaListReadCardResult: " + rawCoverageAreaListReadCardResult);
        if (rawCoverageAreaListReadCardResult.isSuccess()) {
            CoverageAreaListDecoder coverageAreaListDecoder = coverageAreaListDecoderFactory.create();
            List<CoverageArea> coverageAreaList = coverageAreaListDecoder.decode(rawCoverageAreaListReadCardResult.getData());
            readBscResult.rawCoverageAreaList = rawCoverageAreaListReadCardResult.getData();
            readBscResult.coverageAreaList = coverageAreaList;
        } else {
            return readBscResult;
        }

        ReadCardResult<byte[]> edsReadCardResult = cardReader.readEds();
        Logger.trace(TAG, "readCardWithServiceData, edsReadCardResult: " + edsReadCardResult);
        if (edsReadCardResult.isSuccess()) {
            readBscResult.eds = edsReadCardResult.getData();
        } else {
            return readBscResult;
        }

        // Читаем метку прохода
        readBscResult.passageMark = readPassageMark(cardReader);
        Logger.trace(TAG, "readCardWithServiceData, passage mark: " + readBscResult.passageMark);

        if (readBscResult.passageMark == null) {
            // Если не удалось прочитать метку
            if (shouldHavePassageMarkChecker.shouldHavePassageMark(cardReader)) {
                // Если карта должна иметь метку прохода с точки зрения логики
                Logger.trace(TAG, "readCardWithServiceData: cardReader should have passage mark");
                if (canRewritePassageMarkChecker.canPassageMarkBeRewritten(cardReader)) {
                    // Если ПТК имеет право ее перезаписать (восстановить)
                    Logger.trace(TAG, "readCardWithServiceData: passage mark can be rewritten");
                    // Создаем новую метку прохода
                    PassageMark passageMarkForWrite = passageMarkForFirstPassageBuilder.build(cardReader, readBscResult.serviceData);
                    if (passageMarkForWrite == null) {
                        // Не удалось собрать метку прохода для записи
                        return readBscResult;
                    }
                    readBscResult.passageMark = overridePassageMark(cardReader, passageMarkForWrite);
                    Logger.trace(TAG, "readCardWithServiceData, passage mark: " + readBscResult.passageMark);
                    if (readBscResult.passageMark == null) {
                        // Если после попытки восстановления метки её так и не появилось, завершаем работу
                        return readBscResult;
                    }
                } else {
                    // Если нет прав на перезапись метки, завершаем работу
                    return readBscResult;
                }
            }
        }

        Logger.trace(TAG, "readCardWithServiceData, handling data");
        // Выполняем обработку считанного результата (валидация, проверка ЭЦП)
        ServiceTicketControlCardData serviceTicketControlCardData = handleServiceDataResult(readBscResult);

        if (readBscResult.passageMark != null) {
            if (controlReadBscParams.isIncrementPmHwUsageCounter() &&
                    incrementPmHwUsageCounterChecker.isIncrementRequired(serviceTicketControlCardData)) {
                Logger.trace(TAG, "readCardWithServiceData: usage counter increment required");
                // Производим увеличение счетчика использования карты
                readBscResult.passageMark = pmUsageCounterUpdater.incrementUsageCounter(cardReader, readBscResult.passageMark);
                Logger.trace(TAG, "readCardWithServiceData, incrementUsageCounter result: " + readBscResult.passageMark);
                if (readBscResult.passageMark == null) {
                    // Если произошла ошибка при увеличении счетчика использования карты, завершаем работу
                    return readBscResult;
                }
            }
        }

        readBscResult.serviceTicketControlCardData = serviceTicketControlCardData;
        Logger.trace(TAG, "readCardWithServiceData: serviceTicket is read");

        return readBscResult;
    }

    private PassageMark readPassageMark(CardReader cardReader) {
        Logger.trace(TAG, "readPassageMark cardReader = " + cardReader);
        if (cardReader instanceof ReadPassageMarkReader) {
            // Если ридер поддерживает чтение метки прохода
            ReadCardResult<PassageMark> readPassageMarkResult = readPassageMarkInteractor.readPassageMark((ReadPassageMarkReader) cardReader);
            if (readPassageMarkResult.isSuccess()) {
                // Если метка успешно считана, получаем её
                return readPassageMarkResult.getData();
            }
        }
        return null;
    }

    private List<Pd> readPdList(CardReader cardReader) {
        Logger.trace(TAG, "readPdList");
        if (!(cardReader instanceof ReadPdReader)) {
            // Если ридер не поддерживает чтение ПД, завершаем работу
            // http://agile.srvdev.ru/browse/CPPKPP-35253
            return null;
        }

        ReadCardResult<List<Pd>> readPdListResult = ((ReadPdReader) cardReader).readPdList();
        Logger.trace(TAG, "readPdList, readPdListResult: " + readPdListResult);

        if (readPdListResult.isSuccess()) {
            // Если удалось прочитать список ПД, получаем его
            return readPdListResult.getData();
        } else {
            // Если не удалось прочитать список ПД, завершаем работу
            return null;
        }
    }

    private boolean readEds(CardReader cardReader, @NonNull List<PD> legacyPdList) {
        Logger.trace(TAG, "readEds");
        if (!(cardReader instanceof ReadEdsReader)) {
            // Если ридер не поддерживает чтение ЭЦП, завершаем работу
            return false;
        }

        ReadCardResult<byte[]> readEdsResult = ((ReadEdsReader) cardReader).readEds();
        Logger.trace(TAG, "readEds, readEdsResult: " + readEdsResult);

        if (readEdsResult.isSuccess()) {
            // Если удалось прочитать ЭЦП, получаем её
            byte[] eds = readEdsResult.getData();
            for (PD legacyPd : legacyPdList) {
                legacyPd.ecp = eds;
            }
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private PassageMark overridePassageMark(@NonNull CardReader cardReader, @NonNull PassageMark passageMarkForWrite) {
        Logger.trace(TAG, "overridePassageMark");

        // Пишем метку независимо от режима работы ПТК
        // http://agile.srvdev.ru/browse/CPPKPP-36166

        if (cardReader instanceof WritePassageMarkReader) {
            // Если ридер поддерживает запись метки прохода
            // Количество попыток записи метки прохода https://aj.srvdev.ru/browse/CPPKPP-30055
            int maxCount = 3;
            for (int i = 1; i <= maxCount; i++) {
                WriteCardResult writePassageMarkResult = ((WritePassageMarkReader) cardReader).writePassageMark(passageMarkForWrite);
                Logger.trace(TAG, "overridePassageMark, writePassageMarkResult = " + writePassageMarkResult);
                if (writePassageMarkResult.isSuccess()) {
                    Logger.info(TAG, "Успешно обновили метку прохода с " + i + "-й попытки");
                    break;
                } else {
                    Logger.info(TAG, "Неудачная попытка записи метки №" + i);
                }
            }
            // Читаем метку заново независимо от того, завершилась запись успешно или нет
            // Даже если попытки записи исчерпаны и успеха не было, теоретически, что-то могло записаться
            ReadCardResult<PassageMark> readPassageMarkResult = readPassageMarkInteractor.readPassageMark((ReadPassageMarkReader) cardReader);
            Logger.trace(TAG, "overridePassageMark, readPassageMarkResult = " + readPassageMarkResult);
            if (readPassageMarkResult.isSuccess()) {
                // Если метка успешно считана, получаем её
                return readPassageMarkResult.getData();
            } else {
                //если прочитать не удалось - вернем null - тогда логика будет знать что карта битая
                Logger.error(TAG, "Ошибка записи метки прохода на карту: " + readPassageMarkResult.toString());
                return null;
            }
        } else {
            // Если ридер не поддерживает запись метки прохода, завершаем работу
            return null;
        }
    }

    @NonNull
    private ServiceTicketControlCardData handleServiceDataResult(ReadBscResult readBscResult) {
        ServiceTicketControlCardData serviceTicketControlCardData = new ServiceTicketControlCardData();
        serviceTicketControlCardData.setCardInformation(readBscResult.cardInformation);
        serviceTicketControlCardData.setServiceData(readBscResult.serviceData);
        serviceTicketControlCardData.setRawServiceData(readBscResult.rawServiceData);
        serviceTicketControlCardData.setCoverageAreaList(readBscResult.coverageAreaList);
        serviceTicketControlCardData.setRawCoverageAreaList(readBscResult.rawCoverageAreaList);
        serviceTicketControlCardData.setEds(readBscResult.eds);
        serviceTicketControlCardData.setPassageMark(readBscResult.passageMark);
        serviceCardDataHandler.handle(serviceTicketControlCardData);
        return serviceTicketControlCardData;
    }

    @NonNull
    private PdControlCardData handlePdListResult(ReadBscResult readBscResult) {
        PdControlCardData pdControlCardData = new PdControlCardData();
        pdControlCardData.setCardInformation(readBscResult.cardInformation);
        pdControlCardData.setPassageMark(readBscResult.passageMark);
        pdControlCardData.setTroykaWallet(readBscResult.troykaWallet);
        pdControlCardData.setCardPdData(readBscResult.cardPdData);
        pdControlCardData.setPdList(readBscResult.pdList);
        pdControlCardData.setHwCounters(readBscResult.hwCounters);
        pdHandler.handle(readBscResult.legacyPdList);
        return pdControlCardData;
    }

    private boolean handleResult(ReadBscResult readBscResult) {
        Logger.trace(TAG, "handleResult");
        if (readBscResult.serviceTicketControlCardData != null) {
            serviceTicketControlCardDataStorage.putCardData(readBscResult.serviceTicketControlCardData);
            return true;
        } else if (readBscResult.pdControlCardData != null) {
            pdControlCardDataStorage.putCardData(readBscResult.pdControlCardData);
            return true;
        }
        return false;
    }

    private void onBscResultRead(ReadBscResult readBscResult) {
        Logger.trace(TAG, "onBscResultRead:" + readBscResult);
        bscResultRead = true;
        if (readBscResult.serviceTicketControlCardData != null) {
            navigator.navigateToServiceTicketControlActivity();
        } else if (readBscResult.pdControlCardData != null) {
            navigator.navigateToRfidResultActivity(readBscResult.legacyPdList, readBscResult.legacyBscInformation, controlReadBscParams.getReadForTransferParams());
        }
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
                        view.setState(ControlReadBscView.State.SEARCH_CARD_ERROR);
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
            uiThread.post(() -> view.setState(ControlReadBscView.State.UNKNOWN_ERROR));
        }
    }

    private static class ReadBscResult {
        PdControlCardData pdControlCardData;
        ServiceTicketControlCardData serviceTicketControlCardData;
        CardInformation cardInformation;
        CardPdData cardPdData;
        PassageMark passageMark;
        MetroWallet troykaWallet;
        //////////////
        byte[] rawServiceData;
        ServiceData serviceData;
        byte[] rawCoverageAreaList;
        List<CoverageArea> coverageAreaList;
        byte[] eds;
        //////////////
        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation;
        ArrayList<PD> legacyPdList;
        List<Pd> pdList;
        List<Integer> hwCounters;

        @Override
        public String toString() {
            return "ReadBscResult{" +
                    "pdControlCardData=" + pdControlCardData +
                    ", serviceTicketControlCardData=" + serviceTicketControlCardData +
                    ", cardInformation=" + cardInformation +
                    ", cardPdData=" + cardPdData +
                    ", passageMark=" + passageMark +
                    ", troykaWallet=" + troykaWallet +
                    ", rawServiceData=" + Arrays.toString(rawServiceData) +
                    ", serviceData=" + serviceData +
                    ", rawCoverageAreaList=" + Arrays.toString(rawCoverageAreaList) +
                    ", coverageAreaList=" + coverageAreaList +
                    ", eds=" + Arrays.toString(eds) +
                    ", legacyBscInformation=" + legacyBscInformation +
                    ", legacyPdList=" + legacyPdList +
                    ", pdList=" + pdList +
                    ", hwCounters=" + hwCounters +
                    '}';
        }
    }

    interface Navigator {
        void navigateToRfidResultActivity(@Nullable ArrayList<PD> pdList, @Nullable ru.ppr.cppk.dataCarrier.entity.BscInformation bscInformation, @Nullable ReadForTransferParams readForTransferParams);

        void navigateToPdSaleActivity(PdSaleParams pdSaleParams);

        void navigateToServiceTicketControlActivity();
    }

    private class CardNotFoundException extends Exception {
    }
}
