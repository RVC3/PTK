package ru.ppr.cppk.statistics;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.SmartCardDao;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogButtonStyle;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.ui.helper.ShiftEventStatusStringifier;
import ru.ppr.cppk.utils.adapters.QuickAdapter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

public class StatisticsCheckETTFragment extends FragmentParent {

    public static Fragment newInstance() {
        return new StatisticsCheckETTFragment();
    }

    Globals globals;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globals = (Globals) getActivity().getApplication();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.statistics_check_ett, null);

        AQuery aQuery = new AQuery(view);

        ShiftEvent shiftEvent = getLocalDaoSession()
                .getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);


        if (shiftEvent != null) {

            ShiftEvent.Status status = shiftEvent.getStatus();

            aQuery.id(R.id.statistic_check_ett_shift_number).text(String.valueOf(shiftEvent.getShiftNumber()));
            aQuery.id(R.id.statistic_check_ett_shiftOpeningTime).text(DateFormatOperations.getDateForOut(shiftEvent.getStartTime()));
            aQuery.id(R.id.statistic_check_ett_shiftState).text(new ShiftEventStatusStringifier().stringify(status));
            if (status == ShiftEvent.Status.ENDED) {
                aQuery
                        .id(R.id.statistic_check_ett_shiftClosingTime)
                        .text(DateFormatOperations.getDateForOut(shiftEvent.getCloseTime()));
            }
            final QuickAdapter adapter = new QuickEttAdapter(getActivity(),
                    new EttDataSource(shiftEvent.getStartTime().getTime()),
                    Di.INSTANCE.nsiVersionManager(),
                    Dagger.appComponent().stationRepository());
            int ettCount = adapter.getCount();
            aQuery.id(R.id.statistic_check_ett_checkedTotalCount).text(String.valueOf(ettCount));
            if (ettCount > 0) {
                aQuery.id(R.id.statistic_check_ett_list).adapter(adapter);
            } else {
                view.findViewById(R.id.dataContainer).setVisibility(View.GONE);
                //показываем диалог - нет данных
                showDialogForEmptyData();
            }
        } else {
            view.findViewById(R.id.dataContainer).setVisibility(View.GONE);
            //Открытых смен не было, показываем диалог что нет данных
            showDialogForEmptyData();
        }

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

    private class EttDataSource implements QuickAdapter.DataSource {

        /**
         * Время открытия последней смены
         */
        private final long shiftOpenTime;

        public EttDataSource(long timeStamp) {
            shiftOpenTime = timeStamp;
        }

        @Override
        public Cursor getRowIds() {

            /*
             * Формируем запрос
             * Select SmartCard._id
             *	from SmartCard join TicketEventBase on SmartCard._id = TicketEventBase.SmartCardId
             *	where SmartCard.TypeCode = 0 and SmartCard._id IN
             *		(select TicketEventBase.SmartCardId
             *		from TicketEventBase join CPPKTicketControls ON TicketEventBase._id = CPPKTicketControls.TicketEventBaseId
             *		where CPPKTicketControls.EventId IN (Select Event._id from Event where Event.CreationTimestamp > 1424451395070))
             */

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Select ").append(SmartCardDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id).append(" from ")
                    .append(SmartCardDao.TABLE_NAME).append(" join ").append(TicketEventBaseDao.TABLE_NAME)
                    .append(" on ").append(SmartCardDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id).append(" = ").append(TicketEventBaseDao.Properties.SmartCardId)
                    .append(" where ").append(SmartCardDao.TABLE_NAME).append(".").append(SmartCardDao.Properties.TypeCode).append(" = ").append(TicketStorageType.ETT.getDBCode())
                    .append(" and ").append(SmartCardDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id).append(" in ")
                    .append("(select ").append(TicketEventBaseDao.Properties.SmartCardId)
                    .append(" from ").append(TicketEventBaseDao.TABLE_NAME).append(" join ").append(CppkTicketControlsDao.TABLE_NAME)
                    .append(" on ").append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id).append(" = ").append(CppkTicketControlsDao.Properties.TicketEventBaseId)
                    .append(" where ").append(CppkTicketControlsDao.Properties.EventId).append(" in ")
                    .append("(select ").append(EventDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id).append(" from ").append(EventDao.TABLE_NAME)
                    .append(" where ").append(EventDao.Properties.CreationTimestamp).append(" > ").append(shiftOpenTime).append("))");


            Logger.info("SQL", stringBuilder.toString());

            return globals.getLocalDb().rawQuery(stringBuilder.toString(), null);
        }

        @Override
        public Cursor getRowById(long rowId) {

            /*
             *
             * Формируем запрос:
             * Select SmartCard.OuterNumber, TicketEventBase.DepartureStationCode, TicketEventBase.DestinationStationCode,
             * 			TicketEventBase.TicketNumber, TicketEventBase.WayType, TicketEventBase.SaleDateTime
             * from SmartCard join TicketEventBase on SmartCard._id = TicketEventBase.SmartCardId where SmartCard._id = 1
             *
             */

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Select ")
                    .append(SmartCardDao.Properties.OuterNumber).append(", ")
                    .append(TicketEventBaseDao.Properties.DepartureStationId).append(", ")
                    .append(TicketEventBaseDao.Properties.DestinationStationId).append(", ")
                    .append(CppkTicketControlsDao.Properties.TicketNumber).append(", ")
                    .append(TicketEventBaseDao.Properties.WayType).append(", ")
                    .append(TicketEventBaseDao.Properties.SaleDateTime)
                    .append(" from ").append(CppkTicketControlsDao.TABLE_NAME).append(" join ")
                    .append(TicketEventBaseDao.TABLE_NAME)
                    .append(" on ")
                    .append(CppkTicketControlsDao.Properties.TicketEventBaseId)
                    .append(" = ").append(TicketEventBaseDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id)
                    .append(" join ").append(SmartCardDao.TABLE_NAME).append(" on ")
                    .append(TicketEventBaseDao.Properties.SmartCardId)
                    .append(" = ").append(SmartCardDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id)
                    .append(" where ")
                    .append(SmartCardDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Id)
                    .append(" = ").append(rowId);

            Logger.info("SQL", stringBuilder.toString());

            return globals.getLocalDb().rawQuery(stringBuilder.toString(), null);
        }
    }
}
