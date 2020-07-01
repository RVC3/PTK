package ru.ppr.cppk.ui.fragment.pd.simple;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;

import com.google.common.base.Preconditions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.barcodereal.MobileBarcodeReader;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.TransferPdChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.TicketTypeTrainCategoryExemptionChecker;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.pd.SeasonTicketForDaysValidityDaysCalculator;
import ru.ppr.cppk.logic.pd.checker.ValidAndControlNeededChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForDaysPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SeasonForPeriodPdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SimplePdViewParams;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.SurchargeSinglePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.simple.model.TicketPdViewModel;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationTransferRouteRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Временное решение по выносу логики из {@link SimplePdFragment}.
 *
 * @author Aleksandr Brazhkin
 */
@Deprecated
public class SimplePdActivityLogic {

    private static final String TAG = Logger.makeLogTag(SimplePdActivityLogic.class);

    //Максимальная длина станции, которая влазит в одну строчку
    private static final int STATION_LENGTH_SINGLE_PAGE = 7;

    //region Di
    @Inject
    PtkModeChecker ptkModeChecker;
    @Inject
    TransferPdChecker transferPdChecker;
    @Inject
    ValidAndControlNeededChecker validAndControlNeededChecker;
    @Inject
    LocalDaoSession localDaoSession;
    @Inject
    NsiDaoSession nsiDaoSession;
    @Inject
    ExemptionRepository exemptionRepository;
    @Inject
    PrivateSettings privateSettings;
    @Inject
    CommonSettings commonSettings;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    TariffPlanRepository tariffPlanRepository;
    @Inject
    StationRepository stationRepository;
    @Inject
    StationTransferRouteRepository stationTransferRouteRepository;
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    PdValidityPeriodCalculator pdValidityPeriodCalculator;
    @Inject
    TicketCategoryChecker ticketCategoryChecker;
    //endregion

    //region Arguments
    private final PD argPD;
    /**
     * данный флаг сигнализирует что ПД только что записан, и в статусе у него
     * необходимо отобразить "Записан"
     */
    private final boolean argIsNewPd;
    private final boolean argFromControl;
    private final boolean argEnableZoom;
    private final ValidityPdVariants argVariants;
    private final boolean argTransferSaleButtonCanBeShown;
    /**
     * Флаг, указывающий, что ПД будет использован для продажи трансфера
     */
    private final boolean argTransfer;
    //endregion

    private final Activity activity;
    private final Callback callback;

    public SimplePdActivityLogic(
            Activity activity,
            SimplePdFragment simplePdFragment,
            Callback callback,
            PD pd,
            ValidityPdVariants variants,
            boolean isNewPd,
            boolean fromControl,
            boolean enableZoom,
            boolean transferSaleButtonCanBeShown,
            boolean transfer) {
        this.activity = activity;
        this.callback = callback;
        argPD = pd;
        argIsNewPd = isNewPd;
        argFromControl = fromControl;
        argEnableZoom = enableZoom;
        argVariants = variants;
        argTransferSaleButtonCanBeShown = transferSaleButtonCanBeShown;
        argTransfer = transfer;

        Dagger.appComponent().inject(this);
        simplePdFragment.setCallback(fragmentCallback);
        simplePdFragment.setSimplePdViewParams(fillScreenParams());
    }

