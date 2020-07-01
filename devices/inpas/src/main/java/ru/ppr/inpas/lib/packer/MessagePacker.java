package ru.ppr.inpas.lib.packer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.model.Field;
import ru.ppr.inpas.lib.packer.model.Header;
import ru.ppr.inpas.lib.packer.model.Message;
import ru.ppr.inpas.lib.packer.model.Tag;
import ru.ppr.inpas.lib.protocol.model.PosSignalType;
import ru.ppr.inpas.lib.utils.ByteUtils;

/**
 * Класс служит для распаковки массива байт в сообщения согласно протоколу SA.
 *
 * @see Message
 */
public class MessagePacker {
    private static final String TAG = InpasLogger.makeTag(MessagePacker.class);

    private static final int MESSAGE_PREFIX_LENGTH = 1;
    private static final int MESSAGE_LENGTH_LENGTH = 2;
    private static final int HEADER_LENGTH_LENGTH = 2;
    private static final int FIELD_NUMBER_LENGTH = 1;
    private static final int FIELD_LENGTH_LENGTH = 2;
    private static final int TAG_NUMBER_LENGTH = 1;
    private static final int TAG_LENGTH_LENGTH = 2;
    private static final int MESSAGE_CRC_LENGTH = 2;

    private static final int FIELD_DATA_MIN_LENGTH = 1;
    private static final int FIELD_DATA_MAX_LENGTH = Integer.MAX_VALUE;

    /**
     * Метод для разбора префикса сообщения.
     *
     * @param data   данные содержащие префикс.
     * @param offset смещение внури данных.
     * @return префикс сообщения.
     */
    private static int parsePrefix(final byte[] data, final int offset) {
        return ByteUtils.byteToInt(data[offset]);
    }

    /**
     * Метод для прроверки корректности префикса.
     *
     * @param value значение для проверки.
     * @return результат проверки.
     */
    private static boolean isValidPrefix(final int value) {
        return (PosSignalType.SOH.getValue() == value) || (PosSignalType.STX.getValue() == value);
    }

    /**
     * Меод для прроверки корректности префикса.
     *
     * @param data данные для проверки.
     * @return результат проверки.
     */
    public static boolean isValidPrefix(@NonNull final byte[] data) {
        boolean result = false;

        if (data.length > 0) {
            final int prefix = ByteUtils.byteToInt(data[0]);
            result = isValidPrefix(prefix);
        }

        return result;
    }

    /**
     * Метод для разбора длины сообщения.
     *
     * @param data   данные содержащие длину.
     * @param offset смещение внутри данных.
     * @return длина данных.
     */
    private static int parseLength(final byte[] data, final int offset) {
        final int lowPart = ByteUtils.byteToInt(data[offset]);
        final int highPart = ByteUtils.byteToInt(data[offset + 1]) << 8;

        return lowPart + highPart;
    }

    /**
     * Метод для проверки корретности данных.
     *
     * @param value значение для проверки.
     * @return результат проверки.
     */
    private static boolean isValidLength(final int value) {
        return (value >= FIELD_DATA_MIN_LENGTH) && (value < FIELD_DATA_MAX_LENGTH);
    }

    /**
     * Метод для проверки корректности данных.
     *
     * @param data данные для проверки.
     * @return результат проверки.
     */
    public static boolean isValidPart(@NonNull final byte[] data) {
        boolean result = false;

        if (isValidPrefix(data)) {
            final int prefix = parsePrefix(data, 0);
            final int length = parseLength(data, MESSAGE_PREFIX_LENGTH);

            if (isPartial(prefix)) {
                final Header header = parseHeader(data, MESSAGE_PREFIX_LENGTH + MESSAGE_LENGTH_LENGTH);
                result = (header != null);
            } else {
                final int serviceLength = MESSAGE_PREFIX_LENGTH + MESSAGE_LENGTH_LENGTH + MESSAGE_CRC_LENGTH;
                result = (length <= (data.length - serviceLength));
            }
        }

        return result;
    }

    /**
     * Метод для проверки является ли сообщение составным.
     *
     * @param value значение для проверки.
     * @return результат проверки.
     */
    private static boolean isPartial(final int value) {
        return (PosSignalType.SOH.getValue() == value);
    }

    /**
     * Метод для разбора тега.
     *
     * @param data       данные для разбора.
     * @param startIndex смещение внутри данных.
     * @return тег.
     * @see Tag
     */
    @NonNull
    private static Tag parseTag(final byte[] data, final int startIndex) {
        int offset = startIndex;

        final int number = data[offset];
        offset += TAG_NUMBER_LENGTH;

        final int length = parseLength(data, offset);
        offset += TAG_LENGTH_LENGTH;

        final byte[] tagData = new byte[length];
        System.arraycopy(data, offset, tagData, 0, length);

        return new Tag(number, length, tagData);
    }

    /**
     * Метод для разбора заголовка сообщения.
     *
     * @param data       данные для разбора.
     * @param startIndex смещение внутри данных.
     * @return заголовок.
     * @see Header
     */
    @Nullable
    private static Header parseHeader(final byte[] data, final int startIndex) {
        Header header = null;
        final int length = parseLength(data, startIndex);

        if (isValidLength(length)) {
            int offset = startIndex + HEADER_LENGTH_LENGTH;
            header = new Header(length);

            while (offset < (startIndex + length)) {
                final Tag tag = parseTag(data, offset);
                offset += TAG_NUMBER_LENGTH + TAG_LENGTH_LENGTH
                        + tag.getLength();

                header.addTag(tag);
            }
        } else {
            throw new IllegalArgumentException("Wrong header length.");
        }

        return header;
    }

