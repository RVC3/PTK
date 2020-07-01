package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;

/**
 * DAO для таблицы НСИ <i>Exemptions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionDao extends BaseEntityDao<Exemption, Integer> {

    private static final String TAG = Logger.makeLogTag(ExemptionDao.class);

    public static final String TABLE_NAME = "Exemptions";

    public static class Properties {
        public static final String ExemptionExpressCode = "ExemptionExpressCode";
        public static final String NewExemptionExpressCode = "NewExemptionExpressCode";
        public static final String RegionOkatoCode = "RegionOkatoCode";
        public static final String ExemptionOrganizationCode = "ExemptionOrganizationCode";
        public static final String Percentage = "Percentage";
        public static final String ChildTicketAvailable = "ChildTicketAvailable";
        public static final String MassRegistryAvailable = "MassRegistryAvailable";
        public static final String CppkRegistryBan = "CppkRegistryBan";
        public static final String Presale7000WithPlace = "Presale7000WithPlace";
        public static final String Presale6000Once = "Presale6000Once";
        public static final String Presale6000Abonement = "Presale6000Abonement";
        public static final String Leavy = "Leavy";
        public static final String RequireSnilsNumber = "RequireSnilsNumber";
        public static final String RequireSocialCard = "RequireSocialCard";
        public static final String NotRequireFIO = "NotRequireFIO";
        public static final String NotRequireDocumentNumber = "NotRequireDocumentNumber";
        public static final String IsRegionOnly = "IsRegionOnly";
        public static final String ActiveFromDate = "ActiveFromDate";
        public static final String ActiveTillDate = "ActiveTillDate";
        public static final String ExemptionGroupCode = "ExemptionGroupCode";
        public static final String Name = "Name";
        public static final String Code = "Code";
        public static final String IsTakeProcessingFee = "IsTakeProcessingFee";
    }

    private SimpleDateFormat simpleDateFormat;

    public ExemptionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Exemption fromCursor(Cursor cursor) {
        Exemption out = new Exemption();
        int index = cursor.getColumnIndex(ExemptionDao.Properties.NewExemptionExpressCode);
        if (index != -1)
            out.setNewExemptionExpressCode(cursor.getInt(index));

        index = cursor.getColumnIndex(BaseEntityDao.Properties.VersionId);
        out.setVersionId(cursor.getInt(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.RegionOkatoCode);
        if (index != -1)
            if (!cursor.isNull(index))
                out.setRegionOkatoCode(cursor.getString(index));
            else
                out.setRegionOkatoCode(null);

        index = cursor.getColumnIndex(ExemptionDao.Properties.ExemptionOrganizationCode);
        if (index != -1)
            out.setExemptionOrganizationCode(cursor.getString(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.IsTakeProcessingFee);
        if (index != -1)
            out.setIsTakeProcessingFee(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.Percentage);
        if (index != -1)
            out.setPercentage(cursor.getInt(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.ChildTicketAvailable);
        if (index != -1)
            out.setChildTicketAvailable(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.MassRegistryAvailable);
        if (index != -1)
            out.setMassRegistryAvailable(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.CppkRegistryBan);
        if (index != -1)
            out.setCppkRegistryBan(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.Presale7000WithPlace);
        if (index != -1)
            out.setPresale7000WithPlace(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.Presale6000Once);
        if (index != -1)
            out.setPresale6000Once(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.Presale6000Abonement);
        if (index != -1)
            out.setPresale6000Abonement(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.Leavy);
        if (index != -1)
            out.setLeavy(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.RequireSnilsNumber);
        if (index != -1)
            out.setRequireSnilsNumber(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.RequireSocialCard);
        if (index != -1)
            out.setRequireSocialCard(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.NotRequireFIO);
        if (index != -1)
            out.setNotRequireFIO(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.NotRequireDocumentNumber);
        if (index != -1)
            out.setNotRequireDocumentNumber(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.IsRegionOnly);
        if (index != -1)
            out.setRegionOnly(cursor.getInt(index) == 1);

        index = cursor.getColumnIndex(ExemptionDao.Properties.ActiveFromDate);
        if (index != -1)
            out.setActiveFromDate(parseDate(cursor.getString(index)));

        index = cursor.getColumnIndex(ExemptionDao.Properties.ActiveTillDate);
        if (index != -1)
            out.setActiveTillDate(parseDate(cursor.getString(index)));

        index = cursor.getColumnIndex(ExemptionDao.Properties.ExemptionGroupCode);
        if (index != -1)
            if (!cursor.isNull(index))
                out.setExemptionGroupCode(cursor.getInt(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.Name);
        if (index != -1)
            out.setName(cursor.getString(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));

        index = cursor.getColumnIndex(ExemptionDao.Properties.ExemptionExpressCode);
        if (index != -1)
            out.setExemptionExpressCode(cursor.getInt(index));

        return out;
    }


    private synchronized Date parseDate(String dateString) {

        Date date = null;

        if (TextUtils.isEmpty(dateString)) {
            return date;
        }

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }

        return date;
    }
}
