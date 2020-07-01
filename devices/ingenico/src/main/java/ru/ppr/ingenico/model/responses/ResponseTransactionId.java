package ru.ppr.ingenico.model.responses;

import ru.ppr.ingenico.utils.BER;
import ru.ppr.ingenico.utils.BitUtils;
import ru.ppr.ingenico.utils.Arcus2Utils;

/**
 * Created by Dmitry Nevolin on 23.11.2015.
 */
public class ResponseTransactionId extends Response {

    private int transactionId;

    public ResponseTransactionId(int transactionId) {
        super(String.valueOf(transactionId));

        this.transactionId = transactionId;
    }

    @Override
    public byte[] packSelf() {
        return Arcus2Utils.packDefault(BER.encodeUniversalPrimitive(0x1F26, BitUtils.convertToBytes(transactionId)));
    }

}
