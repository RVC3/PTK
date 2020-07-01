package ru.ppr.core.dataCarrier.pd.v64;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;

/**
 * Декодер ПД v.64.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV64Decoder implements PdDecoder {

    @Override
    public PdV64 decode(@NonNull byte[] data) {

        if (data.length < PdV64Structure.PD_SIZE)
            return null;

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV64Structure.SALE_DATE_TIME_BYTE_INDEX, PdV64Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV64Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV64Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV64Impl pdV64 = new PdV64Impl();
        pdV64.setSaleDateTime(saleDateTime);
        pdV64.setEdsKeyNumber(edsKeyNumber);

        return pdV64;
    }

}
