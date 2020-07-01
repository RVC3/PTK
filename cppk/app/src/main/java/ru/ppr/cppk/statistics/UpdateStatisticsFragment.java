package ru.ppr.cppk.statistics;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.settings.SharedPreferencesUtils;

public class UpdateStatisticsFragment extends FragmentParent {

    public static Fragment newInstance() {
        return new UpdateStatisticsFragment();
    }

    ViewHolder viewHolder;
    Globals globals;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globals = (Globals) getActivity().getApplication();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.update_statistics, null);

        viewHolder = new ViewHolder();
        viewHolder.connectionToArmDate = (TextView) view.findViewById(R.id.connection_to_arm_date);
        viewHolder.uploadingDataDate = (TextView) view.findViewById(R.id.uploading_data_date);
        viewHolder.updatingPODate = (TextView) view.findViewById(R.id.updating_PO_date);
        viewHolder.updatingPOVersion = (TextView) view.findViewById(R.id.updating_PO_version);
        viewHolder.updatingNCIDate = (TextView) view.findViewById(R.id.updating_NCI_date);
        viewHolder.updatingNCIVersion = (TextView) view.findViewById(R.id.updating_NCI_version);
        viewHolder.updatingStopListsDate = (TextView) view.findViewById(R.id.updating_stop_lists_date);
        viewHolder.updatingStopListsVersion = (TextView) view.findViewById(R.id.updating_stop_lists_version);

        // НСИ
        //int NSIVersion = NsiDbOperations.getNsiVersionCurrent(globals.getNsiDb());
        UpdateEvent item = Dagger.appComponent().updateEventRepository().getLastUpdateEvent(UpdateEventType.NSI, true);
        int NSIVersion = -1;
        long NSIUpdateDateTime = 0;
        if (item != null) {
            NSIVersion = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
            NSIUpdateDateTime = item.getOperationTime().getTime();
        }

        if (NSIVersion > -1) {
            viewHolder.updatingNCIVersion.setText(Integer.toString(NSIVersion));
        }
        if (NSIUpdateDateTime != 0) {
            viewHolder.updatingNCIDate.setText(formatDateTime(new Date(NSIUpdateDateTime)));
        }

        // ПО
        UpdateEvent lastUpdateEvent = Dagger.appComponent().updateEventRepository().getLastUpdateEvent(UpdateEventType.SW, false);
        String SWVersion = lastUpdateEvent == null ? new String() : lastUpdateEvent.getVersion();
        if (SWVersion != null && !SWVersion.isEmpty()) {
            viewHolder.updatingPOVersion.setText(SWVersion);
        }
        long SWUpdateDateTime = lastUpdateEvent == null ? 0 : lastUpdateEvent.getOperationTime().getTime();
        if (SWUpdateDateTime != 0) {
            viewHolder.updatingPODate.setText(formatDateTime(new Date(SWUpdateDateTime)));
        }

        // Стоп-листы
        String stopListVersion = getSecurityDaoSession().getSecurityStopListVersionDao().getSmartCardStoplistItemVersion();
        if (stopListVersion != null && !stopListVersion.isEmpty()) {
            viewHolder.updatingStopListsVersion.setText(stopListVersion);
        }
        UpdateEvent stopListItem = Dagger.appComponent().updateEventRepository().getLastUpdateEvent(UpdateEventType.STOP_LISTS, true);
        if (stopListItem != null) {
            long stopListUpdateDateTime = stopListItem.getOperationTime().getTime();
            if (stopListUpdateDateTime != 0) {
                viewHolder.updatingStopListsDate.setText(formatDateTime(new Date(stopListUpdateDateTime)));
            }
        }

        // Выгрузка данных о контроле и продаже ПД
        long getEventRespDateTime = SharedPreferencesUtils.getGetEventRespDateTime(globals);
        if (getEventRespDateTime != 0) {
            viewHolder.uploadingDataDate.setText(formatDateTime(new Date(getEventRespDateTime)));
        }

        // Выгрузка данных о контроле и продаже ПД
        long ARMConnectedDateTime = SharedPreferencesUtils.getARMConnectedDateTime(globals);
        if (ARMConnectedDateTime != 0) {
            viewHolder.connectionToArmDate.setText(formatDateTime(new Date(ARMConnectedDateTime)));
        }

        return view;
    }

    class ViewHolder {
        TextView connectionToArmDate;
        TextView uploadingDataDate;
        TextView updatingPODate;
        TextView updatingPOVersion;
        TextView updatingNCIDate;
        TextView updatingNCIVersion;
        TextView updatingStopListsDate;
        TextView updatingStopListsVersion;
    }

    public static String formatDateTime(Date dateTime) {
        String date = "";
        String format = "dd.MM.yyyy\nHH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        date = dateFormat.format(dateTime);
        return date;
    }

}
