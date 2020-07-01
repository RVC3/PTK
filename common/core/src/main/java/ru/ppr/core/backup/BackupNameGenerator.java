package ru.ppr.core.backup;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Генератор названий для файлов резервных копий.
 *
 * @author Dmitry Nevolin
 */
public class BackupNameGenerator {

    private static final String PREFIX = "backup";
    private static final String SEPARATOR = "_";
    private static final String EXTENSION = ".zip";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault());

    @Inject
    BackupNameGenerator() {

    }

    /**
     * Генерирует, на основе переданного типа, имя для файла резервной копии с расширением .zip
     *
     * @param type тип резервной копии, произвольная строка, не может быть пустой или null
     * @return сгенерированное имя для файла резервной копии.
     */
    @NonNull
    public String generateTyped(String type, String deviceId) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type can't be null or empty");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFIX);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(type);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(deviceId);
        stringBuilder.append(SEPARATOR);
        stringBuilder.append(DATE_FORMAT.format(new Date()));
        stringBuilder.append(EXTENSION);
        stringBuilder.trimToSize();

        return stringBuilder.toString();
    }
}
