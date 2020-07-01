package ru.ppr.cppk.systembar;

import android.content.Intent;
import android.os.Bundle;

import ru.ppr.logger.Logger;

/**
 * Created by григорий on 08.09.2016.
 */
public class LoggedActivity extends FeedbackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.info(getClass(), "onCreate() " + this.toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.info(getClass(), "onNewIntent() " + this.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.info(getClass(), "onDestroy() " + this.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.info(getClass(), "onPause() " + this.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.info(getClass(), "onResume() " + this.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.info(getClass(), "onStop() " + this.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.info(getClass(), "onStart() " + this.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Logger.info(getClass(), "onBackPressed() " + this.toString());
    }
}
