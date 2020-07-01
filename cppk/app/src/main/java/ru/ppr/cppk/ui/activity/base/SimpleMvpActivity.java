package ru.ppr.cppk.ui.activity.base;

import android.os.Bundle;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.core.ui.mvp.presenter.MvpPresenter;
import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Базовая активити с простейшей реализацией биндинга {@link MvpView}.
 * Унаследована от {@link SystemBarActivity} для обратной совместимости.
 * Презентер создается в {@link #onCreate(Bundle)}, к нему сразу биндится Activity.
 * Отвязывание Activity от презентера происходит в {@link #onDestroy()}.
 * <p>
 * Если требуется чаще делать {@link MvpPresenter#bind(MvpView)}/{@link MvpPresenter#unbind(MvpView)},
 * то следует писать свою реализацию.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class SimpleMvpActivity extends SystemBarActivity {

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mvpDelegate.saveState(outState);
    }

    @Override
    public void onDestroy() {
        mvpDelegate.unbindView();
        mvpDelegate.destroy(keepAlive());
        super.onDestroy();
    }

    protected boolean keepAlive() {
        return !isFinishing();
    }
}
