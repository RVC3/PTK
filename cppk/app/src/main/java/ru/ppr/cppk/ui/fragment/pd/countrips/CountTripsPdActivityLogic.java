package ru.ppr.cppk.ui.fragment.pd.countrips;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import com.google.common.base.Preconditions;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.RealPd;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.ReadCardInformationInteractor;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.PassageMarkToLegacyMapper;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.NeedCreateControlEventChecker;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.TransferPdChecker;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.pd.checker.PdLastPassageChecker;
import ru.ppr.cppk.logic.pd.checker.ValidAndControlNeededChecker;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripData;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripResult;
import ru.ppr.cppk.ui.activity.decrementtrip.sharedstorage.DecrementTripDataStorage;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.countrips.interactor.PdV23V24TripsCountCalculator;
import ru.ppr.cppk.ui.fragment.pd.countrips.interactor.TripsCountCalculator;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.CountTripsPdControlData;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.CountTripsPdViewParams;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CombinedCountTripsPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.CountTripsPdViewModel;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Временное решение по выносу логики из {@link CountTripsFragment}.
 *
 * @author Aleksandr Brazhkin
 */
@Deprecated
public class CountTripsPdActivityLogic {

    private static final String TAG = Logger.makeLogTag(CountTripsPdActivityLogic.class);

    // region Di
    @Inject
    LocalDaoSession localDaoSession;
    @Inject
    NsiDaoSession nsiDaoSession;
    @Inject
    PrivateSettings privateSettings;
    @Inject
    CommonSettings commonSettings;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    TariffPlanRepository tariffPlanRepository;
    @Inject
    DeferredActionHandler deferredActionHandler;
    @Inject
    PtkModeChecker ptkModeChecker;
    @Inject
    TransferPdChecker transferPdChecker;
    @Inject
    FindCardTaskFactory findCardTaskFactory;
    @Inject
    PdControlCardDataStorage pdControlCardDataStorage;
    @Inject
    ReadCardInformationInteractor readCardInformationInteractor;
    @Inject
    TripsCountCalculator tripsCountCalculator;
    @Inject
    PdV23V24TripsCountCalculator pdV23V24TripsCountCalculator;
    @Inject
    DecrementTripDataStorage decrementTripDataStorage;
    @Inject
    NeedCreateControlEventChecker needCreateControlEventChecker;
    @Inject
    ValidAndControlNeededChecker validAndControlNeededChecker;
    @Inject
    PdLastPassageChecker lastPassageChecker;
    @Inject
    PdValidityPeriodCalculator pdValidityPeriodCalculator;
    @Inject
    PdVersionChecker pdVersionChecker;
    //endregion

    //region Arguments
    private final PD legacyPd;
    private final ValidityPdVariants validityVariants;
    private final RealPd pd;
    private int availableTripsCount;
    private boolean tripsCountDecremented;
    private boolean trips7000CountDecremented;
    private boolean fixPassageMarkDecrementBtn;
    //endregion

    private final Activity activity;
    private final CountTripsFragment fragment;
    private final Callback callback;

    private final CountTripsPdViewParams screenParams = new CountTripsPdViewParams();

    public CountTripsPdActivityLogic(Activity activity,
                                     CountTripsFragment fragment,
                                     Callback callback,
                                     @NonNull PD legacyPd,
                                     @NonNull ValidityPdVariants validityVariants) {
        this.activity = activity;
        this.fragment = fragment;
        this.callback = callback;
        this.legacyPd = legacyPd;
        this.validityVariants = validityVariants;

        pd = (RealPd) new PdFromLegacyMapper().fromLegacyPd(legacyPd);

        Dagger.appComponent().inject(this);
        fillScreenParams();
        fragment.setCallback(fragmentCallback);
        fragment.setCountTripsPdViewParams(screenParams);
    }