    /**
     * Метод для разбора поля сообщения.
     *
     * @param data       данные для разбора.
     * @param startIndex смещение внутри данных.
     * @return поле сообщения.
     * @see Field
     */
    @NonNull
    private static Field parseField(final byte[] data, final int startIndex) {
        int offset = startIndex;

        final int number = data[offset];
        offset += FIELD_NUMBER_LENGTH;

        final int length = parseLength(data, offset);
        offset += FIELD_LENGTH_LENGTH;

        final int availableBytes = data.length - offset - MESSAGE_CRC_LENGTH;
        final int fieldLength = (length > availableBytes) ? availableBytes : length;
        final byte[] fieldData = new byte[fieldLength];
        System.arraycopy(data, offset, fieldData, 0, fieldLength);

        return new Field(number, length, fieldData);
    }

    /**
     * Метод для разбора поля сообщения.
     *
     * @param data       данные для разбора.
     * @param startIndex смещение внутри данных.
     * @param number     номер поля.
     * @param length     длина поля.
     * @return поле сообщения.
     * @see Field
     */
    @NonNull
    private static Field parseField(final byte[] data, final int startIndex,
                                    final int number,
                                    final int length) {
        final int availableBytes = data.length - startIndex - MESSAGE_CRC_LENGTH;
        final int fieldLength = (length > availableBytes) ? availableBytes : length;
        final byte[] fieldData = new byte[fieldLength];
        System.arraycopy(data, startIndex, fieldData, 0, fieldLength);

        return new Field(number, fieldLength, fieldData);
    }

    /**
     * Метод для распаковки сообщений из массива данных.
     *
     * @param data             данные для распаковки.
     * @param firstFieldNumber номер первого поля.
     * @param firstFieldLength длина первого поля.
     * @return распакованные сообщения.
     * @see Message
     */
    @NonNull
    public static List<Message> unpack(@NonNull final byte[] data,
                                       final int firstFieldNumber,
                                       final int firstFieldLength) {
        final List<Message> messages = new ArrayList<>();
        int offset = 0;
        int nextMessageIndex = 0;

        while (offset < data.length) {
            final int prefix = parsePrefix(data, offset);
            offset += MESSAGE_PREFIX_LENGTH;

            if (isValidPrefix(prefix)) {
                final int length = parseLength(data, offset);
                offset += MESSAGE_LENGTH_LENGTH;
                nextMessageIndex = offset + length;

                if (isValidLength(length) && (length < data.length)) {
                    final Message message = new Message();
                    final boolean isPartial = isPartial(prefix);
                    Header header = null;

                    if (isPartial) {
                        header = parseHeader(data, offset);

                        if (header != null) {
                            message.setHeader(header);
                            final int fullHeaderLength = header.getLength() + HEADER_LENGTH_LENGTH;
                            offset += fullHeaderLength;
                        }
                    }

                    while (offset < nextMessageIndex) {
                        final Field field = messages.isEmpty()
                                ? parseField(data, offset,
                                firstFieldNumber,
                                firstFieldLength)
                                : parseField(data, offset);
                        offset += FIELD_NUMBER_LENGTH + FIELD_LENGTH_LENGTH
                                + field.getLength();

                        message.addField(field);
                    }

                    messages.add(message);
                    offset += MESSAGE_CRC_LENGTH;
                } else {
                    final String message = String.format(Locale.getDefault(),
                            "Wrong message length. Length = %d, Data length = %d",
                            length, data.length
                    );
                    InpasLogger.error(TAG, message);
                    throw new IllegalArgumentException(message);
                }
            } else {
                throw new IllegalArgumentException("Wrong message prefix.");
            }
        }

        return messages;
    }

    /**
     * Метод для распаковки сообщений из массива данных.
     *
     * @param data данные для распаковки.
     * @return распакованные сообщения.
     * @see Message
     */
    @NonNull
    public static List<Message> unpack(@NonNull final byte[] data) {
        final List<Message> messages = new ArrayList<>();
        int offset = 0;
        int nextMessageIndex = 0;

        while (offset < data.length) {
            final int prefix = parsePrefix(data, offset);
            offset += MESSAGE_PREFIX_LENGTH;

            if (isValidPrefix(prefix)) {
                final int length = parseLength(data, offset);
                offset += MESSAGE_LENGTH_LENGTH;
                nextMessageIndex = offset + length;

                if (isValidLength(length) && (length < data.length)) {
                    final Message message = new Message();
                    final boolean isPartial = isPartial(prefix);
                    Header header = null;

                    if (isPartial) {
                        header = parseHeader(data, offset);

                        if (header != null) {
                            message.setHeader(header);
                            final int fullHeaderLength = header.getLength() + HEADER_LENGTH_LENGTH;
                            offset += fullHeaderLength;
                        }
                    }

                    while (offset < nextMessageIndex) {
                        final Field field = parseField(data, offset);
                        offset += FIELD_NUMBER_LENGTH + FIELD_LENGTH_LENGTH
                                + field.getLength();

                        message.addField(field);
                    }

                    messages.add(message);
                    offset += MESSAGE_CRC_LENGTH;
                } else {
                    final String message = String.format(Locale.getDefault(),
                            "Wrong message length. Length = %d, Data length = %d",
                            length, data.length
                    );
                    InpasLogger.error(TAG, message);
//                    throw new IllegalArgumentException(message);
                }
            } else {
                final String message = "Wrong message prefix.";
                InpasLogger.error(TAG, message);
//                throw new IllegalArgumentException(message);
            }
        }

        return messages;
    }

}