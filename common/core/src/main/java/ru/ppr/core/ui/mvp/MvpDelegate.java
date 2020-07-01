package ru.ppr.core.ui.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.core.ui.mvp.core.PresenterProvider;
import ru.ppr.core.ui.mvp.presenter.MvpPresenter;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
public class MvpDelegate {

    private static final String MVP_DELEGATE_TAG = "MVP_DELEGATE_TAG";

    private final MvpProcessor mvpProcessor;
    private final Map<String, MvpDelegate> childDelegates = new HashMap<>();
    private final Map<MvpPresenter, String> presenters = new HashMap<>();
    private final MvpView view;
    private String delegateTag;

    public MvpDelegate(MvpProcessor mvpProcessor, MvpView view) {
        this.mvpProcessor = mvpProcessor;
        this.view = view;
    }

    public <P extends MvpPresenter> P getPresenter(@NonNull PresenterProvider<P> presenterProvider, String tag) {
        P presenter = mvpProcessor.getPresenter(presenterProvider, tag);
        presenters.put(presenter, tag);
        return presenter;
    }

    public <P extends MvpPresenter> P getPresenter(@NonNull PresenterProvider<P> presenterProvider, @NonNull Class<P> pClass) {
        String tag = delegateTag + "_" + pClass.getName();
        P presenter = mvpProcessor.getPresenter(presenterProvider, tag);
        presenters.put(presenter, tag);
        return presenter;
    }

    public void init(Bundle bundle) {
        if (bundle != null) {
            delegateTag = bundle.getString(MVP_DELEGATE_TAG);
        }
        if (delegateTag == null) {
            delegateTag = generateTag();
        }
    }

    public void init(MvpDelegate parent, String id) {
        delegateTag = parent.delegateTag + "$" + id;
        parent.childDelegates.put(delegateTag, this);
    }

    public void bindView() {
        for (MvpPresenter mvpPresenter : presenters.keySet()) {
            mvpPresenter.bind(view);
        }
        for (MvpDelegate childDelegate : childDelegates.values()) {
            childDelegate.bindView();
        }
    }

    public void unbindView() {
        for (MvpDelegate childDelegate : childDelegates.values()) {
            childDelegate.unbindView();
        }
        for (MvpPresenter mvpPresenter : presenters.keySet()) {
            mvpPresenter.unbind(view);
        }
    }

    public void saveState(Bundle outState) {
        outState.putString(MVP_DELEGATE_TAG, delegateTag);
    }

    public void destroy(boolean keepAlive) {
        for (MvpDelegate childDelegate : childDelegates.values()) {
            if (childDelegate instanceof LegacyMvpDelegate) {
                continue;
            }
            childDelegate.destroy(keepAlive);
        }
        for (Map.Entry<MvpPresenter, String> entry : presenters.entrySet()) {
            mvpProcessor.freePresenter(entry.getKey(), entry.getValue(), keepAlive);
        }
    }

    private String generateTag() {
        return view.toString();
    }

    /**
     * Выполняет удаление {@code mvpDelegate} из списка дочерних делегатов родителя.
     * Используется для удаления {@link LegacyMvpDelegate} фрагмента из {@link MvpDelegate}
     * содержащей его Activity, чтобы после уничтожения фрагмента, на него не оставалось ссылок
     * и последующих вызовов методов от MVP.
     * http://agile.srvdev.ru/browse/CPPKPP-41619
     *
     * @param mvpDelegate Дочерний {@link MvpDelegate}
     */
    @Deprecated
    public void removeChildDelegate(LegacyMvpDelegate mvpDelegate) {
        for (Iterator<Map.Entry<String, MvpDelegate>> it = childDelegates.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, MvpDelegate> entry = it.next();
            if (entry.getValue().equals(mvpDelegate)) {
                it.remove();
                return;
            }
        }
    }
}
