package ru.ppr.security;

/**
 * @author Aleksandr Brazhkin
 */
public interface SecurityDbSessionManager {
    SecurityDaoSession getDaoSession();
}
