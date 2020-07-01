package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.BankCardPayment;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class BankCardPaymentWriter extends BaseWriter<BankCardPayment> {

    private final DateFormatter dateFormatter;

    public BankCardPaymentWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void writeProperties(BankCardPayment bankCardPayment, ExportJsonWriter writer) throws IOException {
        writer.name("BankCode").value(bankCardPayment.BankCode);
        writer.name("Rrn").value(bankCardPayment.Rrn);
        writer.name("AuthCode").value(bankCardPayment.AuthCode);
        writer.name("TerminalId").value(bankCardPayment.TerminalId);
        writer.name("CardNumber").value(bankCardPayment.CardNumber);
        writer.name("CardType").value(bankCardPayment.CardType);
        writer.name("PaymentDateTime").value(dateFormatter.formatDateForExport(bankCardPayment.PaymentDateTime));
        writer.name("Sum").value(bankCardPayment.Sum);
        writer.name("OrganizationId").value(bankCardPayment.OrganizationId);
        writer.name("CurrencyCode").value(bankCardPayment.CurrencyCode);
        writer.name("SellerNumber").value(bankCardPayment.SellerNumber);
        writer.name("ApplicationName").value(bankCardPayment.ApplicationName);
        writer.name("CheckNumber").value(bankCardPayment.CheckNumber);
        writer.name("ResponseSum").value(bankCardPayment.ResponseSum);
    }

}
