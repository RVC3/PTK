package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.logger.Logger;

/**
 * Created by Александр on 05.09.2016.
 */
public class DateView extends FrameLayout {

    private static final String TAG = Logger.makeLogTag(SimpleLseView.class);

    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat();

    ////////////////////////////////////////////
    // Views
    ////////////////////////////////////////////
    private TextView textView;
    private ImageButton clearBtn;
    ////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////
    private Date date = null;
    private Date initialDate = null;
    private DateFormat dateFormat = DEFAULT_DATE_FORMAT;

    public DateView(Context context) {
        this(context, null, 0);
    }

    public DateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        FrameLayout.LayoutParams textViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        textView = new TextView(context, attrs, android.R.attr.editTextStyle);
        textView.setFocusable(false);
        textView.setFocusableInTouchMode(false);
        addView(textView, textViewLayoutParams);

        FrameLayout.LayoutParams clearBtnLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        clearBtnLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;

        clearBtn = new ImageButton(context, attrs, android.R.attr.imageButtonStyle);
        clearBtn.setImageResource(R.drawable.ic_clear);
        clearBtn.setBackground(null);
        clearBtn.setOnClickListener(v -> setDate(null));
        addView(clearBtn, clearBtnLayoutParams);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textView.setEnabled(enabled);
        clearBtn.setEnabled(enabled);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        textView.setOnClickListener(l);
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setDate(Date date) {
        this.date = date;
        if (date != null) {
            textView.setText(dateFormat.format(date));
        } else {
            textView.setText("");
        }
    }

    public Date getDate() {
        return date;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.dateFormat = dateFormat;
        ss.date = date;
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDateFormat(ss.dateFormat);
        setDate(ss.date);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;
        DateFormat dateFormat;
        Date date;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            dateFormat = (DateFormat) in.readSerializable();
            long dateLong = in.readLong();
            date  = dateLong == 0 ? null : new Date(dateLong);
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(dateFormat);
            out.writeLong(date == null ? 0 : date.getTime());
            out.writeSparseArray(childrenStates);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return null;
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
