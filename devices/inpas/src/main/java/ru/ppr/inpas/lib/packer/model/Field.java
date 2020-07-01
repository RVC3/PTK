package ru.ppr.inpas.lib.packer.model;

import android.support.annotation.NonNull;

/**
 * Класс представляющий поле сообщения согласно протоколу SA.
 */
public class Field {

    private final int mNumber;
    private final int mLength;
    private byte[] mData;

    public Field(final int number, final int length, final byte[] data) {
        mNumber = number;
        mLength = length;
        mData = data;
    }

    /**
     * Метод возвращает номер поля.
     *
     * @return номер поля.
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * Метод возвращающий длину поля.
     *
     * @return длина поля.
     */
    public int getLength() {
        return mLength;
    }

    /**
     * Метод возвращает данные поля.
     *
     * @return данные поля.
     * @throws IllegalArgumentException если данных нет
     */
    public byte[] getData() {
        if (null == mData) {
            throw new IllegalArgumentException("No field data.");
        }

        return mData;
    }

    /**
     * Метод для добавления данных в поле.
     *
     * @param data данные для добавления.
     */
    public void addData(@NonNull final byte[] data) {
        if (data.length > getBytesCountToComplete()) {
            throw new IllegalArgumentException("Wrong data length.");
        }

        final byte[] buffer = new byte[mData.length + data.length];
        System.arraycopy(mData, 0, buffer, 0, mData.length);
        System.arraycopy(data, 0, buffer, mData.length, data.length);

        mData = buffer;
    }

    /**
     * Метод для проверки является ли поле завершенным.
     *
     * @return признак завершенности поля.
     */
    public boolean isComplete() {
        return (mData.length == mLength);
    }


    /**
     * Метод возвращает количество байт необходимых для того чтобы поле было завершенным.
     *
     * @return количество байт необходимых для того чтобы поле было завершенным.
     */
    public int getBytesCountToComplete() {
        return mLength - mData.length;
    }

    /**
     * Метод для представления поля в удобочитаемом виде.
     *
     * @return строковое представление поля.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        //sb.append(super.toString());
        sb.append("Field: ");
        sb.append(mNumber);
        sb.append(", Length: ");
        sb.append(mLength);
        sb.append(", Complete: ");
        sb.append(isComplete());
        sb.append(", Data: ");

        if (mData != null) {
            for (int i = 0; i < mData.length; i++) {
                sb.append(mData[i]);

                if (i != (mData.length - 1)) {
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