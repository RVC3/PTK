package ru.ppr.ingenico.model.operations;

import ru.ppr.ingenico.model.responses.Response;

/**
 * Created by Dmitry Nevolin on 20.02.2016.
 */
public class OperationInvokeAdministrativeMenu extends OperationResultTransaction {

    public OperationInvokeAdministrativeMenu() {
        super(0, 53);
    }

    @Override
    protected Response pingResponse() {
        return RESPONSE_OK;
    }

    @Override
    protected Response printResponse() {
        return RESPONSE_OK;
    }

    @Override
    protected Response set_tagsResponse() {
        return RESPONSE_OK;
    }

    @Override
    protected Response store_rcResponse() {
        return RESPONSE_OK;
    }

    @Override
    protected Response end_trResponse() {
        complete();

        return RESPONSE_OK;
    }

}
