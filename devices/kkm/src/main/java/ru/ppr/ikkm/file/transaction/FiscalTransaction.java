package ru.ppr.ikkm.file.transaction;

import android.annotation.SuppressLint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Item;

/**
 * Внимание: данный класс является "одноразовым". Т.е. повторной вызов метов управлением транзакцикй
 * не изменит итоговый чек, который вернулся после первого build'a.
 * Created by Артем on 22.01.2016.
 */
public class FiscalTransaction implements Transaction {

    private boolean started;
    private final IPrinter.DocType docType;
    private final List<Item> items;
    private final int spdnNumber;
    private Check check;
    private IPrinter.PaymentType paymentType;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal payment = BigDecimal.ZERO;
    private Date printDate;

    public FiscalTransaction(IPrinter.DocType docType, int spdnNumber) {
        this.docType = docType;
        started = true;
        items = new ArrayList<>();
        this.spdnNumber = spdnNumber;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void endTransaction(IPrinter.DocType docType) {
        printDate = new Date();
        started = false;
    }

    @Override
    public void addItem(String description, BigDecimal amount, BigDecimal nds) throws TransactionException {

        if(docType != IPrinter.DocType.SALE) {
            throw new TransactionException("Current document type is not sale");
        }
        addItemToList(description, amount, nds);
    }

    @Override
    public void addItemRefund(String description, BigDecimal amount, BigDecimal nds) throws TransactionException {

        if(docType != IPrinter.DocType.RETURN) {
            throw new TransactionException("Current document type is not return");
        }
        addItemToList(description, amount, nds);
    }

    /**
     * Добавляет позицию продажи/аннулирования в список
     * @param description
     * @param amount
     * @param nds
     */
    private void addItemToList(String description, BigDecimal amount, BigDecimal nds) {
        Item item = new Item();
        item.setGoodDescription(description);
        item.setSum(amount);
        item.setNds(nds);
        item.setTotal(amount);
        items.add(item);
    }

    @Override
    public void addDiscount(BigDecimal discount, BigDecimal newAmount, BigDecimal newNds) throws TransactionException {
        Item lastItem = items.get(items.size() - 1);
        if (lastItem == null) {
            throw new TransactionException("Not items in check");
        }

        lastItem.setDiscount(discount);
        lastItem.setNds(newNds);
        lastItem.setTotal(newAmount);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void printTotal(BigDecimal total, BigDecimal payment, IPrinter.PaymentType paymentType) throws TransactionException {

        //сравним тотал который передали с тоталом который получится после сложения всех позиций в ПД
        BigDecimal totalFromItems = BigDecimal.ZERO;

        for (Item item : items)
            totalFromItems = totalFromItems.add(item.getTotal());

        if(total.compareTo(totalFromItems) != 0) {
            throw new TransactionException(String.format(TransactionException.TOTAL_NOT_EQUALS,
                    total, totalFromItems));
        }

        if(total.compareTo(payment) == -1) {
            throw new TransactionException(String.format(TransactionException.PAYED_LESS_THAN_TOTAL,
                    total, payment));
        }

        this.paymentType = paymentType;
        this.total = total;
        this.payment = payment;
    }

    @Override
    public Check build() throws Exception {

        if(isStarted()) {
            throw new Exception("Transaction not closed");
        }

        if(check == null) {
            check = new Check();

            //добавим всем позициям текущий чек
            for (Item item: items) {
                item.setCheck(check);
            }
            check.setItems(items);
            check.setSpdnNumber(spdnNumber);
            check.setType(docType);
            check.setPaymentMethod(paymentType);
            check.setTotal(total);
            check.setPayment(payment);
            check.setPrintTime(printDate);
        }
        return check;
    }
}
