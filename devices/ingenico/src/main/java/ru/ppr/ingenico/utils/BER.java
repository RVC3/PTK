package ru.ppr.ingenico.utils;

import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс-утилита для выполнения кодирования/декодирования данных по Basic Encoding Rules.
 * Подробное описание алгоритмов смотреть по ссылке https://ru.wikipedia.org/wiki/X.690
 *
 * @author Dmitry Nevolin
 */
public class BER {

    /**
     * Блок закодированной информации в понятии X.690
     */
    public static class Block {

        /**
         * Часть блока, содержащая идентификатор.
         * Представляет собой один или несколько октетов, в которых содержится информация о типе закодированных данных
         */
        private class Identifier {

            private static final byte MASK_KLASS = (byte) 0xc0; //0b11000000
            private static final byte MASK_TYPE = (byte) 0x20; // 0b00100000
            private static final byte MASK_TAG = (byte) 0x1f; //0b00011111
            private static final byte MASK_TAG_BYTE_HEAD = (byte) 0x80; //0b10000000
            private static final byte MASK_TAG_BYTE_BODY = (byte) 0x7f; //0b01111111

            private static final byte KLASS_UNIVERSAL = (byte) 0x0; //0b00000000
            private static final byte KLASS_APPLICATION = (byte) 0x1; //0b00000001
            private static final byte KLASS_CONTEXT_SPECIFIC = (byte) 0x2; //0b00000010
            private static final byte KLASS_PRIVATE = (byte) 0x3; //0b00000011

            private static final byte TYPE_PRIMITIVE = (byte) 0x0; //0b00000000
            private static final byte TYPE_CONSTRUCTED = (byte) 0x1; //0b00000001

            private static final byte TAG_COMPLEX = (byte) 0x1f; //0b00011111
            private static final byte TAG_LAST_BYTE = (byte) 0x0; //0b00000000
            private static final byte TAG_LAST_BYTE_HEAD = (byte) 0x7f; //0b01111111

            private byte klass;
            private byte type;
            private byte[] bytes;
        }

        /**
         * Часть блока, содержащая информацию о длине блока.
         * Представляет собой дин или несколько октетов, в которых содержится информация о длине закодированных данных
         */
        private class Length {
            private static final byte MASK_FORM = (byte) 0x80; //0b10000000
            private static final byte MASK_FORM_LONG = (byte) 0x7f; //0b01111111

            private static final byte FORM_SHORT = (byte) 0x0; //0b00000000
            private static final byte FORM_LONG = (byte) 0x1; //0b00000001
            private static final byte FORM_INDEFINITE = (byte) 0x80; //0b10000000

            private byte form;
            private byte[] bytes;
        }

        /**
         * Часть блока, содержащая закодированную информацию.
         */
        private class Data {
            private static final byte END_BYTE = (byte) 0; //0b00000000

            private byte[] bytes;
        }

        private int end;

        private final Identifier identifier;
        private final Length length;
        private final Data data;

        private Block() {
            identifier = new Identifier();
            length = new Length();
            data = new Data();
        }

        public byte[] getIdentifier() {
            return identifier.bytes;
        }

        public byte[] getData() {
            return length.form == Length.FORM_INDEFINITE ? Arrays.copyOfRange(data.bytes, 0, data.bytes.length - 2) : data.bytes;
        }

    }

    /**
     * Кодирует данные для передачи по каналу данных
     *
     * @param identifierArg Идентификатор, тип закодированных данных
     * @param data          Кодируемые данные
     * @return закодированные по правилам BER данные
     */
    public static byte[] encodeUniversalPrimitive(int identifierArg, @NonNull byte[] data) {
        //список байтов, описывающих идентефикатор данных
        List<Byte> identifier = new ArrayList<Byte>();

        int identifierLength = 1;
        int identifierBits = 255;

        //считаем количество байтов, которое необходимо для представления тэга
        while (identifierBits < identifierArg) {
            identifierBits *= 255;
            identifierLength++;
        }

        //зная длину просто смещаем исходное число на нужное количество битов и записываем в список
        for (int i = identifierLength; i > 0; i--) {
            int tmp = identifierArg;

            for (int j = 1; j < i; j++)
                tmp = tmp >> 8;

            identifier.add((byte) tmp);
        }

        //список байтов, описывающих длину данных
        List<Byte> length = new ArrayList<Byte>();

        //используем длинную форму
        if (data.length > 127) {
            int lengthLength = 1;
            int bits = 127;

            //считаем количество байтов, которое необходимо для представления длины
            while (bits < data.length) {
                bits *= 127;
                lengthLength++;
            }

            //записываем количество байтов, 8 бит должен быть равен 1
            length.add((byte) (Block.Length.MASK_FORM & 0xff | Block.Length.MASK_FORM_LONG & lengthLength));

            //зная длину просто смещаем исходное число на нужное количество битов и записываем в список
            for (int i = lengthLength; i > 0; i--) {
                int tmp = data.length;

                for (int j = 1; j < i; j++)
                    tmp = tmp >> 8;

                length.add((byte) (tmp & 0xff));
            }

            //используем короткую форму
        } else
            length.add((byte) data.length);

        //формируем результат
        byte[] result = new byte[identifier.size() + length.size() + data.length];

        for (int i = 0; i < identifier.size(); i++)
            result[i] = identifier.get(i);

        for (int i = 0; i < length.size(); i++)
            result[i + identifier.size()] = length.get(i);

        for (int i = 0; i < data.length; i++)
            result[i + identifier.size() + length.size()] = data[i];

        return result;
    }

