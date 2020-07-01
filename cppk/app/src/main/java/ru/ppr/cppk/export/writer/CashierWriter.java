package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.sync.kpp.model.Cashier;


/**
 * @author Grigoriy Kashka
 */
public class CashierWriter {

    public JSONObject getJson(Cashier cashier) throws JSONException {
        if (cashier == null)
            return null;
        JSONObject json = new JSONObject();

        json.put("UserLogin", cashier.userLogin);
        json.put("OfficialCode", cashier.officialCode);
        json.put("Fio", cashier.fio);

        return json;
    }

}
