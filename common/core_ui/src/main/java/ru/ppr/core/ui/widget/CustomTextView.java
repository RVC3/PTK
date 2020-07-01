package ru.ppr.core.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import ru.ppr.core.ui.helper.FontInjector;

/**
 * Текстовое поле с кастомными шрифтами.
 *
 * @author Aleksandr Brazhkin
 */
public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        FontInjector.injectFont(this, attrs);
    }

}
