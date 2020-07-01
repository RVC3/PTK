package ru.ppr.cppk.ui.activity.selectionActivity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.filters.StationFilter;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Station;

/**
 * Экран выбора текущей станции работы ПТК
 */
public class StationSelectionActivity extends BaseSelectionActivity<Station> {

    private volatile List<Station> items;

    @Override
    protected List<Station> getData() {

        List<Station> localList = items;

        if (localList == null) {
            synchronized (this) {
                if (items == null) {
                    items = Dagger.appComponent().stationRepository().getStationsForProductionSection(
                            di().getPrivateSettings().get().getProductionSectionId(),
                            di().nsiVersionManager().getCurrentNsiVersionId());
                }
            }
            return items;
        }

        return localList;
    }

    @Override
    protected Intent getIntent(Station selectedItem) {

        Intent intent = new Intent();
        intent.putExtra("code", selectedItem.getCode());
        intent.putExtra("name", selectedItem.getName());
        setResult(RESULT_OK, intent);
        return intent;
    }

    @Override
    protected BaseListAdapter<Station> getAdapter() {
        BaseListAdapter<Station> adapter = new StationsAdapter(this);
        adapter.setCurrentItem(Dagger.appComponent().stationRepository().load(
                (long) Di.INSTANCE.getPrivateSettings().get().getCurrentStationCode(),
                Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId()));
        adapter.setItems(getData());
        adapter.setFilter(new StationFilter(adapter, getData()));
        return adapter;
    }

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, StationSelectionActivity.class);
        return intent;
    }

    @Override
    protected int getTitleId() {
        return R.string.work_station;
    }

    @Override
    protected int getHintId() {
        return R.string.new_workstation;
    }

    @Override
    protected
    @Nullable
    Station getItemForName(String name) {

        Station station = null;

        for (Station st : getData()) {
            if (st.getName().toLowerCase().equals(name.toLowerCase()))
                station = st;
        }

        return station;
    }
}
