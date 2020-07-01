package ru.ppr.ingenico.model.operations;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.ingenico.model.requests.Request;
import ru.ppr.ingenico.model.responses.Response;
import ru.ppr.ingenico.model.responses.ResponseError;
import ru.ppr.ingenico.model.responses.ResponseOk;
import ru.ppr.ingenico.utils.Arcus2Utils;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Created by Dmitry Nevolin on 13.11.2015.
 */
public abstract class Operation<T extends TransactionResult> {

    private static final byte SPLIT = 0x1B;
    /**
     * Таймаут на выполнение опреации по умолчанию, мс
     */
    private static final long DEFAULT_OPERATION_TIMEOUT = 30000;

    protected static final Response RESPONSE_OK = new ResponseOk();
    protected static final Response RESPONSE_ERROR = new ResponseError();

    private boolean completed;
    private List<Request.Body> requestBodies;

    private int operationClass;
    private int operationCode;

    Operation(int operationClass, int operationCode) {
        requestBodies = new ArrayList<Request.Body>();

        this.operationClass = operationClass;
        this.operationCode = operationCode;
    }

    public final byte[] packSelf() {
        return Arcus2Utils.packDefault(getBytes());
    }

    protected byte[] currencyCode() {
        return new byte[0];
    }

    protected byte[] sum() {
        return new byte[0];
    }

    protected byte[] track1() {
        return new byte[0];
    }

    protected byte[] track2() {
        return new byte[0];
    }

    private byte[] getBytes() {
        List<Byte> data = new ArrayList<Byte>();

        //класс операции
        for (byte tmp : Arcus2Utils.convertStringToBytes(String.valueOf(getOperationClass())))
            data.add(tmp);

        //разделитель
        data.add(SPLIT);

        //код операции
        for (byte tmp : Arcus2Utils.convertStringToBytes(String.valueOf(getOperationCode())))
            data.add(tmp);

        //разделитель
        data.add(SPLIT);

        //код валюты
        for (byte tmp : currencyCode())
            data.add(tmp);

        //разделитель
        data.add(SPLIT);

        //сумма в формате рубли.копейки (.??)
        for (byte tmp : sum())
            data.add(tmp);

        //разделитель
        data.add(SPLIT);

        //Track 1
        for (byte tmp : track1())
            data.add(tmp);

        //разделитель
        data.add(SPLIT);

        //Track 2
        for (byte tmp : track2())
            data.add(tmp);

        byte[] result = new byte[data.size()];

        for (int i = 0; i < data.size(); i++)
            result[i] = data.get(i);

        return result;
    }

    protected Response traceResponse() {
        return RESPONSE_ERROR;
    }

    protected Response printResponse() {
        return RESPONSE_ERROR;
    }

    protected Response pingResponse() {
        return RESPONSE_ERROR;
    }

    protected Response statusResponse() {
        return RESPONSE_ERROR;
    }

    protected Response store_rcResponse() {
        return RESPONSE_ERROR;
    }

    protected Response i344Response() {
        return RESPONSE_ERROR;
    }

    protected Response device_openResponse() {
        return RESPONSE_ERROR;
    }

    protected Response device_closeResponse() {
        return RESPONSE_ERROR;
    }

    protected Response io_ctlResponse() {
        return RESPONSE_ERROR;
    }

    protected Response connectResponse() {
        return RESPONSE_ERROR;
    }

    protected Response disconnectResponse() {
        return RESPONSE_ERROR;
    }

    protected Response writeResponse() {
        return RESPONSE_ERROR;
    }

    protected Response readResponse() {
        return RESPONSE_ERROR;
    }

    protected Response menuResponse() {
        return RESPONSE_ERROR;
    }

    protected Response amount_entryResponse() {
        return RESPONSE_ERROR;
    }

    protected Response data_entryResponse() {
        return RESPONSE_ERROR;
    }

    protected Response warningResponse() {
        return RESPONSE_ERROR;
    }

    protected Response card_reqResponse() {
        return RESPONSE_ERROR;
    }

    protected Response end_trResponse() {
        complete();

        return RESPONSE_ERROR;
    }

    protected Response yes_noResponse() {
        return RESPONSE_ERROR;
    }

    protected Response begin_trResponse() {
        return RESPONSE_ERROR;
    }

    protected Response op_detResponse() {
        return RESPONSE_ERROR;
    }

