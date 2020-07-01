package ru.ppr.cppk.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

import ru.ppr.cppk.R;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.SecuritySettings;

/**
 * Экран блокировки
 * Created by Александр on 05.10.2016.
 */
public class LockScreenActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(LockScreenActivity.class);

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, LockScreenActivity.class);
        return intent;
    }

    /////////////////////////////
    // Views
    /////////////////////////////
    private EditText pinView;
    private View errorView;
    private TextView errorMsgView;
    private Button changeUserBtn;
    private TextView toolbar;
    /////////////////////////////
    // Other
    /////////////////////////////
    private UserSessionInfo userSessionInfo;
    private SecuritySettings securitySettings;
    private int availableAttemptCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        denyScreenLock();

        pinView = (EditText) findViewById(R.id.pinView);
        errorView = findViewById(R.id.errorView);
        errorMsgView = (TextView) findViewById(R.id.errorMsgView);
        changeUserBtn = (Button) findViewById(R.id.changeUserBtn);
        toolbar = (TextView) findViewById(R.id.toolbar);

        pinView.setOnClickListener(v -> {
            if (errorView.getVisibility() == View.VISIBLE) {
                // Очищаем поле только после ошибки
                pinView.setText("");
            }
            hideError();
        });

        pinView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (checkPin()) {
                    Logger.trace(TAG, "Successful screen unlock");
                    //http://agile.srvdev.ru/browse/CPPKPP-43557
                    //обновим таймер, т.к. на ПТК тапы по кнопкам клавиатуры не регистрируются по dispatchKeyEvent
                    updateLastActionTimestamp();
                    finish();
                }
            }
            return false;
        });

        changeUserBtn.setOnClickListener(v -> {
            Navigator.navigateToSplashActivity(this, false);
            finish();
        });

        userSessionInfo = di().getUserSessionInfo();
        toolbar.setText(userSessionInfo.getCurrentUser() == null ? null : userSessionInfo.getCurrentUser().getName());

        securitySettings = getSecurityDaoSession().getSettingDao().getSecuritySettings();
        availableAttemptCount = securitySettings.getLimitLoginAttempts();
        // http://agile.srvdev.ru/browse/CPPKPP-33286
        // Не применять настройку DevicePincodeLength
        // int length = securitySettings.getDevicePincodeLength();
        // Ограничить сверху длину пинкода в 30 символов.
        int length = 30;
        pinView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});

    }

    private void showError(String msg) {
        errorMsgView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorView.setVisibility(View.GONE);
    }

    private boolean checkPin() {

        String pin = pinView.getText().toString();

        if (!pin.matches("^[0-9]+$")) {
            showError(getString(R.string.lock_screen_msg_error_empty_pin));
            return false;
        }

        String storedPin = userSessionInfo.getCurrentUserPin();
        if (!TextUtils.equals(pin, storedPin)) {
            if (--availableAttemptCount <= 0) {
                SharedPreferencesUtils.setTimeLockedAccess(getApplicationContext(), new Date(System.currentTimeMillis()));
                Navigator.navigateToSplashActivity(LockScreenActivity.this, false);
                finish();
            } else {
                showError(getString(R.string.lock_screen_msg_error_invalid_pin, availableAttemptCount));
            }
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        /* NOP*/
    }
}
