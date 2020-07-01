package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.DaoException;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.AuthInfoEntityDao;
import ru.ppr.chit.localdb.greendao.OAuth2TokenEntityDao;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = OAuth2TokenEntity.TABLE_NAME)
public class OAuth2TokenEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "OAuth2Token";
    private static final String AuthInfoIdField = "AuthInfoId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(AuthInfoEntity.TABLE_NAME, AuthInfoIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "AccessToken")
    private String accessToken;
    @Property(nameInDb = "TokenType")
    private String tokenType;
    @Property(nameInDb = "RefreshToken")
    private String refreshToken;
    @Property(nameInDb = "Issued")
    private String issued;
    @Property(nameInDb = "Expires")
    private String expires;
    @Property(nameInDb = "ExpiresIn")
    private Long expiresIn;
    @Property(nameInDb = "ClientId")
    private String clientId;
    @Property(nameInDb = "Broken")
    private boolean broken;
    @Property(nameInDb = AuthInfoIdField)
    private Long authInfoId;
    @ToOne(joinProperty = "authInfoId")
    private AuthInfoEntity authInfo;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 127601533)
    private transient OAuth2TokenEntityDao myDao;
    @Generated(hash = 223554268)
    public OAuth2TokenEntity(Long id, String accessToken, String tokenType,
            String refreshToken, String issued, String expires, Long expiresIn,
            String clientId, boolean broken, Long authInfoId) {
        this.id = id;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
        this.issued = issued;
        this.expires = expires;
        this.expiresIn = expiresIn;
        this.clientId = clientId;
        this.broken = broken;
        this.authInfoId = authInfoId;
    }
    @Generated(hash = 898356484)
    public OAuth2TokenEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAccessToken() {
        return this.accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getTokenType() {
        return this.tokenType;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public String getRefreshToken() {
        return this.refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getIssued() {
        return this.issued;
    }
    public void setIssued(String issued) {
        this.issued = issued;
    }
    public String getExpires() {
        return this.expires;
    }
    public void setExpires(String expires) {
        this.expires = expires;
    }
    public Long getExpiresIn() {
        return this.expiresIn;
    }
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    public String getClientId() {
        return this.clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public boolean getBroken() {
        return this.broken;
    }
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
    public Long getAuthInfoId() {
        return this.authInfoId;
    }
    public void setAuthInfoId(Long authInfoId) {
        this.authInfoId = authInfoId;
    }
    @Generated(hash = 60087028)
    private transient Long authInfo__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 806875191)
    public AuthInfoEntity getAuthInfo() {
        Long __key = this.authInfoId;
        if (authInfo__resolvedKey == null || !authInfo__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AuthInfoEntityDao targetDao = daoSession.getAuthInfoEntityDao();
            AuthInfoEntity authInfoNew = targetDao.load(__key);
            synchronized (this) {
                authInfo = authInfoNew;
                authInfo__resolvedKey = __key;
            }
        }
        return authInfo;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1586639161)
    public void setAuthInfo(AuthInfoEntity authInfo) {
        synchronized (this) {
            this.authInfo = authInfo;
            authInfoId = authInfo == null ? null : authInfo.getId();
            authInfo__resolvedKey = authInfoId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2086098007)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOAuth2TokenEntityDao() : null;
    }



}
