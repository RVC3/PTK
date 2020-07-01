package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.sync.kpp.model.CashRegister;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterWriter {

    public JSONObject getJson(CashRegister cashRegister) throws JSONException {
        if (cashRegister == null)
            return null;
        JSONObject json = new JSONObject();

        json.put("Model", cashRegister.model);
        json.put("SerialNumber", cashRegister.serialNumber);
        json.put("INN", cashRegister.inn);
        json.put("EKLZNumber", cashRegister.eklzNumber);
        json.put("FNSerial", cashRegister.fnSerial);

        return json;
    }

}
