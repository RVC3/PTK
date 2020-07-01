package ru.ppr.cppk.ui.adapter.base;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {

    protected List<T> items = new ArrayList<>();

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        if (items == null) {
            throw new IllegalStateException("items shouldn't be null");
        }
        this.items = items;
        notifyDataSetChanged();
    }

    public void appendItems(List<T> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void appendItem(T item) {
        this.items.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }
}
