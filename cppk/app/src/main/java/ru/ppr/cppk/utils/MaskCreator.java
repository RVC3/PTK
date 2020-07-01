package ru.ppr.cppk.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

/**
 * Класс для создания Bitmap маски, которая показывается при выборе станций во время продажи ПД.
 * <p/>
 * Иаска должна быть прорисованна над всеми элементами, кроме активного EditText. Для этого мы разбиваем ее на 2 части:
 * - верхняя;
 * - нижняя.
 * <p/>
 * Верхняя часть начинается от верхнего края экрана и до нижнего края EditText. Так же в ней должна быть дырка под активный EditText.
 * Нижняя часть - это сплошной треуглоьник от нижней части EditText до нижнего края экрана.
 *
 * @author Artem Ushakov
 */
public class MaskCreator {

    private final int totalWidth;
    private final int colorToMask;

    public MaskCreator(int totalWidth, int colorToMask) {
        this.totalWidth = totalWidth;
        this.colorToMask = colorToMask;
    }

    /**
     * Создает Bitmap для верхней части
     *
     * @param parentView view, вокруг которого рисуем маску
     * @return
     */
    public Bitmap createTopBitmap(View parentView) {

        ViewMargins margins = getTotalMargins(parentView);

        final int height = parentView.getHeight();
        final int width = parentView.getWidth();

        Bitmap locaBitmap = Bitmap.createBitmap(totalWidth, height + margins.top, Bitmap.Config.ARGB_8888);
        locaBitmap.eraseColor(colorToMask);
        int[] transparentColor = new int[width * height];
        Arrays.fill(transparentColor, 0, transparentColor.length, Color.TRANSPARENT);
        locaBitmap.setPixels(transparentColor, 0, width, parentView.getLeft() + margins.left, margins.top, width, height);

        return locaBitmap;
    }

    /**
     * Создает Bitmap для нижней части
     *
     * @param height     высота экрана
     * @param parentView вью, под которым рисуем маску
     * @return
     */
    public Bitmap createBottomBitmap(int height, View parentView) {

        Bitmap localBitmap = Bitmap.createBitmap(totalWidth, height - parentView.getHeight(), Bitmap.Config.ARGB_8888);
        localBitmap.eraseColor(colorToMask);

        return localBitmap;

    }

    private ViewMargins getTotalMargins(View view) {
        ViewMargins margins = new ViewMargins();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        margins.left += params.leftMargin;
        margins.right += params.rightMargin;
        margins.top += params.topMargin;
        margins.bottom += params.bottomMargin;
        View parentView = (View) view.getParent();
        if (parentView != view.getRootView()) {
            ViewMargins parentMargins = getTotalMargins(parentView);
            margins.left += parentMargins.left;
            margins.right += parentMargins.right;
            margins.top += parentMargins.top;
            margins.bottom += parentMargins.bottom;
        }
        return margins;
    }

    private class ViewMargins {
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
    }

}
