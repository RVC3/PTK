package ru.ppr.inpas.lib.packer.model;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import ru.ppr.inpas.lib.utils.ByteUtils;

/**
 * Класс представляющий заголовок сообщения согласно протоколу SA.
 */
public class Header {
    private static final int HEADER_VERSION = 1;
    private static final int PACKET_NUMBER = 2;
    private static final int LAST_PACKET_VALUE = 0;

    private final int mLength; // <LL LH>
    private final List<Tag> mTags = new LinkedList<>(); // <TAG>...<TAG>

    public Header(final int length) {
        mLength = length;
    }

    /**
     * Метод возращающий длину заголовка.
     *
     * @return длина заголовка.
     */
    public int getLength() {
        return mLength;
    }

    /**
     * Метод для проверки корректности тега.
     *
     * @param tag тег для проверки.
     * @return результат проверки.
     * @see Tag
     */
    private boolean isValidTag(@NonNull final Tag tag) {
        return (HEADER_VERSION == tag.getNumber()) || (PACKET_NUMBER == tag.getNumber());
    }

    /**
     * Метод для добавления тегов в заголовок.
     *
     * @param tag тег для добавления.
     * @see Tag
     */
    public void addTag(final Tag tag) {
        if (!isValidTag(tag)) {
            throw new IllegalArgumentException("Wrong number of tag.");
        }

        mTags.add(tag);
    }

    /**
     * Метод возвращает списов тегов заголовка.
     *
     * @return списов тегов заголовка.
     * @see Tag
     */
    @NonNull
    public List<Tag> getTags() {
        return new LinkedList<>(mTags);
    }

    /**
     * Метод возвращает номер заголовка.
     *
     * @return номер заголовка.
     */
    public int getNumber() {
        int index = 0;

        for (Tag tag : mTags) {
            if (isNumberTag(tag) && (1 == tag.getLength())) {
                index = ByteUtils.byteToInt(tag.getData()[0]);
                break;
            }
        }

        return index;
    }

    /**
     * Метод для проверки содержит ли тег номер пакета.
     *
     * @param tag тег для проверки.
     * @return результат проверки.
     * @see Tag
     */
    private boolean isNumberTag(@NonNull final Tag tag) {
        return (tag.getNumber() == PACKET_NUMBER);
    }

    /**
     * Метод для проверки является ли текущий заголовок последним.
     *
     * @return результат проверки.
     */
    public boolean isLast() {
        return (LAST_PACKET_VALUE == getNumber());
    }

    /**
     * Метод для представления заголовка в удобочитаемом виде.
     *
     * @return строковое представление поля.
     */
    @Override
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append("Header: ");
        sb.append("Length: ");
        sb.append(mLength);

        if (mTags.size() > 0) {
            sb.append(lineSeparator);

            for (Tag tag : mTags) {
                sb.append(tag);
                sb.append(lineSeparator);
            }
        } else {
            sb.append("No tags.");
        }

        sb.trimToSize();

        return sb.toString();
    }

}