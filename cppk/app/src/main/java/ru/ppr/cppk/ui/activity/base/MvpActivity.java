package ru.ppr.cppk.ui.activity.base;

import android.os.Bundle;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Базовая активити с нормальной реализацией биндинга {@link MvpView}.
 * Унаследована от {@link SystemBarActivity} для обратной совместимости.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class MvpActivity extends SystemBarActivity {

    /**
     * Делегат для хранения/получения тега
     */
    private MvpDelegate mvpDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), (MvpView) this);
        mvpDelegate.init(savedInstanceState);
    }

    public MvpDelegate getMvpDelegate() {
        return mvpDelegate;
    }

    @Override
    public void onStart() {
        super.onStart();
        mvpDelegate.bindView();
    }

    @Override
    public void onStop() {
        mvpDelegate.unbindView();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mvpDelegate.saveState(outState);
    }

    @Override
    public void onDestroy() {
        mvpDelegate.destroy(keepAlive());
        super.onDestroy();
    }

    protected boolean keepAlive() {
        return !isFinishing();
    }
}
