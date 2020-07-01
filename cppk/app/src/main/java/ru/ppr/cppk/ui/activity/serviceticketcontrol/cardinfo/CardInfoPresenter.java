package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardPdData;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.EttCardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.SkmSkmoIpkCardInformation;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.troyka.PassageMarkTroyka;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.DataCarrierReadSettings;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardDataStorage;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.cppk.logic.interactor.FindCardInteractor;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.controlreadbsc.utils.PassageMarkTroykaVerificator;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.interactor.LastPassageInfoBuilder;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.interactor.CardValidityChecker;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.entity.Region;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.RegionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.entity.StopCriteriaType;
import ru.ppr.utils.DateFormatOperations;
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
class CardInfoPresenter extends BaseMvpViewStatePresenter<CardInfoView, CardInfoViewState> {

    private static final String TAG = Logger.makeLogTag(CardInfoPresenter.class);
    /**
     * Номер турникета на станции, через который был совершен проход по ПД. На ПТК записывается 255. При продаже - 0.
     */
    private static final int GATE_NUMBER_FOR_PTK = 255; // В будущем: 14.10.2017 Убрать отсюда
    private final String defaultStr = " — ";
    private static final int TIMER_VALUE = (int) (DataCarrierReadSettings.RFID_FIND_TIME / 1000);

    private Navigator navigator;

    private Subscription readCardSubscription = Subscriptions.unsubscribed();
    private Subscription timerSubscription = Subscriptions.unsubscribed();

    private final UiThread uiThread;


    private final NsiVersionManager nsiVersionManager;
    private final CardValidityChecker cardValidityChecker;
    private final RegionRepository regionRepository;
    private final ExemptionRepository exemptionRepository;
    private final ExemptionGroupRepository exemptionGroupRepository;
    private final StationRepository stationRepository;
    private final PassageMarkTroykaVerificator passageMarkTroykaVerificator;
    private final FindCardInteractor findCardInteractor;
    private final PrivateSettings privateSettings;
    private final ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage;
    private final PdControlCardDataStorage pdControlCardDataStorage;
    private final LastPassageInfoBuilder lastPassageInfoBuilder;
    private boolean validProzod;

    @Inject
    CardInfoPresenter(CardInfoViewState cardInfoViewState,
                      NsiVersionManager nsiVersionManager,
                      CardValidityChecker cardValidityChecker,
                      RegionRepository regionRepository,
                      FindCardInteractor findCardInteractor,
                      ExemptionRepository exemptionRepository,
                      ExemptionGroupRepository exemptionGroupRepository,
                      StationRepository stationRepository,
                      UiThread uiThread,
                      PrivateSettings privateSettings,
                      ServiceTicketControlCardDataStorage serviceTicketControlCardDataStorage,
                      PdControlCardDataStorage pdControlCardDataStorage,
                      PassageMarkTroykaVerificator passageMarkV6Verificator,
                      LastPassageInfoBuilder lastPassageInfoBuilder) {
        super(cardInfoViewState);
        this.nsiVersionManager = nsiVersionManager;
        this.cardValidityChecker = cardValidityChecker;
        this.regionRepository = regionRepository;
        this.exemptionRepository = exemptionRepository;
        this.exemptionGroupRepository = exemptionGroupRepository;
        this.stationRepository = stationRepository;
        this.uiThread = uiThread;
        this.privateSettings = privateSettings;
        this.serviceTicketControlCardDataStorage = serviceTicketControlCardDataStorage;
        this.passageMarkTroykaVerificator = passageMarkV6Verificator;
        this.pdControlCardDataStorage = pdControlCardDataStorage;
        this.lastPassageInfoBuilder = lastPassageInfoBuilder;
        this.findCardInteractor = findCardInteractor;
    }