    /**
     * Декодирует данные, зкодированные по правилам BER.
     *
     * @param source данные, зкодированные по правилам BER
     * @return набор BER-блоков с данными
     */
    public static List<Block> decode(@NonNull byte[] source) {
        if (source.length == 0)
            return new ArrayList<>();

        List<Block> decoded = new ArrayList<>();
        decoded.add(decode(source, 0));

        while (true) {
            Block previous = decoded.get(decoded.size() - 1);

            if (source.length == previous.end + 1)
                break;

            decoded.add(decode(source, previous.end + 1));
        }

        return decoded;
    }

    /**
     * Декодирует данные, зкодированные по правилам BER, начиная с указанной позиции.
     *
     * @param source данные, зкодированные по правилам BER
     * @param shift  смещение
     * @return набор BER-блоков с данными
     */
    public static Block decode(@NonNull byte[] source, int shift) {
        //объект-блок данных
        Block block = new Block();

        //список байтов, описывающих идентефикатор данных
        List<Byte> identifier = new ArrayList<Byte>();
        identifier.add(source[shift]);

        //первые два бита (8, 7) - класс
        byte klass = (byte) (source[shift] & Block.Identifier.MASK_KLASS);
        //следующий бит (6) - тип
        byte type = (byte) (source[shift] & Block.Identifier.MASK_TYPE);
        //остальные биты (5, 4, 3, 2, 1) - тэг
        byte tag = (byte) (source[shift] & Block.Identifier.MASK_TAG);

        //если тэг равен 11111, значит он сложный и состоит из нескольких байт
        while (tag == Block.Identifier.TAG_COMPLEX) {
            identifier.add(source[++shift]);

            //если 8 бит очередного байта равен нулю, значит это последний байт тэга
            if ((source[shift] & Block.Identifier.MASK_TAG_BYTE_HEAD) == Block.Identifier.TAG_LAST_BYTE)
                break;
        }

        block.identifier.klass = klass;
        block.identifier.type = type;
        block.identifier.bytes = new byte[identifier.size()];

        for (int i = 0; i < identifier.size(); i++)
            block.identifier.bytes[i] = identifier.get(i);

        //список байтов, описывающих длину данных
        List<Byte> length = new ArrayList<Byte>();
        length.add(source[++shift]);

        //форма представления длины
        byte form;

        //если 8 бит длины равен 0, значит это короткая форма
        if ((source[shift] & Block.Length.MASK_FORM) == Block.Length.FORM_SHORT) {
            form = Block.Length.FORM_SHORT;

            //если длина равна 10000000, то это форма с неопределённой длиной
        } else if (source[shift] == Block.Length.FORM_INDEFINITE) {
            form = Block.Length.FORM_INDEFINITE;

            //остаётся только длинная форма
        } else {
            form = Block.Length.FORM_LONG;

            //в первом байте содержится количество байтов, которое описывает длину данных (кроме первого бита)
            int bytes = source[shift] & Block.Length.MASK_FORM_LONG;

            for (int i = 0; i < bytes; i++)
                length.add(source[++shift]);
        }

        block.length.form = form;
        block.length.bytes = new byte[length.size()];

        for (int i = 0; i < length.size(); i++)
            block.length.bytes[i] = length.get(i);

        //список байтов, описывающих данные
        List<Byte> data = new ArrayList<Byte>();

        //в короткой форме длина данных указана в первом и единственном байте
        if (block.length.form == Block.Length.FORM_SHORT) {
            int size = block.length.bytes[0];

            for (int i = 0; i < size; i++)
                data.add(source[++shift]);

            //в длинной форме длина данных указана в совокупности всех байтов кроме первого
        } else if (block.length.form == Block.Length.FORM_LONG) {
            //берем всё, кроме первого байта
            BigInteger size = new BigInteger(1, Arrays.copyOfRange(block.length.bytes, 1, block.length.bytes.length));

            if (size.compareTo(BigInteger.valueOf(Integer.MAX_VALUE - shift + 1)) == 1)
                throw new RuntimeException("Data is too big, can not parse it!");

            for (BigInteger i = BigInteger.valueOf(0); i.compareTo(size) == -1; i = i.add(BigInteger.ONE))
                data.add(source[++shift]);

            //в форме с неопределённой длиной длина данных неизвестна, считываем до тех пор пока не встретим два бйта 00 00, которые означают конец массива данных
        } else {
            byte previous;
            byte current = source[shift + 1];

            while (true) {
                previous = current;
                current = source[++shift];

                //добавляем в любом случае, даже если это байты конца массива данных, чтобы сдвиг был правильный
                data.add(current);

                if (previous == Block.Data.END_BYTE && current == Block.Data.END_BYTE)
                    break;
            }
        }

        block.data.bytes = new byte[data.size()];

        for (int i = 0; i < data.size(); i++)
            block.data.bytes[i] = data.get(i);

        block.end = shift;

        return block;
    }

}
