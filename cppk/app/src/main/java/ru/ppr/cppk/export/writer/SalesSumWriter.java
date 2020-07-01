package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.SalesSum;

/**
 * @author Grigoriy Kashka
 */
public class SalesSumWriter {

    public JSONObject getJson(SalesSum state) throws JSONException {
        if (state == null)
            return null;
        JSONObject json = new JSONObject();

        json.put("TicketsSum", state.ticketsSum);
        json.put("LuggageSum", state.luggageSum);
        json.put("FeeSum", state.feeSum);
        json.put("FinesSum", state.finesSum);

        return json;
    }
}
