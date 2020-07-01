package ru.ppr.cppk.db.migration.base;

import android.content.Context;
import android.database.Cursor;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.local.LocalDbVersionDao;
import ru.ppr.cppk.db.migration.MigrationVV100;
import ru.ppr.cppk.db.migration.MigrationVV101;
import ru.ppr.cppk.db.migration.MigrationVV102;
import ru.ppr.cppk.db.migration.MigrationVV103;
import ru.ppr.cppk.db.migration.MigrationVV104;
import ru.ppr.cppk.db.migration.MigrationVV105;
import ru.ppr.cppk.db.migration.MigrationVV106;
import ru.ppr.cppk.db.migration.MigrationVV46;
import ru.ppr.cppk.db.migration.MigrationVV47;
import ru.ppr.cppk.db.migration.MigrationVV48;
import ru.ppr.cppk.db.migration.MigrationVV49;
import ru.ppr.cppk.db.migration.MigrationVV50;
import ru.ppr.cppk.db.migration.MigrationVV51;
import ru.ppr.cppk.db.migration.MigrationVV52;
import ru.ppr.cppk.db.migration.MigrationVV53;
import ru.ppr.cppk.db.migration.MigrationVV54;
import ru.ppr.cppk.db.migration.MigrationVV55;
import ru.ppr.cppk.db.migration.MigrationVV56;
import ru.ppr.cppk.db.migration.MigrationVV57;
import ru.ppr.cppk.db.migration.MigrationVV58;
import ru.ppr.cppk.db.migration.MigrationVV59;
import ru.ppr.cppk.db.migration.MigrationVV60;
import ru.ppr.cppk.db.migration.MigrationVV61;
import ru.ppr.cppk.db.migration.MigrationVV62;
import ru.ppr.cppk.db.migration.MigrationVV63;
import ru.ppr.cppk.db.migration.MigrationVV64;
import ru.ppr.cppk.db.migration.MigrationVV65;
import ru.ppr.cppk.db.migration.MigrationVV66;
import ru.ppr.cppk.db.migration.MigrationVV67;
import ru.ppr.cppk.db.migration.MigrationVV68;
import ru.ppr.cppk.db.migration.MigrationVV69;
import ru.ppr.cppk.db.migration.MigrationVV70;
import ru.ppr.cppk.db.migration.MigrationVV71;
import ru.ppr.cppk.db.migration.MigrationVV72;
import ru.ppr.cppk.db.migration.MigrationVV73;
import ru.ppr.cppk.db.migration.MigrationVV74;
import ru.ppr.cppk.db.migration.MigrationVV75;
import ru.ppr.cppk.db.migration.MigrationVV76;
import ru.ppr.cppk.db.migration.MigrationVV77;
import ru.ppr.cppk.db.migration.MigrationVV78;
import ru.ppr.cppk.db.migration.MigrationVV79;
import ru.ppr.cppk.db.migration.MigrationVV80;
import ru.ppr.cppk.db.migration.MigrationVV81;
import ru.ppr.cppk.db.migration.MigrationVV82;
import ru.ppr.cppk.db.migration.MigrationVV83;
import ru.ppr.cppk.db.migration.MigrationVV84;
import ru.ppr.cppk.db.migration.MigrationVV85;
import ru.ppr.cppk.db.migration.MigrationVV86;
import ru.ppr.cppk.db.migration.MigrationVV87;
import ru.ppr.cppk.db.migration.MigrationVV88;
import ru.ppr.cppk.db.migration.MigrationVV89;
import ru.ppr.cppk.db.migration.MigrationVV90;
import ru.ppr.cppk.db.migration.MigrationVV91;
import ru.ppr.cppk.db.migration.MigrationVV92;
import ru.ppr.cppk.db.migration.MigrationVV93;
import ru.ppr.cppk.db.migration.MigrationVV94;
import ru.ppr.cppk.db.migration.MigrationVV95;
import ru.ppr.cppk.db.migration.MigrationVV96;
import ru.ppr.cppk.db.migration.MigrationVV97;
import ru.ppr.cppk.db.migration.MigrationVV98;
import ru.ppr.cppk.db.migration.MigrationVV99;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;

