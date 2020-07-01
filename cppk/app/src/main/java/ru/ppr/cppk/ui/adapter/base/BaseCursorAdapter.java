package ru.ppr.cppk.ui.adapter.base;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;

public abstract class BaseCursorAdapter<T> extends CursorAdapter {

    public BaseCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public T getItem(int position) {
        return getItemFromCursor((Cursor) super.getItem(position));
    }

    protected abstract T getItemFromCursor(Cursor cursor);
}
