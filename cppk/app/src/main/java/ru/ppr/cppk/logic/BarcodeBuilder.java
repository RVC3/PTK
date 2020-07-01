package ru.ppr.cppk.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.v1.PdV1Impl;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * @author Dmitry Nevolin
 */
public class BarcodeBuilder {

    private static final String TAG = Logger.makeLogTag(BarcodeBuilder.class);

    @Inject
    BarcodeBuilder() {

    }

    /**
     * @param dataSalePD
     * @return
     */
    public Pd buildAsPd(DataSalePD dataSalePD) {
        PdV1Impl pdV1 = new PdV1Impl();
        pdV1.setOrderNumber(dataSalePD.getPDNumber());
        pdV1.setDirection(dataSalePD.getDirection() == TicketWayType.TwoWay ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV1.setPaymentType(dataSalePD.getPaymentType() == PaymentType.INDIVIDUAL_CASH ? PdWithPaymentType.PAYMENT_TYPE_CASH : PdWithPaymentType.PAYMENT_TYPE_CARD);
        pdV1.setStartDayOffset(dataSalePD.getTerm());
        pdV1.setExemptionCode(dataSalePD.getExemptionForEvent() == null ? 0 : dataSalePD.getExemptionForEvent().getExpressCode());
        pdV1.setSaleDateTime(dataSalePD.getSaleDateTime());
        pdV1.setTariffCode(dataSalePD.getTariffThere().getCode());
        return pdV1;
    }

    public byte[] buildAsByteArray(SignDataResult signDataResult) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ByteBuffer ecpDataBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            ecpDataBuffer.putInt(EcpUtils.convertLongToInt(signDataResult.getEdsKeyNumber()));
            byte[] ecpKeyLittleEndian = ecpDataBuffer.array();
            // замутим данные для ШК
            stream.write(signDataResult.getData());
            stream.write(ecpKeyLittleEndian);
            stream.write(signDataResult.getSignature());
            byte[] data = stream.toByteArray();
            // ВЫВОД ДЛЯ ТЕСТА
            if (data != null) {
                Logger.trace(TAG, "buildBarcodeData: " + CommonUtils.bytesToHexWithoutSpaces(data));
            }
            return data;
        } catch (IOException e) {
            Logger.error(TAG, e);
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
        }
    }

}
