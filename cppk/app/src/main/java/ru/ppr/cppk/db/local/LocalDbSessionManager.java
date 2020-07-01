package ru.ppr.cppk.db.local;

import ru.ppr.cppk.db.LocalDaoSession;

/**
 * @author Grigoriy Kashka
 */
public interface LocalDbSessionManager {
    LocalDaoSession getDaoSession();
}
