package ru.ppr.cppk.sync.kpp.model;

import java.math.BigDecimal;

/**
 * @author Grigoriy Kashka
 */
public class Exemption {

    public String Fio;

    /**
     * 4-значный код льготы
     */
    public String Code;

    public String RegionOkatoCode;

    /**
     * Сумма потерь
     */
    public BigDecimal LossSum;

    public SmartCard SmartCardFromWhichWasReadAboutExemption;

    public String TypeOfDocumentWhichApproveExemption;

    public String NumberOfDocumentWhichApproveExemption;

    public String Organization;

    public boolean IsSnilsUsed;

    /**
     * Признак оформления проездного документа с использованием социальной карты
     */
    public boolean RequireSocialCard;
    
}
