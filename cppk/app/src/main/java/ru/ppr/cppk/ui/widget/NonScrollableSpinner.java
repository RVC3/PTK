package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.widget.Spinner;

import ru.ppr.cppk.R;

/**
 * Spinner, использующий тему с отключенным отображаеним сколлбара.
 *
 * @author Aleksandr Brazhkin
 */
public class NonScrollableSpinner extends Spinner {

    public NonScrollableSpinner(Context context) {
        this(context, null, android.R.attr.spinnerStyle, -1);
    }

    public NonScrollableSpinner(Context context, int mode) {
        this(context, null, android.R.attr.spinnerStyle, mode);
    }

    public NonScrollableSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.spinnerStyle, -1);
    }

    public NonScrollableSpinner(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, -1);
    }

    public NonScrollableSpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(new ContextThemeWrapper(context, R.style.AppTheme_NonScrollableSpinner), attrs, defStyle, mode);
    }
}
