package ru.ppr.cppk.ui.activity.enterETicketData;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.model.ETicketDataParams;

/**
 * @author Grigoriy Kashka
 */
interface EnterETicketDataView extends MvpView {

    void setEmailAndPhone(ETicketDataParams eTicketDataParams);

    void showError(boolean isDataOk);
}
