package ru.ppr.chit.ui.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
public abstract class BaseActivity extends AppCompatActivity {

    private final static String TAG = Logger.makeLogTag(BaseActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.trace(TAG, "onCreate: " + this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.trace(TAG, "onNewIntent: " + this);
    }

    @Override
    protected void onStart() {
        Logger.trace(TAG, "onStart: " + this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        Logger.trace(TAG, "onResume: " + this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.trace(TAG, "onPause: " + this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.trace(TAG, "onStop: " + this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.trace(TAG, "onDestroy: " + this);
        super.onDestroy();
    }

}
