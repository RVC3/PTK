package ru.ppr.cppk.db.nsi;

import android.content.Context;
import android.database.Cursor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Di;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.dao.RoleDvcDao;
import ru.ppr.security.entity.RoleDvc;

@Deprecated
public class NsiDbOperations {

    private static final String TAG = Logger.makeLogTag(NsiDbOperations.class);

    private static SecurityDaoSession getSecurityDaoSession() {
        return Globals.getInstance().getSecurityDaoSession();
    }


    /**
     * Возвращает timestamp даты создания последнего файла в папке
     * /CPPConnect/SftTransport/In/ что является версией пакета открытых ключей
     *
     * @return
     */
    @Deprecated
    private static long getOpenKeysPackageVersion(Context context) {
        long out = 0;
        File inFolder = Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir();
        File[] files = inFolder.listFiles();
        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
            out = files[0].lastModified();
        }
        return out;
    }

    /**
     * Возвращает 0 либо дату изменения последнего файла в In
     * /CPPConnect/SftTransport/In/ что является версией пакета открытых ключей
     *
     * @return
     */
    @Deprecated
    public static String getOpenKeysPackageVersionString(Context context) {
        long keysVersion = NsiDbOperations.getOpenKeysPackageVersion(context);
        String openKeysPackageVersion = (keysVersion == 0) ? null
                : (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(keysVersion));
        return openKeysPackageVersion;
    }


    /**
     * Возвращает роль по ее id или Unknown в случае неудачи
     *
     * @param securityDatabase
     * @param id
     * @return
     */
    @Deprecated
    public static RoleDvc getRoleToId(Database securityDatabase, int id) {
        RoleDvc out = null;
        if (securityDatabase == null)
            return out;
        Cursor cursor = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Select * FROM ").append(RoleDvcDao.TABLE_NAME).append(" WHERE ").append(RoleDvcDao.Properties.ID).append(" = ").append(id);
        try {
            cursor = securityDatabase.rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                out = getSecurityDaoSession().getRoleDvcDao().fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return out;
    }

}
