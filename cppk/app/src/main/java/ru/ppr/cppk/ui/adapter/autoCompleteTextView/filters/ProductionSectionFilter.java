package ru.ppr.cppk.ui.adapter.autoCompleteTextView.filters;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.ProductionSection;

public class ProductionSectionFilter extends Filter {

    private final BaseListAdapter<ProductionSection> adapter;
    private final List<ProductionSection> fullList;

    public ProductionSectionFilter(BaseListAdapter<ProductionSection> adapter, List<ProductionSection> list) {
        this.adapter = adapter;
        this.fullList = list;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results != null && results.count > 0) {
            adapter.setItems((List<ProductionSection>) results.values);
        } else {
            adapter.notifyDataSetInvalidated();
        }
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        String text = constraint == null ? "" : constraint.toString();
        List<ProductionSection> stations;

        if (text.isEmpty()) {
            stations = fullList;
        } else {
            stations = new ArrayList<>();
            for (ProductionSection station : fullList) {
                if (station.getName().trim().toLowerCase(Locale.getDefault())
                        .contains(text.trim().toLowerCase(Locale.getDefault()))) {
                    stations.add(station);
                }
            }
        }
        // Assign the data to the FilterResults
        filterResults.values = stations;
        filterResults.count = stations.size();
        return filterResults;
    }

}