    @Override
    protected void onInitialize2() {
        Logger.trace(TAG, "onInitialize");

        CardInformation cardInformation = null;
        PassageMark passageMark = null;
        MetroWallet troykaWallet = null;
        CardPdData cardPdData = null;
        StopCriteriaType stopCriteriaType = null;
        boolean serviceTicket = false;

        if (serviceTicketControlCardDataStorage.getLastCardData() != null) {
            ServiceTicketControlCardData serviceTicketControlCardData = serviceTicketControlCardDataStorage.getLastCardData();
            cardInformation = serviceTicketControlCardData.getCardInformation();
            passageMark = serviceTicketControlCardData.getPassageMark();
            stopCriteriaType = StopCriteriaType.SERVICE_TICKET_USAGE;
            serviceTicket = true;
        } else if (pdControlCardDataStorage.getLastCardData() != null) {
            PdControlCardData pdControlCardData = pdControlCardDataStorage.getLastCardData();
            cardInformation = pdControlCardData.getCardInformation();
            passageMark = pdControlCardData.getPassageMark();
            troykaWallet = pdControlCardData.getTroykaWallet();
            cardPdData = pdControlCardData.getCardPdData();
            stopCriteriaType = StopCriteriaType.READ_AND_WRITE;
        }

        if (cardInformation == null) {
            Logger.trace(TAG, "Information not found");
            return;
        }

        updatePassageMark(passageMark);

        showTroykaMarkData(passageMark, troykaWallet, cardPdData);

        TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
        view.setTicketStorageType(ticketStorageType);
        view.setCardNumber(cardInformation.getOuterNumberAsFormattedString());

        CardValidityChecker.Result validityCheckResult = cardValidityChecker.check(cardInformation, stopCriteriaType, serviceTicket, nsiVersionManager.getCurrentNsiVersionId());

        Logger.trace(TAG, "CheckResult = " + validityCheckResult.toString());

        if (validityCheckResult.isInStopList()) {
            // Если карта в стоп-листе
            view.setCardStatus(CardInfoView.CardStatus.IN_STOP_LIST);
            view.setInStopListReasonVisible(true);
            view.setInStopListReason(validityCheckResult.getStoListReason());
        } else {
            // Если карта в не стоп-листе
            view.setInStopListReasonVisible(false);
            if (!validityCheckResult.isCardTimeValid()) {
                // Если срок действия карты истек
                view.setCardStatus(CardInfoView.CardStatus.HAS_EXPIRED);
                view.setValidToVisible(true);
                view.setValidToDate(cardInformation.getExpiryDate());
            } else {
                // Если срок действия карты не истек
                view.setValidToVisible(false);
                view.setCardStatus(CardInfoView.CardStatus.VALID);
            }
        }


        int exemptionCode = getExemptionCode(cardInformation);
        Logger.trace(TAG, "ExemptionCode = " + exemptionCode);

        if (!validityCheckResult.isInStopList() && exemptionCode > 0) {
            // Если карта не в стоп-листе и на ней есть льгота
            view.setExemptionInfoVisible(true);
            view.setExemptionCode(exemptionCode);
            int productionSectionsCode = privateSettings.getProductionSectionId();
            Region region = regionRepository.getRegionForProductionSection(productionSectionsCode, nsiVersionManager.getCurrentNsiVersionId());
            List<Exemption> actualExemptions = null;
            Exemption exemption = null;
            if (region != null) {
                actualExemptions = exemptionRepository.getActualExemptionsForRegion(exemptionCode, region.getCode(), new Date(), nsiVersionManager.getCurrentNsiVersionId());
            }
            if (actualExemptions != null && !actualExemptions.isEmpty()) {
                exemption = actualExemptions.get(0);
            }
            if (exemption != null) {
                view.setExemptionPercentageVisible(true);
                view.setExemptionPercentageValue(exemption.getPercentage());
                ExemptionGroup group = exemption.getExemptionGroup(exemptionGroupRepository, nsiVersionManager.getCurrentNsiVersionId());
                view.setExemptionGroupName(group == null ? null : group.getGroupName());
            } else {
                view.setExemptionPercentageVisible(false);
                view.setExemptionGroupName(null);
            }

            if (cardInformation instanceof EttCardInformation) {
                EttCardInformation ettCardInformation = (EttCardInformation) cardInformation;
                EttData ettData = ettCardInformation.getEttData();
                if (ettData != null) {
                    view.setFio(ettData.getFirstName(), ettData.getSecondName(), ettData.getSurname());
                } else {
                    view.setFio(null, null, null);
                }
            } else if (cardInformation instanceof SkmSkmoIpkCardInformation) {
                SkmSkmoIpkCardInformation skmSkmoIpkCardInformation = (SkmSkmoIpkCardInformation) cardInformation;
                PersonalData personalData = skmSkmoIpkCardInformation.getPersonalData();
                if (personalData != null) {
                    view.setFio(personalData.getName(), personalData.getSecondName(), personalData.getSurname());
                } else {
                    view.setFio(null, null, null);
                }
            } else {
                view.setFio(null, null, null);
            }
        } else {
            // Если карта в стоп-листе или на ней нет льготы
            view.setExemptionInfoVisible(false);
        }
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
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
    }


