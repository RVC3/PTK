package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.PtkShiftSummary;
import ru.ppr.cppk.sync.writer.base.DateFormatter;

/**
 * @author Grigoriy Kashka
 */
public class PtkShiftSummaryWriter {

    private final SalesSumWriter salesSumWriter;
    private final CashierWriter cashierWriter;
    private final CashRegisterWriter cashRegisterWriter;
    private final DateFormatter dateFormatter;

    public PtkShiftSummaryWriter() {
        this.salesSumWriter = new SalesSumWriter();
        this.cashierWriter = new CashierWriter();
        this.cashRegisterWriter = new CashRegisterWriter();
        this.dateFormatter = new DateFormatter();
    }

    public JSONObject getJson(PtkShiftSummary state) throws JSONException {
        if (state == null)
            return null;
        JSONObject json = new JSONObject();

        json.put("DeviceId", state.deviceId);
        json.put("ShiftNumber", state.shiftNumber);
        json.put("OpenDate", dateFormatter.formatDateForExport(state.openDate));
        json.put("Cashier", cashierWriter.getJson(state.cashier));
        json.put("CashRegister", cashRegisterWriter.getJson(state.cashRegister));
        json.put("IsOpened", state.isOpened);
        json.put("CashSum", salesSumWriter.getJson(state.cashSum));
        json.put("CashlessSum", salesSumWriter.getJson(state.cashlessSum));

        return json;
    }

}
