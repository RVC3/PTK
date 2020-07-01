package ru.ppr.cppk.ui.fragment.base;

import android.app.Fragment;
import android.os.Bundle;

import ru.ppr.core.ui.mvp.LegacyMvpDelegate;
import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.di.Dagger;

/**
 * Базовый фрагмент с нормальной реализацией биндинга {@link MvpView}.
 * Унаследована от {@link FragmentParent} для обратной совместимости.
 *
 * @author Aleksandr Brazhkin
 * @deprecated Use {@link MvpFragment} instead
 */
@Deprecated
public abstract class LegacyMvpFragment extends FragmentParent {

    private LegacyMvpDelegate mvpDelegate;
    /**
     * Флаг, что был вызван {@link #onSaveInstanceState(Bundle)}
     */
    private boolean mIsStateSaved;
    /**
     * Ссылка на родительский MVP delegate
     */
    private MvpDelegate parentMvpDelegate;

    public void init(MvpDelegate parent, String id) {
        mvpDelegate = new LegacyMvpDelegate(Dagger.appComponent().mvpProcessor(), (MvpView) this);
        mvpDelegate.init(parent, id);
        parentMvpDelegate = parent;
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
    public void onResume() {
        super.onResume();
        mIsStateSaved = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mvpDelegate.saveState(outState);
        mIsStateSaved = true;
    }

    @Override
    public void onStop() {
        mvpDelegate.unbindView();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        boolean keepAlive = keepAlive();
        mvpDelegate.destroy(keepAlive);
        if (!keepAlive) {
            // http://agile.srvdev.ru/browse/CPPKPP-41619
            // Удаляем mvpDelegate из списка дочерних в родителе
            if (parentMvpDelegate != null) {
                parentMvpDelegate.removeChildDelegate(mvpDelegate);
            }
        }
        super.onDestroy();
    }

    protected boolean keepAlive() {
        boolean keepAlive = true;

        if (mIsStateSaved) {
            mIsStateSaved = false;
        } else {
            boolean anyParentIsRemoving = false;

            Fragment parent = getParentFragment();
            while (!anyParentIsRemoving && parent != null) {
                anyParentIsRemoving = parent.isRemoving();
                parent = parent.getParentFragment();
            }

            if (isRemoving() || anyParentIsRemoving || getActivity().isFinishing()) {
                keepAlive = false;
            }
        }
        return keepAlive;
    }
}
