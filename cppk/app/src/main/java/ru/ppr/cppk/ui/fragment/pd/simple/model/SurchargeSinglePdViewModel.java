package ru.ppr.cppk.ui.fragment.pd.simple.model;

/**
 * Разовый ПД по доплате, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class SurchargeSinglePdViewModel extends SinglePdViewModel {
    /**
     * Номер родительского ПД
     */
    private int parentPdNumber;

    public int getParentPdNumber() {
        return parentPdNumber;
    }

    public void setParentPdNumber(int parentPdNumber) {
        this.parentPdNumber = parentPdNumber;
    }
}
