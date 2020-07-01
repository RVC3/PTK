package ru.ppr.inpas.lib.protocol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import ru.ppr.inpas.lib.protocol.model.SaField;


/**
 * Основная сущность для протокола SA.
 *
 * @see SaField
 */
public class SaPacket {
    private static final String DEFAULT_ENCODING = "Cp1251";
    private static final int MIN_PACKET_FIELD_SIZE = 1;
    private static final int MAX_PACKET_FIELD_SIZE = Integer.MAX_VALUE;

    private final Map<SaField, byte[]> mParams = new HashMap<>();

    /**
     * Метод возвращает текущие параметры пакета SA.
     *
     * @return параметры пакета SA.
     */
    @NonNull
    public Map<SaField, byte[]> getParams() {
        return new HashMap<>(mParams);
    }

    /**
     * Метод позволяет узнать наличие данных в пакете SA.
     *
     * @return результат наличия данных в пакете SA.
     */
    public boolean isEmpty() {
        return mParams.isEmpty();
    }

    /**
     * Метод необходимый для проверки пакета на соответсвие критериям валидности.
     *
     * @param packet проверяемый пакет.
     * @return результат проверки.
     */
    public static boolean isValid(@Nullable final SaPacket packet) {
        return (packet != null) && !packet.isEmpty();
    }

    /**
     * Метод для проверки наличия опеределенного поля в пакете.
     *
     * @param field поле для проверки.
     * @return результат наличия поля в пакете.
     * @see SaField
     */
    public boolean hasField(@NonNull final SaField field) {
        return mParams.containsKey(field);
    }

    /**
     * Метод позволяет получить данные определенного поля в пакете, при его наличии в пакете.
     *
     * @param field поле данные которого необходимо получить.
     * @return данные поля.
     * @see SaField
     */
    @Nullable
    public byte[] getBytes(@NonNull final SaField field) {
        byte[] buffer = null;

        if (hasField(field)) {
            final byte[] data = mParams.get(field);
            buffer = new byte[data.length];
            System.arraycopy(data, 0, buffer, 0, data.length);
        }

        return buffer;
    }

    /**
     * Метод для размещения данных в опеределенном поле пакета.
     *
     * @param field поле пакета в которое будут помещены данные.
     * @param data  данные для размещения в конктретном поле.
     * @see SaField
     */
    public void putBytes(@NonNull final SaField field, @Nullable final byte[] data) {
        if (data != null) {
            final int length = data.length;

            if ((length < MIN_PACKET_FIELD_SIZE) || (length == MAX_PACKET_FIELD_SIZE)) {
                throw new IllegalArgumentException("Invalid field size.");
            }

            mParams.put(field, data);
        } else if (this.mParams.containsKey(field)) {
            mParams.remove(field);
        }

    }

    /**
     * Метод позволяет получить данные размещенные в опеределенном поле пакета в виде строки.
     *
     * @param field поле пакета из которого будут браться данные.
     * @return данные поля в виде строки.
     * @see SaField
     */
    @Nullable
    public String getString(@NonNull SaField field) {
        final byte[] data = getBytes(field);
        String result = null;

        if (data != null) {
            try {
                result = new String(data, DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Метод необходим для размещения данных в виде строки в определенном поле пакета.
     *
     * @param field поле пакета в которое будут помещены данные.
     * @param value данные для размещения в определенном поле пакета.
     * @see SaField
     */
    public void putString(@NonNull final SaField field, @Nullable String value) {
        if (value != null) {
            try {
                mParams.put(field, value.getBytes(DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();

                if (mParams.containsKey(field)) {
                    mParams.remove(field);
                }
            }
        } else if (hasField(field)) {
            mParams.remove(field);
        }

    }

    /**
     * Метод позволяет получить данные размещенные в опеределенном поле пакета в виде целочисленного значения.
     *
     * @param field поле пакета из которого будут браться данные.
     * @return данные поля в виде целочисленного значения.
     * @see SaField
     */
    @Nullable
    public Integer getInteger(@NonNull final SaField field) {
        final String value = getString(field);
        Integer result = null;

        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Метод необходим для размещения данных в виде целочисленного значения в определенном поле пакета.
     *
     * @param field поле пакета в которое будут помещены данные.
     * @param value данные для размещения в определенном поле пакета.
     * @see SaField
     */
    public void putInteger(@NonNull final SaField field, @Nullable final Integer value) {
        putString(field, String.valueOf(value));
    }

    /**
     * Метод для возвращения удобчитаемого вида пакета.
     *
     * @return строковое представления содержимого пакета.
     * @see SaField
     */
    @Override
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        sb.append(SaPacket.class.getSimpleName());
        sb.append(": ");
        sb.append(super.toString());
        sb.append(lineSeparator);

        for (Map.Entry<SaField, byte[]> entry : mParams.entrySet()) {
            sb.append(entry.getKey());
            sb.append("(");
            sb.append(entry.getKey().getValue());
            sb.append(")");
            sb.append(" = ");
            sb.append(getString(entry.getKey()));
            sb.append(lineSeparator);
        }

        sb.trimToSize();

        return sb.toString();
    }

}
