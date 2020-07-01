package ru.ppr.chit.ui.activity.rootaccess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;

/**
 * Экран доступа к рут-меню.
 *
 * @author Dmitry Nevolin
 */
public class RootAccessActivity extends MvpActivity implements RootAccessView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RootAccessActivity.class);
    }

    // region Di
    private RootAccessComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    // endregion
    // region Views
    private EditText rootPassword;
    private View error;
    // endregion
    // region Other
    private RootAccessPresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerRootAccessComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::rootAccessPresenter, RootAccessPresenter.class);

        setContentView(R.layout.activity_root_access);

        statusBarDelegate.init(getMvpDelegate());

        error = findViewById(R.id.error);

        rootPassword = (EditText) findViewById(R.id.root_password);
        rootPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.onRootPasswordProvided(rootPassword.getText().toString());
                return true;
            }
            return false;
        });

        findViewById(R.id.done).setOnClickListener(v -> presenter.onRootPasswordProvided(rootPassword.getText().toString()));
        // Показываем клавиатуру
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        presenter.setNavigator(rootAccessNavigator);
        presenter.initialize();
    }

    @Override
    protected void onPause() {
        // Скрываем клавиатуру
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(rootPassword.getWindowToken(), 0);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void setErrorVisible(boolean visible) {
        if (visible) {
            error.setVisibility(View.VISIBLE);
            rootPassword.setText("");
        } else {
            error.setVisibility(View.GONE);
        }
    }

    private final RootAccessPresenter.Navigator rootAccessNavigator = new RootAccessPresenter.Navigator() {

        @Override
        public void navigateToRoot() {
            navigator.navigateToRoot(true);
            finish();
        }

        @Override
        public void navigateToSplash() {
            navigator.navigateToSplash(true);
        }

    };
}
