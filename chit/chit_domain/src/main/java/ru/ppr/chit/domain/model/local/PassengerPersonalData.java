package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * @author Aleksandr Brazhkin
 */
public class PassengerPersonalData implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Код типа документа
     */
    private Long documentTypeCode;
    /**
     * Тип документа
     */
    private CredentialDocumentType documentType;
    /**
     * Номер документа
     */
    private String documentNumber;
    /**
     * Фамилия
     */
    private String lastName;
    /**
     * Имя
     */
    private String firstName;
    /**
     * Отчество
     */
    private String middleName;
    /**
     * Пол
     */
    private Gender gender;
    /**
     * Дата рождения
     */
    private String birthday;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    //region DocumentType getters and setters
    public Long getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(Long documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
        if (this.documentType != null && !ObjectUtils.equals(this.documentType.getCode(), documentTypeCode)) {
            this.documentType = null;
        }
    }

    public CredentialDocumentType getDocumentType(CredentialDocumentTypeRepository documentTypeRepository, int versionId) {
        CredentialDocumentType local = documentType;
        if (local == null && documentTypeCode != null) {
            synchronized (this) {
                if (documentType == null) {
                    documentType = documentTypeRepository.load(documentTypeCode, versionId);
                }
            }
            return documentType;
        }
        return local;
    }

    public void setDocumentType(CredentialDocumentType documentType) {
        this.documentType = documentType;
        this.documentTypeCode = documentType != null ? documentType.getCode() : null;
    }
    //endregion

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

}
