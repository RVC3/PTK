package ru.ppr.cppk.ui.fragment.removeExemption;

import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface RemoveExemptionView extends MvpView {

    void setExemptionInfo(ExemptionInfo exemptionInfo);

    void setSnilsFieldVisible(boolean visible);

    void setDocumentNumberFieldVisible(boolean visible);

    class ExemptionInfo {
        int expressCode;
        String groupName;
        int percentage;
        String fio;
        String documentNumber;
        Date documentIssueDate;
        String bscNumber;
        String bscType;
    }
}
