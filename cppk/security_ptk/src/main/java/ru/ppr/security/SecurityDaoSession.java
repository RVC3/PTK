package ru.ppr.security;

import ru.ppr.database.DbOpenHelper;
import ru.ppr.database.Database;
import ru.ppr.security.dao.PermissionDvcDao;
import ru.ppr.security.dao.PtkDataContractsVersionDao;
import ru.ppr.security.dao.PtsKeyDao;
import ru.ppr.security.dao.RoleDvcDao;
import ru.ppr.security.dao.RolePermissionDvcDao;
import ru.ppr.security.dao.SecurityCardDao;
import ru.ppr.security.dao.SecurityDataVersionDao;
import ru.ppr.security.dao.SecurityStopListVersionDao;
import ru.ppr.security.dao.SettingDao;
import ru.ppr.security.dao.SmartCardStopListItemDao;
import ru.ppr.security.dao.TicketStopListItemDao;
import ru.ppr.security.dao.TicketWhiteListItemDao;
import ru.ppr.security.dao.UserDvcDao;

/**
 * Высокоуровневая обертка над Security БД.
 * Точка входа в слой для работы с Security БД.
 * Объединяет в себе все мелкие DAO-объекты.
 * Никак не управляет подключением!
 * В случае закрытия соединения с БД через {@link DbOpenHelper}
 * и повторного получения БД через {@link DbOpenHelper#getReadableDatabase()}
 * нужно создавать новый объект {@link SecurityDaoSession} на основе {@link Database}
 *
 * @author Aleksandr Brazhkin
 */
public class SecurityDaoSession {

    private final Database database;
    /////////////////////////////////////
    private final UserDvcDao userDvcDao;
    private final SecurityCardDao securityCardDao;
    private final RoleDvcDao roleDvcDao;
    private final SmartCardStopListItemDao smartCardStopListItemDao;
    private final RolePermissionDvcDao rolePermissionDvcDao;
    private final PermissionDvcDao permissionDvcDao;
    private final TicketStopListItemDao ticketStopListItemDao;
    private final TicketWhiteListItemDao ticketWhiteListItemDao;
    private final PtkDataContractsVersionDao ptkDataContractsVersionDao;
    private final SecurityStopListVersionDao securityStopListVersionDao;
    private final SecurityDataVersionDao securityDataVersionDao;
    private final SettingDao settingDao;
    private final PtsKeyDao ptsKeyDao;

    public SecurityDaoSession(Database database) {
        this.database = database;
        this.userDvcDao = new UserDvcDao(this);
        this.securityCardDao = new SecurityCardDao(this);
        this.roleDvcDao = new RoleDvcDao(this);
        this.smartCardStopListItemDao = new SmartCardStopListItemDao(this);
        this.rolePermissionDvcDao = new RolePermissionDvcDao(this);
        this.permissionDvcDao = new PermissionDvcDao(this);
        this.ticketStopListItemDao = new TicketStopListItemDao(this);
        this.ticketWhiteListItemDao = new TicketWhiteListItemDao(this);
        this.ptkDataContractsVersionDao = new PtkDataContractsVersionDao(this);
        this.securityStopListVersionDao = new SecurityStopListVersionDao(this);
        this.securityDataVersionDao = new SecurityDataVersionDao(this);
        this.settingDao = new SettingDao(this);
        this.ptsKeyDao = new PtsKeyDao(this);
    }

    /**
     * Возвращает Security БД
     *
     * @return Security БД
     */
    public Database getSecurityDb() {
        return database;
    }

    public UserDvcDao getUserDvcDao() {
        return userDvcDao;
    }

    public SecurityCardDao getSecurityCardDao() {
        return securityCardDao;
    }

    public RoleDvcDao getRoleDvcDao() {
        return roleDvcDao;
    }

    public SmartCardStopListItemDao getSmartCardStopListItemDao() {
        return smartCardStopListItemDao;
    }

    public RolePermissionDvcDao getRolePermissionDvcDao() {
        return rolePermissionDvcDao;
    }

    public PermissionDvcDao getPermissionDvcDao() {
        return permissionDvcDao;
    }

    public TicketStopListItemDao getTicketStopListItemDao() {
        return ticketStopListItemDao;
    }

    public TicketWhiteListItemDao getTicketWhiteListItemDao() {
        return ticketWhiteListItemDao;
    }

    public PtkDataContractsVersionDao getPtkDataContractsVersionDao() {
        return ptkDataContractsVersionDao;
    }

    public SecurityStopListVersionDao getSecurityStopListVersionDao() {
        return securityStopListVersionDao;
    }

    public SecurityDataVersionDao getSecurityDataVersionDao() {
        return securityDataVersionDao;
    }

    public SettingDao getSettingDao() {
        return settingDao;
    }

    public PtsKeyDao getPtsKeyDao() {
        return ptsKeyDao;
    }

}
