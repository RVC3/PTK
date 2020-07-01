package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Arrays;

import ru.ppr.cppk.R;
import ru.ppr.logger.Logger;

/**
 * Created by Александр on 22.12.2015.
 */
public class SelectStationMask extends LinearLayout {

    private static final String TAG = Logger.makeLogTag(SelectStationMask.class);

    private ImageView maskForTextView;
    private ImageView bottomMask;
    private int idCurrentView = -1;

    private Bitmap bottomBitmapMask;
    private Bitmap topBitmapMask;

    public SelectStationMask(Context context) {
        super(context);
        init(context);
    }

    public SelectStationMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectStationMask(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        inflate(getContext(), R.layout.view_select_station_mask, this);

        maskForTextView = (ImageView) findViewById(R.id.sell_pd_mask);
        bottomMask = (ImageView) findViewById(R.id.sell_pd_mask_bottom);
        bottomMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {/* NOP */
            }
        });
    }

    public void clearResources() {
        if (topBitmapMask != null) {
            topBitmapMask.recycle();
            topBitmapMask = null;
        }
        maskForTextView.setImageDrawable(null);

        if (bottomBitmapMask != null) {
            bottomBitmapMask.recycle();
            bottomBitmapMask = null;
        }
        bottomMask.setImageDrawable(null);
        idCurrentView = -1;
    }

    public void showMask(boolean isShow, View parentView) {
        if (isShow) {
            Logger.info(TAG, "View with id - " + parentView.getId() + " now has focus - " + isShow);

            drawMask(parentView);
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    public boolean isMaskShown() {
        return getVisibility() == View.VISIBLE;
    }


    private void drawMask(View parentView) {

        if (parentView.getId() != idCurrentView) {
            maskForTextView.setImageResource(0);

            int totalWidth = getWidth();
            int totalHeight = getHeight();

            Logger.info(TAG, "ParentViewId - " + parentView.getId());

            Rect margins = getTotalMargins(parentView);

            Bitmap topBitmap = createTopBitmap(
                    totalWidth,
                    parentView.getHeight() + margins.top,
                    parentView.getLeft() + margins.left,
                    margins.top,
                    parentView.getWidth(),
                    parentView.getHeight());

            maskForTextView.setImageBitmap(topBitmap);
            idCurrentView = parentView.getId();
            if (bottomMask.getDrawable() == null) {
                Bitmap bottomBitmap = createBottomBitmap(totalWidth, totalHeight - parentView.getHeight());
                bottomMask.setImageBitmap(bottomBitmap);
            }
        }

        if (getVisibility() != View.VISIBLE)
            setVisibility(View.VISIBLE);
    }

    /**
     * @param width             длина битмапа маски
     * @param height            высота битмапа маски
     * @param x                 координата прозрачной облости
     * @param y                 координата прозрачной области
     * @param widthTransparent  длина прозрачной области
     * @param heightTransparent высота прозрачной области
     * @return
     */
    private Bitmap createTopBitmap(int width, int height, int x, int y, int widthTransparent, int heightTransparent) {

        Bitmap locaBitmap = topBitmapMask;
        if (locaBitmap != null) {
            locaBitmap.recycle();
            locaBitmap = null;
        }

        locaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        locaBitmap.eraseColor(getResources().getColor(R.color.blue_transparent));
        int[] transparentColor = new int[widthTransparent * heightTransparent];
        Arrays.fill(transparentColor, 0, transparentColor.length, Color.TRANSPARENT);
        locaBitmap.setPixels(transparentColor, 0, widthTransparent, x, y, widthTransparent, heightTransparent);
        topBitmapMask = locaBitmap;

        return locaBitmap;
    }

    private Bitmap createBottomBitmap(int width, int height) {

        Bitmap localBitmap = bottomBitmapMask;

        if (localBitmap == null) {
            localBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            localBitmap.eraseColor(getResources().getColor(R.color.blue_transparent));
            bottomBitmapMask = localBitmap;
        }

        return localBitmap;
    }

    private Rect getTotalMargins(View view) {
        Rect margins = new Rect();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        margins.left += params.leftMargin;
        margins.right += params.rightMargin;
        margins.top += params.topMargin;
        margins.bottom += params.bottomMargin;
        View parentView = (View) view.getParent();
        if (parentView != view.getRootView()) {
            Rect parentMargins = getTotalMargins(parentView);
            margins.left += parentMargins.left;
            margins.right += parentMargins.right;
            margins.top += parentMargins.top;
            margins.bottom += parentMargins.bottom;
        }
        return margins;
    }
}
