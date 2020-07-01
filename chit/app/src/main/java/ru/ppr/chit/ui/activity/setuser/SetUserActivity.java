package ru.ppr.chit.ui.activity.setuser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.helpers.AppDialogHelper;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;

/**
 * Экран установки пользователя (проводника) для обслуживания поезда
 *
 * @author Dmitry Nevolin
 */
public class SetUserActivity extends MvpActivity implements SetUserView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SetUserActivity.class);
    }

    // region Di
    private SetUserComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private EditText userName;
    // endregion
    // region Other
    private SetUserPresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerSetUserComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::setUserPresenter, SetUserPresenter.class);

        setContentView(R.layout.activity_set_user);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        userName = (EditText) findViewById(R.id.user_name);
        userName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.onUserNameProvided(userName.getText().toString());
                return true;
            }
            return false;
        });

        findViewById(R.id.done).setOnClickListener(v -> presenter.onUserNameProvided(userName.getText().toString()));
        // Показываем клавиатуру
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        presenter.setNavigator(setUserNavigator);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    protected void onPause() {
        // Скрываем клавиатуру
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(userName.getWindowToken(), 0);
        }
        super.onPause();
    }

    private final SetUserPresenter.Navigator setUserNavigator = new SetUserPresenter.Navigator() {

        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }

        @Override
        public void navigateBackToMain() {
            navigator.navigateToMain(true);
        }

        @Override
        public void navigateForwardToMain() {
            navigator.navigateToMain(false);
        }

    };

    @Override
    public void setUserNameEmptyErrorVisible(boolean visible) {
        if (visible) {
            // В будущем убрать, временное решение, эта обработка должна происходить на экране чтения QR-кода
            Toast.makeText(this, "Имя проводника не должно быть пустым!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showError(String message){
        AppDialogHelper.showError(this, message);
    }

}
