package ru.ppr.core.dataCarrier.smartCard.entity;

import java.util.Date;

/**
 * Данные ЭТТ.
 *
 * @author Aleksandr Brazhkin
 */
public class EttData {
    /**
     * Шифр категории пассажира
     */
    private String passengerCategoryCode;
    /**
     * Код билетной группы
     */
    private String divisionCode;
    /**
     * Код организации
     */
    private String organizationCode;
    /**
     * Номер требования
     */
    private String ettNumber;
    /**
     * Шифр льготы "Экспресс"
     */
    private String exemptionExpressCode;
    /**
     * Фамилия пассажира
     */
    private String surname;
    /**
     * Имя пассажира
     */
    private String firstName;
    /**
     * Отчество пассажира
     */
    private String secondName;
    /**
     * Дата рождения пассажира
     */
    private Date birthDate;
    /**
     * Государство выдачи документа
     */
    private String documentIssuingCountry;
    /**
     * Место рождения пассажира
     */
    private String birthPlace;
    /**
     * Пол пассажира
     */
    private String gender;
    /**
     * Фаимилия, инициалы работника
     */
    private String guardianFio;
    /**
     * Код СНИЛС
     */
    private String snilsCode;

    public EttData() {

    }

    public String getPassengerCategoryCode() {
        return passengerCategoryCode;
    }

    public void setPassengerCategoryCode(String passengerCategoryCode) {
        this.passengerCategoryCode = passengerCategoryCode;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getEttNumber() {
        return ettNumber;
    }

    public void setEttNumber(String ettNumber) {
        this.ettNumber = ettNumber;
    }

    public String getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(String exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getDocumentIssuingCountry() {
        return documentIssuingCountry;
    }

    public void setDocumentIssuingCountry(String documentIssuingCountry) {
        this.documentIssuingCountry = documentIssuingCountry;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGuardianFio() {
        return guardianFio;
    }

    public void setGuardianFio(String guardianFio) {
        this.guardianFio = guardianFio;
    }

    public String getSnilsCode() {
        return snilsCode;
    }

    public void setSnilsCode(String snilsCode) {
        this.snilsCode = snilsCode;
    }
}
