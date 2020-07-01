package ru.ppr.cppk;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.EnumSet;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.lifecycle.LifecycleDelegate;
import ru.ppr.cppk.helpers.lifecycle.LifecycleEvent;
import ru.ppr.cppk.helpers.lifecycle.LifecycleListener;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;

/**
 * Базовый класс для всех фрагментов.
 */
public class FragmentParent extends Fragment {

    /**
     * Провайдер жизненного цикла
     */
    private LifecycleDelegate mLifecycleDelegate = new LifecycleDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.info(getClass(), "onCreate()");
        mLifecycleDelegate.onCreate();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleDelegate.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.info(getClass(), "onResume()");
        mLifecycleDelegate.onResume();
    }

    @Override
    public void onPause() {
        Logger.info(getClass(), "onPause()");
        mLifecycleDelegate.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mLifecycleDelegate.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Logger.info(getClass(), "onDestroy()");
        mLifecycleDelegate.onDestroy();
        super.onDestroy();
    }

    public void addLifecycleListener(LifecycleListener lifecycleListener, EnumSet<LifecycleEvent> lifecycleEvents) {
        mLifecycleDelegate.addLifecycleListener(lifecycleListener, lifecycleEvents);
    }

    public void removeLifecycleListener(LifecycleListener lifecycleListener, EnumSet<LifecycleEvent> lifecycleEvents) {
        mLifecycleDelegate.removeLifecycleListener(lifecycleListener, lifecycleEvents);
    }

    protected Di di() {
        return Di.INSTANCE;
    }

    protected LocalDaoSession getLocalDaoSession() {
        return Globals.getInstance().getLocalDaoSession();
    }

    protected NsiDaoSession getNsiDaoSession() {
        return Globals.getInstance().getNsiDaoSession();
    }

    protected SecurityDaoSession getSecurityDaoSession() {
        return Globals.getInstance().getSecurityDaoSession();
    }


    protected void denyScreenLock() {
        ((SystemBarActivity) getActivity()).denyScreenLock();
    }

    protected void allowScreenLock() {
        ((SystemBarActivity) getActivity()).allowScreenLock();
    }

    /**
     * Метод для проверки прав у авторизированного пользователя.
     *
     * @param permission разрешение для проверки.
     * @return результат проверки.
     * @see PermissionDvc
     */
    protected boolean hasPermission(@NonNull final PermissionDvc permission) {
        final RoleDvc role = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole();
        return getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, permission);
    }

}
