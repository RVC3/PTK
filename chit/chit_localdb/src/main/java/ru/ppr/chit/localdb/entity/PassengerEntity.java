package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = PassengerEntity.TABLE_NAME)
public class PassengerEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "Passenger";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCNoLinkRemovable {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public boolean gcHandleNoLinkRemoveData(Database database) {
            // стандартный алгоритм удаления записей, на которые нет ссылок
            return false;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "FirstName")
    private String firstName;
    @Property(nameInDb = "LastName")
    private String lastName;
    @Property(nameInDb = "MiddleName")
    private String middleName;
    @Property(nameInDb = "DocumentTypeCode")
    private Long documentTypeCode;
    @Property(nameInDb = "DocumentNumber")
    private String documentNumber;
    @Generated(hash = 1443341867)
    public PassengerEntity(Long id, String firstName, String lastName,
            String middleName, Long documentTypeCode, String documentNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.documentTypeCode = documentTypeCode;
        this.documentNumber = documentNumber;
    }
    @Generated(hash = 1674888195)
    public PassengerEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getMiddleName() {
        return this.middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public Long getDocumentTypeCode() {
        return this.documentTypeCode;
    }
    public void setDocumentTypeCode(Long documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }
    public String getDocumentNumber() {
        return this.documentNumber;
    }
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

}
