package ru.ppr.core.ui.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> items = new ArrayList<T>();
    AdapterCountChangedListener adapterCountChangedListener;

    protected Context context;
    protected LayoutInflater mLayoutInflater;

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void appendItems(List<T> items) {
        insertItems(this.items.size(), items);
        onCountChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        context = null;
        mLayoutInflater = null;
    }

    public int binarySearchInSortedList(T item) {

        Comparator comparator = getComparator();

        int low = 0;
        int high = items.size() - 1;

        while (high >= low) {           //the loop only stops when
            //high gets updated to something
            //it shouldn't

            int mid = (low + high) / 2;  //note what this does
            //if (low + high) is odd

            int compare = comparator.compare(item, items.get(mid));
            if (compare == -1)         //update index of the
                high = mid - 1;           //right-most element considered

            else if (compare == 1)    //update index of
                low = mid + 1;            //left-most element considered

            else
                return mid;               //found it! now return the
            //index and exit the method

        }

        return low;
        //return -1 - low;  //key was not found, so
        //return negative value
        //that doubles as an insertion
        //point when turned back
        //to a positive value
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
        onCountChanged();
    }

    public Comparator<T> getComparator() {
        return null;
    }

    public T getFirstItem() {
        return items.size() > 0 ? items.get(0) : null;
    }

    public T getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        } else {
            return null;
        }
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
        onCountChanged();
    }

    public void setItems(List<T> items, BaseDiffUtilCallback<T> diffUtilCallback) {
        if (items == null) {
            throw new IllegalStateException("items shouldn't be null");
        }
        diffUtilCallback.setItems(this.items, items);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback, false);
        this.items = items;
        diffResult.dispatchUpdatesTo(this);
        onCountChanged();
    }

    public T getLastItem() {
        return items.size() > 0 ? items.get(items.size() - 1) : null;
    }

    public void insertItem(int position, T item) {
        items.add(position, item);
        notifyItemInserted(position);
        onCountChanged();
    }

    public void insertItem(T item) {
        if (getComparator() == null) {
            insertItem(items.size(), item);
        } else {
            int position = binarySearchInSortedList(item);
            insertItem(position, item);
        }
    }

    public void insertItems(int position, List<T> items) {
        if (items == null) {
            throw new IllegalStateException("items shouldn't be null");
        }
        this.items.addAll(position, items);
        notifyItemRangeInserted(position, items.size());
        onCountChanged();
    }

    protected void onCountChanged() {
        if (adapterCountChangedListener != null) {
            adapterCountChangedListener.onCountChanged(items.size());
        }
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        onCountChanged();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setAdapterCountChangedListener(AdapterCountChangedListener adapterCountChangedListener) {
        this.adapterCountChangedListener = adapterCountChangedListener;
    }

    public interface AdapterCountChangedListener {
        void onCountChanged(int count);
    }
}
