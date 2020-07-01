package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Поле ввода с колбеком о нажатии кнопки "Назад".
 */
public class FilterEditText extends EditText {

    OnBackListener listener = null;

    public interface OnBackListener {
        boolean onBackAtEditPressed();
    }

    public FilterEditText(Context context) {
        super(context);
    }

    public FilterEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (listener != null && event.getAction() == KeyEvent.ACTION_UP) {
            post(() -> listener.onBackAtEditPressed());
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnBackListener(OnBackListener listener) {
        this.listener = listener;
    }

}