    private SimplePdViewParams fillScreenParams() {
        SimplePdViewParams screenParams = new SimplePdViewParams();
        TicketPdViewModel ticketPdViewModel;

        Tariff tariff = argPD.getTariff();
        Preconditions.checkNotNull(tariff);

        Station stationDeparture = tariff.getStationDeparture(nsiDaoSession);
        Station stationDestination = tariff.getStationDestination(nsiDaoSession);

        //Коротки ли названия станций для отображения после продажи http://agile.srvdev.ru/browse/CPPKPP-30273
        boolean isStationNameShortForSale = stationDeparture == null || stationDestination == null;
        if (!isStationNameShortForSale) {
            isStationNameShortForSale = stationDeparture.getShortName().length() <= STATION_LENGTH_SINGLE_PAGE && stationDestination.getShortName().length() <= STATION_LENGTH_SINGLE_PAGE;
        }

        screenParams.setZoomEnabled(argEnableZoom);

        MobileBarcodeReader mobileBarcodeReader = MobileBarcodeReader.getInstance();
        final boolean mobileTicket = mobileBarcodeReader.getIfLastCodeMobileWithoutStatusClean();

        //если продажа и длинное название, тогда используем режим 2х ПД
        if (!(!isStationNameShortForSale && !argFromControl) && (argVariants == ValidityPdVariants.ONE_PD_INVALID || argVariants == ValidityPdVariants.ONE_PD_VALID)
                && !mobileTicket) {
            //все крупное
            screenParams.setSmallSize(false);
        } else { //все мелкое
            screenParams.setSmallSize(true);
        }

        ////////////////////////////////

        Logger.info(TAG, "Номер билета: " + argPD.numberPD);

        ////////////////////////////////

        TicketType ticketType = argPD.getTariff().getTicketType(nsiDaoSession);
        TicketCategory ticketCategory = ticketType.getTicketCategory(nsiDaoSession);

        SinglePdViewModel singlePdViewModel = null;
        SurchargeSinglePdViewModel surchargeSinglePdViewModel = null;
        SeasonForPeriodPdViewModel seasonForPeriodPdViewModel = null;
        SeasonForDaysPdViewModel seasonForDaysPdViewModel = null;
        if (ticketCategoryChecker.isSingleTicket(ticketCategory.getCode())) {
            if (argPD.versionPD == PdVersion.V2.getCode()) {
                surchargeSinglePdViewModel = new SurchargeSinglePdViewModel();
                singlePdViewModel = surchargeSinglePdViewModel;
                ticketPdViewModel = surchargeSinglePdViewModel;
            } else {
                singlePdViewModel = new SinglePdViewModel();
                ticketPdViewModel = singlePdViewModel;
            }
        } else if (ticketCategoryChecker.isSeasonTicket(ticketCategory.getCode())) {
            if (ticketCategoryChecker.isSeasonForDaysTicket(ticketCategory.getCode())) {
                seasonForDaysPdViewModel = new SeasonForDaysPdViewModel();
                ticketPdViewModel = seasonForDaysPdViewModel;
            } else {
                seasonForPeriodPdViewModel = new SeasonForPeriodPdViewModel();
                ticketPdViewModel = seasonForPeriodPdViewModel;
            }
        } else {
            throw new IllegalArgumentException("Unsupported ticket category");
        }
        ////////////////////////////////

        // Устанавливаем номер билета
        ticketPdViewModel.setNumber(argPD.numberPD);

        // Устанавливаем станцию отправления
        if (stationDeparture == null) {
            ticketPdViewModel.setDepStationName(null);
        } else {
            ticketPdViewModel.setDepStationName(stationDeparture.getShortName());
        }

        // Устанавливаем станцию назначения
        if (stationDestination == null) {
            ticketPdViewModel.setDestStationName(null);
        } else {
            ticketPdViewModel.setDestStationName(stationDestination.getShortName());
        }

        // Устанавливаем тип ПД
        ticketPdViewModel.setTitle(ticketType.toString());

        if (singlePdViewModel != null) {
            singlePdViewModel.setSoldNow(argIsNewPd);
            // Устанавливаем направление
            if (argPD.wayType == TicketWayType.OneWay || (argPD.parentTicketInfo != null && TicketWayType.TwoWay.equals(argPD.wayType))) {
                // Если у билета направление туда, или это доплата в направлении обратно
                singlePdViewModel.setTwoWay(false);
            } else {
                singlePdViewModel.setTwoWay(true);
            }
            // Устанавливаем значение времени продажи\проезда
            Date dateValue = (argFromControl) ? argPD.getStartPdDate() : argPD.getSaleDate();
            singlePdViewModel.setControlMode(argFromControl);
            singlePdViewModel.setValidityDate(dateValue);
        } else if (seasonForPeriodPdViewModel != null) {
            // Рассчитываем период действия ПД
            Date startPdDate = argPD.getStartPdDate();
            // Вычитаем 1 день, т.к. 1-ый день действия уже учтен во времени начала действия ПД
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(startPdDate);
            endTime.add(Calendar.DAY_OF_MONTH, pdValidityPeriodCalculator.calcValidityPeriod(startPdDate, argPD.wayType, ticketType, tariff.getVersionId()) - 1);

            seasonForPeriodPdViewModel.setValidityFromDate(startPdDate);
            seasonForPeriodPdViewModel.setValidityToDate(endTime.getTime());
        } else if (seasonForDaysPdViewModel != null) {
            // Рассчитываем даты действия ПД
            Logger.info(TAG, "дата продажи абонемента: " + DateFormatOperations.getDateddMMyyyyHHmm(argPD.saleDatetimePD));
            SeasonTicketForDaysValidityDaysCalculator seasonTicketForDaysValidityDaysCalculator = new SeasonTicketForDaysValidityDaysCalculator();
            List<Date> days = seasonTicketForDaysValidityDaysCalculator.getValidityDays(argPD.getStartPdDate(), (int) argPD.actionDays);
            seasonForDaysPdViewModel.setValidityDates(days);
        }

        // Определеям, трансфер это или нет
        Pd pd = new PdFromLegacyMapper().fromLegacyPd(argPD);
        boolean transfer = transferPdChecker.check(pd);
        ticketPdViewModel.setTransfer(transfer);

        // Валидность
        List<PassageResult> errorType = argPD.errors;

        setErrors(ticketPdViewModel, errorType);

        if (!transfer) {
            // Рассчитываем льготу
            int exemptionExpressCode = argPD.exemptionCode == null ? 0 : argPD.exemptionCode;
            ticketPdViewModel.setExemptionExpressCode(exemptionExpressCode);

            // Определяем категорию поезда
            String trainCategory = tariff.getTariffPlan(tariffPlanRepository).getTrainCategory(nsiDaoSession).name;
            ticketPdViewModel.setTrainCategoryName(trainCategory);

            // Определяем номер родительского ПД
            if (surchargeSinglePdViewModel != null) {
                surchargeSinglePdViewModel.setParentPdNumber(argPD.parentTicketInfo.getTicketNumber());
            }

            showSalePdExtraChargeBtnIfNeed(errorType, screenParams);

            // Настраиваем видимость кнопки "Оформить трансфер"
            screenParams.setSellTransferBtnVisible(argTransferSaleButtonCanBeShown);
        }
        // настроим отображение кнопок
        setupButton(argVariants, screenParams);

        screenParams.setPdViewModel(ticketPdViewModel);
        return screenParams;
    }

