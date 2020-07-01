package ru.ppr.ikkm.file.transaction;

import java.math.BigDecimal;

import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.file.state.model.Check;

/**
 * Created by Артем on 22.01.2016.
 */
public interface Transaction {

    boolean isStarted();

    void endTransaction(IPrinter.DocType docType);

    void addItem(String description, BigDecimal amount, BigDecimal nds) throws TransactionException;

    void addItemRefund(String description, BigDecimal amount, BigDecimal nds) throws TransactionException;

    void addDiscount(BigDecimal discount, BigDecimal newAmount, BigDecimal newNds) throws TransactionException;

    void printTotal(BigDecimal total, BigDecimal payment, IPrinter.PaymentType paymentType) throws TransactionException;

    Check build() throws Exception;
}
