package ru.ppr.nsi.entity;

import java.util.Date;

/**
 * Категория билета.
 */
public class TicketCategory {

    private String name = null;
    private String expressTicketCategoryCode = null;
    private String abbreviation = null;
    private Integer presale = null;
    private Integer delayPassback = null;
    private Date chagedDateTime = null;
    private Integer code = null;
    private Integer versionId = null;
    private boolean isCompositeType = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpressTicketCategoryCode() {
        return expressTicketCategoryCode;
    }

    public void setExpressTicketCategoryCode(String expressTicketCategoryCode) {
        this.expressTicketCategoryCode = expressTicketCategoryCode;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Integer getPresale() {
        return presale;
    }

    public void setPresale(Integer presale) {
        this.presale = presale;
    }

    public Integer getDelayPassback() {
        return delayPassback;
    }

    public void setDelayPassback(Integer delayPassback) {
        this.delayPassback = delayPassback;
    }

    public Date getChagedDateTime() {
        return chagedDateTime;
    }

    public void setChagedDateTime(Date chagedDateTime) {
        this.chagedDateTime = chagedDateTime;
    }

    public int getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public boolean isCompositeType() {
        return isCompositeType;
    }

    public void setCompositeType(boolean compositeType) {
        isCompositeType = compositeType;
    }

    public static class Code {
        /**
         * Разовый
         */
        public static final long SINGLE = 1;
        /**
         * Ручная кладь и багаж
         */
        public static final long BAGGAGE = 2;
        /**
         * Абонемент «Ежедневно»
         */
        public static final long SEASON_TICKET_BY_PERIOD = 3;
        /**
         * Абонемент «Выходного дня»
         */
        public static final long SEASON_TICKET_ON_WEEKEND = 4;
        /**
         * Абонемент «Рабочего дня»
         */
        public static final long SEASON_TICKET_ON_WORKDAYS = 5;
        /**
         * Абонемент на определенные дни
         */
        public static final long SEASON_TICKET_ON_CERTAIN_DAYS = 6;
        /**
         * Абонемент «На количество поездок»
         */
        public static final long SEASON_TICKET_BY_TRIPS_COUNT = 7;
        /**
         * Специальные предложения
         */
        public static final long SPECIAL_OFFER = 8;
        /**
         * Трансфер разовый
         */
        public static final long SINGLE_TRANSFER = 11;
        /**
         * Трансфер абонемент «Ежедневно»
         */
        public static final long TRANSFER_SEASON_TICKET_BY_PERIOD = 12;
        /**
         * Трансфер абонемент «Выходного дня»
         */
        public static final long TRANSFER_SEASON_TICKET_ON_WEEKEND = 13;
        /**
         * Трансфер абонемент «Рабочего дня»
         */
        public static final long TRANSFER_SEASON_TICKET_ON_WORKDAYS = 14;
        /**
         * Трансфер абонемент «На количество поездок»
         */
        public static final long TRANSFER_SEASON_TICKET_BY_TRIPS_COUNT = 15;
        /**
         * Трансфер Специальные предложения
         */
        public static final long TRANSFER_SPECIAL_OFFER = 16;
        /**
         * Комбинированный абонемент 6000 + 7000
         */
        public static final long COMBINED_COUNT_TRIPS_SEASON_TICKET = 22;
    }
}
