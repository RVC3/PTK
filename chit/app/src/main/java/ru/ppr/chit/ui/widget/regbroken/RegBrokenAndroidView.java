package ru.ppr.chit.ui.widget.regbroken;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.helpers.AppDialogHelper;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает индикатор, указывающий что текущая регистрация на
 * базовой станции поломана, т.е. нельзя больше ничего отправить/получить
 * с текущими авторизационными даными на/с БС
 *
 * @author Dmitry Nevolin
 */
public class RegBrokenAndroidView extends FrameLayout implements RegBrokenView {

    private MvpDelegate mvpDelegate;
    private RegBrokenComponent component;
    private RegBrokenPresenter presenter;

    private View indicator;

    public RegBrokenAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public RegBrokenAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RegBrokenAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RegBrokenAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_reg_broken, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerRegBrokenComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        indicator = findViewById(R.id.rb_indicator);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::regBrokenPresenter, RegBrokenPresenter.class);
        presenter.initialize();
    }

    @Override
    public void setIndicatorVisible(boolean visible) {
        indicator.setVisibility(visible ? VISIBLE : GONE);
    }

    @Override
    public void showConnectionBrokenError(){
        AppDialogHelper.showError(getContext(), getContext().getString(R.string.reg_broken_error_title), getContext().getString(R.string.reg_broken_error_message));
    }

}
