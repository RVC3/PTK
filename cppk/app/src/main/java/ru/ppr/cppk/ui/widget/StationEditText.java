package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

/**
 * @author Aleksandr Brazhkin
 */
public class StationEditText extends AutoCompleteTextView {

    private OnBackListener mOnBackListener;


    public StationEditText(Context context) {
        this(context, null, android.R.attr.autoCompleteTextViewStyle);
    }

    public StationEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public StationEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackground(null);
        setSingleLine(true);
        setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        setImeOptions(EditorInfo.IME_ACTION_DONE);
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
    protected void replaceText(CharSequence text) {
        // nop
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    public interface OnBackListener {
        boolean onBackPressed();
    }
}
