package ru.ppr.cppk.ui.adapter.spinner;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.cppk.ui.adapter.base.BaseListAdapter;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * Адаптер для спиннера тарифных планов на экране продажи.
 * <p>
 * Обратить внимание, используется костыльный минимый элемент в позиции 0 для реализации поведения:
 * https://aj.srvdev.ru/browse/CPPKPP-31605
 * Наталья Владимировна:
 * Если мы в поезде, то очистим поле тарифного плана, если нет тарифного плана для текущей категории поезда.
 * <p>
 * Причина:
 * Спиннер не поддерживает пустое выбранное значение, если в адаптере есть элементы.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffPlanForSaleAdapter extends BaseListAdapter<TariffPlan> {

    private final LayoutInflater layoutInflater;
    private final Context context;
    private final int itemLayoutId;

    public TariffPlanForSaleAdapter(Context context, @LayoutRes int itemLayoutId) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemLayoutId = itemLayoutId;
        setSelectedPosition(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(itemLayoutId, parent, false);
        }

        if (position != 0) {
            TariffPlan tariffPlan = getItem(position - 1);

            TextView textView = (TextView) view;
            textView.setText(tariffPlan.getShortName());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        if (position != 0 && getSelectedPosition() != position) {

            view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            TariffPlan tariffPlan = getItem(position - 1);
            if (tariffPlan != null) {
                textView.setText(tariffPlan.getShortName());
            }
        } else {
            view = new View(context);
            view.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        } else {
            TariffPlan tariffPlan = getItem(position - 1);
            return tariffPlan.getCode();
        }
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }
}
