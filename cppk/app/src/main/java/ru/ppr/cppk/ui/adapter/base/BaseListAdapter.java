package ru.ppr.cppk.ui.adapter.base;

import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter implements Filterable {

    protected List<T> items = new ArrayList<T>();
    private T currentItem = null;
    private Filter filter;

    @Override
    public int getCount() {
        return getItems().size();
    }

    @Override
    public T getItem(int position) {
        return getItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        if (items == null)
            throw new IllegalStateException("items shouldn't be null");

        this.items = items;

        notifyDataSetChanged();
    }

    public void setCurrentItem(T item) {
        this.currentItem = item;
    }

    public boolean equalsCurrentItem(T item) {

        if (currentItem == null) {
            return false;
        }

        return currentItem.equals(item);
    }


    private int selectedPosition = -1;
    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition=selectedPosition;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
