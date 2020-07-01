package ru.ppr.inpas.lib.packer;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ru.ppr.inpas.lib.packer.model.Field;
import ru.ppr.inpas.lib.packer.model.Message;

/**
 * Класс использующийся для создания сообщений из массива байт.
 *
 * @see Message
 */
public class MessageConstructor {

    private final Queue<Message> mMessages = new LinkedList<>();
    private final Queue<Message> mUnsortedMessages = new LinkedList<>();

    private Message mIncompleteMessage = null;

    /**
     * Метод для проверки на наличие завершенности сообщений.
     *
     * @return результат проверки.
     */
    public boolean isComplete() {
        return !mMessages.isEmpty()
                && (mUnsortedMessages.isEmpty() && !hasIncompleteMessage());
    }

    /**
     * Метод показывающий наличие незавершенных сообщений.
     *
     * @return результат проверки.
     */
    private boolean hasIncompleteMessage() {
        return (mIncompleteMessage != null);
    }

    public boolean hasMessage() {
        return !mMessages.isEmpty();
    }

    /**
     * Метод для получения одного сообщения и его удаление из внутреннего хранилища.
     *
     * @return сообщение.
     * @see Message
     */
    @NonNull
    public Message poll() {
        return mMessages.poll();
    }

    /**
     * Метод для распаковки сообщений из массива данных.
     *
     * @param data данные для распаковки.
     * @return распакованные сообщения.
     * @see Message
     */
    @NonNull
    private List<Message> unpack(@NonNull final byte[] data) {
        final List<Message> unpackedMessages = new LinkedList<>();

        if (mIncompleteMessage != null) {
            if (mIncompleteMessage.isComplete()) {
                mMessages.add(mIncompleteMessage);
                mIncompleteMessage = null;

                unpackedMessages.addAll(MessagePacker.unpack(data));
            } else {
                final Field incompleteField = mIncompleteMessage.getFirstIncompleteField();

                if (incompleteField != null) {
                    unpackedMessages.addAll(MessagePacker.unpack(data,
                            incompleteField.getNumber(),
                            incompleteField.getLength()
                            )
                    );
                } else {
                    throw new IllegalArgumentException("Can't find an incomplete field.");
                }
            }
        } else {
            unpackedMessages.addAll(MessagePacker.unpack(data));
        }

        return unpackedMessages;
    }

    /**
     * Метод для добавления данных в последнее назавершенное сообщение.
     *
     * @param data данные для добавления.
     */
    public void append(@NonNull final byte[] data) {
        final List<Message> unpackedMessages = unpack(data);
        mUnsortedMessages.addAll(unpackedMessages);

        while (!mUnsortedMessages.isEmpty()) {
            final Message unsortedMessage = mUnsortedMessages.poll();

            if (mIncompleteMessage != null) {
                final Field incompleteField = mIncompleteMessage.getFirstIncompleteField();

                if (incompleteField != null) {
                    final Field unpackedField = unsortedMessage.getFirstField();

                    if (unpackedField != null) {
                        incompleteField.addData(unpackedField.getData());

                        if (mIncompleteMessage.isComplete()) {
                            mMessages.add(mIncompleteMessage);
                            mIncompleteMessage = null;
                        }
                    } else {
                        throw new IllegalArgumentException("Can't find a field of the unpacked message.");
                    }
                } else {
                    throw new IllegalArgumentException("Can't find an incomplete field.");
                }
            } else {
                if (unsortedMessage.isComplete()) {
                    mMessages.add(unsortedMessage);
                } else {
                    mIncompleteMessage = unsortedMessage;

                    break;
                }
            }
        }
    }

}