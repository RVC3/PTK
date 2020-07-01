package ru.ppr.core.ui.mvp.core;

import ru.ppr.core.ui.mvp.presenter.MvpPresenter;

/**
 * Идентификатор презентера.
 *
 * @author Aleksandr Brazhkin
 */
class Key {
    private final Class<? extends MvpPresenter> mPresenterClass;
    private final String mPresenterTag;

    public Key(Class<? extends MvpPresenter> mPresenterClass, String mPresenterTag) {
        this.mPresenterClass = mPresenterClass;
        this.mPresenterTag = mPresenterTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!mPresenterClass.equals(key.mPresenterClass)) return false;
        return mPresenterTag != null ? mPresenterTag.equals(key.mPresenterTag) : key.mPresenterTag == null;

    }

    @Override
    public int hashCode() {
        int result = mPresenterClass.hashCode();
        result = 31 * result + (mPresenterTag != null ? mPresenterTag.hashCode() : 0);
        return result;
    }
}
