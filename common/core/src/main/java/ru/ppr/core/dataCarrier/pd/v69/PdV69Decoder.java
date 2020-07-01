package ru.ppr.core.dataCarrier.pd.v69;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;

/**
 * Декодер ПД v.69.
 *
 * @author isedoi
 */
public class PdV69Decoder implements PdDecoder {

    @Override
    public PdV69 decode(@NonNull byte[] data) {

        if (data.length < PdV69Structure.PD_SIZE)
            return null;

        final int appTransportCode = DataCarrierUtils.getValue(data, 0, 10);

        PdV69 pdV69 = new PdV69Impl();

        return pdV69;
    }

}
