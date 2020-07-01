package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>TrainInfo</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TrainInfoDao extends BaseEntityDao<TrainInfo, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(TrainInfoDao.class);

    public static final String TABLE_NAME = "TrainInfo";

    public static class Properties {
        public static final String Id = "_id";
        public static final String TrainCategory = "TrainCategory";
        public static final String CarClass = "CarClass";
        public static final String TrainCategoryCode = "TrainCategoryCode";
    }

    public TrainInfoDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TrainInfo fromCursor(Cursor cursor) {
        TrainInfo out = new TrainInfo();
        out.setId(cursor.getInt(cursor.getColumnIndex(Properties.Id)));
        out.setTrainCategory(cursor.getString(cursor.getColumnIndex(Properties.TrainCategory)));
        out.setTrainCategoryCode(cursor.getInt(cursor.getColumnIndex(Properties.TrainCategoryCode)));
        out.setCarClass(cursor.getString(cursor.getColumnIndex(Properties.CarClass)));
        return out;
    }

    @Override
    public ContentValues toContentValues(TrainInfo entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CarClass, entity.getCarClass());
        contentValues.put(Properties.TrainCategory, entity.getTrainCategory());
        contentValues.put(Properties.TrainCategoryCode, entity.getTrainCategoryCode());
        return contentValues;
    }

    @Override
    public Long getKey(TrainInfo entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull TrainInfo entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем стандартный алгоритм сборщика мусора, удаляющий записи, на кторые нет ссылок
        return false;
    }

}
