package ru.ppr.cppk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.cppk.entity.AuthResult;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.entity.log.Message;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.utils.AuthCardChecker;
import ru.ppr.cppk.utils.CppkUtils;
import ru.ppr.cppk.utils.ErrorFactory;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.RoleDvc;
import ru.ppr.security.entity.SecuritySettings;
import rx.android.schedulers.AndroidSchedulers;

public class EnterPinActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(EnterPinActivity.class);

    // EXTRAS
    private static final String EXTRA_AUTH_CARD = "EXTRA_AUTH_CARD";

    public static Intent getCallingIntent(Context context, AuthCard authCard) {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EXTRA_AUTH_CARD, authCard);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    private static final int DELAY_FOR_SHOW_KEYBOARD = 400;

    private int countAttempt = 3;

    private TextView attemptCount;
    private TextView errorMessage;
    private TextView emptyPin;
    private TextView emptyPinTryAgain;
    private View errorLayout;
    private EditText enterPin;
    private View validationProgress;

    private AuthCard authCard = null;

    int attemptCounter = 0;

    private enum ErrorType {
        PIN_IS_EMPTY,
        PIN_OK;

        public String getDescription(Context context) {
            String errorMessageString = null;
            switch (this) {
                case PIN_IS_EMPTY:
                    errorMessageString = context.getString(R.string.empty_pin);
                    break;

                case PIN_OK:
                    errorMessageString = context.getString(R.string.pin_ok);
                    break;

                default:
                    errorMessageString = context.getString(R.string.unknown_error);
                    break;
            }

            return errorMessageString;
        }

    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.enter_pin_layout);

        denyScreenLock();

        // На данном экране нет необходимости получать сообщения о истечении
        // времени закрытия смены
        resetRegisterReceiver();

        Bundle bundle = getIntent().getExtras();
        authCard = bundle.getParcelable(EXTRA_AUTH_CARD);
        findViews();

        Globals globals = (Globals) getApplication();
        SecuritySettings securitySettings = getSecurityDaoSession().getSettingDao().getSecuritySettings();
        countAttempt = securitySettings.getLimitLoginAttempts();
    }

    private void findViews() {
        attemptCount = (TextView) findViewById(R.id.enter_pint_count_attempt);
        errorMessage = (TextView) findViewById(R.id.enter_pin_error_message);
        emptyPinTryAgain = (TextView) findViewById(R.id.enter_pin_is_empty_try_again);
        errorLayout = findViewById(R.id.enter_pin_error_layout);
        validationProgress = findViewById(R.id.validationProgress);
        emptyPin = (TextView) findViewById(R.id.enter_pin_is_empty);
        enterPin = (EditText) findViewById(R.id.pin_enter);
        enterPin.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                checkPin();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ограничим максимальную длину пин-кода
        // http://agile.srvdev.ru/browse/CPPKPP-33286
        // Не применять настройку DevicePincodeLength
        // int length = SecureDbOperations.getSecuritySettings(getSecurityDaoSession().getSecurityDb()).getDevicePincodeLength();
        // Ограничить сверху длину пинкода в 30 символов.
        int length = 30;
        enterPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
    }

    private void setupErrorView(ErrorType errorType, AuthResult result) {

        String message = errorType.getDescription(getApplicationContext());
        if (errorType == ErrorType.PIN_OK)
            message = result.getDescription(getApplicationContext());

        errorMessage.setText(message);

        switch (errorType) {
            case PIN_OK:
                errorLayout.setVisibility(View.VISIBLE);
                emptyPin.setVisibility(View.GONE);
                emptyPinTryAgain.setVisibility(View.GONE);
                attemptCount.setVisibility(result.isReEnterPinEnabled() ? View.VISIBLE : View.GONE);
                errorMessage.setVisibility(View.VISIBLE);
                break;

            case PIN_IS_EMPTY:
                errorLayout.setVisibility(View.VISIBLE);
                emptyPin.setVisibility(View.VISIBLE);
                emptyPinTryAgain.setVisibility(View.VISIBLE);
                attemptCount.setVisibility(View.GONE);
                errorMessage.setVisibility(View.GONE);
                break;

            default:
                break;
        }

        if (errorType != ErrorType.PIN_OK || result.isReEnterPinEnabled()) {
            enterPin.setEnabled(true);
            showKeyboard();
        }

        if (errorType == ErrorType.PIN_OK) {

            if (result.isReEnterPinEnabled()) {
                attemptCounter++;
                if (attemptCounter == countAttempt) {
                    SharedPreferencesUtils.setTimeLockedAccess(getApplicationContext(), new Date(System.currentTimeMillis()));
                    Navigator.navigateToSplashActivity(EnterPinActivity.this, false);
                } else {
                    attemptCount.setText(getString(R.string.attempt_count) + String.valueOf(countAttempt - attemptCounter));
                    enterPin.setText("");
                }
            } else {
                enterPin.setEnabled(false);
            }
        }


    }

    /**
     * Открывает клавиатуру с фокусом на поле с вводом пина
     */
    private void showKeyboard() {
        if (enterPin != null) {
            enterPin.postDelayed(() -> {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(enterPin, 0);
            }, DELAY_FOR_SHOW_KEYBOARD);
        }
    }

    private void checkPin() {

        String pin = enterPin.getText().toString();

        if (pin.isEmpty() || !pin.matches("^[0-9]+$")) {
            setupErrorView(ErrorType.PIN_IS_EMPTY, AuthResult.ECP_ERROR);
            return;
        }

        if (BuildConfig.DEBUG && pin.equals("1684")) {
            Logger.info(TAG, "Авторизация под ROOT!!!");
            checkAuthDataFinish(AuthResult.SUCCESS, new LocalUser("Иванов И. И.", RoleDvc.getRootRole(), "root", null), pin);
            return;
        }

        validationProgress.setVisibility(View.VISIBLE);

        AuthCardChecker checker = new AuthCardChecker(pin, authCard,
                Dagger.appComponent().edsManager(),
                Dagger.appComponent().securityDaoSession(),
                Dagger.appComponent().nsiVersionManager(),
                Dagger.appComponent().productionSectionRepository(),
                Dagger.appComponent().fioFormatter());
        checker.authCheckRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objects -> {

                    AuthResult result = objects.result;
                    LocalUser localUser = new LocalUser();

                    localUser.setName(objects.fio);
                    localUser.setRole(objects.roleDvc);
                    localUser.setLogin(objects.login);

                    if (authCard != null && authCard.getBscInformation() != null) {
                        if (authCard.getCardUID() != null) {
                            localUser.setCardUid(CppkUtils.convertCardUIDToStopListNumber(authCard.getCardUID()));
                        }
                    }
                    validationProgress.setVisibility(View.GONE);
                    if (result == AuthResult.SUCCESS) {
                        SharedPreferencesUtils.setTimeLockedAccess(getApplicationContext(), new Date(0));
                    } else if (result == AuthResult.CARD_NOT_ACTIVE) {
                        SharedPreferencesUtils.setTimeLockedAccess(getApplicationContext(), new Date(0));
                    }
                    checkAuthDataFinish(result, localUser, pin);

                });
    }

    private void checkAuthDataFinish(AuthResult result, LocalUser user, String pin) {

        Logger.info(getClass(), "checkAuthDataFinish(" + result + ") START");

        String message = ErrorFactory.getAuthError(getApplicationContext(), result);

        //добавим событие авторизации
        LogEvent logEventAuth = Dagger.appComponent().logEventBuilder()
                .setLogActionType(result == AuthResult.SUCCESS ? LogActionType.AUTH_SUCCESS : LogActionType.AUTH_ERROR)
                .setMessage(message)
                .setUser(user)
                .build();

        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventAuth);

        if (result == AuthResult.SUCCESS) {

            //прихраним текущего юзера
            di().getUserSessionInfo().setCurrentUser(user);

            //созраним событие авторизации в обычном режиме
            if (user.getRole() != null) {
                LogEvent logEventStandard = Dagger.appComponent().logEventBuilder()
                        .setLogActionType(LogActionType.STANDARD_MODE_ON)
                        .setMessage(Message.AUTH_AS_ + user.getRole().getName())
                        .build();
                Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventStandard);
            }
            di().getUserSessionInfo().setCurrentUserPin(pin);
            authOk();
        } else {
            setupErrorView(ErrorType.PIN_OK, result);
        }
    }

    /**
     * Отправляет пользователя на экран с информацией о его имени и роли
     */
    private void authOk() {
        validationProgress.setVisibility(View.GONE);
        Navigator.navigateToWelcomeActivity(this, false);
        finish();
    }

    @Override
    /** Переопределяем обработчик чтобы запретить переход на главное меню по нажатию на кнопку Settings*/
    public void onClickSettings() {
    }

    @Override
    public void onBackPressed() {
        Navigator.navigateToSplashActivity(this, false);
    }
}
