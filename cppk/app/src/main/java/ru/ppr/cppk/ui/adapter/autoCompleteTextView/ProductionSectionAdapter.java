package ru.ppr.cppk.ui.adapter.autoCompleteTextView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import ru.ppr.cppk.R;
import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.ProductionSection;

public class ProductionSectionAdapter extends BaseListAdapter<ProductionSection> implements Filterable {

    private final Context context;
    private final LayoutInflater inflater;

    public ProductionSectionAdapter(Context context) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView element;
        if (convertView == null) {
            element = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            element = (TextView) convertView;
        }

        ProductionSection productionSection = getItem(position);
        element.setText(productionSection.getName());

        if (equalsCurrentItem(productionSection)) {
            element.setTextColor(context.getResources().getColor(R.color.blue));
        } else {
            element.setTextColor(Color.BLACK);
        }

        return element;
    }
}
