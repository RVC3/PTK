package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.ppr.cppk.R;

/**
 * Обертка над {@link AutoCompleteTextView} с добавлением гамбургера.
 */
public class SmartAutoCompleteTextView extends RelativeLayout {

    public static final int LEFT_CORNERS = 1;
    public static final int RIGHT_CORNERS = 2;
    public static final int FULL_CORNERS = 3;
    private static final int TOP_CORNERS = 4; // используется только здесь, снаружи не должен быть виден

    private static final int DEFAULT_TITLE_TEXT_SIZE = 16;

    private CharSequence title;
    private float titleTextSize;
    private int heightForEditText;
    private String hint;

    private TextView titleTextView;
    private AutoCompleteTextViewWithBack autoCompleteTextView;
    private View hamburgerView;
    private int cornersToDrawStyle;

    private OnKeyListener onKeyListener;
    private AutoCompleteTextViewWithBack.OnBackListener onBackListener;
    private TextView.OnEditorActionListener onEditorActionListener;
    private OnFocusChangeListener focusChangeListener;
    private OnClickListener hamburgerClickListener;
    private OnItemClickListener onItemClickListener;
    private OnClickListener onClickListener;

    public SmartAutoCompleteTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray array = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.SmartAutocompleateTextView, 0, 0);
        try {
            getAttribute(array);
            View.inflate(context, R.layout.smart_autocompleate_textview, this);
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
        title = array.getString(R.styleable.SmartAutocompleateTextView_top_title);
        titleTextSize = array.getDimensionPixelSize(R.styleable.SmartAutocompleateTextView_title_text_size, DEFAULT_TITLE_TEXT_SIZE);
        cornersToDrawStyle = array.getInt(R.styleable.SmartAutocompleateTextView_corners_to_draw, FULL_CORNERS);
        heightForEditText = array.getDimensionPixelSize(R.styleable.SmartAutocompleateTextView_height_for_edit_text, ViewGroup.LayoutParams.WRAP_CONTENT);
        hint = array.getString(R.styleable.SmartAutocompleateTextView_hint);
    }

    private void setupView() {
        titleTextView = (TextView) findViewById(R.id.smart_autocompleate_text_view_title);
        if (title != null) {
            titleTextView.setText(title);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        }
        autoCompleteTextView = (AutoCompleteTextViewWithBack) findViewById(R.id.smart_autocompleate_textview);
        autoCompleteTextView.setHint(hint);
        autoCompleteTextView.setOnItemClickListener(localItemClickListener);
        autoCompleteTextView.setOnKeyListener(localKeyListener);
        autoCompleteTextView.setOnEditorActionListener(localEditorListener);
        autoCompleteTextView.setOnBackListener(localOnBackListener);
        autoCompleteTextView.setOnClickListener(localClickListener);
        autoCompleteTextView.setOnFocusChangeListener(localFocusChangeListener);
//        autoCompleteTextView.setSaveFromParentEnabled(false);

        hamburgerView = findViewById(R.id.smart_autocompleate_text_view_img);
        hamburgerView.setOnClickListener(localHamburgerClickListener);
        setStyleForTextView(cornersToDrawStyle);
        setHeightForEditText(heightForEditText);
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (autoCompleteTextView != null) {
            autoCompleteTextView.setAdapter(adapter);
        }
    }

    public
    @Nullable
    ListAdapter getAdapter() {
        ListAdapter adapter = null;
        if (autoCompleteTextView != null) {
            adapter = autoCompleteTextView.getAdapter();
        }
        return adapter;
    }

    public void setThreshold(int threshold) {
        autoCompleteTextView.setThreshold(threshold);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        autoCompleteTextView.addTextChangedListener(watcher);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        onItemClickListener = l;
    }

    public void setOnKeyListener(OnKeyListener l) {
        onKeyListener = l;
    }

    public void setOnBackListener(AutoCompleteTextViewWithBack.OnBackListener onBackListener) {
        this.onBackListener = onBackListener;
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        this.onEditorActionListener = onEditorActionListener;
    }

    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    public void setText(CharSequence text) {
        autoCompleteTextView.setText(text);
    }

    public EditText getTextView() {
        return autoCompleteTextView;
    }

    public void showDropDown() {
        autoCompleteTextView.showDropDown();
    }

    public void hideDropDown() {
        autoCompleteTextView.dismissDropDown();
    }

    public Editable getText() {
        return autoCompleteTextView.getText();
    }

    public void setHint(String text) {
        autoCompleteTextView.setHint(text);
    }

    public void setHint(int resId) {
        autoCompleteTextView.setHint(resId);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        focusChangeListener = l;
    }

    public void showTopCorners(boolean isShow) {

        if (cornersToDrawStyle != FULL_CORNERS) {
            if (isShow) {
                setStyleForTextView(TOP_CORNERS);
            } else {
                setStyleForTextView(cornersToDrawStyle);
            }
        }
    }

    /**
     * Устанавливает углы, которые будут отображаться у текствью
     *
     * @param type
     */
    public void setStyleForTextView(int type) {

        switch (type) {
            case LEFT_CORNERS:
                setTextViewStyle(R.drawable.left_corners);
                cornersToDrawStyle = type;
                break;

            case RIGHT_CORNERS:
                setTextViewStyle(R.drawable.right_corners);
                cornersToDrawStyle = type;
                break;

            case FULL_CORNERS:
                setTextViewStyle(R.drawable.bg_white_with_corners_5dp);
                cornersToDrawStyle = type;
                break;

            case TOP_CORNERS:
                //здесь не перезаписываем текущий стиль углов, т.к. верхние углы должны быть активны только
                //когда поле в фокусе, после потери фокуса углы должны установиться такими, как были до этого
                setTextViewStyle(R.drawable.top_corners);
                break;

            default:
                break;
        }
    }

    /**
     * Возвращает стиль нарисованных углов
     *
     * @return
     */
    public int getStyleForTextView() {
        return cornersToDrawStyle;
    }

    /**
     * Устанавливает бакграунд стиль для edit text
     *
     * @param drawableId
     */
    private void setTextViewStyle(int drawableId) {
        autoCompleteTextView.setBackgroundResource(drawableId);
    }

    public void setHeightForEditText(int dimension) {
        autoCompleteTextView.setHeight(dimension);
    }

    public void setSelection(int index) {
        getTextView().setSelection(index);
    }

    /**
     * Устанавливает слушателя для "гамбургера"
     *
     * @param listener
     */
    public void setOnHamburgerClickListener(OnClickListener listener) {
        hamburgerClickListener = listener;
    }


    private OnItemClickListener localItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(parent, view, position, id);
            }
        }
    };

    private OnFocusChangeListener localFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (focusChangeListener != null) {
                focusChangeListener.onFocusChange(SmartAutoCompleteTextView.this, hasFocus);
            }
        }
    };

    private OnClickListener localHamburgerClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (hamburgerClickListener != null) {
                hamburgerClickListener.onClick(SmartAutoCompleteTextView.this);
            }
        }
    };

    private OnClickListener localClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onClick(SmartAutoCompleteTextView.this);
            }
        }
    };

    private TextView.OnEditorActionListener localEditorListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return (onEditorActionListener != null)
                    && onEditorActionListener.onEditorAction(v, actionId, event);
        }
    };


    private AutoCompleteTextViewWithBack.OnBackListener localOnBackListener = new AutoCompleteTextViewWithBack.OnBackListener() {
        @Override
        public boolean onBackPressed() {
            return onBackListener != null && onBackListener.onBackPressed();
        }
    };

    private OnKeyListener localKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            return onKeyListener != null
                    && onKeyListener.onKey(SmartAutoCompleteTextView.this, keyCode, event);

        }
    };

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState bundle = new SavedState(parcelable);
        final int childCount = getChildCount();
        bundle.childrenState = new SparseArray<>(childCount);
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).saveHierarchyState(bundle.childrenState);
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).restoreHierarchyState(savedState.childrenState);
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

        SparseArray childrenState;

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private SavedState(Parcel source, ClassLoader loader) {
            super(source);
            childrenState = source.readSparseArray(loader);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSparseArray(childrenState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hamburgerView.setEnabled(enabled);
        autoCompleteTextView.setEnabled(enabled);
    }
}
