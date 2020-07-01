package ru.ppr.cppk.ui.fragment.exemptionEnterSurname;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface ExemptionEnterSurnameView extends MvpView {

    void showEmptyDocumentError(boolean isSnils);

    void showInvalidSnilsError();

    void showInvalidFioError();

    void showEmptyIssueDateError();

    void setSnilsFieldVisible(boolean visible);

    void setIssueDateFieldVisible(boolean visible);

    void setDocumentNumberFieldVisible(boolean visible);

    void setExemptionInfo(ExemptionInfo exemptionInfo);

    class ExemptionInfo {
        Integer exemptionExpressCode;
        Integer percentage;
        String groupName;
    }
}
