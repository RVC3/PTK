package ru.ppr.inpas.lib.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.inpas.lib.parser.model.Receipt;
import ru.ppr.inpas.lib.parser.model.ReceiptField;
import ru.ppr.inpas.lib.parser.model.ReceiptTag;

/**
 * Класс для получения данных из образа чека.
 */
public class ReceiptParser {

    private static final String RECEIPT_NUMBER_TAG = "ЧЕК";

    private final ReceiptField mReceiptField;
    private Receipt mReceipt;

    public ReceiptParser(@NonNull final ReceiptField receiptField) {
        mReceiptField = receiptField;
    }

    @Nullable
    public Receipt getReceipt() {
        return mReceipt;
    }

    /**
     * Метод для получения номера чека из образа.
     *
     * @param field строка содержащая номер чека.
     * @return номер чека.
     */
    @NonNull
    private String getReceiptNumber(@NonNull final String field) {
        //                        ЧЕК КАССИРА                 0007

        String receiptNumber = "";
        final String subFields[] = field.split("\\s+");

        if (subFields.length >= 3) {
            receiptNumber = subFields[2].trim();
        }

        return receiptNumber;
    }

    /**
     * Разбор образа чека, согласно текущему представлению.
     */
    public void parse() {
        if (mReceiptField.getTag().equals(ReceiptTag.RECEIPT.getValue())
                || mReceiptField.getTag().equals(ReceiptTag.CASHIER_RECEIPT.getValue())) {

            final String[] fields = mReceiptField.getValue().split(System.getProperty("line.separator"));

            if (fields.length > 0) {
                mReceipt = new Receipt();

                for (String field : fields) {
                    if (field.contains(RECEIPT_NUMBER_TAG)) {
                        mReceipt.setReceiptNumber(getReceiptNumber(field));
                    }
                }
            }

        }
    }

}