    protected Response time_syncResponse() {
        return RESPONSE_ERROR;
    }

    protected Response tms_idResponse() {
        return RESPONSE_ERROR;
    }

    protected Response tms_scriptResponse() {
        return RESPONSE_ERROR;
    }

    protected Response displayResponse() {
        return RESPONSE_ERROR;
    }

    protected Response clear_dispResponse() {
        return RESPONSE_ERROR;
    }

    protected Response disp_invResponse() {
        return RESPONSE_ERROR;
    }

    protected Response get_tagsResponse() {
        return RESPONSE_ERROR;
    }

    protected Response set_tagsResponse() {
        return RESPONSE_ERROR;
    }

    protected Response sp_resultResponse() {
        return RESPONSE_ERROR;
    }

    protected Response sp_requestResponse() {
        return RESPONSE_ERROR;
    }

    protected Response key_reqResponse() {
        return RESPONSE_ERROR;
    }

    protected Response write_barcodeResponse() {
        return RESPONSE_ERROR;
    }

    protected Response oob_sessionResponse() {
        return RESPONSE_ERROR;
    }

    protected Response startResponse() {
        return RESPONSE_ERROR;
    }

    protected Response checkResponse() {
        return RESPONSE_ERROR;
    }

    protected Response endResponse() {
        return RESPONSE_ERROR;
    }

    protected Response continueResponse() {
        return RESPONSE_ERROR;
    }

    protected Response stand_byResponse() {
        return RESPONSE_ERROR;
    }

    public Response makeResponse(Request.Body body) {
        requestBodies.add(body);

        switch (body.getType()) {
            case TRACE:
                return traceResponse();
            case PRINT:
                return printResponse();
            case PING:
                return pingResponse();
            case STATUS:
                return statusResponse();
            case STORE_RC:
                return store_rcResponse();
            case I344:
                return i344Response();
            case DEVICE_OPEN:
                return device_openResponse();
            case DEVICE_CLOSE:
                return device_closeResponse();
            case IO_CTL:
                return io_ctlResponse();
            case CONNECT:
                return connectResponse();
            case DISCONNECT:
                return disconnectResponse();
            case WRITE:
                return writeResponse();
            case READ:
                return readResponse();
            case MENU:
                return menuResponse();
            case AMOUNT_ENTRY:
                return amount_entryResponse();
            case DATA_ENTRY:
                return data_entryResponse();
            case WARNING:
                return warningResponse();
            case CARD_REQ:
                return card_reqResponse();
            case END_TR:
                return end_trResponse();
            case YES_NO:
                return yes_noResponse();
            case BEGIN_TR:
                return begin_trResponse();
            case OP_DET:
                return op_detResponse();
            case TIME_SYNC:
                return time_syncResponse();
            case TMS_ID:
                return tms_idResponse();
            case TMS_SCRIPT:
                return tms_scriptResponse();
            case DISPLAY:
                return displayResponse();
            case CLEAR_DISP:
                return clear_dispResponse();
            case DISP_INV:
                return disp_invResponse();
            case GET_TAGS:
                return get_tagsResponse();
            case SET_TAGS:
                return set_tagsResponse();
            case SP_RESULT:
                return sp_resultResponse();
            case SP_REQUEST:
                return sp_requestResponse();
            case KEY_REQ:
                return key_reqResponse();
            case WRITE_BARCODE:
                return write_barcodeResponse();
            case OOB_SESSION:
                return oob_sessionResponse();
            case START:
                return startResponse();
            case CHECK:
                return checkResponse();
            case END:
                return endResponse();
            case CONTINUE:
                return continueResponse();
            case STAND_BY:
                return stand_byResponse();
            default:
                complete();
                return RESPONSE_ERROR;
        }
    }

    protected final void complete() {
        completed = true;
    }

    protected final List<Request.Body> getRequestBodies() {
        return new ArrayList<Request.Body>(requestBodies);
    }

    public T getResult() {
        return null;
    }

    public final boolean isCompleted() {
        return completed;
    }

    public final int getOperationClass() {
        return operationClass;
    }

    public final int getOperationCode() {
        return operationCode;
    }

    /**
     * Возвращает таймаут на выполнение операции
     *
     * @return таймаут, мс
     */
    public long getTimeout() {
        return DEFAULT_OPERATION_TIMEOUT;
    }

}
