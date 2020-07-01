package ru.ppr.cppk.ui.adapter.base;

import android.view.View;
import android.widget.AdapterView;

/**
 * Адаптер для {@link AdapterView.OnItemSelectedListener}, чтобы не заставлять переопределять метод {{@link #onNothingSelected(AdapterView)}}.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class OnItemSelectedListenerAdapter implements AdapterView.OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
