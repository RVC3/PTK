package ru.ppr.cppk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;

public class SetUserIdActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(SetUserIdActivity.class);

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, SetUserIdActivity.class);
        return intent;
    }

    private EditText idEnterEditText;
    private LinearLayout errorLayout;
    private Globals globals;
    private long userId = -1;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.enter_user_id);

        resetRegisterReceiver();

        globals = (Globals) getApplication();

        errorLayout = (LinearLayout) findViewById(R.id.invalidUSerId);

        idEnterEditText = (EditText) findViewById(R.id.user_id_enter);

        idEnterEditText.setOnKeyListener(listener);
    }

    private OnKeyListener listener = (v, keyCode, event) -> {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            configViews();
            return true;
        }
        return false;
    };

    private boolean isUserIdValid() {
        if (idEnterEditText != null) {
            String userId = idEnterEditText.getText().toString();
            Logger.trace(TAG, "isUserIdValid() userId='" + userId + "'");
            userId = userId.trim();
            if (userId.matches("^[0-9]+$")) {
                try {
                    long longValue = Long.valueOf(userId);
                    if (longValue >> 32 != 0) {
                        return false;
                    }
                    this.userId = longValue;
                    return true;
                } catch (NumberFormatException e) {
                    Logger.error(TAG, e);
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private void configEditText(EditText et, boolean isValid) {
        if (isValid) {
            et.setBackground(getResources().getDrawable(R.drawable.pin_border_black_4px));
        } else {
            et.setBackground(getResources().getDrawable(R.drawable.pin_border_red_4px));
        }
    }

    private void configViews() {
        boolean isUserIdValid = isUserIdValid();

        if (isUserIdValid) {
            //сохраняем userId
            PrivateSettings cSettings = new PrivateSettings(privateSettingsHolder.get());
            cSettings.setTerminalNumber(userId);
            Dagger.appComponent().privateSettingsRepository().savePrivateSettings(cSettings);
            privateSettingsHolder.set(cSettings);

            //переположим в DI текущий девайс, поскольку там уже лежит с пустым номером.
            di().getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());
            //обновим EdsManager, т.к. теперь у нас есть userId для нее
            Dagger.appComponent().edsManagerConfigSyncronizer().sync();

            Navigator.navigateToSplashActivity(this, false);
            finish();
        } else {
            configEditText(idEnterEditText, false);
            showError();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showError() {
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    /** Переопределяем обработчик чтобы запретить переход на главное меню по нажатию на кнопку Settings*/
    public void onClickSettings() {
    }
}
