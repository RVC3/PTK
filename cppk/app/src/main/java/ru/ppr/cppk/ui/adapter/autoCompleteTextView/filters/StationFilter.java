package ru.ppr.cppk.ui.adapter.autoCompleteTextView.filters;

import android.text.TextUtils;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.Station;

public class StationFilter extends Filter {

    private final BaseListAdapter<Station> adapter;
    private List<Station> fullList;
    private String conditionNotEqualsIgnoreCase;

    public StationFilter(BaseListAdapter<Station> adapter, List<Station> list) {
        this.adapter = adapter;
        this.fullList = list;
    }

    public void setFullList(List<Station> fullList) {
        this.fullList = fullList;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results != null && results.count > 0) {
            adapter.setItems((List<Station>) results.values);
        } else {
            adapter.notifyDataSetInvalidated();
        }
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        List<Station> stations;

        if (constraint == null || constraint.toString().isEmpty()) {
            stations = fullList;
        } else {
            stations = new ArrayList<>();
            for (Station station : fullList) {
                if ((conditionNotEqualsIgnoreCase == null
                        || !station.getName().trim().equalsIgnoreCase(conditionNotEqualsIgnoreCase))
                        && -1 != (station.getName().toLowerCase(Locale.getDefault())
                        .indexOf(constraint.toString().toLowerCase(Locale.getDefault())))) {
                    stations.add(station);
                }
            }
        }
        // Assign the data to the FilterResults
        filterResults.values = stations;
        filterResults.count = stations.size();
        return filterResults;
    }

    public void setConditionNotEqualsIgnoreCase(String conditionNotEqualsIgnoreCase) {
        this.conditionNotEqualsIgnoreCase = conditionNotEqualsIgnoreCase;
    }

    public Station findStationByName(String name) {
        Station station = null;
        if (name == null) {
            return station;
        }
        String nameInLowerCase = name.toLowerCase(Locale.getDefault());
        for (Station st : fullList) {
            if (TextUtils.equals(st.getName().toLowerCase(Locale.getDefault()), nameInLowerCase)) {
                station = st;
                break;
            }
        }
        return station;
    }
}
