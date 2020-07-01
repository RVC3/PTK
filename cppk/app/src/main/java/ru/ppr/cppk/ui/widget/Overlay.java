package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Слой накладывается на экран, чтобы запретить тапы
 * @author Aleksandr Brazhkin
 */
public class Overlay extends View {

    private final Paint fillColorPaint;

    public Overlay(Context context) {
        this(context, null, 0);
    }

    public Overlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Overlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        fillColorPaint = new Paint();
        fillColorPaint.setColor(Color.parseColor("#550033FF"));
        fillColorPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(fillColorPaint);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return true;
//    }
}
