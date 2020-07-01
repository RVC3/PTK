package ru.ppr.cppk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;

/**
 * Домашнаяя активити для лаунчера.
 * <p>
 * Зарегистрирована, как запускаемая по нажатию кнопки HOME.
 * Но, благодоря тому, что не испольузется флаг "singleTask",
 * данная активити не может запуститься по нажатию кнопки HOME.
 * Т.е. имеем ситауацию, что кнопка HOME заблокирована.
 * <p>
 * Если приложение установлено как лаунчер по умолчанию, автоматически запускается при включении устройства.
 *
 * @author Aleksandr Brazhkin
 */
public class LauncherHomeActivity extends Activity {

    private static final String TAG = Logger.makeLogTag(LauncherHomeActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.trace(TAG, "onCreate");
        Navigator.navigateToSplashActivity(this, false);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.trace(TAG, "onNewIntent");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.trace(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.trace(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.trace(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.trace(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.trace(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.trace(TAG, "onDestroy");
    }
}
