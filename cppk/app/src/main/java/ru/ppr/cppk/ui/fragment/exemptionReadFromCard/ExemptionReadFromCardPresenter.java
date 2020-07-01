package ru.ppr.cppk.ui.fragment.exemptionReadFromCard;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceCardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.EttCardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.ReadCardInformationInteractor;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.SkmSkmoIpkCardInformation;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.cppk.legacy.SmartCardBuilder2;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.cppk.logic.SmartCardStopListChecker;
import ru.ppr.cppk.logic.SmartCardValidityTimeChecker;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;
import ru.ppr.security.repository.SmartCardStopListItemRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionReadFromCardPresenter extends BaseMvpViewStatePresenter<ExemptionReadFromCardView, ExemptionReadFromCardViewState> {

    private static final String TAG = Logger.makeLogTag(ExemptionReadFromCardPresenter.class);

    private static final int SEARCH_CARD_TIMEOUT = 5;

    private final Object LOCK = new Object();

    private InteractionListener mInteractionListener;

    private boolean initialized = false;
    /**
     * UI-поток
     */
    private UiThread uiThread;
    private NsiDaoSession nsiDaoSession;
    private SmartCardStopListItemRepository smartCardStopListItemRepository;
    private ExemptionChecker exemptionChecker;
    private CommonSettings commonSettings;
    private FindCardTaskFactory findCardTaskFactory;
    private SelectExemptionParams selectExemptionParams;
    private NsiVersionManager nsiVersionManager;
    private TicketTypeRepository ticketTypeRepository;
    private ExemptionGroupRepository exemptionGroupRepository;
    private SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;
    private ExemptionRepository exemptionRepository;
    private FioFormatter fioFormatter;

    private FindCardTask findCardTask;
    private Subscription timerSubscription;
    private Subscription readCardSubscription;
    private CardInformation cardInformation;
    private final List<ExemptionForEvent> exemptionsForEvent = new ArrayList<>();
    private final List<Exemption> exemptions = new ArrayList<>();
    private int expressCode;

    public ExemptionReadFromCardPresenter() {

    }

    @Override
    protected ExemptionReadFromCardViewState provideViewState() {
        return new ExemptionReadFromCardViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    void initialize(
            UiThread uiThread,
            NsiDaoSession nsiDaoSession,
            SmartCardStopListItemRepository smartCardStopListItemRepository,
            ExemptionChecker exemptionChecker,
            CommonSettings commonSettings,
            FindCardTaskFactory findCardTaskFactory,
            SelectExemptionParams selectExemptionParams,
            NsiVersionManager nsiVersionManager,
            TicketTypeRepository ticketTypeRepository,
            ExemptionGroupRepository exemptionGroupRepository,
            SmartCardCancellationReasonRepository smartCardCancellationReasonRepository,
            ExemptionRepository exemptionRepository,
            FioFormatter fioFormatter
    ) {
        if (!initialized) {
            this.initialized = true;
            this.uiThread = uiThread;
            this.nsiDaoSession = nsiDaoSession;
            this.smartCardStopListItemRepository = smartCardStopListItemRepository;
            this.exemptionChecker = exemptionChecker;
            this.commonSettings = commonSettings;
            this.findCardTaskFactory = findCardTaskFactory;
            this.selectExemptionParams = selectExemptionParams;
            this.nsiVersionManager = nsiVersionManager;
            this.ticketTypeRepository = ticketTypeRepository;
            this.exemptionGroupRepository = exemptionGroupRepository;
            this.smartCardCancellationReasonRepository = smartCardCancellationReasonRepository;
            this.exemptionRepository = exemptionRepository;
            this.fioFormatter = fioFormatter;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        readExemption();
    }

    void onRetryBtnClicked() {
        readExemption();
    }

    void onUseExemptionBtnClicked() {
        // Проверяем, удалось ли найти льготы для всех регионов
        if (exemptions.size() == selectExemptionParams.getRegionCodes().size()) {
            AdditionalInfoForEtt additionalInfoForEtt = null;
            String ettPassengerCategory = null;
            if (cardInformation instanceof EttCardInformation) {
                EttCardInformation ettCardInformation = (EttCardInformation) cardInformation;
                EttData ettData = ettCardInformation.getEttData();
                if (ettData != null) {
                    ettPassengerCategory = ettData.getPassengerCategoryCode();

                    additionalInfoForEtt = new AdditionalInfoForEtt();
                    // На картах ЭТТ нет информации о дате выпуска карты
                    // Зачем это поле в датконтрактах - неизвестно
                    additionalInfoForEtt.setIssueDateTime(null); // В будущем: 15.08.2017 null или new Date(0)
                    additionalInfoForEtt.setGuardianFio(ettData.getGuardianFio());
                    additionalInfoForEtt.setIssueUnitCode(ettData.getDivisionCode());
                    additionalInfoForEtt.setOwnerOrganizationCode(ettData.getOrganizationCode());
                    additionalInfoForEtt.setPassengerCategory(ettData.getPassengerCategoryCode());
                    additionalInfoForEtt.setPassengerFio(fioFormatter.getFullNameAsSurnameWithInitials(ettData.getSurname(), ettData.getFirstName(), ettData.getSecondName()));
                    additionalInfoForEtt.setSnils(ettData.getSnilsCode());
                } else {
                    Logger.info(TAG, "ettData is null");
                }
            }

            for (int i = 0; i < selectExemptionParams.getRegionCodes().size(); i++) {
                int regionCode = selectExemptionParams.getRegionCodes().get(i);
                ExemptionForEvent exemptionForEvent = exemptionsForEvent.get(i);
                Exemption exemption = exemptions.get(i);
                ExemptionChecker.CheckResult checkResult = exemptionChecker.check(selectExemptionParams, exemptionForEvent, exemption, regionCode, ettPassengerCategory);
                if (checkResult == ExemptionChecker.CheckResult.SUCCESS) {
                    exemptionForEvent.fillFromExemption(exemption);
                } else {
                    ExemptionReadFromCardView.ExemptionUsageDisabledMessage exemptionUsageDisabledMessage = new ExemptionReadFromCardView.ExemptionUsageDisabledMessage();
                    exemptionUsageDisabledMessage.checkResult = checkResult;
                    exemptionUsageDisabledMessage.exemptionExpressCode = exemptionForEvent.getExpressCode();
                    TicketType ticketType = ticketTypeRepository.load(selectExemptionParams.getTicketTypeCode(), selectExemptionParams.getVersionNsi());
                    exemptionUsageDisabledMessage.ticketTypeName = ticketType.getShortName();
                    view.showExemptionUsageDisabledMessage(exemptionUsageDisabledMessage);
                    return;
                }
            }
            mInteractionListener.onExemptionSelected(exemptionsForEvent, additionalInfoForEtt);
        } else {
            view.showExemptionNotFoundMessage(expressCode);
        }
    }

    void onCancelBtnClicked() {
        mInteractionListener.onCancelSelectExemption();
    }

    boolean onBackPressed() {
        mInteractionListener.onCancelSelectExemption();
        return true;
    }

    private Single<CardReader> findCardRx() {
        return Single
                .create((SingleSubscriber<? super Single<CardReader>> subscriber) -> {

                    subscriber.add(new Subscription() {

                        private boolean unSubscribed;

                        @Override
                        public void unsubscribe() {
                            if (findCardTask != null) {
                                findCardTask.cancel();
                                findCardTask = null;
                                unSubscribed = true;
                            }
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return unSubscribed;
                        }
                    });

                    findCardTask = findCardTaskFactory.create();

                    CardReader cardReader = findCardTask.find();
                    if (cardReader == null) {
                        subscriber.onError(new Exception("Could not found card"));
                    } else {
                        subscriber.onSuccess(Single.just(cardReader));
                    }
                })
                .flatMap((Single<CardReader> cardReaderObservable) -> cardReaderObservable);
    }

    private void readExemption() {
        exemptionsForEvent.clear();
        exemptions.clear();
        startTimer();
        readCard();
    }

    private void readCard() {
        readCardSubscription = findCardRx()
                .doOnSuccess(cardReader -> {
                    Logger.trace(TAG, "doOnSuccess");
                    stopTimer();
                    uiThread.post(() -> view.showReadCardState());
                })
                .doOnError(throwable -> {
                    Logger.error(TAG, throwable);
                    stopTimer();
                    uiThread.post(() -> {
                        view.showCardNotFoundError();
                        view.setRetryBtnVisible(true);
                    });
                })
                .toObservable()
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(cardReader -> {
                    boolean serviceTicket = false;
                    if (!(cardReader instanceof BscInformationReader)) {
                        uiThread.post(() -> view.showNoExemptionOnCardError());
                        return;
                    } else if (cardReader instanceof ServiceCardReader) {
                        serviceTicket = true;
                    }
                    BscInformationReader bscInformationReader = (BscInformationReader) cardReader;

                    Date forDate = new Date();

                    ReadCardResult<BscInformation> bscInformationResult = bscInformationReader.readBscInformation();
                    if (!bscInformationResult.isSuccess()) {
                        Logger.trace(TAG, "bscInformationResult error:" + bscInformationResult.getDescription());
                        uiThread.post(() -> {
                            view.showReadCardError();
                            view.setRetryBtnVisible(true);
                        });
                        return;
                    }

                    BscInformation bscInformation = bscInformationResult.getData();
                    int exemptionCode = bscInformation.getExemptionCode();
                    if (exemptionCode == 0) {
                        uiThread.post(() -> view.showNoExemptionOnCardError());
                        return;
                    }
                    ReadCardInformationInteractor readCardInformationInteractor = new ReadCardInformationInteractor();
                    ReadCardResult<CardInformation> cardInformationResult = readCardInformationInteractor.readCardInformation(cardReader);

                    if (!cardInformationResult.isSuccess()) {
                        Logger.trace(TAG, "cardInformationResult error:" + cardInformationResult.getDescription());
                        uiThread.post(() -> {
                            view.showReadCardError();
                            view.setRetryBtnVisible(true);
                        });
                        return;
                    }
                    cardInformation = cardInformationResult.getData();

                    int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();
                    SmartCardStopListChecker smartCardStopListChecker = new SmartCardStopListChecker(smartCardStopListItemRepository, smartCardCancellationReasonRepository);
                    Pair<SmartCardStopListItem, String> stopItemResult = smartCardStopListChecker.findSmartCardStopListItem(
                            cardInformation,
                            EnumSet.of(StopCriteriaType.READ_AND_WRITE, StopCriteriaType.WRITE),
                            nsiVersion
                    );

                    if (stopItemResult != null) {
                        uiThread.post(() -> {
                            view.showCardInStopListError(stopItemResult.second);
                            view.setRetryBtnVisible(false);
                        });
                        return;
                    }

                    SmartCardValidityTimeChecker smartCardValidityTimeChecker = new SmartCardValidityTimeChecker(commonSettings);
                    if (!smartCardValidityTimeChecker.isCardTimeValid(cardInformation, forDate, serviceTicket)) {
                        uiThread.post(() -> {
                            view.showCardValidityTimeError();
                            view.setRetryBtnVisible(false);
                        });
                        return;
                    }

                    TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
                    uiThread.post(() -> {
                        view.setBscType(TextUtils.isEmpty(ticketStorageType.getAbbreviation()) ? null : ticketStorageType.getAbbreviation());
                        view.setBscNumber(cardInformation.getOuterNumberAsFormattedString());
                    });

                    for (int i = 0; i < selectExemptionParams.getRegionCodes().size(); i++) {
                        exemptionsForEvent.add(new ExemptionForEvent());
                    }
                    /*
                     https://aj.srvdev.ru/browse/CPPKPP-24463

                     Александр Бражкин:
                     1. Документ, подтверждающий право льготного проезда
                     2. Номер карты, с которой считана льгота
                     Два этих понятия могут существовать одновременно для одной продажи?

                     Лившиц Николай Викторович:
                     нет
                     */
                    String fio = null;
                    if (cardInformation instanceof EttCardInformation) {
                        EttCardInformation ettCardInformation = (EttCardInformation) cardInformation;
                        if (ettCardInformation.getEttData() != null) {
                            EttData ettData = ettCardInformation.getEttData();
                            String fioLocal = Dagger.appComponent().fioFormatter().getFullNameAsSurnameWithInitials(ettData.getSurname(), ettData.getFirstName(), ettData.getSecondName());
                            fio = fioLocal;
                            uiThread.post(() -> view.setFio(fioLocal));
                            for (ExemptionForEvent exemptionForEvent : exemptionsForEvent) {
                                exemptionForEvent.setNumberOfDocumentWhichApproveExemption(ettCardInformation.getEttData().getSnilsCode());
                                exemptionForEvent.setSnilsUsed(true);
                            }
                        }
                    } else if (cardInformation instanceof SkmSkmoIpkCardInformation) {
                        SkmSkmoIpkCardInformation skmSkmoIpkCardInformation = (SkmSkmoIpkCardInformation) cardInformation;
                        if (skmSkmoIpkCardInformation.getPersonalData() != null) {
                            PersonalData personalData = skmSkmoIpkCardInformation.getPersonalData();
                            String fioLocal = Dagger.appComponent().fioFormatter().getFullNameAsSurnameWithInitials(personalData.getSurname(), personalData.getName(), personalData.getSecondName());
                            fio = fioLocal;
                            uiThread.post(() -> view.setFio(fioLocal));
                        }
                    }

                    this.expressCode = bscInformation.getExemptionCode();

                    for (int i = 0; i < selectExemptionParams.getRegionCodes().size(); i++) {
                        int regionCode = selectExemptionParams.getRegionCodes().get(i);
                        ExemptionForEvent exemptionForEvent = exemptionsForEvent.get(i);

                        exemptionForEvent.setExpressCode(this.expressCode);
                        exemptionForEvent.setFio(fio);
                        exemptionForEvent.setManualInput(false);
                        exemptionForEvent.setSmartCardFromWhichWasReadAboutExemption(new SmartCardBuilder2().setBscInformation(cardInformation).build());

                        List<Exemption> exemptions = exemptionRepository.getActualExemptionsForRegion(
                                exemptionForEvent.getExpressCode(), regionCode, new Date(), nsiVersionManager.getCurrentNsiVersionId());
                        if (exemptions.isEmpty()) {
                            break;
                        } else {
                            this.exemptions.add(exemptions.get(0));
                        }
                    }

                    // http://agile.srvdev.ru/browse/CPPKPP-34481
                    // Со слов Рзянкиной Натальи Владимировны данные в льготах из разных регоинов не пойдут в конфликт,
                    // поэтому отображаем информацию в UI только по первой льготе
                    // * Множество разных регионов в даннос случае ограничено Москвой и Московской областью
                    // Проверяем количество найденных льгот в базе НСИ: если нет льготы хотя бы для одного региона, ничего не показываем
                    Exemption exemptionForUi = exemptions.size() != selectExemptionParams.getRegionCodes().size() ? null : exemptions.get(0);

                    if (exemptionForUi != null) {
                        ExemptionGroup exemptionGroup = exemptionGroupRepository.load(
                                exemptionForUi.getExemptionGroupCode(),
                                selectExemptionParams.getVersionNsi()
                        );
                        ExemptionReadFromCardView.ExemptionInfo exemptionInfo = new ExemptionReadFromCardView.ExemptionInfo();
                        exemptionInfo.exemptionExpressCode = exemptionForUi.getExemptionExpressCode();
                        exemptionInfo.percentage = exemptionForUi.getPercentage();
                        exemptionInfo.groupName = exemptionGroup == null ? null : exemptionGroup.getGroupName();

                        uiThread.post(() -> view.setExemptionInfo(exemptionInfo));
                    } else {
                        uiThread.post(() -> view.setExemptionInfo(null));
                    }

                    uiThread.post(() -> view.showReadCompletedState());
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    view.showUnknownError();
                }, () -> Logger.trace(TAG, "onCompleted"));
    }

    private void startTimer() {
        synchronized (LOCK) {
            timerSubscription = Observable
                    .interval(0, 1, TimeUnit.SECONDS, SchedulersCPPK.background())
                    .doOnSubscribe(() -> view.showSearchCardState())
                    .take(SEARCH_CARD_TIMEOUT + 1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(second -> view.setTimerValue(String.valueOf(SEARCH_CARD_TIMEOUT - second)),
                            throwable -> Logger.error(TAG, throwable),
                            () -> {
                                synchronized (LOCK) {
                                    timerSubscription = null;
                                    if (findCardTask != null) {
                                        findCardTask.cancel();
                                    }
                                }
                            });
        }
    }

    private void stopTimer() {
        synchronized (LOCK) {
            if (timerSubscription != null) {
                timerSubscription.unsubscribe();
                timerSubscription = null;
            }
        }
    }

    @Override
    public void destroy() {
        stopTimer();
        if (readCardSubscription != null) {
            readCardSubscription.unsubscribe();
            readCardSubscription = null;
        }
        super.destroy();
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onCancelSelectExemption();

        void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionForEvents, @Nullable AdditionalInfoForEtt additionalInfoForEtt);
    }

}
