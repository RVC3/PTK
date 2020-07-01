package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Date;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = AuthInfoEntity.TABLE_NAME)
public class AuthInfoEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "AuthInfo";
    private static final String AuthorizationDateField = "AuthorizationDate";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    static class Meta extends BaseLocalMeta implements GCOldDataRemovable {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public void gcRemoveOldData(Database database, Date dateBefore) {
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(TABLE_NAME).append(" where ").append(AuthorizationDateField).append(" < ").append(dateBefore.getTime());

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "BaseUri")
    private String baseUri;
    @Property(nameInDb = "AuthorizationCode")
    private String authorizationCode;
    @Property(nameInDb = "ClientId")
    private String clientId;
    @Property(nameInDb = "ClientSecret")
    private String clientSecret;
    @Property(nameInDb = "TerminalId")
    private Long terminalId;
    @Property(nameInDb = "BaseStationId")
    private String baseStationId;
    @Property(nameInDb = "Thumbprint")
    private String thumbprint;
    @Property(nameInDb = "SerialNumber")
    private String serialNumber;
    @Property(nameInDb = AuthorizationDateField)
    private Date authorizationDate;
    @Generated(hash = 1181095865)
    public AuthInfoEntity(Long id, String baseUri, String authorizationCode, String clientId, String clientSecret, Long terminalId, String baseStationId,
            String thumbprint, String serialNumber, Date authorizationDate) {
        this.id = id;
        this.baseUri = baseUri;
        this.authorizationCode = authorizationCode;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.terminalId = terminalId;
        this.baseStationId = baseStationId;
        this.thumbprint = thumbprint;
        this.serialNumber = serialNumber;
        this.authorizationDate = authorizationDate;
    }
    @Generated(hash = 2113917163)
    public AuthInfoEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getBaseUri() {
        return this.baseUri;
    }
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
    public String getAuthorizationCode() {
        return this.authorizationCode;
    }
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    public String getClientId() {
        return this.clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientSecret() {
        return this.clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public Long getTerminalId() {
        return this.terminalId;
    }
    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }
    public String getBaseStationId() {
        return this.baseStationId;
    }
    public void setBaseStationId(String baseStationId) {
        this.baseStationId = baseStationId;
    }
    public String getThumbprint() {
        return this.thumbprint;
    }
    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }
    public String getSerialNumber() {
        return this.serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public Date getAuthorizationDate() {
        return this.authorizationDate;
    }
    public void setAuthorizationDate(Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

}