    private void fillScreenParams() {

        CountTripsPdViewModel countTripsPdViewModel;
        CombinedCountTripsPdViewModel combinedCountTripsPdViewModel = null;

        switch (pd.getVersion()) {
            case V23:
            case V24: {
                // Это комбинированный абонемент
                combinedCountTripsPdViewModel = new CombinedCountTripsPdViewModel();
                countTripsPdViewModel = combinedCountTripsPdViewModel;
                break;
            }
            default: {
                countTripsPdViewModel = new CountTripsPdViewModel();
            }
        }

        screenParams.setPdViewModel(countTripsPdViewModel);
        CountTripsPdViewModel pdViewModel = screenParams.getPdViewModel();

        PdControlCardData pdControlCardData = pdControlCardDataStorage.getLastCardData();
        Tariff tariff = legacyPd.getTariff();
        TicketType ticketType = tariff == null ? null : tariff.getTicketType(nsiDaoSession);
        ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = legacyPd.getPassageMark();
        PassageMark passageMark = pdControlCardData == null ? null : pdControlCardData.getPassageMark();
        Integer hwCounter = legacyPd.getHwCounterValue();

        if (pdControlCardData == null || ticketType == null || passageMark == null || legacyPassageMark == null || hwCounter == null) {
            Logger.info(TAG, "Неверные входные данные");
            screenParams.setInvalidData(true);
            return;
        }

        Logger.info(TAG, "onCreateView() Номер билета: " + legacyPd.numberPD);

        if ((validityVariants == ValidityPdVariants.ONE_PD_INVALID || validityVariants == ValidityPdVariants.ONE_PD_VALID)) {
            screenParams.setSmallSize(false);
        } else {
            screenParams.setSmallSize(true);
        }

        // Устанавливаем номер ПД
        pdViewModel.setNumber(pd.getOrderNumber());
        // Устанавливаем станцию отправления
        Station departureStation = tariff.getStationDeparture(nsiDaoSession);
        pdViewModel.setDepStationName(departureStation == null ? null : departureStation.getName());
        // Устанавливаем станцию назначения
        Station destinationStation = tariff.getStationDestination(nsiDaoSession);
        pdViewModel.setDestStationName(destinationStation == null ? null : destinationStation.getName());
        // Устанавливаем название ПД
        pdViewModel.setTitle(ticketType.toString());
        // Устанавливаем дату начала действия
        pdViewModel.setValidityFromDate(legacyPd.getStartPdDate());
        // Устанавливаем дату окончания действия
        // Вычитаем 1 день, т.к. 1-ый день действия уже учтен во времени начала действия ПД
        final Calendar endTime = Calendar.getInstance();
        endTime.setTime(legacyPd.getStartPdDate());
        endTime.add(Calendar.DAY_OF_MONTH, pdValidityPeriodCalculator.calcValidityPeriod(legacyPd.getStartPdDate(), legacyPd.wayType, ticketType, tariff.getVersionId()) - 1);
        pdViewModel.setValidityToDate(endTime.getTime());
        // Устанавливаем категорию поезда для ПД
        TariffPlan tariffPlan = tariff.getTariffPlan(tariffPlanRepository);
        TrainCategory trainCategory = tariffPlan == null ? null : tariffPlan.getTrainCategory(nsiDaoSession);
        pdViewModel.setTrainCategoryName(trainCategory == null ? null : trainCategory.name);
        // Устанавливаем максимальный интервал времени с момента последнего списания поездки
        int maxTimeFormPrevPassage = commonSettings.getMaxTimeAgoMark();
        pdViewModel.setMaxHoursAgo(maxTimeFormPrevPassage);

        // Устанавливаем количество оставшихся поездок
        setTripsCountInfo(screenParams, pdViewModel);
        // Устанавливаем информацию о последнем проходе
        setLastPassageInfo(pdViewModel, legacyPd);

        // Отображаем ошибки
        setErrors(pdViewModel);

        // Настраиваем видимость кнопок контроля
        if (!ptkModeChecker.isTransferControlMode() && validAndControlNeededChecker.isValidAndControlNeeded(legacyPd)) {
            // Если мы не в автобусе и ПД валиден и требуется создание события контроля
            // Покажем кнопку "Билет НЕдействителен"
            screenParams.setTicketNotValidBtnVisible(true);
        } else {
            screenParams.setTicketNotValidBtnVisible(false);
        }

        showSalePdExtraChargeBtnIfNeed(legacyPd.errors, screenParams);

        boolean transferPd = transferPdChecker.check(pd);
        pdViewModel.setTransfer(transferPd);

        boolean decrementTripDenied = false;
        // Настраиваем видимость кнопки "Списать поездку"
        if (!commonSettings.isDecrementTripAllowed()) {
            // 1. Запрещаем списание поездки, если запрет утсановлен в настройках
            decrementTripDenied = true;
        } else if (availableTripsCount == 0) {
            // 2. Запрещаем списание поездки, если поездок уже не осталось
            decrementTripDenied = true;
        } else if (legacyPd.isLastPassageValid()) {
            // 3. Запрещаем списание поездки, если метка валидна с учетом режима контроля
            decrementTripDenied = true;
        } else if (!legacyPd.errors.isEmpty()) {
            // 4. Запрещаем списание поездки, если есть ошибки в валидности ПД
            decrementTripDenied = true;
        } else if (!needCreateControlEventChecker.check(legacyPd)) {
            // 5. Запрещаем списание поездки, если ПД не должен контролироваться в текущем режиме работы
            decrementTripDenied = true;
        }

        screenParams.setDecrementTripBtnVisible(!decrementTripDenied);
    }

