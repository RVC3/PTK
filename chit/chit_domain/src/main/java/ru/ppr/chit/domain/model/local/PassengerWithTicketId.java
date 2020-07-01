package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Информация о пассажире.
 *
 * @author Aleksandr Brazhkin
 */
public class PassengerWithTicketId {

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
     * Номер документа
     */
    private String documentNumber;
    /**
     * Код типа документа
     */
    private Long documentTypeCode;
    /**
     * Id билета
     */
    private Long ticketId;
    /**
     * Тип документа
     */
    private CredentialDocumentType documentType;
    /**
     * Станция посадки
     */
    private Long departureStationCode;
    /**
     * Признак что посадка была
     */
    private boolean wasBoarded;
    /**
     * Признак что станция посадки совпадает с текущей
     */
    private boolean isCurrentStationBoarding;

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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
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

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public void setDepartureStationCode(Long departureStationCode){
        this.departureStationCode = departureStationCode;
    }

    public Long getDepartureStationCode(){
        return departureStationCode;
    }

    public void setWasBoarded(boolean wasBoarded){
        this.wasBoarded = wasBoarded;
    }

    public boolean getWasBoarded(){
        return wasBoarded;
    }

    public void setIsCurrentStationBoarding(boolean value){
        this.isCurrentStationBoarding = value;
    }

    public boolean getIsCurrentStationBoarding(){
        return isCurrentStationBoarding;
    }

}
