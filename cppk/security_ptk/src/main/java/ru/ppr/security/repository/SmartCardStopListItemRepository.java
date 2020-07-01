package ru.ppr.security.repository;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.QueryBuilder;
import ru.ppr.security.SecurityDbSessionManager;
import ru.ppr.security.dao.BaseEntityDao;
import ru.ppr.security.dao.SmartCardStopListItemDao;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;
import ru.ppr.security.repository.base.BaseRepository;
import ru.ppr.utils.UnsignedLongUtils;

/**
 * @author Aleksandr Brazhkin
 */
@Singleton
public class SmartCardStopListItemRepository extends BaseRepository<SmartCardStopListItem, Void> {

    @Inject
    SmartCardStopListItemRepository(SecurityDbSessionManager securityDbSessionManager) {
        super(securityDbSessionManager);
    }

    @Override
    protected BaseEntityDao<SmartCardStopListItem, Void> selfDao() {
        return daoSession().getSmartCardStopListItemDao();
    }

    /**
     * Производит поиск БСК по стоп листам.
     *
     * @param smartCardTypeCode   Код типа смарт-карты
     * @param crystalSerialNumber Серийный номер кристалла
     * @param outerNumber         Внешний номер
     * @param stopCriteriaTypes
     * @return Информациия о постановке в стоп-лист.
     */
    public SmartCardStopListItem findStopListItemForSmartCard(long smartCardTypeCode, @Nullable String crystalSerialNumber, @Nullable String outerNumber, @Nullable EnumSet<StopCriteriaType> stopCriteriaTypes) {

        QueryBuilder qb = new QueryBuilder();

        qb.selectAll().from(SmartCardStopListItemDao.TABLE_NAME).where();
        qb.field(SmartCardStopListItemDao.Properties.SMART_CARD_TYPE_CODE).eq(smartCardTypeCode);
        if (crystalSerialNumber != null) {
            qb.and().field(SmartCardStopListItemDao.Properties.CRYSTAL_SERIAL_NUMBER).eq(crystalSerialNumber);
        }
        if (outerNumber != null) {
            long signedOuterNumber = UnsignedLongUtils.parseUnsignedLong(outerNumber, 10);
            qb.and().field(SmartCardStopListItemDao.Properties.OUTER_NUMBER).eq(signedOuterNumber);
        }

        if (stopCriteriaTypes != null) {
            List<Integer> codes = new ArrayList<>();
            for (StopCriteriaType stopCriteriaType : stopCriteriaTypes) {
                codes.add(stopCriteriaType.getCode());
            }
            qb.and().field(SmartCardStopListItemDao.Properties.STOP_CRITERIA_TYPE).in(codes);
        }

        SmartCardStopListItem smartCardStopListItem = null;

        Cursor cursor = qb.build().run(daoSession().getSecurityDb());
        try {
            if (cursor.moveToFirst()) {
                smartCardStopListItem = selfDao().fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return smartCardStopListItem;
    }

}
