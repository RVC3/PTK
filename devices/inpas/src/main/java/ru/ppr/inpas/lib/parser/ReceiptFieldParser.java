package ru.ppr.inpas.lib.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.inpas.lib.parser.model.ReceiptField;
import ru.ppr.inpas.lib.parser.model.ReceiptTag;

/**
 * Класс для разбора полей передаваемых в образе чека.
 */
public class ReceiptFieldParser {

    private static final String FIELD_SEPARATOR = "~";
    private static final String FIELD_ELEMENT_SEPARATOR = "^";

    private List<ReceiptField> mFields;

    private int mStartIndex;
    private String mText;

    public ReceiptFieldParser(final String text) {
        mFields = new ArrayList<>();
        setText(text);
    }

    /**
     * Метод для установки текста для разбора.
     *
     * @param text тест для разбора.
     */
    public void setText(@NonNull final String text) {
        mText = text;
        mStartIndex = 0;

        if (!mFields.isEmpty()) {
            mFields.clear();
        }
    }

    /**
     * Метод позволяющий узнать о наличии полей в образе чека.
     *
     * @return наличие полей в образе чека.
     */
    public boolean hasFields() {
        return !mFields.isEmpty();
    }

    /**
     * Метод возвращает поля образа чека.
     *
     * @return поля образа чека.
     */
    @NonNull
    public List<ReceiptField> getFields() {
        return mFields;
    }

    /**
     * Метод для возвращения поля по тегу.
     *
     * @param tag тег образа чека.
     * @return поле образа чека.
     */
    public ReceiptField getField(@NonNull final ReceiptTag tag) {
        ReceiptField field = null;

        if (!mFields.isEmpty()) {
            for (ReceiptField item : mFields) {
                if (item.getTag().equals(tag.getValue())) {
                    field = item;
                    break;
                }
            }
        }

        return field;
    }

    /**
     * Метод для разбора образа чека.
     */
    public void parse() {
        while (mStartIndex < mText.length()) {
            final ReceiptField field = getField();

            if (field != null) {
                mFields.add(field);
            }
        }
    }

    /**
     * Метод для получения значения тега.
     *
     * @param tag тег для разбора.
     * @return значение тега.
     */
    private String getNext(final String tag) {
        String value = "";
        final int endValueIndex = mText.indexOf(tag, mStartIndex);

        if (endValueIndex >= mStartIndex) {
            value = mText.substring(mStartIndex, endValueIndex);
            mStartIndex = endValueIndex + tag.length();
        } else {
            mStartIndex = mText.length();
        }

        return value;
    }

    /**
     * Метод возвращающий поле содержащееся в образе чека.
     *
     * @return поле образа чека.
     */
    @Nullable
    private ReceiptField getField() {
        ReceiptField field = null;

        if (!mText.isEmpty()) {
            field = new ReceiptField();
            field.setIndex(mStartIndex);
            field.setTag(getNext(FIELD_ELEMENT_SEPARATOR));
            field.setName(getNext(FIELD_ELEMENT_SEPARATOR));
            field.setValue(getNext(FIELD_SEPARATOR));
        }

        return field;
    }

}
