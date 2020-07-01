package ru.ppr.cppk.export.writer;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ppr.cppk.export.model.State;

/**
 * Конвертер в json для модельки {@link State}
 *
 * @author Grigoriy Kashka
 */
public class StateWriter {

    EventsDateTimeWriter eventsDateTimeWriter;

    public StateWriter() {
        eventsDateTimeWriter = new EventsDateTimeWriter();
    }

    public JSONObject getJson(State state) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("TerminalType", state.terminalType);
        json.put("DeviceId", state.id);
        json.put("SoftwareVersion", state.softwareVersion);
        json.put("DataContracts", state.dataContracts);
        json.put("RdsVersion", state.rdsVersion);
        json.put("SecurityVersion", state.securityVersion);
        json.put("SftInKeysVersion", state.sftInKeysVersion);
        json.put("TicketStopListItem", state.ticketStopListItem);
        json.put("TicketWhitelistItem", state.ticketWhitelistItem);
        json.put("SmartCardStopListItem", state.smartCardStopListItem);
        json.put("PtkSftState", state.ptkSftState);
        json.put("latestEventTimestamp", state.latestEventTimestamp);
        json.put("LastTimestampsForEvent", eventsDateTimeWriter.getJson(state.lastTimestampsForEvent));
        json.put("LastTimestampsForSentEvent", eventsDateTimeWriter.getJson(state.lastTimestampsForSentEvent));

        return json;
    }

}
