package ru.ppr.ipos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Aleksandr Brazhkin
 */
public class FakeTransactionResult extends TransactionResult {

    public FakeTransactionResult(int transactionId) {
        setId(transactionId);
        setTimeStamp(new Date());
        setApproved(true);
        setTerminalId("StubTerminal");
        setInvoiceNumber(0);
    }

    @Override
    public List<String> getReceipt() {

        List<String> receipt = new ArrayList<>();
        receipt.add("id = " + getId());
        receipt.add("timeStamp = " + getTimeStamp());
        receipt.add("approved = " + isApproved());
        receipt.add("terminalId = " + getTerminalId());
        receipt.add("invoiceNumber = " + getInvoiceNumber());
        receipt.add("bankResponseCode = " + getBankResponseCode());
        receipt.add("bankResponse = " + getBankResponse());

        return receipt;
    }
}
