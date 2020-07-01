package ru.ppr.nsi.repository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BannedDeviceExemptionDao;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.ExemptionDao;
import ru.ppr.nsi.dao.ExemptionsToRegionDao;
import ru.ppr.nsi.dao.RegionDao;
import ru.ppr.nsi.entity.DeviceType;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.Region;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ExemptionRepository extends BaseRepository<Exemption, Integer> {

    private final SimpleDateFormat simpleDateFormat;

    @Inject
    ExemptionRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected BaseEntityDao<Exemption, Integer> selfDao() {
        return daoSession().getExemptionDao();
    }


    public String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * Ищет льготу для региона по экспресс-коду
     *
     * @param exemptionExpressCode
     * @param regionCode
     * @param date
     * @return
     */
    public Exemption getExemptionForRegion(int exemptionExpressCode, int regionCode, Date date, int nsiVersion) {
        List<Exemption> exemptions = getActualExemptionsForRegion(exemptionExpressCode, regionCode, date, nsiVersion);
        return exemptions.isEmpty() ? null : exemptions.get(0);
    }

    /**
     * Ищет все подходящие льготы для региона по экспресс-коду
     *
     * @param exemptionExpressCode
     * @param regionCode
     * @param date
     * @return
     */
    public List<Exemption> getActualExemptionsForRegion(int exemptionExpressCode, int regionCode, Date date, int nsiVersion) {

        /**
         * Ниже формируется такой скрипт:
         *
         SELECT
         CASE
         WHEN Exemptions.RegionOkatoCode IS NULL THEN 1
         WHEN Exemptions.RegionOkatoCode = 38000 THEN 1
         WHEN Regions.RegionOkatoCode IS NOT NULL
         AND Regions.RegionOkatoCode = 38000 THEN 1
         ELSE 0
         END AS ForThatRegion,
         CASE
         WHEN Exemptions.ActiveFromDate < '2015-12-31 22:00:00'
         AND (Exemptions.ActiveTillDate > '2015-12-31 22:00:00'
         OR ActiveTillDate IS NULL) THEN 1
         WHEN Exemptions.NewExemptionExpressCode IS NOT NULL THEN 1
         ELSE 0
         END AS ValidTimeOrNewCode,
         CASE
         WHEN Exemptions.RegionOkatoCode IS NULL THEN 3
         WHEN Exemptions.RegionOkatoCode = 38000 THEN 2
         WHEN Regions.RegionOkatoCode IS NOT NULL
         AND Regions.RegionOkatoCode = 38000 THEN 1
         ELSE 0
         END AS RegionLevel,
         CASE
         WHEN Exemptions.ActiveFromDate < '2015-12-31 22:00:00'
         AND (Exemptions.ActiveTillDate > '2015-12-31 22:00:00'
         OR ActiveTillDate IS NULL) THEN 2
         WHEN Exemptions.NewExemptionExpressCode IS NOT NULL THEN 1
         ELSE 0
         END AS ValidTimeLevel,
         Exemptions.*
         FROM Exemptions
         LEFT JOIN ExemptionsToRegions ON Exemptions.Code = ExemptionsToRegions.ExemptionCode
         AND Exemptions.IsRegionOnly = 0
         AND Exemptions.RegionOkatoCode IS NOT NULL
         LEFT JOIN Regions ON ExemptionsToRegions.RegionCode = Regions.Code
         WHERE 1 = 1
         AND Exemptions.DeleteInVersionId IS NULL
         AND ExemptionsToRegions.DeleteInVersionId IS NULL
         AND Regions.DeleteInVersionId IS NULL
         AND Exemptions.ExemptionExpressCode = 2605 --AND ExemptionExpressCode = 3109
         ORDER BY
         ForThatRegion DESC,
         ValidTimeOrNewCode DESC,
         RegionLevel DESC,
         ValidTimeLevel DESC,
         Exemptions.ActiveFromDate DESC
         */

        List<String> selectionArgsList = new ArrayList<>();

        Region region = daoSession().getRegionDao().load(regionCode, nsiVersion);
        String dateString = formatDate(date);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        {
            stringBuilder.append(" CASE ");
            {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.RegionOkatoCode).append(" IS NULL");
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".")
                        .append(RegionDao.Properties.RegionOkatoCode).append(" IS NOT NULL");
                stringBuilder.append(" AND ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".")
                        .append(RegionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" ELSE 0 ");
            }
            stringBuilder.append(" END  ").append(" AS ").append("ForThatRegion").append(",");
            stringBuilder.append(" CASE ");
            {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveFromDate).append(" < ").append("?");
                selectionArgsList.add(dateString);
                stringBuilder.append(" AND ");
                stringBuilder.append(" ( ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveTillDate).append(" > ").append("?");
                selectionArgsList.add(dateString);
                stringBuilder.append(" OR ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveTillDate).append(" IS NULL");
                stringBuilder.append(" ) ");
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.NewExemptionExpressCode).append(" IS NOT NULL");
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" ELSE 0 ");
            }
            stringBuilder.append(" END ").append(" AS ").append("ValidTimeOrNewCode").append(",");
            stringBuilder.append(" CASE ");
            {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.RegionOkatoCode).append(" IS NULL");
                stringBuilder.append(" THEN 3 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 2 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".")
                        .append(RegionDao.Properties.RegionOkatoCode).append(" IS NOT NULL");
                stringBuilder.append(" AND ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".")
                        .append(RegionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" ELSE 0 ");
            }
            stringBuilder.append(" END  ").append(" AS ").append("RegionLevel").append(",");
            stringBuilder.append(" CASE ");
            {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveFromDate).append(" < ").append("?");
                selectionArgsList.add(dateString);
                stringBuilder.append(" AND ");
                stringBuilder.append(" ( ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveTillDate).append(" > ").append("?");
                selectionArgsList.add(dateString);
                stringBuilder.append(" OR ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.ActiveTillDate).append(" IS NULL");
                stringBuilder.append(" ) ");
                stringBuilder.append(" THEN 2 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".")
                        .append(ExemptionDao.Properties.NewExemptionExpressCode).append(" IS NOT NULL");
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" ELSE 0 ");
            }
            stringBuilder.append(" END ").append(" AS ").append("ValidTimeLevel").append(",");
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append("*");
        }
        stringBuilder.append(" FROM ");
        stringBuilder.append(ExemptionDao.TABLE_NAME);
        stringBuilder.append(" LEFT JOIN ");
        stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        {
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.Code);
            stringBuilder.append(" = ");
            stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(ExemptionsToRegionDao.Properties.ExemptionCode);
            stringBuilder.append(" AND ");
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.IsRegionOnly).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(0));
            stringBuilder.append(" AND ");
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.RegionOkatoCode).append(" IS NOT NULL");
        }
        stringBuilder.append(" LEFT JOIN ");
        stringBuilder.append(RegionDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        {
            stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(ExemptionsToRegionDao.Properties.RegioneCode);
            stringBuilder.append(" = ");
            stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(RegionDao.Properties.Code);
        }
        stringBuilder.append(" WHERE ");
        stringBuilder.append(" ( ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(ExemptionDao.TABLE_NAME, nsiVersion));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(" ( ");
        stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(ExemptionsToRegionDao.TABLE_NAME, nsiVersion));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(" ( ");
        stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(RegionDao.TABLE_NAME, nsiVersion));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.ExemptionExpressCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(exemptionExpressCode));
        stringBuilder.append(" ORDER BY ");
        stringBuilder.append(" ForThatRegion DESC ").append(",").append(" ValidTimeOrNewCode DESC ").append(",");
        stringBuilder.append(" RegionLevel DESC ").append(",").append(" ValidTimeLevel DESC ").append(",");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.ActiveFromDate).append(" DESC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<Exemption> exemptions = new ArrayList<>();
        Set<Integer> newExemptionCodes = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                /**
                 * RegionLevel:
                 * 3 - Федеральная
                 * 2 - Этот регион
                 * 1 - От другого региона
                 * 0 - Не подходит
                 *
                 * ValidTimeLevel:
                 * 2 - Время валидное
                 * 1 - Время невалидное, но есть newExemptionExpressCode
                 * 0 - Не подходит
                 */
                int regionLevel = cursor.getInt(cursor.getColumnIndex("RegionLevel"));
                int validTimeLevel = cursor.getInt(cursor.getColumnIndex("ValidTimeLevel"));
                boolean isForThatRegion = cursor.getInt(cursor.getColumnIndex("ForThatRegion")) == 1;
                boolean isValidTimeOrNewCode = cursor.getInt(cursor.getColumnIndex("ValidTimeOrNewCode")) == 1;
                if (isForThatRegion) {
                    // Можно использовать в нашем регионе
                    if (isValidTimeOrNewCode) {
                        Exemption exemption = selfDao().fromCursor(cursor);
                        if (validTimeLevel == 2) {
                            // Льгота актуальна, счиатем кандидатом
                            exemptions.add(exemption);
                        } else if (validTimeLevel == 1) {
                            // Есть новый код, проверим его рекурсивно, если ещё не проверяли
                            if (!newExemptionCodes.contains(exemption.getNewExemptionExpressCode())) {
                                List<Exemption> exemptionsWithNewCode = getActualExemptionsForRegion(exemption.getNewExemptionExpressCode(), regionCode, date, nsiVersion);
                                exemptions.addAll(exemptionsWithNewCode);
                                newExemptionCodes.add(exemption.getNewExemptionExpressCode());
                            }
                        }
                    } else {
                        // Льгота неактуальная и не имеет нового кода
                        // Аналогиченое верно для остальных записей благодая сортировке
                        // Выходим
                        if (cursor.getPosition() == 0) {
                            // Есть льгота для региона, но с кривым временем
                            // Т.к. лучше вариантов у нас нет, вернем её
                            Exemption exemption = selfDao().fromCursor(cursor);
                            exemptions.add(exemption);
                        }
                        break;
                    }
                } else {
                    // Нельзя использовать в нашем регионе
                    // Аналогиченое верно для остальных записей благодая сортировке
                    // Выходим
                    if (cursor.getPosition() == 0) {
                        // Есть льгота, но не подходит нашему региону
                        // Т.к. лучше вариантов у нас нет, вернем её
                        Exemption exemption = selfDao().fromCursor(cursor);
                        exemptions.add(exemption);
                    }
                    break;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exemptions;
    }

    /**
     * Выполянет посик льготы по коду и времени начала действия
     *
     * @param code           Код льготы
     * @param activeFromDate Время начала действия
     * @param versionId
     * @return
     */
    public Exemption getExemption(int code, Date activeFromDate, long versionId) {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append("*");
        stringBuilder.append(" FROM ");
        /////////////////////////////////////
        stringBuilder.append(ExemptionDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(ExemptionDao.Properties.Code).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(code));
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionDao.Properties.ActiveFromDate).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(formatDate(activeFromDate)));
        stringBuilder.append(" AND ");
        stringBuilder.append(BaseEntityDao.Properties.VersionId).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Exemption exemption = null;
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                exemption = selfDao().fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exemption;
    }

    /**
     * Выполняет проверку валидности региона для данной льготы
     *
     * @param exemptionCode Код льготы
     * @param regionCode    Код региона
     * @param versionId
     * @return
     */
    public boolean isExemptionSupportedInRegion(int exemptionCode, int regionCode, int versionId) {
        /**
         * Ниже формируется такой скрипт:
         *
         SELECT
         CASE
         WHEN Exemptions.RegionOkatoCode IS NULL THEN 1
         WHEN Exemptions.RegionOkatoCode = 38000 THEN 1
         WHEN Regions.RegionOkatoCode IS NOT NULL
         AND Regions.RegionOkatoCode = 38000 THEN 1
         ELSE 0
         END AS ForThatRegion
         FROM Exemptions
         LEFT JOIN ExemptionsToRegions ON Exemptions.Code = ExemptionsToRegions.ExemptionCode
         AND Exemptions.IsRegionOnly = 0
         AND Exemptions.RegionOkatoCode IS NOT NULL
         LEFT JOIN Regions ON ExemptionsToRegions.RegionCode = Regions.Code
         WHERE 1 = 1
         AND Exemptions.DeleteInVersionId IS NULL
         AND ExemptionsToRegions.DeleteInVersionId IS NULL
         AND Regions.DeleteInVersionId IS NULL
         AND Exemptions.ExemptionExpressCode = 2605 --AND ExemptionExpressCode = 3109
         AND ForThatRegion = 1
         LIMIT 1
         */

        //http://agile.srvdev.ru/browse/CPPKPP-33942
        List<String> selectionArgsList = new ArrayList<>();

        Region region = daoSession().getRegionDao().load(regionCode, versionId);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        {
            stringBuilder.append(" CASE ");
            {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.RegionOkatoCode).append(" IS NULL");
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" WHEN ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(RegionDao.Properties.RegionOkatoCode).append(" IS NOT NULL");
                stringBuilder.append(" AND ");
                stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(RegionDao.Properties.RegionOkatoCode).append(" = ").append("?");
                selectionArgsList.add(region.getRegionOkatoCode());
                stringBuilder.append(" THEN 1 ");
                ///
                stringBuilder.append(" ELSE 0 ");
            }
            stringBuilder.append(" END  ").append(" AS ").append("ForThatRegion");
        }
        stringBuilder.append(" FROM ");
        stringBuilder.append(ExemptionDao.TABLE_NAME);
        stringBuilder.append(" LEFT JOIN ");
        stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        {
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.Code);
            stringBuilder.append(" = ");
            stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(ExemptionsToRegionDao.Properties.ExemptionCode);
            stringBuilder.append(" AND ");
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.IsRegionOnly).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(0));
            stringBuilder.append(" AND ");
            stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.RegionOkatoCode).append(" IS NOT NULL");
        }
        stringBuilder.append(" LEFT JOIN ");
        stringBuilder.append(RegionDao.TABLE_NAME);
        stringBuilder.append(" ON ");
        {
            stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(ExemptionsToRegionDao.Properties.RegioneCode);
            stringBuilder.append(" = ");
            stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(RegionDao.Properties.Code);
        }
        stringBuilder.append(" WHERE ");
        stringBuilder.append(" ( ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(ExemptionDao.TABLE_NAME, versionId));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(" ( ");
        stringBuilder.append(ExemptionsToRegionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(ExemptionsToRegionDao.TABLE_NAME, versionId));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(" ( ");
        stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.VersionId).append(" IS NULL");
        stringBuilder.append(" OR ");
        stringBuilder.append(NsiUtils.checkVersion(RegionDao.TABLE_NAME, versionId));
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(ExemptionDao.TABLE_NAME).append(".").append(ExemptionDao.Properties.Code).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(exemptionCode));
        stringBuilder.append(" AND ");
        stringBuilder.append("ForThatRegion").append(" = ").append(1);
        //selectionArgsList.add(String.valueOf(1));
        stringBuilder.append(" LIMIT ").append(1);

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        boolean isSupported = false;
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                isSupported = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isSupported;
    }

    /**
     * Возвращает номер региона, для которого дейсвтует льгота
     *
     * @param exemptionCode код льготы
     * @param versionId     версия НСИ
     * @return
     */
    public
    @NonNull
    List<Integer> getRegionsExemption(int exemptionCode,
                                      int versionId) {

        List<Integer> regionsCode = new ArrayList<>();

        Cursor cursor = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ExemptionsToRegionDao.Properties.ExemptionCode).append(" = ").append(exemptionCode)
                .append(NsiUtils.checkVersion(ExemptionsToRegionDao.TABLE_NAME, versionId));

        String[] columns = new String[]{ExemptionsToRegionDao.Properties.RegioneCode};
        try {
            cursor = daoSession().getNsiDb().query(ExemptionsToRegionDao.TABLE_NAME, columns, stringBuilder.toString(),
                    null, null, null, null);
            while (cursor.moveToNext())
                regionsCode.add(cursor.getInt(0));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return regionsCode;
    }

    /**
     * Проверка действительности льготы на ПТК
     *
     * @param exemption     льгота
     * @param regionCode    код региона
     * @param trainCategory типа поезда
     * @return
     */
    public boolean isBannedDeviceForExemption(Exemption exemption, int regionCode, TrainCategory trainCategory, int versionId) {

        boolean out = false;

        Cursor cursor = null;
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("select count() from ");
        stringBuilder.append(BannedDeviceExemptionDao.TABLE_NAME);
        stringBuilder.append(" where ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.DeviceTypeCode).append("=").append(DeviceType.Ptk.getCode());
        stringBuilder.append(" and ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.ExemptionCode).append("=").append(exemption.getExemptionExpressCode());
        stringBuilder.append(" and ");
        if (exemption.getRegionOkatoCode() == null)
            stringBuilder.append(BannedDeviceExemptionDao.Properties.RegionOkatoCode + " is null ");
        else
            stringBuilder.append(BannedDeviceExemptionDao.Properties.RegionOkatoCode).append("=").append(exemption.getRegionOkatoCode());
        stringBuilder.append(" and ");
        stringBuilder.append(" ( ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.RegionCode).append("=").append(regionCode);
        stringBuilder.append(" or ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.RegionCode + " is null ");
        stringBuilder.append(" ) ");
        stringBuilder.append(" and ");
        stringBuilder.append(" ( ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.TrainCategory).append(" = '")
                .append(trainCategory.category).append("' or ");
        stringBuilder.append(BannedDeviceExemptionDao.Properties.TrainCategory + " is null ");
        stringBuilder.append(" ) ");
        stringBuilder.append(" AND ");
        stringBuilder.append(NsiUtils.checkVersion(BannedDeviceExemptionDao.TABLE_NAME, versionId));

        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                out = cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }

}
