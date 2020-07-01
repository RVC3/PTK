package ru.ppr.cppk.utils.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class QuickAdapter extends BaseAdapter {

    private final DataSource dataSource;
    private int size = 0;
    private Cursor cursor = null;
    private final Context context;
    private DataSetObserver mDataSetObserver;

    public QuickAdapter(Context context, DataSource dataSource) {
        this.dataSource = dataSource;
        this.context = context;
        mDataSetObserver = new NotifyingDataSetObserver();
        getRowIds();
    }

    private void getRowIds() {
        if (cursor != null)
            cursor.close();

        cursor = dataSource.getRowIds();
        size = cursor.getCount();
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;
        Cursor cursor = (Cursor) getItem(position);
        if (cursor != null) {
            cursor.moveToFirst();
            if (convertView == null)
                view = newView(context, cursor, parent);
            else
                view = convertView;
            bindView(view, context, cursor);
            cursor.close();
        }
        return view;
    }

    @Override
    public Object getItem(int position) {
        if (cursor.moveToPosition(position)) {
            long rowId = getItemId(position);
            return dataSource.getRowById(rowId);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (cursor.moveToPosition(position)) {
            return cursor.getLong(0);
        } else {
            return 0;
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return null;
        }
        final Cursor oldCursor = cursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        cursor = newCursor;
        if (cursor != null) {
            if (mDataSetObserver != null) {
                cursor.registerDataSetObserver(mDataSetObserver);
            }
            notifyDataSetChanged();
        } else {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Создает viewHolder для элемента списка
     *
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    public abstract View newView(Context context, Cursor cursor, ViewGroup parent);

    /**
     * Восстанавливает элемент списка
     *
     * @param view
     * @param context
     * @param cursor
     */
    public abstract void bindView(View view, Context context, Cursor cursor);

    public interface DataSource {
        Cursor getRowIds();

        Cursor getRowById(long rowId);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetChanged();
        }
    }

}
