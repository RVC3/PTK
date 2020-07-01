package ru.ppr.cppk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;

/**
 * Created by nevolin on 11.08.2016.
 * Окно ввода рутового пинкода
 */
public class RootAccessRequestActivity extends Activity {

    private static final String TAG = Logger.makeLogTag(RootAccessRequestActivity.class);

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RootAccessRequestActivity.class);
    }

    private String rootKey;

    private EditText password;
    private View passwordError;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        rootKey = buildRootKey();

        if (rootKey == null || rootKey.equals("0") || rootKey.length() < 6) {
            Logger.error(TAG,
                    "Ошибка при построении рут ключа: " + System.currentTimeMillis() +
                            " | " + BuildConfig.VERSION_NAME +
                            " | " + privateSettingsHolder.get().getTerminalNumber());
            Navigator.navigateToSplashActivity(this, false);

            return;
        }

        setContentView(R.layout.activity_root_access_request);

        password = (EditText) findViewById(R.id.password);
        password.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                checkPassword();

                return true;
            }

            return false;
        });

        passwordError = findViewById(R.id.password_error);
    }

    private void checkPassword() {
        //пароль - последние 6 символов рут ключа
        final String password = rootKey.substring(rootKey.length() - 6, rootKey.length());

        if (password.equals(this.password.getText().toString()) || (BuildConfig.DEBUG && TextUtils.equals("1684", this.password.getText()))) {
            LocalUser localUser = LocalUser.getRootUser();
            Di.INSTANCE.getUserSessionInfo().setCurrentUser(localUser);
            Navigator.navigateToDebugActivity(this, true);
            finish();
        } else {
            passwordError.setVisibility(View.VISIBLE);
        }
    }

    @Nullable
    private String buildRootKey() {
        try {
            final Date rawDate = new Date();
            final String rawVersionName = BuildConfig.VERSION_NAME;
            final long rawDeviceId = privateSettingsHolder.get().getTerminalNumber();
            final Integer rawFactor = 11;

            final Calendar calendar = Calendar.getInstance();

            calendar.setTime(rawDate);

            final int datePart = calendar.get(Calendar.DAY_OF_MONTH) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.YEAR) % 100;
            final int versionNamePart = Integer.valueOf(rawVersionName.substring(rawVersionName.lastIndexOf('.') + 1)) + 1;
            final long deviceIdPart = rawDeviceId + 1;
            final int factorPart = rawFactor;

            final BigDecimal rootKey = BigDecimal.valueOf(datePart)
                    .multiply(BigDecimal.valueOf(versionNamePart))
                    .multiply(BigDecimal.valueOf(deviceIdPart))
                    .multiply(BigDecimal.valueOf(factorPart));

            return rootKey.toPlainString();
        } catch (Throwable error) {
            Logger.error(TAG, error);

            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Navigator.navigateToSplashActivity(this, false);
    }

}
