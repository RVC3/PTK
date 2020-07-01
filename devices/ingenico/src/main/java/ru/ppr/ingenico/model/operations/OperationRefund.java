package ru.ppr.ingenico.model.operations;

import java.util.Locale;

import ru.ppr.ingenico.model.responses.Response;
import ru.ppr.ingenico.utils.Arcus2Utils;

/**
 * Created by Dmitry Nevolin on 20.11.2015.
 */
public class OperationRefund extends OperationResultFinancialTransaction {

    private int price;

    public OperationRefund(int price) {
        super(0, 130);

        this.price = price;
    }

    @Override
    protected byte[] sum() {
        return Arcus2Utils.convertStringToBytes(String.format(Locale.US, "%.2f", price == 0 ? 0f : price / 100f));
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
