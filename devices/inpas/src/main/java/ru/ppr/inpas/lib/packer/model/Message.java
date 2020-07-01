package ru.ppr.inpas.lib.packer.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Класс представляющий сообщени согласно протоколу SA.
 */
public class Message {
    private static final String TAG = InpasLogger.makeTag(Message.class);

    private final List<Field> mFields = new LinkedList<>();
    private Header mHeader;

    /**
     * Метод для проверки пустое ли сообщение.
     *
     * @return результат проверки.
     */
    public boolean isEmpty() {
        return mFields.isEmpty();
    }

    /**
     * Метод для проверки наличия заголовка.
     *
     * @return результат проверки.
     * @see Header
     */
    private boolean hasHeader() {
        return (mHeader != null);
    }

    /**
     * Метод для установки заголовка.
     *
     * @param header заголовок сообщения.
     * @see Header
     */
    public void setHeader(@Nullable final Header header) {
        mHeader = header;
    }

    /**
     * Метод для проверки является ли сообщение завершенным.
     *
     * @return результат проверки.
     */
    public boolean isComplete() {
        boolean result = true;

        for (Field field : mFields) {
            if (!field.isComplete()) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Метод для добавления поля в сообщение.
     *
     * @param field поле сообщения.
     * @see Field
     */
    public void addField(final Field field) {
        mFields.add(field);
    }


    /**
     * Метод для получения поелей в сообщении.
     *
     * @return поля сообщения.
     */
    @NonNull
    public List<Field> getFields() {
        return new LinkedList<>(mFields);
    }

    /**
     * Метод возвращает первое незавершенное поле.
     *
     * @return незавершенное поле.
     * @see Field
     */
    @Nullable
    public Field getFirstIncompleteField() {
        Field field = null;

        for (Field item : mFields) {
            if (!item.isComplete()) {
                field = item;
                break;
            }
        }

        return field;
    }

    /**
     * Метод для получения первого поля сообщения.
     *
     * @return поле сообщения.
     * @see Field
     */
    @Nullable
    public Field getFirstField() {
        Field field = null;

        if (!mFields.isEmpty()) {
            field = mFields.get(0);
        }

        return field;
    }

    /**
     * Метод для представления ообщения в удобочитаемом виде.
     *
     * @return строковое представление поля.
     */
    @Override
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append("Message: ");
        sb.append(lineSeparator);

        if (mHeader != null) {
            sb.append("\t");
            sb.append(mHeader);
        }

        if (isEmpty()) {
            sb.append("No fields.");
        } else {
            for (Field field : mFields) {
                sb.append("\t");
                sb.append(field);
                sb.append(lineSeparator);
            }
        }

        sb.trimToSize();

        return sb.toString();
    }

}