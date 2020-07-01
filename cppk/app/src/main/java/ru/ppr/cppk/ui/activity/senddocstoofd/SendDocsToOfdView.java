package ru.ppr.cppk.ui.activity.senddocstoofd;

import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Grigoriy Kashka
 */
interface SendDocsToOfdView extends MvpView {

    void setUnsentDocsCount(int unsentDocsCount);

    void setFirstUnsentDocNumber(int firstUnsentDocNumber);

    void setFirstUnsentDocDateTime(Date firstUnsentDocDateTime);

    void showError(Error error);

    void showProgress();

    void hideProgress();

    enum Error {
        NONE,
        EXIST_UNSENT_DOCS,
        NOT_ALL_DOCS_SENT,
        GET_DATA
    }
}
