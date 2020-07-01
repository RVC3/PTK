package ru.ppr.cppk.sync.writer;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.baseEntities.EventWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.CheckWriter;
import ru.ppr.cppk.sync.writer.model.StationDeviceWriter;
import ru.ppr.cppk.sync.writer.model.StationWriter;

/**
 * @author Grigoriy Kashka
 */
public class ServiceSaleWriter extends BaseWriter<ServiceSale> {

    private final DateFormatter dateFormatter;
    private final EventWriter eventWriter;
    private final CashierWriter cashierWriter;
    private final CheckWriter checkWriter;

    public ServiceSaleWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        StationDeviceWriter stationDeviceWriter = new StationDeviceWriter();
        StationWriter stationWriter = new StationWriter();
        cashierWriter = new CashierWriter();
        eventWriter = new EventWriter(stationDeviceWriter, stationWriter);
        checkWriter = new CheckWriter(dateFormatter);
    }

    @Override
    public void writeProperties(ServiceSale serviceSale, ExportJsonWriter writer) throws IOException {
        //Fields From event
        eventWriter.writeProperties(serviceSale, writer);

        //From ServiceSale
        writer.name("ServiceCode").value(serviceSale.serviceCode);
        writer.name("ServiceName").value(serviceSale.serviceName);
        cashierWriter.writeField("Cashier", serviceSale.cashier, writer);
        writer.name("Price").value(serviceSale.price);
        writer.name("PriceNds").value(serviceSale.priceNds);
        checkWriter.writeField("Check", serviceSale.check, writer);
        writer.name("SaleDateTime").value(dateFormatter.formatDateForExport(serviceSale.saleDateTime));
        writer.name("PaymentType").value(serviceSale.paymentType);
        writer.name("TicketNumber").value(serviceSale.ticketNumber);
        writer.name("WorkingShiftNumber").value(serviceSale.workingShiftNumber);
    }

}
