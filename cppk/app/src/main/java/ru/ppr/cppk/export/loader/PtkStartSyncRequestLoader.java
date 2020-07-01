package ru.ppr.cppk.export.loader;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.request.PtkStartSyncRequest;

/**
 * @author Grigoriy Kashka
 */
public class PtkStartSyncRequestLoader {

    private final StationDeviceLoader stationDeviceLoader;

    public PtkStartSyncRequestLoader() {
        stationDeviceLoader = new StationDeviceLoader();
    }

    public PtkStartSyncRequest fromJson(JSONObject json) throws JSONException {
        final PtkStartSyncRequest ptkStartSyncRequest = new PtkStartSyncRequest();
        ptkStartSyncRequest.device = stationDeviceLoader.fromJson(json.getJSONObject("Device"));
        return ptkStartSyncRequest;
    }

}
