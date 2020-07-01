package ru.ppr.chit.api.mapper;

import android.support.annotation.Nullable;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
@UtcDate
@Mapper
public class UtcDateMapper {

    private static final String TAG = Logger.makeLogTag(UtcDateMapper.class);

    public static final UtcDateMapper INSTANCE = Mappers.getMapper(UtcDateMapper.class);

    private final ThreadLocal<SimpleDateFormat> outDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    public Date entityToModel(@Nullable String entity) {
        if (entity == null) {
            return null;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(0L);
            calendar.set(Calendar.YEAR, Integer.valueOf(entity.substring(0, 4)));
            calendar.set(Calendar.MONTH, Integer.valueOf(entity.substring(5, 7)) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(entity.substring(8, 10)));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(entity.substring(11, 13)));
            calendar.set(Calendar.MINUTE, Integer.valueOf(entity.substring(14, 16)));
            calendar.set(Calendar.SECOND, Integer.valueOf(entity.substring(17, 19)));
            /*
             * Возможные форматы:
             *
             * yyyy-MM-ddTHH:mm:ssZ
             * yyyy-MM-ddTHH:mm:ss.SZ
             * yyyy-MM-ddTHH:mm:ss.SSZ
             * yyyy-MM-ddTHH:mm:ss.SSSZ
             * yyyy-MM-ddTHH:mm:ss.SSSSZ
             * yyyy-MM-ddTHH:mm:ss.SSSSSZ
             * yyyy-MM-ddTHH:mm:ss.SSSSSSZ
             *
             * Первые 19 символов парсятся одинаково, а дальше в зависимости от длины
             */
            if (entity.length() > 20) { // 20 символов в yyyy-MM-ddTHH:mm:ssZ
                int milliseconds;
                switch (entity.length()) {
                    case 22:
                        milliseconds = Integer.valueOf(entity.substring(20, 21));
                        break;
                    case 23:
                        milliseconds = Integer.valueOf(entity.substring(20, 22));
                        break;
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                        milliseconds = Integer.valueOf(entity.substring(20, 23));
                        break;
                    default:
                        milliseconds = 0;
                }
                calendar.set(Calendar.MILLISECOND, milliseconds);
            }
            return calendar.getTime();
        } catch (NumberFormatException exception) {
            Logger.error(TAG, exception);
            return null;
        }
    }

    public String modelToEntity(@Nullable Date model) {
        return model == null ? null : outDateFormat.get().format(model);
    }

}
