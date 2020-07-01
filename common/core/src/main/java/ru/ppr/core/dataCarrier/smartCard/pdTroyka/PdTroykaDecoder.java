package ru.ppr.core.dataCarrier.smartCard.pdTroyka;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;

/**
 * Декодер комплексные и единые билеты
 *
 * @author Sergey Kolesnikov
 */
public class PdTroykaDecoder implements PdDecoder {

    @Nullable
    @Override
    public MetroPd decode(@NonNull byte[] data) {

        if (data.length < PdCommonTroykaStructure.PD_DATA_SIZE)
            return null;

        int typeTicket1 = DataCarrierUtils.getValue(data, PdCommonTroykaStructure.TYPE_TICKET_1_INDEX, PdCommonTroykaStructure.TYPE_TICKET_1_LENGTH);
        int typeTicket2 = DataCarrierUtils.getValue(data, PdCommonTroykaStructure.TYPE_TICKET_2_INDEX, PdCommonTroykaStructure.TYPE_TICKET_2_LENGTH);
        int formatEncoding = DataCarrierUtils.getValue(data, PdCommonTroykaStructure.FORMAT_ENCODING_INDEX, PdCommonTroykaStructure.FORMAT_ENCODING_LENGTH);
        int extNumberFormat = DataCarrierUtils.getValue(data, PdCommonTroykaStructure.EXTENSIONS_NUMBER_FORMAT_INDEX, PdCommonTroykaStructure.EXTENSIONS_NUMBER_FORMAT_LENGT);
        int dateTimeNow = DataCarrierUtils.getValue(data, PdCommonTroykaStructure.DATE_TIME_NOW_INDEX, PdCommonTroykaStructure.DATE_TIME_NOW_LEGTH);

        PdTroykaImpl pdTroykaImpl = new PdTroykaImpl();
        pdTroykaImpl.setTypeTicket1(typeTicket1);
        pdTroykaImpl.setTypeTicket2(typeTicket2);
        pdTroykaImpl.setDateTimeNow(dateTimeNow);
        pdTroykaImpl.setFormatData(formatEncoding, extNumberFormat);

        return pdTroykaImpl;
    }

}
