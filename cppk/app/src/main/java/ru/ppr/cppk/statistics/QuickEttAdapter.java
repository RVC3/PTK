package ru.ppr.cppk.statistics;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.SmartCardDao;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.utils.adapters.QuickAdapter;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.StationRepository;

public class QuickEttAdapter extends QuickAdapter {

    private final NsiVersionManager nsiVersionManager;
    private final StationRepository stationRepository;

    public QuickEttAdapter(Context context,
                           DataSource dataSource,
                           NsiVersionManager nsiVersionManager,
                           StationRepository stationRepository) {
        super(context, dataSource);
        this.nsiVersionManager = nsiVersionManager;
        this.stationRepository = stationRepository;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.statistics_check_ett_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.departureStation = (TextView) view.findViewById(R.id.departureStation);
        viewHolder.destinationStation = (TextView) view.findViewById(R.id.destinationStation);
        viewHolder.ETTNumber = (TextView) view.findViewById(R.id.ETTNumber);
        viewHolder.PDNumber = (TextView) view.findViewById(R.id.PDNumber);
        viewHolder.PDSellDateTime = (TextView) view.findViewById(R.id.PDSellDateTime);
        viewHolder.arrowThere = (ImageView) view.findViewById(R.id.arrowThere);
        viewHolder.arrowThereBack = (ImageView) view.findViewById(R.id.arrowThereBack);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder != null) {
            long saledatetime = cursor.getLong(cursor.getColumnIndex(TicketEventBaseDao.Properties.SaleDateTime));
            int departureStationCode = cursor.getInt(cursor.getColumnIndex(TicketEventBaseDao.Properties.DepartureStationId));
            int destinationStationCode = cursor.getInt(cursor.getColumnIndex(TicketEventBaseDao.Properties.DestinationStationId));
            int ticketNumber = cursor.getInt(cursor.getColumnIndex(CppkTicketControlsDao.Properties.TicketNumber));
            String ettNumber = cursor.getString(cursor.getColumnIndex(SmartCardDao.Properties.OuterNumber));
            TicketWayType wayType = TicketWayType.valueOf(cursor.getInt(cursor.getColumnIndex(TicketEventBaseDao.Properties.WayType)));

            Station departureStation = stationRepository.load((long) departureStationCode, nsiVersionManager.getNsiVersionIdForDate(new Date(saledatetime * 1000)));
            Station destinationStation = stationRepository.load((long) destinationStationCode, nsiVersionManager.getNsiVersionIdForDate(new Date(saledatetime * 1000)));

            viewHolder.ETTNumber.setText(ettNumber);
            viewHolder.PDNumber.setText(String.format(Locale.getDefault(), "%06d", ticketNumber));
            viewHolder.PDSellDateTime.setText(DateFormatOperations.getDateForOut(new Date(saledatetime * 1000)));
            viewHolder.departureStation.setText(departureStation != null
                    ? departureStation.getName() : context.getString(R.string.not_found));
            viewHolder.destinationStation.setText(destinationStation != null
                    ? destinationStation.getName() : context.getString(R.string.not_found));

            if (wayType == TicketWayType.OneWay) {
                viewHolder.arrowThere.setVisibility(View.VISIBLE);
                viewHolder.arrowThereBack.setVisibility(View.GONE);
            } else {
                viewHolder.arrowThere.setVisibility(View.GONE);
                viewHolder.arrowThereBack.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class ViewHolder {
        private TextView departureStation;
        private TextView destinationStation;
        private TextView ETTNumber;
        private TextView PDNumber;
        private TextView PDSellDateTime;
        private ImageView arrowThere;
        private ImageView arrowThereBack;
    }

}
