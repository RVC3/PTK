package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Direction;

/**
 * DAO для таблицы НСИ <i>Directions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class DirectionDao extends BaseEntityDao<Direction, Long> {

    public static final String TABLE_NAME = "Directions";

    public static class Properties {
        public static final String Code = "Code";
        public static final String Name = "Name";
    }

    public DirectionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Direction fromCursor(Cursor cursor) {
        Direction direction = new Direction();

        direction.setCode(cursor.getLong(cursor.getColumnIndex(Properties.Code)));
        direction.setName(cursor.getString(cursor.getColumnIndex(Properties.Name)));

        return direction;
    }

}
