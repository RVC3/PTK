package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import ru.ppr.cppk.R;

/**
 * Обертка над {@link Spinner} с добавлением гамбургера.
 * {@deprecated} Удалить и отказаться от габургера.
 * Использовать простой {@link Spinner}.
 */
@Deprecated
public class HamburgerSpinner extends RelativeLayout {

    private static final int DEFAULT_TITLE_TEXT_SIZE = 16;

    private CharSequence title;
    private int titleTextSize;
    private int heightForSpinner;

    private TextView titleTextView;
    private Spinner spinner;

    public HamburgerSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HamburgerSpinner, 0, 0);

        try {
            getAttribute(array);
            View.inflate(context, R.layout.hamburger_spinner, this);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupView();
    }

    private void getAttribute(TypedArray array) {
        title = array.getString(R.styleable.HamburgerSpinner_spinner_title);
        titleTextSize = array.getDimensionPixelSize(R.styleable.HamburgerSpinner_spinner_text_size, DEFAULT_TITLE_TEXT_SIZE);
        heightForSpinner = array.getDimensionPixelSize(R.styleable.HamburgerSpinner_spinner_height,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setupView() {
        titleTextView = (TextView) findViewById(R.id.hamburger_spinner_text_view_title);
        if (title != null) {
            titleTextView.setText(title);
            titleTextView.setTextSize(titleTextSize);
        }

        spinner = (Spinner) findViewById(R.id.hamburger_spinner_view);
        android.view.ViewGroup.LayoutParams params = spinner.getLayoutParams();
        params.height = heightForSpinner;
        spinner.setLayoutParams(params);
    }

    public void setAdapter(SpinnerAdapter adapter) {
        spinner.setAdapter(adapter);
    }

    public SpinnerAdapter getAdapter() {
        return spinner.getAdapter();
    }

    public void setSelection(int index) {
        spinner.setSelection(index);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        spinner.setOnItemClickListener(l);
    }

    public Object getSelectedItem() {
        return spinner.getSelectedItem();
    }

    public int getSelectedItemPosition() {
        return spinner.getSelectedItemPosition();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
    }

}
