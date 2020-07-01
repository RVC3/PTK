package ru.ppr.chit.ui.activity.setdeviceid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;


/**
 * Экран установки deviceId.
 *
 * @author Aleksandr Brazhkin
 */
public class SetDeviceIdActivity extends MvpActivity implements SetDeviceIdView {

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, SetDeviceIdActivity.class);
    }

    // region Di
    private SetDeviceIdComponent component;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    ActivityNavigator navigator;
    // endregion
    // region Views
    private EditText deviceId;
    private TextView invalidData;
    private Button doneBtn;
    //endregion
    //region Other
    private SetDeviceIdPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerSetDeviceIdComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::setDeviceIdPresenter, SetDeviceIdPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_set_device_id);

        statusBarDelegate.init(getMvpDelegate());

        deviceId = (EditText) findViewById(R.id.deviceId);
        deviceId.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                presenter.onDoneBtnClicked();
                return true;
            }
            return false;
        });
        deviceId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.onDeviceIdTextChanged(s.toString());
            }
        });
        invalidData = (TextView) findViewById(R.id.invalidData);
        doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(v -> presenter.onDoneBtnClicked());
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(setDeviceIdNavigator);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    private final SetDeviceIdPresenter.Navigator setDeviceIdNavigator = new SetDeviceIdPresenter.Navigator() {
        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }

        @Override
        public void navigateToSplash() {
            navigator.navigateToSplash(false);
        }
    };

    @Override
    public void setInvalidDataErrorVisible(boolean visible) {
        invalidData.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
