package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TrainInfoDao;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.kpp.model.TrainInfo;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TrainInfoLoader extends BaseLoader {

    public TrainInfoLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column TRAIN_CATEGORY = new Column(0, TrainInfoDao.Properties.TrainCategory);
        static final Column CAR_CLASS = new Column(1, TrainInfoDao.Properties.CarClass);
        static final Column TRAIN_CATEGORY_CODE = new Column(2, TrainInfoDao.Properties.TrainCategoryCode);

        public static Column[] all = new Column[]{
                TRAIN_CATEGORY,
                CAR_CLASS,
                TRAIN_CATEGORY_CODE
        };
    }

    public TrainInfo load(Cursor cursor, Offset offset) {
        TrainInfo trainInfo = new TrainInfo();
        trainInfo.TrainCategory = cursor.getString(offset.value + Columns.TRAIN_CATEGORY.index);
        trainInfo.CarClass = cursor.getString(offset.value + Columns.CAR_CLASS.index);
        trainInfo.TrainCategoryCode = cursor.isNull(offset.value + Columns.TRAIN_CATEGORY_CODE.index) ? null : cursor.getInt(offset.value + Columns.TRAIN_CATEGORY_CODE.index);
        offset.value += Columns.all.length;
        return trainInfo;
    }
}