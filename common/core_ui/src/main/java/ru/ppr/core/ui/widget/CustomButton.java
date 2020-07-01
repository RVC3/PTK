package ru.ppr.core.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import ru.ppr.core.ui.helper.FontInjector;


/**
 * Кнопка с кастомными шрифтами.
 *
 * @author Aleksandr Brazhkin
 */
public class CustomButton extends SingleClickButton {

    public CustomButton(Context context) {
        this(context, null);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        FontInjector.injectFont(this, attrs);
    }
}
