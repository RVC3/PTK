package ru.ppr.ingenico.model.operations;

import ru.ppr.ingenico.model.responses.Response;
import ru.ppr.ingenico.model.responses.ResponseTransactionId;

/**
 * Created by Dmitry Nevolin on 20.11.2015.
 */
public class OperationCancel extends OperationResultFinancialTransaction {

    private int transactionId;

    public OperationCancel(int transactionId) {
        super(0, 225);

        this.transactionId = transactionId;
    }

    @Override
    protected Response get_tagsResponse() {
        return new ResponseTransactionId(transactionId);
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
