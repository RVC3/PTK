package ru.ppr.core.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Обработчик нажатий по элементам RecyclerView.
 *
 * @author Aleksandr Brazhkin
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(final RecyclerView rv, MotionEvent e) {
        final View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
            int position = rv.getChildAdapterPosition(childView);
            if (position != RecyclerView.NO_POSITION) {
                rv.post(() -> listener.onItemClick(childView, position));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
