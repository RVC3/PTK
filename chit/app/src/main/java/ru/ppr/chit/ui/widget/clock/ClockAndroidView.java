package ru.ppr.chit.ui.widget.clock;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает информацию о текущем времени
 *
 * @author Dmitry Nevolin
 */
public class ClockAndroidView extends FrameLayout implements ClockView {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    // region Di
    private MvpDelegate mvpDelegate;
    private ClockComponent component;
    private ClockPresenter presenter;
    // endregion
    // region Views
    private TextView date;
    // endregion

    public ClockAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public ClockAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ClockAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ClockAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_clock, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerClockComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        date = (TextView) findViewById(R.id.c_date);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::clockPresenter, ClockPresenter.class);
        presenter.initialize();
    }

    @Override
    public void setDate(Date date) {
        this.date.setText(DATE_FORMAT.format(date));
    }

}
