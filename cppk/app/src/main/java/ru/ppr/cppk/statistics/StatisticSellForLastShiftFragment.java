package ru.ppr.cppk.statistics;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogButtonStyle;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.ui.helper.ShiftEventStatusStringifier;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;

/**
 * Экран статистики продаж за последнюю смену.
 */
public class StatisticSellForLastShiftFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(StatisticSellForLastShiftFragment.class);

    public static Fragment newInstance() {
        return new StatisticSellForLastShiftFragment();
    }

    private List<TicketType> ticketTypes;
    private TicketKind[] ticketKinds;

    private Map<TicketKind, PdCountAndAmount> oneOffPdInfo;
    private Map<Integer, PdCountAndAmount> baggagePdInf;

    private BigDecimal feeAmount = BigDecimal.ZERO;
    private PdCountAndAmount totalOneOffPd;
    private PdCountAndAmount totalBaggagePd;
    private PdCountAndAmount totalTransfer;

    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int versionId = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
        List<Long> ticketCategoryCodes = Collections.singletonList(TicketCategory.Code.BAGGAGE);
        ticketTypes = Dagger.appComponent().ticketTypeRepository().getTicketTypesForTicketCategories(ticketCategoryCodes, versionId);
        ticketKinds = TicketKind.getAllTicketKind();

        oneOffPdInfo = new HashMap<>();
        baggagePdInf = new HashMap<>();

        totalTransfer = new PdCountAndAmount();
        totalBaggagePd = new PdCountAndAmount();
        totalOneOffPd = new PdCountAndAmount();

        //заполним структуры
        for (TicketType ticketType : ticketTypes) {
            baggagePdInf.put(ticketType.getCode(), new PdCountAndAmount());
        }
        for (TicketKind ticketKind : ticketKinds) {
            oneOffPdInfo.put(ticketKind, new PdCountAndAmount());
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistic_sell_for_last_shift, null);

        if (view == null) return super.onCreateView(inflater, container, savedInstanceState);

        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao()
                .getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);

        //заполняем информацию о смене
        if (shiftEvent != null) {

            AQuery aQuery = new AQuery(view);
            aQuery.id(R.id.statistic_sell_shift_number).text(String.valueOf(shiftEvent.getShiftNumber()));

            ShiftEvent.Status status = shiftEvent.getStatus();

            aQuery.id(R.id.statistic_sell_status).text(new ShiftEventStatusStringifier().stringify(status));
            aQuery.id(R.id.statistick_sell_date_open_shift).text(DateFormatOperations.getDateForOut(shiftEvent.getStartTime()));
            if (status == ShiftEvent.Status.ENDED) {
                aQuery.id(R.id.statistick_sell_date_close_shift).text(DateFormatOperations.getDateForOut(shiftEvent.getCloseTime()));
            }

            //получаем список событий продажи за смену
            EnumSet<ProgressStatus> statuses = EnumSet.of(ProgressStatus.Completed, ProgressStatus.CheckPrinted);
            List<CPPKTicketSales> list = getLocalDaoSession().getCppkTicketSaleDao().getSaleEventsForShift(shiftEvent.getShiftId(), statuses, true);
            if (list.isEmpty()) {
                view.findViewById(R.id.dataContainer).setVisibility(View.GONE);
                showDialogForEmptyData();
            } else {
                //анализируем данные
                processingData(list);
                //отображаем информацию
                setStatisticForBaggage(aQuery);
                setStatisticForPd(aQuery);
                setStatisticForTransfer(view);
                //устанавливаем сбор
                aQuery.id(R.id.statistic_sell_fee_amount).text(getCoastString(feeAmount));
            }
        } else {
            Logger.info(TAG, "Could not find info about shift");
            view.findViewById(R.id.dataContainer).setVisibility(View.GONE);
            showDialogForEmptyData();
        }

        return view;
    }

    /**
     * Производит обработку данных полученных из бд
     * Анализирует тип билета, и добавляет информацию о нем в
     * соответствующие структуры
     *
     * @param list
     */
    private void processingData(List<CPPKTicketSales> list) {
        TicketCategoryChecker ticketCategoryChecker = Dagger.appComponent().ticketCategoryChecker();
        for (CPPKTicketSales event : list) {
            CPPKTicketReturn cppkTicketReturn = getLocalDaoSession().getCppkTicketReturnDao()
                    .findLastPdRepealEventForPdSaleEvent(event.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));
            boolean isReturn = cppkTicketReturn != null;
            if (isReturn) {
                continue;
            }
            PdCountAndAmount pdInfoAmount;
            TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(event.getTicketSaleReturnEventBaseId());
            TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            Price price = getLocalDaoSession().getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
            Fee fee = getLocalDaoSession().getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
            int ticketCategoryCode = ticketEventBase.getTicketCategoryCode();
            if (ticketCategoryChecker.isTrainOneOffTicket(ticketCategoryCode)) {
                pdInfoAmount = oneOffPdInfo.get(ticketSaleReturnEventBase.getKind());
                incrementvalue(price, pdInfoAmount, fee);
                totalOneOffPd.incrementCounPd();
                totalOneOffPd.addCostPd(price.getPayed());
            } else if (ticketCategoryChecker.isTrainBaggageTicket(ticketCategoryCode)) {
                pdInfoAmount = baggagePdInf.get(ticketEventBase.getTypeCode());
                incrementvalue(price, pdInfoAmount, fee);
                totalBaggagePd.incrementCounPd();
                totalBaggagePd.addCostPd(price.getPayed());
            } else if (ticketCategoryChecker.isTransferTicket(ticketCategoryCode)) {
                incrementvalue(price, totalTransfer, fee);
            }
        }
    }

    /**
     * Увеличивает значения стоимости и количества для соответствующего вида билета
     *
     * @param pdAmount
     */
    private void incrementvalue(@Nullable Price price,
                                @Nullable PdCountAndAmount pdAmount,
                                @Nullable Fee fee) {
        if (price != null && pdAmount != null) {
            pdAmount.incrementCounPd();
            pdAmount.addCostPd(price.getPayed());
            if (fee != null) {
                feeAmount = feeAmount.add(fee.getTotal());
            }
        }
    }

    /**
     * Заполняет статистику для разовых пд
     *
     * @param aQuery
     */
    @SuppressLint("InflateParams")
    private void setStatisticForPd(AQuery aQuery) {
        aQuery.id(R.id.statistic_sell_count_pd_value).text(String.valueOf(totalOneOffPd.getCountPD()));
        aQuery.id(R.id.statistic_sell_amount_pd_value).text(totalOneOffPd.getCoastString());
        LinearLayout layout = (LinearLayout) aQuery.id(R.id.statistic_amount_pd_layout).getView();
        for (TicketKind ticketKind : ticketKinds) {
            PdCountAndAmount pdCountAndAmount = oneOffPdInfo.get(ticketKind);
            View view = getActivity().getLayoutInflater().inflate(R.layout.statistick_sell_item_layout, null);
            AQuery itemView = new AQuery(view);
            itemView.id(R.id.statistic_sell_count_pd_title).text(ticketKind.getDescription());
            itemView.id(R.id.statistic_sell_count_pd_value).text(String.valueOf(pdCountAndAmount.getCountPD()));
            itemView.id(R.id.statistic_sell_amount_pd_value).text(pdCountAndAmount.getCoastString());
            layout.addView(view);
        }
    }

    /**
     * Заполняет статистику для багажа
     *
     * @param aQuery
     */
    @SuppressLint("InflateParams")
    private void setStatisticForBaggage(AQuery aQuery) {
        aQuery.id(R.id.statistic_sell_count_baggage_value).text(String.valueOf(totalBaggagePd.getCountPD()));
        aQuery.id(R.id.statistic_sell_amount_baggage_value).text(totalBaggagePd.getCoastString());
        LinearLayout layout = (LinearLayout) aQuery.id(R.id.statistic_amount_baggage_layout).getView();
        for (TicketType ticketType : ticketTypes) {
            PdCountAndAmount pdCountAndAmount = baggagePdInf.get(ticketType.getCode());
            View view = getActivity().getLayoutInflater().inflate(R.layout.statistick_sell_item_layout, null);
            AQuery itemView = new AQuery(view);
            itemView.id(R.id.statistic_sell_count_pd_title).text(ticketType.getShortName());
            itemView.id(R.id.statistic_sell_count_pd_value).text(String.valueOf(pdCountAndAmount.getCountPD()));
            itemView.id(R.id.statistic_sell_amount_pd_value).text(pdCountAndAmount.getCoastString());
            layout.addView(view);
        }
    }

    /**
     * Заполняет статистику для трансферов
     */
    private void setStatisticForTransfer(View view) {
        ((TextView) view.findViewById(R.id.statistic_sell_count_transfer_value)).setText(String.valueOf(totalTransfer.getCountPD()));
        ((TextView) view.findViewById(R.id.statistic_sell_amount_transfer_value)).setText(totalTransfer.getCoastString());
    }

    /**
     * Показывае диалог об отсутствии информации
     */
    private void showDialogForEmptyData() {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                getString(R.string.statistic_not_data),
                getString(R.string.dialog_close),
                null,
                CppkDialogButtonStyle.VERTICAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    private class PdCountAndAmount {

        private int countPd = 0;
        private BigDecimal costPds = BigDecimal.ZERO;

        public void incrementCounPd() {
            countPd++;
        }

        public void addCostPd(BigDecimal costOnePd) {
            costPds = costPds.add(costOnePd);
        }

        public int getCountPD() {
            return countPd;
        }

        public BigDecimal getCostPDs() {
            return costPds;
        }

        public int getRubles() {
            return costPds.intValue();
        }

        public int getCents() {
            return costPds.subtract(new BigDecimal(getRubles())).multiply(new BigDecimal("100")).intValue();
        }

        public String getCoastString() {
            String s;
            if (getCents() > 0) {
                s = String.format(getString(R.string.rub_cent), getRubles(), getCents());
            } else {
                s = String.format(getString(R.string.rub_only), getRubles());
            }
            return s;
        }

    }

    public String getCoastString(BigDecimal coast) {
        int rubles = coast.intValue();
        int cents = coast.subtract(new BigDecimal(rubles)).multiply(new BigDecimal("100")).intValue();

        String s;
        if (cents > 0) {
            s = String.format(getString(R.string.rub_cent), rubles, cents);
        } else {
            s = String.format(getString(R.string.rub_only), rubles);
        }
        return s;
    }

}
