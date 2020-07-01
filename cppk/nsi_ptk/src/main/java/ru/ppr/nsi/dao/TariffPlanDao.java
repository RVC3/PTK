package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * DAO для таблицы НСИ <i>TariffPlans</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffPlanDao extends BaseEntityDao<TariffPlan, Integer> {

    public static final String TABLE_NAME = "TariffPlans";

    public static class Properties {
        public static final String TrainCategoryCode = "TrainCategoryCode";
        public static final String Code = "Code";
        public static final String IsSurcharge = "IsSurcharge";
        public static final String CarrierCode = "CarrierCode";
        public static final String ShortName = "ShortName";
    }

    public TariffPlanDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public TariffPlan load(Integer code, int versionId) {
        TariffPlan loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TariffPlan fromCursor(Cursor cursor) {
        TariffPlan out = new TariffPlan();

        int index = cursor.getColumnIndex(TariffPlanDao.Properties.TrainCategoryCode);
        if (index != -1)
            out.setTrainCategoryCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffPlanDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TariffPlanDao.Properties.ShortName);
        if (index != -1)
            out.setShortName(cursor.getString(index));

        index = cursor.getColumnIndex(TariffPlanDao.Properties.IsSurcharge);
        if (index != -1)
            out.setSurcharge(cursor.getInt(index) != 0);

        index = cursor.getColumnIndex(TariffPlanDao.Properties.CarrierCode);
        if (index != -1)
            out.setCarrierCode(cursor.getString(index));

        // out.versionId =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.versionId));
        // out.deleteInVersion =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.deleteInVersion));
        return out;
    }

}
