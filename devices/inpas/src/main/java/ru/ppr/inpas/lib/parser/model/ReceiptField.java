package ru.ppr.inpas.lib.parser.model;

import android.support.annotation.NonNull;

/**
 * Поле содержащееся в образе чека.
 */
public class ReceiptField {

    private int mIndex;
    private String mTag, mName, mValue;

    public ReceiptField() {
        this(0, "", "", "");
    }

    public ReceiptField(
            final int index,
            @NonNull final String tag,
            @NonNull final String name,
            @NonNull final String value) {
        setIndex(index);
        setTag(tag);
        setName(name);
        setValue(value);
    }

    /**
     * Метод возращает индекс поля в образе чека.
     *
     * @return индекс поля в образе чека.
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Метод устанавливает индекс поля в образе чека.
     *
     * @param index значения индекса поля в образе чека.
     */
    public void setIndex(final int index) {
        mIndex = index;
    }

    /**
     * Метод возвращаюий тег поля в образе чека.
     *
     * @return тег поля в образе чека.
     */
    @NonNull
    public String getTag() {
        return mTag;
    }

    /**
     * Метод для установления тега поля.
     *
     * @param tag тег поля.
     */
    public void setTag(@NonNull final String tag) {
        mTag = tag;
    }

    /**
     * Метод возврщает имя поля в образе чека.
     *
     * @return имя поля в образе чека.
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * Метод устанавливает имя поля в образе чека.
     *
     * @param name имени поля.
     */
    public void setName(@NonNull final String name) {
        mName = name;
    }

    /**
     * Метод возвращает значение поля в образе чека.
     *
     * @return значение поля в образе чека.
     */
    @NonNull
    public String getValue() {
        return mValue;
    }

    /**
     * Метод позволяет установить значение поля в образе чека.
     *
     * @param value значение поля.
     */
    public void setValue(@NonNull final String value) {
        mValue = value;
    }

}
