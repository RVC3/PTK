package ru.ppr.inpas.lib.packer.model;

import android.support.annotation.NonNull;

/**
 * Класс представляющий тег сообщения согласно протоколу SA.
 */
public class Tag {
    private static final int MIN_TAG_LENGTH = 4;
    private static final int MAX_TAG_LENGTH = 1024;

    private final int mNumber;
    private final int mLength;
    private final byte[] mData;

    // <N LL LH D>
    public Tag(final int number, final int length, @NonNull final byte[] data) {
        if (!isValidNumber(number)) {
            throw new IllegalArgumentException("Wrong number of the tag.");
        }

        mNumber = number;

        if (!isValidLength(length)) { // В будущем: check.
            throw new IllegalArgumentException("Wrong length of the tag.");
        }

        mLength = length;

        if (!isValidData(data)) {
            throw new IllegalArgumentException("No data.");
        }

        mData = data;
    }

    /**
     * Метод для проверки корретного значения номера тега.
     *
     * @param value результат проверки.
     * @return результат проверки.
     */
    public boolean isValidNumber(final int value) {
        return (value > 0);
    }

    /**
     * Метод возвращает номер тега.
     *
     * @return номер тега.
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * Метод для проверки корретности длины тега.
     *
     * @param value значение длины.
     * @return результат проверки.
     */
    private boolean isValidLength(final int value) {
//        return (value >= MIN_TAG_LENGTH) && (value < MAX_TAG_LENGTH);
        return true;
    }

    /**
     * Метод возращает длину тега.
     *
     * @return значени длины тега.
     */
    public int getLength() {
        return mLength;
    }

    /**
     * Метод для проверки данных.
     *
     * @param data данные для проврки.
     * @return результат проверки.
     */
    private boolean isValidData(@NonNull final byte[] data) {
        return (data.length > 0);
    }

    public byte[] getData() {
        final byte[] data = new byte[mLength];
        System.arraycopy(mData, 0, data, 0, mLength);

        return data;
    }

    /**
     * Метод для представления тега в удобочитаемом виде.
     *
     * @return строковое представление поля.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Tag: ");
        sb.append("Number: ");
        sb.append(mNumber);
        sb.append(", Length: ");
        sb.append(mLength);
        sb.append(", Data: ");

        if (mData != null) {
            for (int i = 0; i < mData.length; i++) {
                sb.append(mData[i]);

                if (i != mData.length - 1) {
                    sb.append(" ");
                }
            }
        } else {
            sb.append("No data.");
        }

        sb.trimToSize();

        return sb.toString();
    }

}