    /**
     * Настраивает видимость кнопок в заисимоссти от валидности ПД
     *
     * @param variants Вариант валидности ПД
     */
    private void setupButton(ValidityPdVariants variants, SimplePdViewParams screenParams) {
        // проверим флаг показа кнопок
        if (argFromControl) {

            if (variants == null)
                throw new IllegalStateException("Ticket validity variants is null");

            switch (variants) {
                case TWO_PD_IS_VALID:
                    if (!argTransfer) {
                        screenParams.setTicketValidBtnVisible(true);
                    }
                    break;

                case ONE_PD_VALID:
                case ONE_OF_TWO_PD_IS_VALID:
                    if (!ptkModeChecker.isTransferControlMode() && !argTransfer && validAndControlNeededChecker.isValidAndControlNeeded(argPD)) {
                        // Если мы не в автобусе и ПД валиден и требуется создание события контроля
                        // Покажем кнопку "Билет НЕдействителен"
                        screenParams.setTicketNotValidBtnVisible(true);
                    } else {
                        // Если кнопка не видна оставим пустое место, иначе пустое место не нужно,
                        // аналогичная логика case TWO_PD_IS_INVALID только для кнопки трансфера
                    }
                    break;
                case TWO_PD_IS_INVALID:
                    // нужно отобразить пустое место в данном случае
                    // будем использовать для этого кнопку продажи ПД по доплате
                    // но сначала необходимо првоерить ее состояние
                    // т.к. при проезде на поезде более высокой категории эта кнопка
                    // находится в видимом состояние, поэтому дополнительное пространство не нужно

                    break;

                case ONE_PD_INVALID:
                    break;
                default:
                    /* NOP */
                    break;
            }
        }
    }

