package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TrainTicketTypeForTransferRegistration;

/**
 * DAO для таблицы НСИ <i>TrainTicketTypesForTransferRegistration</i>.
 *
 * @author Dmitry Vinogradov
 */
public class TrainTicketTypeForTransferRegistrationDao extends BaseEntityDao<TrainTicketTypeForTransferRegistration, Integer> {

    public static final String TABLE_NAME = "TrainTicketTypesForTransferRegistration";

    public static class Properties {
        public static final String TrainTicketTypeCode = "TrainTicketTypeCode";
        public static final String TransferTicketTypeCode = "TransferTicketTypeCode";
        public static final String TrainTicketWayTypeCode = "TrainTicketWayTypeCode";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public TrainTicketTypeForTransferRegistrationDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TrainTicketTypeForTransferRegistration fromCursor(Cursor cursor) {
        TrainTicketTypeForTransferRegistration entity = new TrainTicketTypeForTransferRegistration();
        entity.setTrainTicketTypeCode(cursor.getLong(cursor.getColumnIndex(Properties.TrainTicketTypeCode)));
        entity.setTrainTicketTypeCode(cursor.getLong(cursor.getColumnIndex(Properties.TransferTicketTypeCode)));
        int wayTypeIndex = cursor.getColumnIndex(Properties.TrainTicketWayTypeCode);
        if (!cursor.isNull(wayTypeIndex)) {
            entity.setTrainTicketWayTypeCode(cursor.getInt(wayTypeIndex));
        }
        return entity;
    }

}
