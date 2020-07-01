package ru.ppr.core.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.ppr.core.ui.R;
import ru.ppr.logger.Logger;

/**
 * LseView - Loading, Success, Error
 * Умеет показывать нужную иконку, текст сообщения и от 0-я до 2-х кнопок
 *
 * @author Aleksandr Brazhkin
 */
public class SimpleLseView extends FrameLayout {

    private static final String TAG = Logger.makeLogTag(SimpleLseView.class);

    @State.Mode
    private int mode = State.MODE_UNKNOWN;

    private TextView simpleLseViewMessage;
    private Button simpleLseViewBtn1;
    private Button simpleLseViewBtn2;
    private Button simpleLseViewBtn3;
    private ImageView simpleLseViewImage;
    private ProgressBar simpleLseViewProgressBar;

    public SimpleLseView(Context context) {
        this(context, null, 0);
    }

    public SimpleLseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleLseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.view_simple_lse, this);

        simpleLseViewMessage = (TextView) findViewById(R.id.simpleLseViewMessage);
        simpleLseViewBtn1 = (Button) findViewById(R.id.simpleLseViewBtn1);
        simpleLseViewBtn2 = (Button) findViewById(R.id.simpleLseViewBtn2);
        simpleLseViewBtn3 = (Button) findViewById(R.id.simpleLseViewBtn3);
        simpleLseViewImage = (ImageView) findViewById(R.id.simpleLseViewImage);
        simpleLseViewProgressBar = (ProgressBar) findViewById(R.id.simpleLseViewProgressBar);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SimpleLseView,
                0, 0);

        State state = new State();

        int textMessageSize = getResources().getDimensionPixelSize(R.dimen.simple_lse_view_default_text_msg_size);

        try {
            String textMessage = a.getString(R.styleable.SimpleLseView_textMessage);
            String textButton1 = a.getString(R.styleable.SimpleLseView_textButton1);
            String textButton2 = a.getString(R.styleable.SimpleLseView_textButton2);
            String textButton3 = a.getString(R.styleable.SimpleLseView_textButton3);
            textMessageSize = a.getDimensionPixelSize(R.styleable.SimpleLseView_textMessageSize, textMessageSize);

            state.textMessage = textMessage;
            state.button1Text = textButton1;
            state.button2Text = textButton2;
            state.button3Text = textButton3;
        } finally {
            a.recycle();
        }

        simpleLseViewMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, textMessageSize);

        setState(state);
        hide();

    }

    private void setMode(@State.Mode int mode) {
        switch (mode) {
            case State.MODE_LOADING: {
                simpleLseViewImage.setVisibility(View.GONE);
                simpleLseViewProgressBar.setVisibility(View.VISIBLE);
                break;
            }
            case State.MODE_SUCCESS: {
                simpleLseViewImage.setVisibility(View.VISIBLE);
                simpleLseViewProgressBar.setVisibility(View.GONE);
                simpleLseViewImage.setImageResource(R.drawable.ic_lse_view_success);
                break;
            }
            case State.MODE_QUESTION: {
                simpleLseViewImage.setVisibility(View.VISIBLE);
                simpleLseViewProgressBar.setVisibility(View.GONE);
                simpleLseViewImage.setImageResource(R.drawable.ic_lse_view_question);
                break;
            }
            case State.MODE_ERROR: {
                simpleLseViewImage.setVisibility(View.VISIBLE);
                simpleLseViewProgressBar.setVisibility(View.GONE);
                simpleLseViewImage.setImageResource(R.drawable.ic_lse_view_error);
                break;
            }
            default: {
                simpleLseViewImage.setVisibility(View.GONE);
                simpleLseViewProgressBar.setVisibility(View.GONE);
            }
        }
    }

    public void setState(State state) {
        setMode(state.mode);
        simpleLseViewMessage.setText(state.textMessageRes == 0 ? state.textMessage : getContext().getString(state.textMessageRes));
        simpleLseViewBtn1.setVisibility(state.button1Text == null && state.button1TextRes == 0 ? View.GONE : View.VISIBLE);
        simpleLseViewBtn1.setText(state.button1TextRes == 0 ? state.button1Text : getContext().getString(state.button1TextRes));
        simpleLseViewBtn1.setOnClickListener(state.button1OnClickListener);
        simpleLseViewBtn2.setVisibility(state.button2Text == null && state.button2TextRes == 0 ? View.GONE : View.VISIBLE);
        simpleLseViewBtn2.setText(state.button2TextRes == 0 ? state.button2Text : getContext().getString(state.button2TextRes));
        simpleLseViewBtn2.setOnClickListener(state.button2OnClickListener);
        simpleLseViewBtn3.setVisibility(state.button3Text == null && state.button3TextRes == 0 ? View.GONE : View.VISIBLE);
        simpleLseViewBtn3.setText(state.button3TextRes == 0 ? state.button3Text : getContext().getString(state.button3TextRes));
        simpleLseViewBtn3.setOnClickListener(state.button3OnClickListener);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mode = mode;
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
        setMode(ss.mode);
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
        int mode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            mode = in.readInt();
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mode);
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

    public static class State {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({MODE_UNKNOWN,
                MODE_LOADING,
                MODE_SUCCESS,
                MODE_QUESTION,
                MODE_ERROR})
        public @interface Mode {
        }

        public static final int MODE_UNKNOWN = 0;
        public static final int MODE_LOADING = 1;
        public static final int MODE_SUCCESS = 2;
        public static final int MODE_QUESTION = 3;
        public static final int MODE_ERROR = 4;

        @Mode
        int mode;
        @StringRes
        int textMessageRes;
        CharSequence textMessage;
        @StringRes
        int button1TextRes;
        CharSequence button1Text;
        View.OnClickListener button1OnClickListener;
        @StringRes
        int button2TextRes;
        CharSequence button2Text;
        View.OnClickListener button2OnClickListener;
        @StringRes
        int button3TextRes;
        CharSequence button3Text;
        View.OnClickListener button3OnClickListener;

        State() {

        }

        public static class Builder {
            @Mode
            private int mode;
            @StringRes
            private int textMessageRes;
            private CharSequence textMessage;
            @StringRes
            private int button1TextRes;
            private CharSequence button1Text;
            private View.OnClickListener button1OnClickListener;
            @StringRes
            private int button2TextRes;
            private CharSequence button2Text;
            private View.OnClickListener button2OnClickListener;
            @StringRes
            private int button3TextRes;
            private CharSequence button3Text;
            private View.OnClickListener button3OnClickListener;

            public Builder() {

            }

            public Builder setMode(int mode) {
                this.mode = mode;
                return this;
            }

            public Builder setTextMessage(@StringRes int resId) {
                textMessageRes = resId;
                return this;
            }

            public Builder setTextMessage(String textMessage) {
                this.textMessage = textMessage;
                return this;
            }

            public Builder setButton1(@StringRes int resId, OnClickListener onClickListener) {
                button1TextRes = resId;
                button1OnClickListener = onClickListener;
                return this;
            }

            public Builder setButton1(CharSequence text, OnClickListener onClickListener) {
                button1Text = text;
                button1OnClickListener = onClickListener;
                return this;
            }

            public Builder setButton2(@StringRes int resId, OnClickListener onClickListener) {
                button2TextRes = resId;
                button2OnClickListener = onClickListener;
                return this;
            }

            public Builder setButton2(CharSequence text, OnClickListener onClickListener) {
                button2Text = text;
                button2OnClickListener = onClickListener;
                return this;
            }

            public Builder setButton3(@StringRes int resId, OnClickListener onClickListener) {
                button3TextRes = resId;
                button3OnClickListener = onClickListener;
                return this;
            }

            public Builder setButton3(CharSequence text, OnClickListener onClickListener) {
                button3Text = text;
                button3OnClickListener = onClickListener;
                return this;
            }

            public State build() {
                State state = new State();
                state.mode = mode;
                state.textMessageRes = textMessageRes;
                state.textMessage = textMessage;
                state.button1TextRes = button1TextRes;
                state.button1Text = button1Text;
                state.button1OnClickListener = button1OnClickListener;
                state.button2TextRes = button2TextRes;
                state.button2Text = button2Text;
                state.button2OnClickListener = button2OnClickListener;
                state.button3TextRes = button3TextRes;
                state.button3Text = button3Text;
                state.button3OnClickListener = button3OnClickListener;
                return state;
            }
        }
    }
}
