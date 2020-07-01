package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

/**
 * Created by Brazhkin A.V. on 03.06.2015.
 */
public class AutoCompleteTextViewWithBack extends AutoCompleteTextView {

    private OnBackListener mOnBackListener;


    public AutoCompleteTextViewWithBack(Context context) {
        super(context);
        init(context, null);
    }

    public AutoCompleteTextViewWithBack(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoCompleteTextViewWithBack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnBackListener != null) {
                return mOnBackListener.onBackPressed();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnBackListener(OnBackListener onBackListener) {
        this.mOnBackListener = onBackListener;
    }

    @Override
    public boolean enoughToFilter() {
        //return super.enoughToFilter();
        return true;
    }

    public interface OnBackListener {
        boolean onBackPressed();
    }
}
