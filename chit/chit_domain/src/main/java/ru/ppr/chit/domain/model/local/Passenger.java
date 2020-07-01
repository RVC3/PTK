package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * @author Dmitry Nevolin
 */
public class Passenger implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Имя
     */
    private String firstName;
    /**
     * Фамилия
     */
    private String lastName;
    /**
     * Отчество
     */
    private String middleName;
    /**
     * Код документа
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
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

}
