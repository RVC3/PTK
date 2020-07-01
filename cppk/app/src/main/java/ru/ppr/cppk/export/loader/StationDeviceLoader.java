package ru.ppr.cppk.export.loader;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.StationDevice;

/**
 * @author Grigoriy Kashka
 */
public class StationDeviceLoader {

    public StationDevice fromJson(JSONObject json) throws JSONException {
        final StationDevice stationDevice = new StationDevice();

        stationDevice.id = json.getString("Id");
        stationDevice.model = json.getString("Model");
        stationDevice.serialNumber = json.getString("SerialNumber");
        stationDevice.type = json.getString("Type");
        stationDevice.productionSectionCode = json.getInt("ProductionSectionCode");

        return stationDevice;
    }

}
