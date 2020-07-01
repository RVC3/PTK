package ru.ppr.cppk.repeal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Locale;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;

public class RepealPdFragment extends FragmentParent {

    // ARSG
    private static final String ARG_SALE_EVENT_ID = "ARG_SALE_EVENT_ID";
    private static final String ARG_SKIP_ERRORS = "ARG_SKIP_ERRORS";

    private TextView pdTitle;
    private TextView depStationName;
    private TextView destStationName;
    private ImageView directionImage;
    private TextView pdNumber;
    private TextView pdDateTimeValue;
    private TextView trainCategoryName;
    private TextView exemptionValue;
    private TextView repealFeeValue;
    private TextView repealAmountValue;
    private TextView paymentType;
    private Button repealButton;

    //флаг, ставится в true если на карте было 2 ПД, тогда не нужно сразу отображать всплывашки с предупреждениями и ошибками аннулирования
    private boolean skipErrors = false;

    /**
     * Создает новый экземпляр фрагмента.
     *
     * @param saleEventId Id события продажи
     * @param skipErrors  {@code true} если не нужно выводить ошибки и предупреждения при создании (например когда на карте 2 ПД), {@code false}
     */
    public static RepealPdFragment newInstance(long saleEventId, boolean skipErrors) {
        RepealPdFragment fragment = new RepealPdFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SALE_EVENT_ID, saleEventId);
        args.putBoolean(ARG_SKIP_ERRORS, skipErrors);
        fragment.setArguments(args);
        return fragment;
    }

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private enum PdRepealResult {
        /**
         * Аннулирование возможно
         */
        CAN_REPEAL,
        /**
         * Время аннулирования истекло
         */
        ANNULATE_TIME_IS_END,
        /**
         * Время аннулирования истекло, но аннулирование возможно
         */
        ANNULATE_TIME_IS_END_BUT_CAN_REPAIL,
        /**
         * Данный пд уже был аннулирован
         */
        DOUBLE_REPEAL,
        /**
         * Продано на другом устройстве
         */
        SELL_ON_OTHER_PTK,
        /**
         * Продано в другую смену
         */
        SELL_ON_OTHER_SHIFT,
        /**
         * Аннулируется не последний ПД
         */
        REPEAL_NOT_LAST_PD;

        public static String getErrorMessage(PdRepealResult result, Context context) {

            String errorMessage = null;

            switch (result) {
                case CAN_REPEAL:
                    errorMessage = context.getString(R.string.repeal_is_possibly);
                    break;

                case ANNULATE_TIME_IS_END:
                    errorMessage = context.getString(R.string.repeal_time_end);
                    break;

                case ANNULATE_TIME_IS_END_BUT_CAN_REPAIL:
                    errorMessage = context.getString(R.string.repeal_time_end);
                    break;

                case DOUBLE_REPEAL:
                    errorMessage = context.getString(R.string.double_repeal_message);
                    break;

                case SELL_ON_OTHER_PTK:
                    errorMessage = context.getString(R.string.pd_sell_on_other_ptk);
                    break;

                case SELL_ON_OTHER_SHIFT:
                    errorMessage = context.getString(R.string.pd_sell_on_other_shift);
                    break;

                case REPEAL_NOT_LAST_PD:
                    errorMessage = context.getString(R.string.repeal_not_last_pd);
                    break;

                default:
                    break;
            }

            return errorMessage;
        }
    }

    private CPPKTicketSales salesReturnsEvent = null;

    private Holder<PrivateSettings> privateSettingsHolder;

    public interface OnFragmentInteractionListener {

        /**
         * Выполняет аннулирование билета
         *
         * @param salesReturnsEvent события продажи билета, для которого выполняем аннулирование
         */
        void performRepeal(CPPKTicketSales salesReturnsEvent);

        /**
         * Устанавливает инфо карты, в случае если ПД был считан с карты
         *
         * @param smartCard инфо карты с которой был считан ПД
         */
        void setSmartCardInfo(SmartCard smartCard);

        /**
         * Показывает диалог о том, что аннулируем не последний ПД
         *
         * @param event событие, которое собираемся аннулировать
         */
        void showNotLastRepealPd(CPPKTicketSales event);

        /**
         * Показывает диалог с возникшей ошибкой при аннулировании
         *
         * @param errorMessage сообщение об ошибке
         */
        void showErrorDialog(String errorMessage);

        /**
         * Показывает диалог предупреждающий о истечении времени аннулирования
         */
        void showWarningTimeDialog(CPPKTicketSales event);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activity " + activity.getClass().getName() + " should implement " +
                    "RepealPdFragment.OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        onFragmentInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Dagger.appComponent().privateSettingsHolder();

        long saleEventId = getArguments().getLong(ARG_SALE_EVENT_ID, -1);
        if (saleEventId > -1) {
            this.salesReturnsEvent = getLocalDaoSession().getCppkTicketSaleDao()
                    .getCPPKTicketSalesEventById(saleEventId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.repeal_pd_fragment, container, false);

        if (view == null) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        skipErrors = getArguments().getBoolean(ARG_SKIP_ERRORS, false);

        pdTitle = (TextView) view.findViewById(R.id.pdTitle);
        depStationName = (TextView) view.findViewById(R.id.depStationName);
        destStationName = (TextView) view.findViewById(R.id.destStationName);
        directionImage = (ImageView) view.findViewById(R.id.directionImage);
        pdNumber = (TextView) view.findViewById(R.id.pdNumber);
        pdDateTimeValue = (TextView) view.findViewById(R.id.pdDateTimeValue);
        trainCategoryName = (TextView) view.findViewById(R.id.trainCategoryName);
        exemptionValue = (TextView) view.findViewById(R.id.exemptionValue);
        repealFeeValue = (TextView) view.findViewById(R.id.repail_fragment_fee_value);
        repealAmountValue = (TextView) view.findViewById(R.id.repail_fragment_amount_value);
        paymentType = (TextView) view.findViewById(R.id.paymentType);
        repealButton = (Button) view.findViewById(R.id.repail_fragment_repail_button);

        if (salesReturnsEvent != null) {

            TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(salesReturnsEvent.getTicketSaleReturnEventBaseId());
            TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

            // настраиваем view

            String rub = getString(R.string.rub_cent_as_single);
            Fee fee = getLocalDaoSession().getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
            BigDecimal feeTotal = fee == null ? BigDecimal.ZERO : fee.getTotal();
            Price fullPrice = getLocalDaoSession().getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
            BigDecimal pdAmount = fullPrice.getPayed();
            // устанавливаем сбор
            repealFeeValue.setText(String.format(rub, feeTotal));
            // устанавливаем стоимость пд
            repealAmountValue.setText(String.format(rub, pdAmount));

            // устанавливаем название пд
            String pdTitleValue;

            ParentTicketInfo parentTicketInfo = getLocalDaoSession().getParentTicketInfoDao().load(ticketSaleReturnEventBase.getParentTicketInfoId());
            if (parentTicketInfo != null) {
                if (salesReturnsEvent.getConnectionType() == ConnectionType.TRANSFER) {
                    pdTitleValue = getString(R.string.repeal_pd_fragment_transfer_type);
                } else {
                    pdTitleValue = getString(R.string.fare);
                }
            } else {
                pdTitleValue = ticketEventBase.getTicketTypeShortName();
            }
            pdTitle.setText(pdTitleValue);
            pdTitle.setWidth(150);
            pdTitle.setTypeface(Typeface.DEFAULT_BOLD);

            Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

            // устанавливаем станцию отправления
            Station departureStation = getStationToCode(ticketEventBase.getDepartureStationCode());
            depStationName.setText(departureStation.getShortName());
            depStationName.setTypeface(typeface);

            // устанавливаем станцию назначения
            Station destinationStation = getStationToCode(ticketEventBase.getDestinationStationCode());
            destStationName.setText(destinationStation.getShortName());
            destStationName.setTypeface(typeface);

            // устанавливаем стрелку направления
            TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
            TariffPlanRepository tariffPlanRepository = Dagger.appComponent().tariffPlanRepository();

            TicketWayType wayType = ticketEventBase.getWayType();
            Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                    ticketEventBase.getTariffCode(),
                    Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
            );
            // isSurcharge Проверяет является ли текущий тариф доплатой
            boolean isSurcharge = tariff.getTariffPlan(tariffPlanRepository).isSurcharge();
            int idImage = (wayType == TicketWayType.OneWay || isSurcharge) ? R.drawable.there_direction : R.drawable.there_back_direction;
            directionImage.setImageResource(idImage);

            Check check = getLocalDaoSession().getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
            // устанавливаем номер билета
            pdNumber.setText(String.format(Locale.getDefault(),
                    "№ %06d", check.getOrderNumber()));
            // устанавливаем дату продажи
            pdDateTimeValue.setText(DateFormatOperations.getDateForOut(check.getPrintDatetime()));
            // устанавливаем категорию поезда
            TrainInfo trainInfo = getLocalDaoSession().trainInfoDao().load(ticketSaleReturnEventBase.getTrainInfoId());
            trainCategoryName.setText(trainInfo.getTrainCategoryCategory().getDescription());

            ExemptionForEvent exemptionForEvent = getLocalDaoSession().exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
            if (exemptionForEvent != null) {
                Exemption exemption = Dagger.appComponent().exemptionRepository().getExemption(exemptionForEvent.getCode(), exemptionForEvent.getActiveFromDate(), exemptionForEvent.getVersionId());
                if (exemption != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(exemption.getExemptionExpressCode()).append(" / ").append(exemption.getPercentage()).append("%");
                    exemptionValue.setText(builder.toString());
                } else {
                    Logger.info("RepealActivity", "Can't found exemption for code - " + exemptionForEvent.getExpressCode());
                }
            }

            SmartCard smartCard = getLocalDaoSession().getSmartCardDao().load(ticketEventBase.getSmartCardId());
            // если ПД был считан с карты, то устанавливаем номер этой карты
            if (smartCard != null && onFragmentInteractionListener != null) {
                onFragmentInteractionListener.setSmartCardInfo(smartCard);
            }

            BankTransactionEvent bankTransactionEvent = getLocalDaoSession().getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
            if (bankTransactionEvent != null) {
                // Если ПД был продан по безналу
                paymentType.setText(R.string.repeal_pd_fragment_payment_type_card);
            } else {
                // Если ПД был продан за наличные
                paymentType.setText(R.string.repeal_pd_fragment_payment_type_cash);
            }

            PdRepealResult result = checkRepeal(salesReturnsEvent);
            switch (result) {
                case CAN_REPEAL:
                    showRepealButton();
                    break;

                case ANNULATE_TIME_IS_END_BUT_CAN_REPAIL:
                    showRepealButton();
                    if (!skipErrors)
                        showWarningDialog(salesReturnsEvent);
                    break;

                case SELL_ON_OTHER_SHIFT:
                case SELL_ON_OTHER_PTK:
                case DOUBLE_REPEAL:
                case ANNULATE_TIME_IS_END:
                    if (!skipErrors)
                        showErrorDialog(PdRepealResult.getErrorMessage(result, getActivity()));
                    break;

                default:
                    break;
            }
        }

        return view;
    }

    /**
     * Производит проверку пд на возможность аннулирования
     * см. пункт 4.2.6 ТСОППД ЧТЗ ПТК от 21.08.2014
     *
     * @param pdSaleEvent Событие продажи ПД
     * @return Результат проверки возможности аннулирования ПД
     */
    private PdRepealResult checkRepeal(CPPKTicketSales pdSaleEvent) {

        //проверяем был ли пд аннулирован ранее
        if (checkDoubleRepeal(pdSaleEvent)) {
            return PdRepealResult.DOUBLE_REPEAL;
        }

        //проверяем смену, в которую продан ПД
        TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao().load(ticketEventBase.getShiftEventId());
        if (!checkShift(shiftEvent.getShiftNumber())) {
            return PdRepealResult.SELL_ON_OTHER_SHIFT;
        }

        int timeForAnnulate = privateSettingsHolder.get().getTimeForAnnulate();
        long saleTime = ticketEventBase.getSaledateTime().getTime() / 1000;
        //проверяем возможность аннулирования пд по времени
        if (saleTime + timeForAnnulate * 60 < System.currentTimeMillis() / 1000) {
            if (Dagger.appComponent().commonSettingsStorage().get().isEnableAnnulateAfterTimeOver())
                return PdRepealResult.ANNULATE_TIME_IS_END_BUT_CAN_REPAIL;
            else
                return PdRepealResult.ANNULATE_TIME_IS_END;
        }

        return PdRepealResult.CAN_REPEAL;
    }

    /**
     * Проверяет, был ли ПД аннулирован ранее.
     *
     * @param pdSaleEvent Событие продажи ПД
     * @return {@code true} если ПД уже аннулирован и аннулирование НЕвозможно, {@code false} - иначе.
     */
    private boolean checkDoubleRepeal(@NonNull CPPKTicketSales pdSaleEvent) {
        CPPKTicketReturn ticketReturn = getLocalDaoSession().getCppkTicketReturnDao()
                .findLastPdRepealEventForPdSaleEvent(pdSaleEvent.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));
        return ticketReturn != null;
    }

    /**
     * Проверяет смену в которой продан ПД и в зависимости от этого
     * возвращает результат возможности аннулирования
     *
     * @param saleShiftNumber - номер сммены, в которую продан ПД
     * @return {@code true} если ПД продан в текущей смене и аннулирование возможно, {@code false} - иначе.
     */
    private boolean checkShift(int saleShiftNumber) {

        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao()
                .getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        return shiftEvent != null
                && shiftEvent.getShiftNumber() == saleShiftNumber;
    }

    /**
     * Выполняет проверки текущего билета для аннулирования и последнего
     * проданного
     *
     * @param currentPdNumber номер билета, для которого хотим выполнить аннулирвоание
     * @return true если текущий билет = последнему проданному билета, иначе
     * false
     */
    private boolean checkLastPd(int currentPdNumber) {

        boolean isLastPd = false;

        CppkTicketSaleDao saleDao = getLocalDaoSession().getCppkTicketSaleDao();
        CPPKTicketSales lastSaleEvent = saleDao.getLastSaleEvent();

        if (lastSaleEvent != null) {

            TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(lastSaleEvent.getTicketSaleReturnEventBaseId());
            Check check = getLocalDaoSession().getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
            Preconditions.checkNotNull(check, "Check is null");

            if (check.getOrderNumber() == currentPdNumber)
                isLastPd = true;
        }

        return isLastPd;
    }

    /**
     * Делает видимой кнопку "Аннулировать".
     */
    private void showRepealButton() {
        repealButton.setVisibility(View.VISIBLE);
        repealButton.setOnClickListener(repealListener);
    }

    /**
     * Показывает предупреждающий диалог с предупреждением о истечении времени аннулирования
     */
    private void showWarningDialog(CPPKTicketSales event) {
        if (onFragmentInteractionListener != null)
            onFragmentInteractionListener.showWarningTimeDialog(event);
    }

    /**
     * Показывает диалог с ошибкой аннулирования и причиной
     */
    private void showErrorDialog(String errorMessage) {
        if (onFragmentInteractionListener != null)
            onFragmentInteractionListener.showErrorDialog(errorMessage);
    }

    /**
     * Показывает диалог с предупрежденем что аннулирем не последний ПД
     */
    private void showNotLastPdDialog() {
        if (onFragmentInteractionListener != null)
            onFragmentInteractionListener.showNotLastRepealPd(salesReturnsEvent);
    }

    /**
     * Выполняет аннулирование ПД
     */
    private void performRepeal() {
        if (onFragmentInteractionListener != null)
            onFragmentInteractionListener.performRepeal(salesReturnsEvent);
    }

    /**
     * Обработчик нажатия на кнопку "Аннулировать".
     */
    private OnClickListener repealListener = v -> {

        TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(salesReturnsEvent.getTicketSaleReturnEventBaseId());
        Check check = getLocalDaoSession().getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        if (!checkLastPd(check.getOrderNumber())) {
            showNotLastPdDialog();
        } else {
            PdRepealResult result = checkRepeal(salesReturnsEvent);
            // если было 2 ПД и аннулируется старый - то покажем предупреждение, иначе сразу аннулируем
            if (skipErrors && result == PdRepealResult.ANNULATE_TIME_IS_END_BUT_CAN_REPAIL)
                showWarningDialog(salesReturnsEvent);
            else
                performRepeal();
        }
    };

    private Station getStationToCode(long code) {
        return Dagger.appComponent().stationRepository().load(code, Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
    }

}
