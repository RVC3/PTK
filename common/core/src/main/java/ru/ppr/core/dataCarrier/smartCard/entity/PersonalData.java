package ru.ppr.core.dataCarrier.smartCard.entity;

import java.util.Date;

/**
 * Персональные данные.
 *
 * @author Aleksandr Brazhkin
 */
public class PersonalData {
    public PersonalData() {
        this.surname = "";
        this.name = "";
        this.secondName = "";
        this.birthDate = new Date();
        this.gender = Gender.MALE;
    }

    /**
     * Фамилия
     */
    private String surname;
    /**
     * Имя
     */
    private String name;
    /**
     * Отчество
     */
    private String secondName;
    /**
     * Дата рождения
     */
    private Date birthDate;
    /**
     * Пол
     */
    private Gender gender;


    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public enum Gender {
        MALE,
        FEMALE;
    }
}