    public void startWriteCard(){
        Logger.trace(TAG, "startWriteCard");
        if (!readCardSubscription.isUnsubscribed()) {
            throw new IllegalStateException("Operation is already running");
        }
        readCardSubscription = Completable
                .fromAction(() -> {
                    serviceTicketControlCardDataStorage.clearCardData();
                    pdControlCardDataStorage.clearCardData();
                    view.setState(CardInfoView.State.SEARCH_CARD);
                    startTimer();
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .andThen(findCardInteractor.findCard())
                .onErrorResumeNext(throwable -> {
                    Logger.error(TAG, "CardNotFoundException");
                    return Single.error(new CardNotFoundException());
                })
                .observeOn(SchedulersCPPK.rfid())
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    stopTimer();
                    view.setState(CardInfoView.State.READ_CARD);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(cardReader -> Single.fromCallable(() -> writeCard(cardReader)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    view.setState(CardInfoView.State.PROCESSING_DATA);
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
                        view.setState(CardInfoView.State.SEARCH_CARD_ERROR);
                    } else {
                        view.setState(CardInfoView.State.UNKNOWN_ERROR);
                    }
                });
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
                        view.setState(CardInfoView.State.SEARCH_CARD_ERROR);
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


    /*
    * Реализация записи данных на карту
    * */
    private ReadBscResult writeCard(CardReader cardReader){





        return new ReadBscResult();
    }

    private void stopReadCard() {
        Logger.trace(TAG, "stopReadCard");
        readCardSubscription.unsubscribe();
    }

    private boolean handleResult(ReadBscResult readBscResult) {
        Logger.trace(TAG, "handleResult");
        return true;
/*        if (readBscResult.serviceTicketControlCardData != null) { //TODO реализовать свой способ
            serviceTicketControlCardDataStorage.putCardData(readBscResult.serviceTicketControlCardData);
            return true;
        } else if (readBscResult.pdControlCardData != null) {
            pdControlCardDataStorage.putCardData(readBscResult.pdControlCardData);
            return true;
        }
        return false;*/
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
            uiThread.post(() -> view.setState(CardInfoView.State.UNKNOWN_ERROR));
        }
    }

    private void stopTimer() {
        Logger.trace(TAG, "stopTimer");
        timerSubscription.unsubscribe();
    }

    private void onBscResultRead(ReadBscResult readBscResult) {
        Logger.trace(TAG, "onBscResultRead:" + readBscResult);
        navigator.navigateToServiceTicketControlActivity();
/*        if (readBscResult.serviceTicketControlCardData != null) { //TODO реализовать работа с данными
            navigator.navigateToServiceTicketControlActСivity();
        }*/
    }


    public boolean isValidProzod() {
        return validProzod;
    }


    private void showTroykaMarkData(PassageMark passageMark, MetroWallet troykaWallet, CardPdData cardPdData) {
        if (passageMark instanceof PassageMarkTroyka) {
            view.setInfoVisiblePassageMark(true);
            view.setVisibleOpeningClosureTrip(true);
            showPassMarkData(passageMark);
            PassageMarkTroyka passageMarkTroyka = ((PassageMarkTroyka) passageMark);
            showOutComeStation(passageMarkTroyka.getOutComeStation(), passageMarkTroyka.isCheckExitStation()? 0:  passageMarkTroyka.getTurniketNumber());
            showTroykaPoezdkiLeft(cardPdData);
            showWalletData(troykaWallet);
        } else {
            view.setResOpeningClosureTrip(R.string.default_str);
            view.setVisibleOpeningClosureTrip(false);
            view.setInfoVisiblePassageMark(false);
            view.setLastPassageTime(defaultStr);
            view.setProhodValid(R.color.default_pd_info_normal, R.string.card_info_prohod_no_data);
            view.setWalletUnitsLeft(defaultStr);
            view.showOutComeStation(defaultStr);
        }
    }

    private void showTroykaPoezdkiLeft(CardPdData cardPdData) {
        if (cardPdData != null && cardPdData.isValidFormatData()) {
            String date_time = DateFormatOperations.getDateddMMyyyyHHmm(cardPdData.getValidityDateTime());
            showTroykaPoezdkiLeft(String.valueOf(cardPdData.getCountRematingPerformedTrips()));
            showNameTypeTicket(getNameTypeTicket(cardPdData.getTypeTicket()));
            showValidityDateTime(date_time);
        } else {
            showTroykaPoezdkiLeft(defaultStr);
            showNameTypeTicket(defaultStr);
            showValidityDateTime(defaultStr);
        }
    }

    private void showPassMarkData(PassageMark passageMark) {
        final PassageMarkTroyka passageMarkTroyka = (PassageMarkTroyka) passageMark;
        view.setLastPassageTime(DateFormatOperations.getDateddMMyyyyHHmm(new Date(passageMarkTroyka.getIntersectionLongTime())));
        validProzod = passageMarkTroykaVerificator.isValid(passageMarkTroyka.isCheckExitStation(), passageMarkTroyka.getIntersectionLongTime(), passageMarkTroyka.isValidImitovstavka());

        Logger.trace(TAG, "showPassMarkData: passageMark:" + passageMark + " valid:" + validProzod);

        final int color = validProzod ? R.color.service_ticket_control_card_info_success : R.color.service_ticket_control_card_info_error;
        final int text = validProzod ? R.string.card_info_prohod_valid : R.string.card_info_prohod_not_valid;
        view.setProhodValid(color, text);

        final int str_openness = validProzod ? R.string.btn_close_passage_mark : R.string.btn_open_passage_mark ;
        view.setResOpeningClosureTrip(str_openness);

    }


    private String getNameTypeTicket(int value) {
        return (488 == value) ? "60 поездок\nЕДИНЫЙ ТК" : defaultStr;
/*        switch (value) {
            case 433:
                return "Kошелек";
            case 468:
                return "1 сутки\nЕДИНЫЙ ТК";
            case 469:
                return "30 дней\nЕДИНЫЙ ТК";
            case 470:
                return "90 дней\nЕДИНЫЙ ТК";
            case 471:
                return "365 дней\nЕДИНЫЙ ТК";
            case 481:
                return "Перезапись";
            case 488:
                return "60 поездок\nЕДИНЫЙ ТК";
            case 893:
                return "1 сутки\nМО ТК";
            case 894:
                return "3 сутки\nЕДИНЫЙ ТК";
            case 895:
                return "3 сутки\nМО ТК";
            case 896:
                return "30 дней\nМО ТК";
            case 897:
                return "90 дней\nМО ТК";
            case 898:
                return "365 дней\nМО ТК";
            default:
                return defaultStr;
        }*/
    }

    private void showWalletData(MetroWallet troykaWallet) {
        String walletUnitsRes = defaultStr;
        if (troykaWallet != null && troykaWallet.isValidUnitsData()) {
            final BigDecimal unitsLeft = troykaWallet.getUnitsLeft();
            final boolean overMoney = unitsLeft.compareTo(new BigDecimal(3000.00)) > 0;
            walletUnitsRes = overMoney ? defaultStr : troykaWallet.getUnitsLeft().toString();
        }
        view.setWalletUnitsLeft(walletUnitsRes);
    }

    private void showTroykaPoezdkiLeft(String poezdka_str) {
        view.setTroykaPoezdkiLeft(poezdka_str);
    }


    private void showOutComeStation(final long outStationCode, int numberTurniket) {
        Station station = stationRepository.load(outStationCode, nsiVersionManager.getCurrentNsiVersionId());
        String station1 = station == null ? null : station.getName();
        if(station1 != null)
            station1 = numberTurniket +"\n(" + station1+")";
        view.showOutComeStation(station1);
    }


    private void showNameTypeTicket(String name_type_ticket) {
        view.showNameTypeTicket(name_type_ticket);
    }

    private void showValidityDateTime(String date_str) {
        view.showValidityDateTime(date_str);
    }

    void onPassageMarkChanged(PassageMark passageMark) {
        updatePassageMark(passageMark);
    }

    private int getExemptionCode(CardInformation cardInformation) {
        if (cardInformation instanceof EttCardInformation) {
            EttCardInformation ettCardInformation = (EttCardInformation) cardInformation;
            return ettCardInformation.getBscInformation().getExemptionCode();
        } else if (cardInformation instanceof SkmSkmoIpkCardInformation) {
            SkmSkmoIpkCardInformation skmSkmoIpkCardInformation = (SkmSkmoIpkCardInformation) cardInformation;
            return skmSkmoIpkCardInformation.getBscInformation().getExemptionCode();
        }
        return 0;
    }

    private void updatePassageMark(PassageMark passageMark) {

        ServiceData serviceData = null;
        List<Pd> pdList = null;

        if (serviceTicketControlCardDataStorage.getLastCardData() != null) {
            ServiceTicketControlCardData serviceTicketControlCardData = serviceTicketControlCardDataStorage.getLastCardData();
            serviceData = serviceTicketControlCardData.getServiceData();
        } else if (pdControlCardDataStorage.getLastCardData() != null) {
            PdControlCardData pdControlCardData = pdControlCardDataStorage.getLastCardData();
            pdList = pdControlCardData.getPdList();
        }

        LastPassageInfoBuilder.Result lastPassageInfo = lastPassageInfoBuilder.build(passageMark, pdList, serviceData);
        Logger.trace(TAG, "lastPassageInfo: " + lastPassageInfo);

        showPassTime(passageMark, lastPassageInfo);

        showStation(passageMark, lastPassageInfo);
    }

    private void showPassTime(PassageMark passageMark, LastPassageInfoBuilder.Result lastPassageInfo) {
        Logger.trace(TAG, "showPassTime: " + passageMark + " lastPassageInfo:" + lastPassageInfo );
        if (lastPassageInfo.getPassageTime() == null) {
            if (passageMark instanceof PassageMarkTroyka) {
                final Date lastPassageTime = new Date(((PassageMarkTroyka) passageMark).getIntersectionLongTime());
                view.setLastPassageTime(DateFormatOperations.getDateddMMyyyyHHmm(lastPassageTime));
            } else {
                view.setLastPassageTime(null);
            }
        } else {
            view.setLastPassageTime(DateFormatOperations.getDateddMMyyyyHHmm(new Date(lastPassageInfo.getPassageTime())));
        }
    }

    private void showStation(PassageMark passageMark, LastPassageInfoBuilder.Result lastPassageInfo) {
        if (passageMark != null) {
            final long passageStationCode = passageMark.getPassageStationCode();

            Station station = stationRepository.load(passageStationCode, nsiVersionManager.getCurrentNsiVersionId());
            String station1 = station == null ? null : station.getName();
            if(passageMark instanceof PassageMarkTroyka){
                if(station1 != null){
                    if(((PassageMarkTroyka) passageMark).isCheckExitStation()) {
                        station1 = ((PassageMarkTroyka) passageMark).getTurniketNumber()+ "\n(" + station1+")";
                    }
                }
            }
            view.setPassageStation(station1, CardInfoView.PassageStationType.STATION);
        } else if (lastPassageInfo.getTurnstileNumber() != null && lastPassageInfo.getTurnstileNumber() == GATE_NUMBER_FOR_PTK) {
            view.setPassageStation(null, CardInfoView.PassageStationType.PTK);
        } else {
            view.setPassageStation(null, CardInfoView.PassageStationType.UNKNOWN);
        }
    }


    interface Navigator {
        void navigateToServiceTicketControlActivity();
    }

    private class CardNotFoundException extends Exception {
    }
}
