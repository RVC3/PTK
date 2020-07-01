package ru.ppr.cppk.statistics;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import java.util.List;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogButtonStyle;
import ru.ppr.cppk.entity.event.base34.CPPKTicketControl;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.ui.helper.ShiftEventStatusStringifier;

public class StatisticsCheckTicketFragment extends FragmentParent {

    public static Fragment newInstance() {
        return new StatisticsCheckTicketFragment();
    }

    private Globals globals;

    //счетчики для провереных билетов
    private int pdWithPlace = 0;
    private int pdWithOutPlace = 0;
    private int pdWithExemption = 0;
    private int pdFromBsc = 0;
    private int pdFromBarcode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globals = (Globals) getActivity().getApplication();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistics_check, null);

        AQuery aQuery = new AQuery(view);

        CppkTicketControlsDao controlsDao = getLocalDaoSession().getCppkTicketControlsDao();

        ShiftEventDao shiftDao = getLocalDaoSession().getShiftEventDao();
        ShiftEvent shiftEvent = shiftDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);

        if (shiftEvent == null) {
            //открытых смен еще не было
            //показываем диалог
            showDialogForEmptyData();
            return view;
        }

        ShiftEvent.Status status = shiftEvent.getStatus();
        aQuery.id(R.id.statistics_controls_shift_number).text(String.valueOf(shiftEvent.getShiftNumber()));
        aQuery.id(R.id.statistics_controls_shiftState).text(new ShiftEventStatusStringifier().stringify(status));
        aQuery.id(R.id.statistics_controls_shiftOpeningTime).text(DateFormatOperations.getDateForOut(shiftEvent.getStartTime()));
        if (status == ShiftEvent.Status.ENDED)
            aQuery.id(R.id.statistics_controls_shiftClosingTime).text(DateFormatOperations.getDateForOut(shiftEvent.getCloseTime()));

        List<CPPKTicketControl> contolsList = controlsDao.loadEventByTime(shiftEvent.getStartTime());
        if (contolsList.isEmpty()) {
            //событий проверок еще не было
            //показываем диалог
            view.findViewById(R.id.dataContainer).setVisibility(View.GONE);
            showDialogForEmptyData();
            return view;
        }

        processData(contolsList);

        aQuery.id(R.id.statistics_check_with_place_count).text(String.valueOf(pdWithPlace));
        aQuery.id(R.id.statistics_check_without_place_count).text(String.valueOf(pdWithOutPlace));
        aQuery.id(R.id.statistics_check_from_barcode_count).text(String.valueOf(pdFromBarcode));
        aQuery.id(R.id.statistics_check_from_bsc_count).text(String.valueOf(pdFromBsc));
        aQuery.id(R.id.statistics_check_with_exemption_count).text(String.valueOf(pdWithExemption));

        aQuery.id(R.id.statistics_controls_checkedTotalCount).text(String.valueOf(contolsList.size()));
        int uniquePdCheck = controlsDao.getUniqueCheckPd(shiftEvent);
        aQuery.id(R.id.statistics_controls_checkedTotalUniqueCount).text(String.valueOf(uniquePdCheck));
        return view;
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

    private void processData(@NonNull List<CPPKTicketControl> list) {
        for (CPPKTicketControl controls : list) {

            TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(controls.getTicketEventBaseId());
            //считали с карты или со штрихкода
            SmartCard smartCard = getLocalDaoSession().getSmartCardDao().load(ticketEventBase.getSmartCardId());
            if (smartCard == null)
                pdFromBarcode++;
            else
                pdFromBsc++;

            //пока все билеты являются билетами без места
            pdWithOutPlace++;

            //со льготой или без
            if (controls.getExemptionCode() > 0)
                pdWithExemption++;

        }
    }
}
