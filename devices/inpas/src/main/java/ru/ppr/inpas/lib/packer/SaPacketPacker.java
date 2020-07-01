package ru.ppr.inpas.lib.packer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.packer.model.Field;
import ru.ppr.inpas.lib.packer.model.Message;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.SaField;
import ru.ppr.inpas.lib.utils.ByteUtils;

/**
 * Класс для получения пакетов из сообщений согласно протоколу SA.
 *
 * @see SaPacket
 * @see Message
 */
public class SaPacketPacker {
    private static final String TAG = InpasLogger.makeTag(SaPacketPacker.class);

    private static final byte STX = 0x02;
    private static final int MESSAGE_PREFIX = STX;
    private static final int MESSAGE_PREFIX_LENGTH = 1;
    private static final int MESSAGE_CRC_LENGTH = 2;
    private static final int MESSAGE_LENGTH_LENGTH = 2;
    private static final int MESSAGE_FIELD_NUMBER_LENGTH = 1;
    private static final int MESSAGE_FIELD_LENGTH_LENGTH = 2;
    private static final int MESSAGE_SERVICE_FIELD_LENGTH = MESSAGE_FIELD_NUMBER_LENGTH + MESSAGE_FIELD_LENGTH_LENGTH;

    /**
     * Метод возвращает длину данных в сообщении.
     *
     * @param packet пакет содержащий данные.
     * @return длина данных в сообщении.
     * @see SaPacket
     */
    private static int getMessageDataLength(@NonNull final SaPacket packet) {
        int length = 0;

        for (Map.Entry<SaField, byte[]> entry : packet.getParams().entrySet()) {
            length += MESSAGE_SERVICE_FIELD_LENGTH + entry.getValue().length;
        }

        return length;
    }

    /**
     * Метод упаковывающий одно поле.
     *
     * @param entry  поле для упаковки.
     * @param data   массив данных куда будет упаковано поле.
     * @param offset смещение внутри массива куда будет упаковано поле.
     * @return индекс с которого можно продолжить упаковку элементов в массиве данных.
     */
    private static int packField(@NonNull final Map.Entry<SaField, byte[]> entry, @NonNull final byte[] data, final int offset) {
        int index = offset;
        final int fieldDataLength = entry.getValue().length;

        data[index++] = ByteUtils.intToByte(entry.getKey().getValue());
        data[index++] = ByteUtils.intToByte(fieldDataLength);
        data[index++] = ByteUtils.intToByte(fieldDataLength >> 8);

        System.arraycopy(entry.getValue(), 0, data, index, fieldDataLength);
        index += fieldDataLength;

        return index;
    }

    /**
     * Метод для упаковки SA пакета.
     *
     * @param packet упаковываемый пакет.
     * @param data   массив данных куда будет упаковываться пакет.
     * @param offset смещение внутри массива данных.
     * @return индекс свободного элемента внутра массива данных.
     * @see SaPacket
     */
    private static int packFields(@NonNull final SaPacket packet, @NonNull final byte[] data, final int offset) {
        int index = offset;

        for (Map.Entry<SaField, byte[]> entry : packet.getParams().entrySet()) {
            index = packField(entry, data, index);
        }

        return index;
    }

    /**
     * Метод для упаковки SA пакета.
     *
     * @param packet пакет для упаковки.
     * @return массив с данными содержащий упакованный SA пакет.
     * @throws IllegalArgumentException
     * @see SaPacket
     */
    @NonNull
    public static byte[] pack(@NonNull final SaPacket packet) throws IllegalArgumentException {
        if (packet.isEmpty()) {
            throw new IllegalArgumentException("Packet is empty.");
        }

        int offset = 0;
        final int dataLength = getMessageDataLength(packet);
        final byte[] data = new byte[MESSAGE_PREFIX_LENGTH + MESSAGE_LENGTH_LENGTH + dataLength + MESSAGE_CRC_LENGTH];
        data[offset++] = ByteUtils.intToByte(MESSAGE_PREFIX);
        data[offset++] = ByteUtils.intToByte(dataLength);
        data[offset++] = ByteUtils.intToByte(dataLength >> 8);

        packFields(packet, data, offset);
        ByteUtils.computeCrc16(data, 0, data.length - MESSAGE_CRC_LENGTH, data.length - MESSAGE_CRC_LENGTH);

        return data;
    }

    /**
     * Метод для создания SA пакета из {@link Message}
     *
     * @param message сообщение из которого будет создан SA пакет.
     * @return SA пакет.
     * @see SaPacket
     * @see Message
     */
    @NonNull
    private static SaPacket createFromComplete(@NonNull final Message message) {
        final SaPacket packet = new SaPacket();

        for (Field field : message.getFields()) {
            final SaField saField = SaField.from(field.getNumber());

            if (saField != SaField.SAF_UNKNOWN) {
                packet.putBytes(saField, field.getData());
            } else {
                InpasLogger.error(TAG, "Can't unpack the SaField. Field has number: "
                        + String.valueOf(field.getNumber())
                        + " is unknown and will be ignored.");
            }
        }

        return packet;
    }

    /**
     * Метод для распаковки сообщения в SA пакет.
     *
     * @param message ообщение  для распаковки.
     * @return распакованный SA пакет.
     * @see SaPacket
     */
    @Nullable
    public static SaPacket unpack(@NonNull final Message message) {
        SaPacket packet = null;

        if (message.isComplete()) {
            packet = createFromComplete(message);

            if (packet.isEmpty()) {
                InpasLogger.error(TAG, "SaPacket is empty and won't be added.");
            }
        } else {
            throw new IllegalArgumentException("Message isn't complete.");
        }

        return packet;
    }

    /**
     * Метод для распаковки {@link Message}
     *
     * @param messages сообщения для распаковки.
     * @return распакованные SA пакеты.
     * @see Message
     */
    @NonNull
    public static List<SaPacket> unpack(@NonNull final List<Message> messages) {
        final List<SaPacket> packets = new ArrayList<>();

        for (Message message : messages) {
            if (message.isComplete()) {
                final SaPacket packet = createFromComplete(message);

                if (packet.isEmpty()) {
                    InpasLogger.error(TAG, "SaPacket is empty and won't be added.");
                } else {
                    packets.add(packet);
                }
            } else {
                throw new IllegalArgumentException("Message isn't complete.");
            }
        }

        return packets;
    }

}