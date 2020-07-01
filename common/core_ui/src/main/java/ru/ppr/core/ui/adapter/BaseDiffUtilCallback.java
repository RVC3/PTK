package ru.ppr.core.ui.adapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class BaseDiffUtilCallback<T> extends DiffUtil.Callback {

    private List<T> oldItems;
    private List<T> newItems;

    void setItems(List<T> oldItems, List<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return oldItems.size();
    }

    @Override
    public int getNewListSize() {
        return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldItems.get(oldItemPosition), newItems.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areContentsTheSame(oldItems.get(oldItemPosition), newItems.get(newItemPosition));
    }

    protected abstract boolean areItemsTheSame(T oldItem, T newItem);

    protected abstract boolean areContentsTheSame(T oldItem, T newItem);
}