@LoggerAspect.IncludeClass
public class MigrationManager {

    private static final String TAG = Logger.makeLogTag(MigrationManager.class);

    private static final int REQUIRED_DATABASE_VERSION = 106;
    //последняя версия бд старой миграции (которая стирала все данные)
    //так как данные нам больше тереть нельзя мы ей не пользуемся,
    //но если на устройстве слишком старая версия (до 46) то старую миграцию делать придётся.
    private static final int OLD_MIGRATION_LAST_STRUCTURE_DATABASE_VERSION = 46;

    public static void checkVersionAndMakeUpdateLocalDb(Database localDB) throws MigrationException {

        int currentLocalDbVersionAtStart = getVersionLocalDb(localDB);

        if (currentLocalDbVersionAtStart < REQUIRED_DATABASE_VERSION) {

            Context context = Globals.getInstance();

            localDB.beginTransaction();

            try {
                if (currentLocalDbVersionAtStart < OLD_MIGRATION_LAST_STRUCTURE_DATABASE_VERSION) {
                    new MigrationVV46(context).migrateInTransaction(localDB);
                    //в случае если миграция прошла успешно, нужно обновить версию для проверки на версию из данной миграции
                    currentLocalDbVersionAtStart = getVersionLocalDb(localDB);
                }

                switch (currentLocalDbVersionAtStart) {
                    case 46:
                        new MigrationVV47(context).migrateInTransaction(localDB);
                    case 47:
                        new MigrationVV48(context).migrateInTransaction(localDB);
                    case 48:
                        new MigrationVV49(context).migrateInTransaction(localDB);
                    case 49:
                        new MigrationVV50(context).migrateInTransaction(localDB);
                    case 50:
                        new MigrationVV51(context).migrateInTransaction(localDB);
                    case 51:
                        new MigrationVV52(context).migrateInTransaction(localDB);
                    case 52:
                        new MigrationVV53(context).migrateInTransaction(localDB);
                    case 53:
                        new MigrationVV54(context).migrateInTransaction(localDB);
                    case 54:
                        new MigrationVV55(context).migrateInTransaction(localDB);
                    case 55:
                        new MigrationVV56(context).migrateInTransaction(localDB);
                    case 56:
                        new MigrationVV57(context).migrateInTransaction(localDB);
                    case 57:
                        new MigrationVV58(context).migrateInTransaction(localDB);
                    case 58:
                        new MigrationVV59(context).migrateInTransaction(localDB);
                    case 59:
                        new MigrationVV60(context).migrateInTransaction(localDB);
                    case 60:
                        new MigrationVV61(context).migrateInTransaction(localDB);
                    case 61:
                        new MigrationVV62(context).migrateInTransaction(localDB);
                    case 62:
                        new MigrationVV63(context).migrateInTransaction(localDB);
                    case 63:
                        new MigrationVV64(context).migrateInTransaction(localDB);
                    case 64:
                        new MigrationVV65(context).migrateInTransaction(localDB);
                    case 65:
                        new MigrationVV66(context).migrateInTransaction(localDB);
                    case 66:
                        new MigrationVV67(context).migrateInTransaction(localDB);
                    case 67:
                        new MigrationVV68(context).migrateInTransaction(localDB);
                    case 68:
                        new MigrationVV69(context).migrateInTransaction(localDB);
                    case 69:
                        new MigrationVV70(context).migrateInTransaction(localDB);
                    case 70:
                        new MigrationVV71(context).migrateInTransaction(localDB);
                    case 71:
                        new MigrationVV72(context).migrateInTransaction(localDB);
                    case 72:
                        new MigrationVV73(context).migrateInTransaction(localDB);
                    case 73:
                        new MigrationVV74(context).migrateInTransaction(localDB);
                    case 74:
                        new MigrationVV75(context).migrateInTransaction(localDB);
                    case 75:
                        new MigrationVV76(context).migrateInTransaction(localDB);
                    case 76:
                        new MigrationVV77(context).migrateInTransaction(localDB);
                    case 77:
                        new MigrationVV78(context).migrateInTransaction(localDB);
                    case 78:
                        new MigrationVV79(context).migrateInTransaction(localDB);
                    case 79:
                        new MigrationVV80(context).migrateInTransaction(localDB);
                    case 80:
                        new MigrationVV81(context).migrateInTransaction(localDB);
                    case 81:
                        new MigrationVV82(context).migrateInTransaction(localDB);
                    case 82:
                        new MigrationVV83(context).migrateInTransaction(localDB);
                    case 83:
                        new MigrationVV84(context).migrateInTransaction(localDB);
                    case 84:
                        new MigrationVV85(context).migrateInTransaction(localDB);
                    case 85:
                        new MigrationVV86(context).migrateInTransaction(localDB);
                    case 86:
                        new MigrationVV87(context).migrateInTransaction(localDB);
                    case 87:
                        new MigrationVV88(context).migrateInTransaction(localDB);
                    case 88:
                        new MigrationVV89(context).migrateInTransaction(localDB);
                    case 89:
                        new MigrationVV90(context).migrateInTransaction(localDB);
                    case 90:
                        new MigrationVV91(context).migrateInTransaction(localDB);
                    case 91:
                        new MigrationVV92(context).migrateInTransaction(localDB);
                    case 92:
                        new MigrationVV93(context).migrateInTransaction(localDB);
                    case 93:
                        new MigrationVV94(context).migrateInTransaction(localDB);
                    case 94:
                        new MigrationVV95(context).migrateInTransaction(localDB);
                    case 95:
                        new MigrationVV96(context).migrateInTransaction(localDB);
                    case 96:
                        new MigrationVV97(context).migrateInTransaction(localDB);
                    case 97:
                        new MigrationVV98(context).migrateInTransaction(localDB);
                    case 98:
                        new MigrationVV99(context).migrateInTransaction(localDB);
                    case 99:
                        new MigrationVV100(context).migrateInTransaction(localDB);
                    case 100:
                        new MigrationVV101(context).migrateInTransaction(localDB);
                    case 101:
                        new MigrationVV102(context).migrateInTransaction(localDB);
                    case 102:
                        new MigrationVV103(context).migrateInTransaction(localDB);
                    case 103:
                        new MigrationVV104(context).migrateInTransaction(localDB);
                    case 104:
                        new MigrationVV105(context).migrateInTransaction(localDB);
                    case 105:
                        new MigrationVV106(context).migrateInTransaction(localDB);
                }

                localDB.setTransactionSuccessful();
                Logger.info(TAG, "Migration LocalDb full from " + currentLocalDbVersionAtStart + " to " + REQUIRED_DATABASE_VERSION + " successful");
            } catch (MigrationException e) {
                Logger.error(TAG, "Migration LocalDb full from " + currentLocalDbVersionAtStart + " to " + REQUIRED_DATABASE_VERSION + " failed");
                Logger.error(TAG, e);
                throw new MigrationException(currentLocalDbVersionAtStart, e.getToVersionNumber(), e);
            } finally {
                localDB.endTransaction();
            }
        }
    }

    /**
     * Возвращает текущую версию локальной БД
     */
    public static int getVersionLocalDb(Database localDB) {
        int version = -1;
        String sql = "select max(VersionId) from " + LocalDbVersionDao.TABLE_NAME;
        Cursor cursor = localDB.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            version = cursor.getInt(0);
        }
        cursor.close();
        return version;
    }

}
