package ru.ppr.cppk.sync.loader.base;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class BaseLoader {

    protected final LocalDaoSession localDaoSession;
    protected final NsiDaoSession nsiDaoSession;

    public BaseLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
    }

    protected String createColumnsForSelect(String prefix, Column[] columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i != 0) sb.append(", ");
            sb.append(prefix).append(".").append(columns[i].name);
        }
        return sb.toString();
    }
}