    private void setTripsCountInfo(@NonNull CountTripsPdViewParams screenParams, @NonNull CountTripsPdViewModel pdViewModel) {
        // Устанавливаем количество оставшихся поездок
        int hwCounterValue = legacyPd.getHwCounterValue() == null ? 0 : legacyPd.getHwCounterValue();
        availableTripsCount = tripsCountCalculator.calcAvailableTripsCount(pd, hwCounterValue);
        Logger.info(TAG, "availableTripsCount: " + availableTripsCount);

        if (pdViewModel instanceof CombinedCountTripsPdViewModel) {
            // Это комбинированный абонемент
            CombinedCountTripsPdViewModel combinedCountTripsPdViewModel = (CombinedCountTripsPdViewModel) pdViewModel;
            PdV23V24TripsCountCalculator.Result tripsCountResult = pdV23V24TripsCountCalculator.calcTripsCount(hwCounterValue);
            Logger.info(TAG, "availableTripsCount: " + tripsCountResult);
            int availableTripsTotalCount = tripsCountResult.getAvailableTripsTotalCount();
            int availableTrips7000Count = tripsCountResult.getAvailableTrips7000Count();
            combinedCountTripsPdViewModel.setAvailableTripsCount(availableTripsTotalCount);
            combinedCountTripsPdViewModel.setAvailableTripsCount7000(availableTrips7000Count);
            int availableTripsCount6000 = availableTripsTotalCount - availableTrips7000Count;
            availableTripsCount6000 = Math.max(0, availableTripsCount6000);
            combinedCountTripsPdViewModel.setAvailableTripsCount6000(availableTripsCount6000);

            // Настраиваем текст кнопки списания поездки
            if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                // Если мы в скором поезде
                if (availableTripsTotalCount < tripsCountResult.getAvailableTrips7000RawCount()
                        && legacyPd.isLastPassage6000Valid()) {
                    // http://agile.srvdev.ru/browse/CPPKPP-43822
                    // Если есть валидный проход 6000 и общее количество оставшихся поездок
                    // меньше оставшегося количества на скорый поезд (по данным счетчика),
                    // называем кнопку "Исправить метку прохода", т.к.
                    // при списании поездки количество поездок не изменится
                    screenParams.setDecrement7000(false);
                    screenParams.setFixPassageMark(true);
                    fixPassageMarkDecrementBtn = true;
                } else {
                    // Называем кнопку "Списать поездку на 7000-ый поезд"
                    screenParams.setDecrement7000(true);
                    screenParams.setFixPassageMark(false);
                    fixPassageMarkDecrementBtn = false;
                }
            } else {
                // Если мы в пассажирском поезде
                if (availableTripsCount6000 == 0) {
                    // Если нет поездок 6000, называем кнопку "Списать поездку на 7000-ый поезд"
                    screenParams.setDecrement7000(true);
                    screenParams.setFixPassageMark(false);
                    fixPassageMarkDecrementBtn = false;
                } else {
                    // Если есть поездки 6000, называем кнопку "Списать поездку"
                    screenParams.setDecrement7000(false);
                    screenParams.setFixPassageMark(false);
                    fixPassageMarkDecrementBtn = false;
                }
            }

            CountTripsPdControlData countTripsPdControlData = new CountTripsPdControlData();
            countTripsPdControlData.setTripsCountDecremented(tripsCountDecremented);
            countTripsPdControlData.setTrips7000CountDecremented(trips7000CountDecremented);
            countTripsPdControlData.setAvailableTripsCount(availableTripsTotalCount);
            countTripsPdControlData.setAvailableTrips7000Count(availableTrips7000Count);
            callback.onCountTripsPdControlDataChanged(legacyPd, countTripsPdControlData);
        } else {
            // Классический сценарий
            pdViewModel.setAvailableTripsCount(availableTripsCount);
            CountTripsPdControlData countTripsPdControlData = new CountTripsPdControlData();
            countTripsPdControlData.setTripsCountDecremented(tripsCountDecremented);
            countTripsPdControlData.setTrips7000CountDecremented(false);
            countTripsPdControlData.setAvailableTripsCount(availableTripsCount);
            countTripsPdControlData.setAvailableTrips7000Count(null);
            callback.onCountTripsPdControlDataChanged(legacyPd, countTripsPdControlData);
        }
    }

    private void setLastPassageInfo(@NonNull CountTripsPdViewModel pdViewModel, @NonNull PD legacyPd) {
        if (pdViewModel instanceof CombinedCountTripsPdViewModel) {
            // Это комбинированный абонемент
            CombinedCountTripsPdViewModel combinedCountTripsPdViewModel = (CombinedCountTripsPdViewModel) pdViewModel;
            if (legacyPd.isLastPassageTurnstile7000()) {
                // Если был вход через турникет 7000
                // Контроль 6000 и 7000
                combinedCountTripsPdViewModel.setLastPassageTime(null);
                combinedCountTripsPdViewModel.setLastPassageTime7000(legacyPd.getLastPassageTime());
                combinedCountTripsPdViewModel.setLastPassageError(false);
                combinedCountTripsPdViewModel.setLastPassage7000Error(!legacyPd.isLastPassageValid());
                combinedCountTripsPdViewModel.setWrongTrainCategory(false);
            } else {
                // Если не было входа через турникет 7000
                combinedCountTripsPdViewModel.setLastPassageTime(legacyPd.getLastPassageTime());
                combinedCountTripsPdViewModel.setLastPassageTime7000(null);
                if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                    // Контроль 7000
                    combinedCountTripsPdViewModel.setLastPassageError(!legacyPd.isLastPassage6000Valid());
                    combinedCountTripsPdViewModel.setLastPassage7000Error(true);
                    combinedCountTripsPdViewModel.setWrongTrainCategory(legacyPd.isLastPassage6000Valid());
                } else {
                    // Контроль 6000
                    combinedCountTripsPdViewModel.setLastPassageError(!legacyPd.isLastPassageValid());
                    combinedCountTripsPdViewModel.setLastPassage7000Error(!legacyPd.isLastPassageValid());
                    combinedCountTripsPdViewModel.setWrongTrainCategory(false);
                }
            }
        } else {
            // Классический сценарий
            pdViewModel.setLastPassageTime(legacyPd.getLastPassageTime());
            pdViewModel.setLastPassageError(!legacyPd.isLastPassageValid());
        }

        if (legacyPd.isLastPassageValid()) {
            // Сообщаем на уровень выше, что метка прохода не устарела
            onPassageMarkChecked();
        }
    }

    private void setErrors(@NonNull CountTripsPdViewModel pdViewModel) {
        List<PassageResult> errors = legacyPd.errors;
        pdViewModel.setValid(errors.isEmpty());
        pdViewModel.setTrainCategoryError(errors.contains(PassageResult.BannedTrainType));
        pdViewModel.setRouteError(errors.contains(PassageResult.InvalidStation));
        pdViewModel.setValidityFromDateError(errors.contains(PassageResult.TooEarly));
        pdViewModel.setValidityToDateError(errors.contains(PassageResult.TooLate));

        if (pdViewModel instanceof CombinedCountTripsPdViewModel) {
            // Это комбинированный абонемент
            CombinedCountTripsPdViewModel combinedCountTripsPdViewModel = (CombinedCountTripsPdViewModel) pdViewModel;
            if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                combinedCountTripsPdViewModel.setNoTrips7000Error(errors.contains(PassageResult.NoTrips));
            } else {
                combinedCountTripsPdViewModel.setNoTripsError(errors.contains(PassageResult.NoTrips));
            }
        } else {
            // Классический сценарий
            pdViewModel.setNoTripsError(errors.contains(PassageResult.NoTrips));
        }

        if (errors.contains(PassageResult.InvalidSign)) {
            pdViewModel.setInvalidEdsKeyError(true);
        } else if (errors.contains(PassageResult.SignKeyRevoked)) {
            pdViewModel.setRevokedEdsKeyError(true);
        } else if (errors.contains(PassageResult.BannedByStopListTickets)) {
            pdViewModel.setTicketInStopListError(true);
        }
    }

    /**
     * Настраивает отображение кнопки "Оформить ПД по доплате"
     *
     * @param errors Результат проверки ПД
     */
    private void showSalePdExtraChargeBtnIfNeed(List<PassageResult> errors, CountTripsPdViewParams screenParams) {
        boolean shouldShowBtn = false;

        // В режиме работы контроля трансфера  не показываем кнопку доплаты
        if (!ptkModeChecker.isTransferControlMode()) {

            boolean shouldShowBtnForMobileCashRegister = false;
            boolean shouldShowBtnForClassicCase = false;
            boolean shouldShowBtnForCombinedCountTripsCase = false;

            // В режиме мобильной кассы показываем кнопку доплаты, если билет валиден, проход валиден
            // (в режиме мобильной кассы всегда установлен режим контроля 6000 поезда)
            if (privateSettings.isMobileCashRegister()) {
                if (legacyPd.isLastPassageValid()) {
                    if (errors.isEmpty()) {
                        // ПД валиден
                        // ПТК: всегда выводить кнопку "доплатить ПД" в режиме мобильной кассы
                        // https://aj.srvdev.ru/browse/CPPKPP-3169931699
                        if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.PASSENGER) {
                            shouldShowBtnForMobileCashRegister = true;
                        }
                    }
                }
            }

            PdVersion version = PdVersion.getByCode(legacyPd.versionPD);
            Preconditions.checkNotNull(version);
            if (pdVersionChecker.isCombinedCountTripsSeasonTicket(version)) {
                // Для комбинированного абонемента на количество поездок
                // в режиме контроля 6000 поезда кнопка доплаты не показывается,
                // в режиме контроля 7000 поезда кнопка доплаты показывается, если билет валиден, проход через 6000 турникет валиден и нет поездок на 7000 поезд.

                int hwCounterValue = legacyPd.getHwCounterValue() != null ? legacyPd.getHwCounterValue() : 0;
                PdV23V24TripsCountCalculator.Result tripsCountResult = pdV23V24TripsCountCalculator.calcTripsCount(hwCounterValue);
                int availableTrips7000Count = tripsCountResult.getAvailableTrips7000Count();

                if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                    if (errors.isEmpty() && legacyPd.isLastPassage6000Valid() && availableTrips7000Count == 0) {
                        shouldShowBtnForCombinedCountTripsCase = true;
                    }
                }
            } else {
                // Для классического абонемента на количество поездок проверяем валидность прохода и наличие ошибки BannedTrainType
                if (legacyPd.isLastPassageValid()) {
                    if (errors.size() == 1 && errors.contains(PassageResult.BannedTrainType)) {
                        shouldShowBtnForClassicCase = true;
                    }
                }
            }

            if (shouldShowBtnForClassicCase || shouldShowBtnForCombinedCountTripsCase || shouldShowBtnForMobileCashRegister) {
                shouldShowBtn = true;
            }
        }

        //покажем кнопку оформить по доплате
        screenParams.setSellSurchargeBtnVisible(shouldShowBtn
                && legacyPd.hasFareTariff
                && permissionChecker.checkPermission(PermissionDvc.SalePdSurchange));
    }

    private void onPassageMarkChecked() {
        if (needCreateControlEventChecker.check(legacyPd)) {
            // Сообщаем на уроень выше, что метка прохода не устарела
            callback.onPassageMarkChecked();
        }
    }

    private void showDecrementTripConfirmDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                activity.getString(fixPassageMarkDecrementBtn ?
                        R.string.fix_passage_mark_dialog_msg :
                        R.string.decrement_trip_dialog_msg
                ),
                activity.getString(R.string.count_trips_decrement_trip_dialog_yes),
                activity.getString(R.string.count_trips_decrement_trip_dialog_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(activity.getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> decrementTrip());
    }

    /**
     * Запускает экран списания поездки
     */
    private void decrementTrip() {

        BscInformation bscInformation = legacyPd.getBscInformation();
        if (bscInformation == null) {
            throw new IllegalStateException("bscInformation should not be null");
        }

        Integer hwCounterValue = legacyPd.getHwCounterValue();
        if (hwCounterValue == null) {
            throw new IllegalStateException("hwCounterValue should not be null");
        }

        ru.ppr.cppk.dataCarrier.entity.PassageMark passageMark = legacyPd.getPassageMark();
        PassageMarkVersion passageMarkVersion = PassageMarkVersion.getByCode(passageMark.getVersionMark());
        // "Счетчик использования карты" есть только на метках V4 и V8
        // На метке V5 поле называется "Последнее показание счетчика при проходе" и имеет другой смысл
        int usageCounterValue = (passageMarkVersion == PassageMarkVersion.V4
                || passageMarkVersion == PassageMarkVersion.V8) ? passageMark.getCounterCard() : 0;

        TrainCategoryPrefix trainCategory = privateSettings.getTrainCategoryPrefix();
        int pdVersion = legacyPd.versionPD == null ? -1 : legacyPd.versionPD;

        DecrementTripParams decrementTripParams = new DecrementTripParams(
                bscInformation.getCardUID(),
                hwCounterValue,
                usageCounterValue,
                legacyPd.orderNumberPdOnCard,
                legacyPd.getSaleDate(),
                pdVersion,
                trainCategory,
                legacyPd.isLastPassage6000Valid()
        );

        fragment.navigateToDecrementTripActivity(decrementTripParams);
    }

    /**
     * Обновляет ифнормацию о метке прохода
     *
     * @param passageMark Мекта прохода
     */
    private void updatePassageMarkView(PassageMark passageMark) {
        CountTripsPdViewModel pdViewModel = screenParams.getPdViewModel();

        ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(passageMark);
        // Выполняем проверку последнего прохода
        lastPassageChecker.checkLastPassage(legacyPd, legacyPassageMark);
        // Устанавливаем количество оставшихся поездок
        setTripsCountInfo(screenParams, pdViewModel);
        // Устанавливаем информацию о последнем проходе
        setLastPassageInfo(pdViewModel, legacyPd);

        // Скрываем кнопку списания
        screenParams.setDecrementTripBtnVisible(false);

        // Обновляем UI
        fragment.setCountTripsPdViewParams(screenParams);

        /*--- Вкладка БСК ---- */
        //расскажем Activity что метка изменилась
        callback.onPassageMarkChanged(passageMark);
    }

    private final CountTripsFragment.Callback fragmentCallback = new CountTripsFragment.Callback() {

        @Override
        public void onZoomDialogShown() {
            callback.setHardwareButtonsEnabled(false);
        }

        @Override
        public void onZoomDialogHidden() {
            callback.setHardwareButtonsEnabled(true);
        }

        @Override
        public void onDecrementTripResultReturned(@Nullable DecrementTripResult decrementTripResult) {
            if (decrementTripResult != null && decrementTripResult.isTripDecremented()) {
                DecrementTripData decrementTripData = decrementTripDataStorage.getLastData();
                if (decrementTripData != null) {
                    PassageMark passageMark = decrementTripData.getPassageMark();
                    legacyPd.setHwCounterValue(decrementTripData.getNewHwCounterValue());
                    ////////
                    tripsCountDecremented = true;
                    if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.EXPRESS) {
                        trips7000CountDecremented = true;
                    }
                    ////////
                    updatePassageMarkView(passageMark);
                }
            }
        }

        @Override
        public void onDecrementTripBtnClicked() {
            showDecrementTripConfirmDialog();
        }

        @Override
        public void onSellWithExtraPaymentBtnClicked() {

            if (!privateSettings.isSaleEnabled()) {
                throw new IllegalStateException("Method should not be called");
            }
            // Переходим на экран оформления доплаты
            // Раньше здесь была проверка возможности использования льготы
            // Удалили в http://agile.srvdev.ru/browse/CPPKPP-33416
            // Потому что теперь мы по умолчанию всё равно оформляем доплату без льготы
            callback.onSaleSurchargeBtnClicked(legacyPd);
        }

        @Override
        public void onPdNotValidBtnClicked() {
            callback.onPdNotValidBtnClicked(legacyPd);
            if (!privateSettings.isSaleEnabled() || !permissionChecker.checkPermission(PermissionDvc.SalePd)) {
                activity.finish();
                return;
            }
            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    activity.getString(R.string.count_trips_sale_new_pd_dialog_msg),
                    activity.getString(R.string.count_trips_sale_new_pd_dialog_yes),
                    activity.getString(R.string.count_trips_sale_new_pd_dialog_no),
                    LinearLayout.HORIZONTAL,
                    0);
            simpleDialog.setCancelable(false);
            simpleDialog.show(activity.getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                PdSaleParams pdSaleParams = new PdSaleParams();
                pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
                pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
                Navigator.navigateToPdSaleActivity(activity, pdSaleParams);
                activity.finish();
            });
            simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
                activity.finish();
            });
        }
    };

    public interface Callback {
        void onSaleSurchargeBtnClicked(@NonNull PD legacyPd);

        void onPassageMarkChanged(PassageMark passageMark);

        void onPassageMarkChecked();

        void setHardwareButtonsEnabled(boolean value);

        void onPdNotValidBtnClicked(@NonNull PD legacyPd);

        void onCountTripsPdControlDataChanged(@NonNull PD legacyPd, @NonNull CountTripsPdControlData pdControlData);
    }
}
