package ru.ppr.ikkm.model;

import java.util.Date;

/**
 * Класс - хранилище для статуса по отправленных в ОФД документам.
 *
 * @author Grigoriy Kashka
 */
public class OfdDocsState {

    /**
     * Количество неотправленных в ОФД документов
     */
    private int unsentDocumentsCount;
    /**
     * Номер первого неотправленного в ОФД документа
     */
    private int firstUnsentDocumentNumber;
    /**
     * Дата создания первого неотправленного в ОФД документа
     */
    private Date firstUnsentDocumentDateTime;

    public int getUnsentDocumentsCount() {
        return unsentDocumentsCount;
    }

    public void setUnsentDocumentsCount(int unsentDocumentsCount) {
        this.unsentDocumentsCount = unsentDocumentsCount;
    }

    public int getFirstUnsentDocumentNumber() {
        return firstUnsentDocumentNumber;
    }

    public void setFirstUnsentDocumentNumber(int firstUnsentDocumentNumber) {
        this.firstUnsentDocumentNumber = firstUnsentDocumentNumber;
    }

    public Date getFirstUnsentDocumentDateTime() {
        return firstUnsentDocumentDateTime;
    }

    public void setFirstUnsentDocumentDateTime(Date firstUnsentDocumentDateTime) {
        this.firstUnsentDocumentDateTime = firstUnsentDocumentDateTime;
    }

    @Override
    public String toString() {
        return "OfdDocsState{" +
                "unsentDocumentsCount=" + unsentDocumentsCount +
                ", firstUnsentDocumentNumber=" + firstUnsentDocumentNumber +
                ", firstUnsentDocumentDateTime=" + firstUnsentDocumentDateTime +
                '}';
    }
}
