package ru.ppr.cppk.repeal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.CppkTicketReturnDao;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;

/**
 * Экран аннулирования ПД из списка оформленных.
 */
public class RepealFromHistoryActivity extends SystemBarActivity implements OnItemClickListener {

    private ListView listView = null;
    private CppkTicketReturnDao ticketReturnDao = null;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RepealFromHistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeal_from_history_activity);

        ticketReturnDao = getLocalDaoSession().getCppkTicketReturnDao();
        listView = (ListView) findViewById(R.id.repail_sell_pd_list);

        List<CPPKTicketSales> salesList = new ArrayList<>();
        ShiftEvent shift = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (shift != null && shift.getStatus() != ShiftEvent.Status.ENDED) {
            EnumSet<ProgressStatus> statuses = EnumSet.of(ProgressStatus.Completed, ProgressStatus.CheckPrinted);
            salesList = getLocalDaoSession().getCppkTicketSaleDao().getSaleEventsForShift(shift.getShiftId(), statuses, true);
        }

        final RepealItemAdapter adapter = new RepealItemAdapter(salesList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((RepealItemAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private class RepealItemAdapter extends BaseAdapter {

        private final List<CPPKTicketSales> items;
        private final int size;

        private RepealItemAdapter(@NonNull List<CPPKTicketSales> items) {
            this.items = items;
            size = items.size();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public CPPKTicketSales getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.repeal_from_history_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.numberPdTextView = (TextView) view.findViewById(R.id.repeal_item_number_pd);
                viewHolder.repealLayoutView = view.findViewById(R.id.repeal_item_reason_layout);
                viewHolder.repealReasonView = (TextView) view.findViewById(R.id.repeal_item_reason);
                viewHolder.timeTextView = (TextView) view.findViewById(R.id.repeal_item_time_sell);
                viewHolder.typeTextView = (TextView) view.findViewById(R.id.repeal_item_type_pdl);
                view.setTag(viewHolder);
            }

            CPPKTicketSales cppkTicketSales = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
            ParentTicketInfo parentTicketInfo = getLocalDaoSession().getParentTicketInfoDao().load(ticketSaleReturnEventBase.getParentTicketInfoId());
            TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

            if (parentTicketInfo != null) {
                if (cppkTicketSales.getConnectionType() == ConnectionType.TRANSFER) {
                    holder.typeTextView.setText(R.string.repeal_activity_from_history_transfer_type);
                } else if (cppkTicketSales.getConnectionType() == ConnectionType.TRANSIT) {
                    holder.typeTextView.setText(R.string.repeal_activity_from_history_transit_type);
                } else {
                    holder.typeTextView.setText(R.string.fare);
                }
            } else {
                holder.typeTextView.setText(ticketEventBase.getTicketTypeShortName());
            }


            Check check = getLocalDaoSession().getCheckDao().load(ticketSaleReturnEventBase.getCheckId());

            holder.timeTextView.setText(DateFormatOperations.getTime(ticketEventBase.getSaledateTime()));
            holder.numberPdTextView.setText(String.format(getString(R.string.number_for_pd), check.getOrderNumber()));

            CPPKTicketReturn returnEvent = ticketReturnDao
                    .findLastPdRepealEventForPdSaleEvent(cppkTicketSales.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));

            if (returnEvent != null) {
                holder.repealReasonView.setText(returnEvent.getRecallReason());
                holder.repealLayoutView.setVisibility(View.VISIBLE);
            } else {
                holder.repealLayoutView.setVisibility(View.GONE);
            }

            return view;
        }

        class ViewHolder {
            TextView timeTextView;
            TextView numberPdTextView;
            TextView typeTextView;
            View repealLayoutView;
            TextView repealReasonView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        View reasonView = view.findViewById(R.id.repeal_item_reason_layout);
        if (reasonView.getVisibility() == View.GONE) {
            Navigator.navigateToRepealFinishActivity(this, null, id);
        }
    }
}
