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
@Entity(nameInDb = PassengerPersonalDataEntity.TABLE_NAME)
public class PassengerPersonalDataEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "PassengerPersonalData";

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
    @Property(nameInDb = "DocumentTypeCode")
    private Long documentTypeCode;
    @Property(nameInDb = "DocumentNumber")
    private String documentNumber;
    @Property(nameInDb = "LastName")
    private String lastName;
    @Property(nameInDb = "FirstName")
    private String firstName;
    @Property(nameInDb = "MiddleName")
    private String middleName;
    @Property(nameInDb = "Gender")
    private Integer gender;
    @Property(nameInDb = "Birthday")
    private String birthday;
    @Generated(hash = 1715261248)
    public PassengerPersonalDataEntity(Long id, Long documentTypeCode,
            String documentNumber, String lastName, String firstName,
            String middleName, Integer gender, String birthday) {
        this.id = id;
        this.documentTypeCode = documentTypeCode;
        this.documentNumber = documentNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.gender = gender;
        this.birthday = birthday;
    }
    @Generated(hash = 1429809776)
    public PassengerPersonalDataEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getMiddleName() {
        return this.middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public Integer getGender() {
        return this.gender;
    }
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    public String getBirthday() {
        return this.birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

}
