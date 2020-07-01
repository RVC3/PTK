package ru.ppr.inpas.lib.logger;

import android.support.annotation.NonNull;

import java.util.Locale;

import ru.ppr.inpas.lib.packer.model.Tag;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.utils.ByteUtils;
import ru.ppr.logger.Logger;

/**
 * Класс для логирования.
 */
public class InpasLogger {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DEFAULT_LOG_TAG = "?> ";

    /**
     * Формат представления данных.
     */
    public enum DataFormat {
        DEFAULT(""),
        OCT("%02o"),
        DEC("%02d"),
        HEX("%02x");

        @NonNull
        private final String mValue;

        DataFormat(@NonNull final String value) {
            mValue = value;
        }

        @NonNull
        public String getValue() {
            return mValue;
        }

    }

    private static boolean mIgnoreLogTag = false;

    /**
     * Метод для игнорирования тега при выводе данных в лог.
     *
     * @param value значение флага.
     */
    public static void setIgnoreLogTag(final boolean value) {
        mIgnoreLogTag = value;
    }

    /**
     * Метод для добавления данных к логам.
     *
     * @param sb     хранилище логов.
     * @param data   данные для добавления.
     * @param format формат логирования данных.
     */
    private static void append(@NonNull StringBuilder sb, @NonNull final byte[] data, @NonNull final DataFormat format) {
        sb.append("[");
        for (int i = 0; i < data.length; i++) {
            if (format != DataFormat.DEFAULT) {
                sb.append(String.format(Locale.getDefault(), format.getValue(), data[i]));
            } else {
                sb.append(ByteUtils.byteToInt(data[i]));
            }

            if ((data.length - 1) != i) {
                sb.append(" ");
            }
        }
        sb.append("]");
    }

    /**
     * Метод для создания тега для лога.
     *
     * @param cls класс на основе которого будет создан тег.
     * @return созданные тег.
     */
    public static String makeTag(Class cls) {
        return Logger.makeLogTag(cls);
    }


    public static void info(@NonNull final String tag, @NonNull final String message) {
        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, message);
        } else {
            Logger.info(tag, message);
        }
    }

    public static void info(@NonNull final String tag, @NonNull final Exception ex) {
        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, ex);
        } else {
            Logger.info(tag, ex);
        }
    }

    public static void info(@NonNull final String tag, @NonNull final String message, @NonNull final byte[] data) {
        info(tag, message, data, DataFormat.HEX);
    }

    public static void info(@NonNull final String tag, @NonNull final String message, @NonNull final byte[] data,
                            @NonNull final DataFormat format) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message);
        append(sb, data, format);
        sb.trimToSize();

        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, sb.toString());
        } else {
            Logger.info(tag, sb.toString());
        }
    }

    public static void info(@NonNull final String tag, @NonNull final String message, @NonNull final SaPacket packet) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(LINE_SEPARATOR);
        sb.append(packet);
        sb.trimToSize();

        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, sb.toString());
        } else {
            Logger.info(tag, sb.toString());
        }
    }

    public static void info(@NonNull final String tag, @NonNull final String message, @NonNull final Tag tagData) {
        info(tag, message, tagData, DataFormat.DEFAULT);
    }

    public static void info(@NonNull final String tag, @NonNull final String message,
                            @NonNull final Tag tagData, @NonNull final DataFormat format) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append("<Number: ");
        sb.append(tagData.getNumber());
        sb.append(", Length: ");
        sb.append(tagData.getLength());
        sb.append(", Data: ");
        append(sb, tagData.getData(), format);
        sb.append(">");
        sb.trimToSize();

        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, sb.toString());
        } else {
            Logger.info(tag, sb.toString());
        }
    }

    public static void error(@NonNull final String tag, @NonNull final String message) {
        if (mIgnoreLogTag) {
            Logger.error(DEFAULT_LOG_TAG, message);
        } else {
            Logger.error(tag, message);
        }
    }

    public static void error(@NonNull final String tag, @NonNull final Exception ex) {
        if (mIgnoreLogTag) {
            Logger.info(DEFAULT_LOG_TAG, ex);
        } else {
            Logger.info(tag, ex);
        }
    }

}
