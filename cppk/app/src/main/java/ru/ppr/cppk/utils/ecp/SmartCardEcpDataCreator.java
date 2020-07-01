package ru.ppr.cppk.utils.ecp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Класс для создания подписываемых данных билета, которые будут записаны на БСК
 * <p>
 * Created by Артем on 18.01.2016.
 */
public class SmartCardEcpDataCreator implements EcpDataCreator {

    private static final String TAG = Logger.makeLogTag(SmartCardEcpDataCreator.class);

    private final byte[] writePd;
    private final BscInformation bscInformation;
    private final byte[] pd;
    private final int oldPdIndex;

    private SmartCardEcpDataCreator(@NonNull Builder builder) {
        pd = builder.pd;
        bscInformation = builder.bscInformation;
        writePd = builder.currentPd;
        oldPdIndex = builder.pdIndex;
    }

    @Override
    public byte[] create() {

        Logger.trace(getClass(), "Create data for sign ecp for smart card");

        byte[] crystalSerialNumber = bscInformation.getCrystalSerialNumber();
        byte[] outerNumber = bscInformation.getOuterNumberBytes();

        Logger.trace(getClass(), "Crystal serial number - " + CommonUtils.bytesToHexWithSpaces(crystalSerialNumber));
        Logger.trace(getClass(), "Outer number - " + CommonUtils.bytesToHexWithSpaces(outerNumber));

        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] dataForSign = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            // если на карте есть ранее записанный пд, то добавляем его в массив для подписи

            if (pd != null) {
                // если старый билет записан на 0 позиции, до добавим сначала старый билет, потом новый
                // иначе наоборот
                if (oldPdIndex == 0) {
                    byteArrayOutputStream.write(pd);
                    byteArrayOutputStream.write(writePd);
                    Logger.trace(getClass(), "Old pd data - " + CommonUtils.bytesToHexWithSpaces(pd));
                    Logger.trace(getClass(), "New pd data - " + CommonUtils.bytesToHexWithSpaces(writePd));
                } else {
                    byteArrayOutputStream.write(writePd);
                    byteArrayOutputStream.write(pd);
                    Logger.trace(getClass(), "New pd data - " + CommonUtils.bytesToHexWithSpaces(writePd));
                    Logger.trace(getClass(), "Old pd data - " + CommonUtils.bytesToHexWithSpaces(pd));
                }
            } else {
                byteArrayOutputStream.write(writePd);
                Logger.trace(getClass(), "New pd data - " + CommonUtils.bytesToHexWithSpaces(writePd));
            }

            byteArrayOutputStream.write(crystalSerialNumber);
            byteArrayOutputStream.write(outerNumber);
            dataForSign = byteArrayOutputStream.toByteArray();
            Logger.trace(getClass(), "Data for sign - " + CommonUtils.bytesToHexWithSpaces(dataForSign));
        } catch (Exception e) {
            Logger.error(TAG, "Error create data for sign", e);
            return null;
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataForSign;
    }

    public static class Builder {

        //данные билета, который продаем
        private final byte[] currentPd;
        //информация о карте
        private final BscInformation bscInformation;
        // билет, который был записан на карте, может быть null
        private byte[] pd;
        // порядковый номер существующего билета на карте
        private int pdIndex;

        public Builder(@NonNull byte[] currentPd, @NonNull BscInformation bscInformation) {
            this.currentPd = currentPd;
            this.bscInformation = bscInformation;
        }

        public Builder setExistPd(@Nullable PD pd) {
            if (pd != null) {
                //если на карте есть ранее записанный пд, то добавляем его в массив для подписи
                byte[] oldPdData = pd.getEcpDataForCheck();
                long ecpKeyNumber = pd.ecpNumberPD;
                byte[] tmpEcpKey = CommonUtils.generateByteArrayFromLong(ecpKeyNumber);
                byte[] data = new byte[oldPdData.length + GlobalConstants.ECP_KEY_BYTES];
                System.arraycopy(oldPdData, 0, data, 0, oldPdData.length);
                System.arraycopy(tmpEcpKey, 0, data, oldPdData.length, GlobalConstants.ECP_KEY_BYTES);
                return setExistPdBytes(data, pd.orderNumberPdOnCard);
            }
            return this;
        }

        /**
         * @param pdBytes данные билета месте с номером ключа эцп
         * @param pdIndex порядковый номер билета на карте, может быть 0 или 1
         * @return
         */
        public Builder setExistPdBytes(@NonNull byte[] pdBytes, int pdIndex) {

            Preconditions.checkArgument(pdIndex >= 0 && pdIndex < 2, "Incorrect pd index - " + pdIndex);
            this.pd = pdBytes;
            this.pdIndex = pdIndex;
            return this;
        }

        public EcpDataCreator build() {
            Preconditions.checkArgument(currentPd.length > 0);
            return new SmartCardEcpDataCreator(this);
        }
    }
}
