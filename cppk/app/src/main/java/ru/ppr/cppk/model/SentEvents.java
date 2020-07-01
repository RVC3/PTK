package ru.ppr.cppk.model;

import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;

/**
 *  Сущность контейнер, соответствует таблица SentEvents в локальной БД
 *
 * @autor Grigoriy Kashka
 */
public class SentEvents {

    /**
     * дата/время последнего события {@link ShiftEvent} успешно принятого ЦОДом
     */
    private long sentShiftEvents;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.CPPKTicketControl} успешно принятого ЦОДом
     */
    private long sentTicketControls;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.CPPKServiceSale} успешно принятого ЦОДом
     */
    private long sentTicketSales;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.TestTicketEvent} успешно принятого ЦОДом
     */
    private long sentTestTickets;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.CPPKTicketReturn} успешно принятого ЦОДом
     */
    private long sentTicketReturns;
    /**
     * дата/время последнего события {@link MonthEvent} успешно принятого ЦОДом
     */
    private long sentMonthClosures;
    /**
     * дата/время последнего события {@link TicketTapeEvent} успешно принятого ЦОДом
     */
    private long sentTicketPaperRolls;
    /**
     * дата/время последнего события {@link BankTransactionEvent} успешно принятого ЦОДом
     */
    private long sentBankTransactions;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.CPPKTicketReSign} успешно принятого ЦОДом
     */
    private long sentTicketReSigns;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.entity.event.base34.CPPKServiceSale} успешно принятого ЦОДом
     */
    private long sentServiceSales;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.sync.kpp.FinePaidEvent} успешно принятого ЦОДом
     */
    private long sentFinePaidEvents;
    /**
     * дата/время последнего события {@link ru.ppr.cppk.sync.kpp.ServiceTicketControls} успешно принятого ЦОДом
     */
    private long sentServiceTicketControls;

    public SentEvents() {
    }

    public long getSentShiftEvents() {
        return sentShiftEvents;
    }

    public void setSentShiftEvents(long sentShiftEvents) {
        this.sentShiftEvents = sentShiftEvents;
    }

    public long getSentTicketControls() {
        return sentTicketControls;
    }

    public void setSentTicketControls(long sentTicketControls) {
        this.sentTicketControls = sentTicketControls;
    }

    public long getSentTicketSales() {
        return sentTicketSales;
    }

    public void setSentTicketSales(long sentTicketSales) {
        this.sentTicketSales = sentTicketSales;
    }

    public long getSentTestTickets() {
        return sentTestTickets;
    }

    public void setSentTestTickets(long sentTestTickets) {
        this.sentTestTickets = sentTestTickets;
    }

    public long getSentTicketReturns() {
        return sentTicketReturns;
    }

    public void setSentTicketReturns(long sentTicketReturns) {
        this.sentTicketReturns = sentTicketReturns;
    }

    public long getSentMonthClosures() {
        return sentMonthClosures;
    }

    public void setSentMonthClosures(long sentMonthClosures) {
        this.sentMonthClosures = sentMonthClosures;
    }

    public long getSentTicketPaperRolls() {
        return sentTicketPaperRolls;
    }

    public void setSentTicketPaperRolls(long sentTicketPaperRolls) {
        this.sentTicketPaperRolls = sentTicketPaperRolls;
    }

    public long getSentBankTransactions() {
        return sentBankTransactions;
    }

    public void setSentBankTransactions(long sentBankTransactions) {
        this.sentBankTransactions = sentBankTransactions;
    }

    public long getSentTicketReSigns() {
        return sentTicketReSigns;
    }

    public void setSentTicketReSigns(long sentTicketReSigns) {
        this.sentTicketReSigns = sentTicketReSigns;
    }

    public long getSentServiceSales() {
        return sentServiceSales;
    }

    public void setSentServiceSales(long sentServiceSales) {
        this.sentServiceSales = sentServiceSales;
    }

    public long getSentFinePaidEvents() {
        return sentFinePaidEvents;
    }

    public void setSentFinePaidEvents(long sentFinePaidEvents) {
        this.sentFinePaidEvents = sentFinePaidEvents;
    }

    public long getSentServiceTicketControls() {
        return sentServiceTicketControls;
    }

    public void setSentServiceTicketControls(long sentServiceTicketControls) {
        this.sentServiceTicketControls = sentServiceTicketControls;
    }
}
