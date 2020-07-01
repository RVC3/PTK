package ru.ppr.cppk.ui.fragment.base;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.di.Dagger;

/**
 * Базовый фрагмент с нормальной реализацией биндинга {@link MvpView}.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class MvpFragment extends FragmentParent {

    private MvpDelegate mvpDelegate;
    /**
     * Di
     */
    private MvpFragmentComponent component;
    /**
     * Флаг, что был вызван {@link #onSaveInstanceState(Bundle)}
     */
    private boolean mIsStateSaved;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component = DaggerMvpFragmentComponent.builder().appComponent(Dagger.appComponent()).build();
        mvpDelegate = new MvpDelegate(component.mvpProcessor(), (MvpView) this);
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
        mvpDelegate.destroy(keepAlive());
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
