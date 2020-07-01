package ru.ppr.cppk.sync.kpp.model;

import java.util.Date;

/**
 * Доп. параметры для ЭТТ
 *
 * @author Grigoriy Kashka
 */
public class AdditionalInfoForEtt {

    /**
     * Шифр категории льготника
     */
    public String PassengerCategory;

    /**
     * Дата выдачи ЭТТ
     */
    public Date IssueDataTime;

    /**
     * Код подразделения (билетного бюро), выдавшего ТТ
     */
    public String IssueUnitCode;

    /**
     * Код организации в штате которой состоит работник
     */
    public String OwnerOrganizationCode;

    /**
     * Фамилия, инициалы пассажира
     */
    public String PassengerFio;

    /**
     * Фамилия, инициалы работника РЖД, на чьем иждивении находится пассажир
     */
    public String GuardianFio;

    public String SNILS;
}