    /**
     * Настраивает отображение кнопки "Оформить ПД по доплате"
     *
     * @param errors Результат проверки ПД
     */
    private void showSalePdExtraChargeBtnIfNeed(@NonNull List<PassageResult> errors, SimplePdViewParams screenParams) {
        boolean shouldShowBtn = false;

        if (!ptkModeChecker.isTransferControlMode() && !argTransfer) {
            if (errors.size() == 1 && errors.contains(PassageResult.BannedTrainType)) {
                shouldShowBtn = true;
            } else if (errors.isEmpty()) {
                // ПД валиден
                // ПТК: всегда выводить кнопку "доплатить ПД" в режиме мобильной кассы
                // https://aj.srvdev.ru/browse/CPPKPP-3169931699
                // Скрываем кнопку в режиме трансфера
                boolean isMobileCashRegister = privateSettings.isMobileCashRegister();
                if (isMobileCashRegister) {
                    if (privateSettings.getTrainCategoryPrefix() == TrainCategoryPrefix.PASSENGER) {
                        shouldShowBtn = true;
                    }
                }
            }
        }

        //покажем кнопку оформить по доплате
        screenParams.setSellSurchargeBtnVisible(argFromControl
                && shouldShowBtn
                && argPD.hasFareTariff
                && permissionChecker.checkPermission(PermissionDvc.SalePdSurchange));
    }

    /**
     * Определяет вьюхи с некореекными данными в зависимости от причин невалидности ПД
     *
     * @param errors Список ошибок при проверке ПД
     */
    private void setErrors(TicketPdViewModel pdViewModel, List<PassageResult> errors) {

        pdViewModel.setValid(errors.isEmpty());

        boolean routeError = errors.contains(PassageResult.InvalidStation);
        pdViewModel.setRouteError(routeError);

        if (pdViewModel instanceof SinglePdViewModel) {
            SinglePdViewModel singlePdViewModel = (SinglePdViewModel) pdViewModel;
            boolean validityDateError = errors.contains(PassageResult.TooEarly) || errors.contains(PassageResult.TooLate);
            singlePdViewModel.setValidityDateError(validityDateError);
        } else if (pdViewModel instanceof SeasonForPeriodPdViewModel) {
            boolean weekendOnlyError = errors.contains(PassageResult.WeekendOnly);
            SeasonForPeriodPdViewModel seasonForPeriodPdViewModel = (SeasonForPeriodPdViewModel) pdViewModel;
            seasonForPeriodPdViewModel.setWeekendOnlyError(weekendOnlyError);

            boolean workingDayOnlyError = errors.contains(PassageResult.WorkingDayOnly);
            seasonForPeriodPdViewModel.setWorkingDayOnlyError(workingDayOnlyError);

            boolean validityFromError = errors.contains(PassageResult.TooEarly);
            seasonForPeriodPdViewModel.setValidityFromDateError(validityFromError);

            boolean validityToError = errors.contains(PassageResult.TooLate);
            seasonForPeriodPdViewModel.setValidityToDateError(validityToError);
        } else if (pdViewModel instanceof SeasonForDaysPdViewModel) {
            SeasonForDaysPdViewModel seasonForDaysPdViewModel = (SeasonForDaysPdViewModel) pdViewModel;
            boolean validityDatesError = errors.contains(PassageResult.TooEarly) || errors.contains(PassageResult.TooLate);
            seasonForDaysPdViewModel.setValidityDatesError(validityDatesError);
        }

        pdViewModel.setInvalidEdsKeyError(errors.contains(PassageResult.InvalidSign));
        pdViewModel.setRevokedEdsKeyError(errors.contains(PassageResult.SignKeyRevoked));
        pdViewModel.setTicketInStopListError(errors.contains(PassageResult.BannedByStopListTickets));
        pdViewModel.setTrainCategoryError(errors.contains(PassageResult.BannedTrainType));
        pdViewModel.setTicketAnnulledError(errors.contains(PassageResult.BannedByCanceled));
    }

