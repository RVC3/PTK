package ru.ppr.cppk.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import ru.ppr.cppk.entity.settings.PrivateSettings;

/**
 * Created by Григорий on 16.03.2017.
 */

public class PrivateSettingsUtils {

    public static PrivateSettingsUtils getInstance() {
        return new PrivateSettingsUtils();
    }

    /**
     * Собирает объект из json
     *
     * @param json
     * @throws JSONException
     */
    public PrivateSettings parseFromJson(JSONObject json) throws JSONException {
        PrivateSettings settings = new PrivateSettings();
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = json.isNull(key) ? null : json.getString(key);
            settings.getSettings().put(key, value);
        }
        return settings;
    }

    /**
     * Вернет JSON объекта частных настроек
     *
     * @return
     * @throws JSONException
     */
    public JSONObject getJSON(PrivateSettings settings) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(PrivateSettings.Entities.TERMINAL_NUMBER, settings.getTerminalNumber());
        jsonObject.put(PrivateSettings.Entities.PRODUCTION_SECTION_CODE, settings.getProductionSectionId());
        jsonObject.put(PrivateSettings.Entities.IS_MOBILE_CASH_REGISTER, settings.isMobileCashRegister());
        jsonObject.put(PrivateSettings.Entities.WORK_STATION_CODE, settings.getCurrentStationCode());
        jsonObject.put(PrivateSettings.Entities.IS_OUTPUT_MODE, settings.isOutputMode());
        jsonObject.put(PrivateSettings.Entities.TIME_FOR_ANNULATE, settings.getTimeForAnnulate());
        jsonObject.put(PrivateSettings.Entities.TRAIN_CATEGORY_PREFIX, settings.getTrainCategoryPrefix().getCode());
        jsonObject.put(PrivateSettings.Entities.STOP_LIST_VALID_TIME, settings.getStopListValidTime());
        jsonObject.put(PrivateSettings.Entities.TIME_TO_CLOSE_SHIFT_MESSAGE, settings.getTimeForShiftCloseMessage());
        jsonObject.put(PrivateSettings.Entities.DAY_CODE, settings.getDayCode());
        jsonObject.put(PrivateSettings.Entities.IS_TIME_SYNC_ENABLED, settings.isTimeSyncEnabled());
        jsonObject.put(PrivateSettings.Entities.IS_AUTO_TIME_SYNC_ENABLED, settings.isAutoTimeSyncEnabled());
        jsonObject.put(PrivateSettings.Entities.IS_POS_ENABLED, settings.isPosEnabled());
        jsonObject.put(PrivateSettings.Entities.IS_SALE_ENABLED, settings.isSaleEnabled());
        jsonObject.put(PrivateSettings.Entities.IS_USE_MOBILE_DATA, settings.isUseMobileDataEnabled());
        jsonObject.put(PrivateSettings.Entities.SALE_STATION_CODE, settings.getSaleStationCode());
        jsonObject.put(PrivateSettings.Entities.ALLOWED_FINE_CODES, settings.getAllowedFineCodes());
        jsonObject.put(PrivateSettings.Entities.IS_TRANSFER_CONTROL_MODE, settings.isTransferControlMode());
        jsonObject.put(PrivateSettings.Entities.TRANSFER_ROUTE_STATIONS, settings.getTransferRouteStationsCodes());
        jsonObject.put(PrivateSettings.Entities.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED, settings.isOutsideProductionSectionSaleEnabled());
        jsonObject.put(PrivateSettings.Entities.IS_TRANSFER_SALE_ENABLED, settings.isTransferSaleEnabled());
        jsonObject.put(PrivateSettings.Entities.PRINTER_DISCONNECT_TIMEOUT, settings.getPrinterDisconnectTimeout());
        return jsonObject;
    }

}
