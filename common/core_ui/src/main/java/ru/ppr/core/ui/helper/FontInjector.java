package ru.ppr.core.ui.helper;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import ru.ppr.core.ui.R;


/**
 * Класс для внедрения кастомного шрифта в текстовые поля.
 *
 * @author Aleksandr Brazhkin
 */
public class FontInjector {

    private static Typeface cached_roboto_regular_typeface = null;
    private static Typeface cached_roboto_light_typeface = null;
    private static Typeface cached_roboto_bold_typeface = null;

    public static final int ROBOTO_REGULAR = 0;
    public static final int ROBOTO_LIGHT = 1;
    public static final int ROBOTO_BOLD = 2;

    public static void injectFont(TextView view, AttributeSet attrs) {

        int typeface = ROBOTO_REGULAR;
        if (attrs != null) {

            TypedArray a = view.getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomFontView, 0, 0);

            try {
                typeface = a.getInt(R.styleable.CustomFontView_typeface, ROBOTO_REGULAR);
            } finally {
                a.recycle();
            }
        }
        setFont(view, typeface);
    }

    @Deprecated
    public static void injectFont(TextView view, int typeface) {
        setFont(view, typeface);
    }

    private static void setFont(TextView view, int typeface) {
        switch (typeface) {
            case ROBOTO_LIGHT: {
                if (cached_roboto_light_typeface == null) {
                    cached_roboto_light_typeface = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/RobotoLight.ttf");
                }
                view.setTypeface(cached_roboto_light_typeface);
                break;
            }
            case ROBOTO_BOLD: {
                if (cached_roboto_bold_typeface == null) {
                    cached_roboto_bold_typeface = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/RobotoBold.ttf");
                }
                view.setTypeface(cached_roboto_bold_typeface);
                break;
            }
            case ROBOTO_REGULAR:
            default: {
                if (cached_roboto_regular_typeface == null) {
                    cached_roboto_regular_typeface = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/RobotoRegular.ttf");
                }
                view.setTypeface(cached_roboto_regular_typeface);
                break;
            }
        }
    }
}
