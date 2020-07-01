package ru.ppr.chit.nsidb.entity.converter;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
public class DateTimeConverter implements PropertyConverter<Date, String> {

    private static final String TAG = Logger.makeLogTag(DateTimeConverter.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    public Date convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        try {
            return dateFormat.parse(databaseValue);
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(Date entityProperty) {
        return entityProperty == null ? null : dateFormat.format(entityProperty);
    }

}
