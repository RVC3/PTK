package ru.ppr.nsi.entity;


import android.support.annotation.Nullable;

/**
 * Причина аннулирования.
 */
public class RepealReason {

    private String reasonRepeal;
    private int codeReason;

    public String getReasonRepeal() {
        return reasonRepeal;
    }

    public void setReasonRepeal(String reasonRepeal) {
        this.reasonRepeal = reasonRepeal;
    }

    public int getCodeReason() {
        return codeReason;
    }

    public void setCodeReason(int codeReason) {
        this.codeReason = codeReason;
    }

    @Override
    public String toString() {
        return textToLowerWithTitle(reasonRepeal);
    }

    /**
     * Форматирует текст любого регистра в нижний регистр с первой заглавной буквой
     *
     * @param string
     * @return
     */
    public String textToLowerWithTitle(@Nullable String string) {

        if (string == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int lenght = string.length();

        builder.append(string.substring(0, 1).toUpperCase()).append(string.substring(1, lenght).toLowerCase());
        return builder.toString();

    }

}
