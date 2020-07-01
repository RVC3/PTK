package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.SeasonTicket;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Dao для сущности {@link TicketSaleReturnEventBase}
 * <p>
 * Created by Dmitry Nevolin on 12.02.2016.
 */
public class TicketSaleReturnEventBaseDao extends BaseEntityDao<TicketSaleReturnEventBase, Long> {

    private static final String TAG = Logger.makeLogTag(TicketSaleReturnEventBaseDao.class);

    public static final String TABLE_NAME = "TicketSaleReturnEventBase";

    public static class Properties {
        public static final String TicketEventBaseId = "TicketEventBaseId";
        public static final String ParentTicketId = "ParentTicketId";
        public static final String LegalEntityId = "LegalEntityId"; // хз что это и нужно ли это, в файле с миграцией не нашел такого поля в этой таблице
        public static final String ExemptionId = "ExemptionId";
        public static final String CheckId = "CheckId";
        public static final String TrainInfoId = "TrainInfoId";
        public static final String SeasonTicketId = "SeasonTicketId";
        public static final String PriceId = "PriceId";
        public static final String FeeId = "FeeId";
        public static final String AdditionInfoForEttId = "AdditionInfoForEttId";
        public static final String BankTransactionCashRegisterEventId = "BankTransactionCashRegisterEventId";
        public static final String Kind = "Kind";
        public static final String IsOneTimeTicket = "IsOneTimeTicket";
        public static final String PaymentTypeCode = "PaymentTypeCode";
        public static final String IsTicketWritten = "IsTicketWritten";
    }


    public TicketSaleReturnEventBaseDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.TicketEventBaseId, TicketEventBaseDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.ParentTicketId, ParentTicketInfoDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.BankTransactionCashRegisterEventId, BankTransactionDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.AdditionInfoForEttId, AdditionalInfoForEttDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.ExemptionId, ExemptionDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CheckId, CheckDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TrainInfoId, TrainInfoDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.SeasonTicketId, SeasonTicketDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.PriceId, PriceDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.FeeId, FeeDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.LegalEntityId, LegalEntityDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TicketSaleReturnEventBase fromCursor(Cursor cursor) {
        TicketSaleReturnEventBase ticketSaleReturnEventBase = new TicketSaleReturnEventBase();
        ticketSaleReturnEventBase.setId(cursor.getLong(
                cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        ticketSaleReturnEventBase.setTicketEventBaseId(cursor.getLong(
                cursor.getColumnIndex(Properties.TicketEventBaseId)));
        ticketSaleReturnEventBase.setCheckId(cursor.getLong(
                cursor.getColumnIndex(Properties.CheckId)));
        ticketSaleReturnEventBase.setKind(TicketKind.getTicketKind(cursor.getInt(
                cursor.getColumnIndex(Properties.Kind))));
        ticketSaleReturnEventBase.setOneTimeTicket(cursor.getInt(
                cursor.getColumnIndex(Properties.IsOneTimeTicket)) == 1);
        ticketSaleReturnEventBase.setFullPriceId(cursor.getLong(
                cursor.getColumnIndex(Properties.PriceId)));
        ticketSaleReturnEventBase.setPaymentMethod(PaymentType.valueOf(cursor.getInt(
                cursor.getColumnIndex(Properties.PaymentTypeCode))));
        ticketSaleReturnEventBase.setLegalEntityId(cursor.getLong(
                cursor.getColumnIndex(Properties.LegalEntityId)));
        ticketSaleReturnEventBase.setTrainInfoId(cursor.getLong(
                cursor.getColumnIndex(Properties.TrainInfoId)));
        ticketSaleReturnEventBase.setFeeId(cursor.getLong(
                cursor.getColumnIndex(Properties.FeeId)));
        ticketSaleReturnEventBase.setTicketWritten(cursor.getInt(
                cursor.getColumnIndex(Properties.IsTicketWritten)) == 1);

        if (!cursor.isNull(cursor.getColumnIndex(Properties.AdditionInfoForEttId))) {
            ticketSaleReturnEventBase.setAdditionalInfoForEttId(cursor.getLong(
                    cursor.getColumnIndex(Properties.AdditionInfoForEttId)));
        }

        if (!cursor.isNull(cursor.getColumnIndex(Properties.ParentTicketId))) {
            ticketSaleReturnEventBase.setParentTicketInfoId(cursor.getLong(
                    cursor.getColumnIndex(Properties.ParentTicketId)));
        }

        if (!cursor.isNull(cursor.getColumnIndex(Properties.ExemptionId))) {
            ticketSaleReturnEventBase.setExemptionForEventId(cursor.getLong(
                    cursor.getColumnIndex(Properties.ExemptionId)));
        }

        if (!cursor.isNull(cursor.getColumnIndex(Properties.SeasonTicketId))) {
            ticketSaleReturnEventBase.setSeasonTicketId(cursor.getLong(
                    cursor.getColumnIndex(Properties.SeasonTicketId)));
        }

        if (!cursor.isNull(
                cursor.getColumnIndex(Properties.BankTransactionCashRegisterEventId))) {

            ticketSaleReturnEventBase.setBankTransactionEventId(cursor.getLong(
                    cursor.getColumnIndex(Properties.BankTransactionCashRegisterEventId)));
        }

        return ticketSaleReturnEventBase;
    }

    @Override
    public ContentValues toContentValues(TicketSaleReturnEventBase entity) {
        ContentValues contentValues = new ContentValues();

        ExemptionForEvent exemptionForEvent = getLocalDaoSession().exemptionDao().load(entity.getExemptionForEventId());
        if (exemptionForEvent != null)
            contentValues.put(Properties.ExemptionId, exemptionForEvent.getId());

        ParentTicketInfo parentTicketInfo = getLocalDaoSession().getParentTicketInfoDao().load(entity.getParentTicketInfoId());
        if (parentTicketInfo != null)
            contentValues.put(Properties.ParentTicketId, parentTicketInfo.getId());

        BankTransactionEvent bankTransactionEvent = getLocalDaoSession().getBankTransactionDao().load(entity.getBankTransactionEventId());
        if (bankTransactionEvent != null)
            contentValues.put(Properties.BankTransactionCashRegisterEventId, bankTransactionEvent.getId());

        TicketEventBase ticketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(entity.getTicketEventBaseId());
        if (ticketEventBase != null)
            contentValues.put(Properties.TicketEventBaseId, ticketEventBase.getId());

        LegalEntity legalEntity = getLocalDaoSession().legalEntityDao().load(entity.getLegalEntityId());
        if (legalEntity != null)
            contentValues.put(Properties.LegalEntityId, legalEntity.getId());

        Check check = getLocalDaoSession().getCheckDao().load(entity.getCheckId());
        if (check != null)
            contentValues.put(Properties.CheckId, check.getId());

        Price fullPrice = getLocalDaoSession().getPriceDao().load(entity.getFullPriceId());
        if (fullPrice != null)
            contentValues.put(Properties.PriceId, fullPrice.getId());

        if (entity.getKind() != null)
            contentValues.put(Properties.Kind, entity.getKind().getTicketKind());

        AdditionalInfoForEtt additionalInfoForEtt = getLocalDaoSession().getAdditionalInfoForEttDao().load(entity.getAdditionalInfoForEttId());
        if (additionalInfoForEtt != null)
            contentValues.put(Properties.AdditionInfoForEttId, additionalInfoForEtt.getId());

        SeasonTicket seasonTicket = getLocalDaoSession().seasonTicketDao().load(entity.getSeasonTicketId());
        if (seasonTicket != null)
            contentValues.put(Properties.SeasonTicketId, seasonTicket.getId());

        TrainInfo trainInfo = getLocalDaoSession().trainInfoDao().load(entity.getTrainInfoId());
        if (trainInfo != null)
            contentValues.put(Properties.TrainInfoId, trainInfo.getId());

        Fee fee = getLocalDaoSession().getFeeDao().load(entity.getFeeId());
        if (fee != null)
            contentValues.put(Properties.FeeId, fee.getId());

        contentValues.put(Properties.IsOneTimeTicket, entity.isOneTimeTicket());
        contentValues.put(Properties.IsTicketWritten, entity.isTicketWritten());
        contentValues.put(Properties.PaymentTypeCode, entity.getPaymentMethod().getCode());

        return contentValues;
    }

    @Override
    public Long getKey(TicketSaleReturnEventBase entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull TicketSaleReturnEventBase entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }
}
