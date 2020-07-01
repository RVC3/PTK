package ru.ppr.ingenico.model.operations;

import ru.ppr.ingenico.model.responses.Response;

/**
 * Created by Dmitry Nevolin on 20.11.2015.
 */
public class OperationTestTerminal extends OperationResultTransaction {

    public OperationTestTerminal() {
        super(0, 166);
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
