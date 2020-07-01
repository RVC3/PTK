package ru.ppr.cppk.ui.activity.selectionActivity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.filters.StationFilter;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Station;

/**
 * Экран выбора Станции привязки ПТК
 *
 * @autor Grigoriy Kashka
 */
public class BindingStationSelectionActivity extends BaseSelectionActivity<Station> {

    // EXTRAS
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    public static final String EXTRA_RESULT_NAME = "EXTRA_RESULT_NAME";

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

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, BindingStationSelectionActivity.class);
        return intent;
    }

    @Override
    protected Intent getIntent(Station selectedItem) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_CODE, selectedItem.getCode());
        intent.putExtra(EXTRA_RESULT_NAME, selectedItem.getName());
        setResult(RESULT_OK, intent);
        return intent;
    }

    @Override
    protected BaseListAdapter<Station> getAdapter() {
        BaseListAdapter<Station> adapter = new StationsAdapter(this);
        Station station =
                Dagger.appComponent().stationRepository().load(
                        (long) Globals.getInstance().getPrivateSettingsHolder().get().getSaleStationCode(),
                        Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
        adapter.setCurrentItem(station);
        adapter.setItems(getData());
        adapter.setFilter(new StationFilter(adapter, getData()));
        return adapter;
    }

    @Override
    protected int getTitleId() {
        return R.string.binding_station;
    }

    @Override
    protected int getHintId() {
        return R.string.new_binding_station;
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