    private final SimplePdFragment.Callback fragmentCallback = new SimplePdFragment.Callback() {
        @Override
        public void onPdValidBtnClicked() {
            callback.onPdValidBtnClicked(argPD);
            activity.finish();
        }

        @Override
        public void onSaleTransferBtnClicked() {
            callback.onSaleTransferBtnClicked(argPD);
        }

        @Override
        public void onPdNotValidBtnClicked() {
            callback.onPdNotValidBtnClicked(argPD);


            if (!privateSettings.isSaleEnabled() || !permissionChecker.checkPermission(PermissionDvc.SalePd)) {
                activity.finish();
                return;
            }

            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    activity.getString(R.string.question_sell_new_pd),
                    activity.getString(R.string.Yes),
                    activity.getString(R.string.No),
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

        @Override
        public void onSellSurchargeBtnClicked() {
            Tariff tariff = argPD.getTariff();
            TicketType ticketType = tariff.getTicketType(nsiDaoSession);
            int ticketCategoryCode = ticketType.getTicketCategoryCode();
            TicketCategoryChecker ticketCategoryChecker = new TicketCategoryChecker();

            if (ticketCategoryChecker.isTrainSingleTicket(ticketCategoryCode)) {

                // http://agile.srvdev.ru/browse/CPPKPP-34256
                // Выполняем проверку на возможность оформления доплаты на поезде более высокой категории
                int exemptionExpressCode = argPD.exemptionCode;
                if (exemptionExpressCode > 0) {
                    boolean saleAllowed = false;
                    String message = "";
                    if (commonSettings.isExtraSaleForPdWithExemptionAllowed()) {
                        int ticketTypeCode = ticketType.getCode();
                        int trainCategoryCode = TrainCategory.CATEGORY_CODE_7;
                        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();
                        Station departureStation = tariff.getStationDeparture(nsiDaoSession);
                        Exemption exemption = exemptionRepository.getExemptionForRegion(exemptionExpressCode, departureStation.getRegionCode(), new Date(), nsiVersion);
                        if (exemption == null) {
                            message = activity.getString(R.string.simple_pd_msg_cant_find_exemption_info);
                        } else if (!new TicketTypeTrainCategoryExemptionChecker(nsiDaoSession).check(exemption.getCode(), ticketTypeCode, trainCategoryCode, nsiVersion)) {
                            message = String.format(activity.getString(R.string.simple_pd_msg_denied_for_ticket_type), exemptionExpressCode);
                        } else {
                            saleAllowed = true;
                        }
                    } else {
                        message = activity.getString(R.string.simple_pd_msg_not_allowed_for_train_category);
                    }

                    if (!saleAllowed) {
                        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                                null,
                                message,
                                activity.getString(R.string.simple_pd_not_allowed_for_train_category_ok),
                                null,
                                LinearLayout.VERTICAL,
                                -1
                        );
                        simpleDialog.show(activity.getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
                        return;
                    }
                }
            }

            // Переходим на экран оформления доплаты
            callback.onSaleSurchargeBtnClicked(argPD);
        }

        @Override
        public void onZoomDialogShown() {
            callback.setHardwareButtonsEnabled(false);
        }

        @Override
        public void onZoomDialogHidden() {
            callback.setHardwareButtonsEnabled(true);
        }
    };

    public interface Callback {
        void onSaleSurchargeBtnClicked(@NonNull PD legacyPd);

        void setHardwareButtonsEnabled(boolean value);

        void onSaleTransferBtnClicked(@NonNull PD legacyPd);

        void onPdValidBtnClicked(@NonNull PD legacyPd);

        void onPdNotValidBtnClicked(@NonNull PD legacyPd);
    }
}
