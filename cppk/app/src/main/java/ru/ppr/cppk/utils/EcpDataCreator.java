package ru.ppr.cppk.utils;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV19V20HwCounter;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV23V24HwCounter;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

public class EcpDataCreator {

    private static final String TAG = Logger.makeLogTag(EcpDataCreator.class);

    /**
     * Собирает подписываемые данные для 2х ПД на БСК, (для mifare Classic)
     *
     * @param firstPd       билет который был записан раньше, у него будет браться номер ключа эцп,
     *                      который будет участовать в формировании данных, которые будут проверятся
     * @param secondPd      основной билет, т.е. билет у котого не будем брать номер ключа эцп
     * @param indexMasterPd незнаю как назвать этот костыль, но смысл в том, что если пришел 0,
     *                      то данные мастера ставим на первое место, если пришел 1 то на второе
     * @return
     */
    public static byte[] createEcpData(@NonNull PD firstPd, @NonNull PD secondPd, int indexMasterPd) {
        // Версия ПД 1 byte[1]
        // Порядковый номер, Сроки, условия. ПД 1 byte[3]
        // Дата и время ПД 1 byte[4]
        // Тариф ПД 1 byte[4]
        // Код льготы ПД 1 byte[2]
        // номер ключа ЭЦП ПД 1 byte[4]
        // Версия ПД 2 byte[1]
        // Порядковый номер ПД 2 byte[3]
        // Дата и время ПД 2 byte[4]
        // Тариф ПД 2 byte[4]
        // Код льготы ПД 2 byte[2]
        // Номер кристалла (UID) byte[4]
        // Внешний номер byte[12]

        // 05 1E0001 78AFBF54 AF630300 0000 520000C0
        // 05 580001 80B6BF54 43620300 0000 3F13780C 0000008f63ddec2200000000


        byte[] key1 = EcpUtils.getEcpKeyNumberFromLong(firstPd.ecpNumberPD);

        BscInformation bscInformation = firstPd.getBscInformation();
        if (bscInformation == null) {
            Logger.info(TAG, "bsc information can not be null where check 2 PD");
            return null;
        }

        byte[] cristallSerialNumber = bscInformation
                .getCrystalSerialNumber();
        byte[] outerNumber = bscInformation.getOuterNumberBytes();

        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] out = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();

            if (indexMasterPd == 0) {
                byteArrayOutputStream.write(firstPd.getEcpDataForCheck());
                byteArrayOutputStream.write(getHwCounterData(firstPd));
                byteArrayOutputStream.write(secondPd.getEcpDataForCheck());
                byteArrayOutputStream.write(getHwCounterData(secondPd));
                byteArrayOutputStream.write(EcpUtils.getEcpKeyNumberFromLong(secondPd.ecpNumberPD));
            } else {
                byteArrayOutputStream.write(firstPd.getEcpDataForCheck());
                byteArrayOutputStream.write(getHwCounterData(firstPd));
                byteArrayOutputStream.write(EcpUtils.getEcpKeyNumberFromLong(firstPd.ecpNumberPD));
                byteArrayOutputStream.write(secondPd.getEcpDataForCheck());
                byteArrayOutputStream.write(getHwCounterData(secondPd));
            }

            byteArrayOutputStream.write(cristallSerialNumber);
            byteArrayOutputStream.write(outerNumber);
            out = byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            Logger.error(TAG, "Error while generate data for ecp sign", e);
            return null;
        } finally {
            if (byteArrayOutputStream != null)
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
        }

        Logger.trace(TAG, "createEcpData(firstPd, secondPd, indexMasterPd=" + indexMasterPd + ") return: " + CommonUtils.bytesToHexWithoutSpaces(out));

        return out;
    }

    /**
     * Вернет показания хардварного счетчика ПД для формирования данных ЭЦП (Ultralight Ev1)
     *
     * @param pd
     * @return
     */
    private static byte[] getHwCounterData(@NonNull PD pd) {
        PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);
        if (pdVersion == PdVersion.V19 || pdVersion == PdVersion.V20) {
            // http://agile.srvdev.ru/browse/CPPKPP-37914
            // В формировании данных для подписи участвуют 12 старших бит 3-хбайтного хардварного счетчика
            int hwCounterValue = pd.getHwCounterValue() == null ? 0 : pd.getHwCounterValue();
            PdV19V20HwCounter pdV19V20HwCounter = new PdV19V20HwCounter(hwCounterValue);
            int maxCounterValue = pdV19V20HwCounter.maxCounterValue();
            // Лебедев Сергей​: дополняем 12 бит до 16 лидирующими нулями, и переворачиваем в little endian
            // http://agile.srvdev.ru/browse/CPPKPP-39146
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) maxCounterValue).array();
        } else if (pdVersion == PdVersion.V23 || pdVersion == PdVersion.V24) {
            // http://agile.srvdev.ru/browse/CPPKPP-42009
            // Счетчик перезаписи карты - увеличивается на 1 при каждой перезаписи карты на кассе или БПА.
            // Является старшей частью аппаратного счетчика карты (соответствующего по номеру номеру ПД на карте). Занимает 9 бит.
            // Дополняется до 16 бит и входит в подпись ПД после данных билета (кода тарифа ПД).
            int hwCounterValue = pd.getHwCounterValue() == null ? 0 : pd.getHwCounterValue();
            PdV23V24HwCounter pdV23V24HwCounter = new PdV23V24HwCounter(hwCounterValue);
            int rewriteCounter = pdV23V24HwCounter.getRewriteCounter();
            // Лебедев Сергей​: дополняем 12 бит до 16 лидирующими нулями, и переворачиваем в little endian
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) rewriteCounter).array();
        }
        return new byte[0];
    }

    /**
     * Собирает подписываемые данные для одного ДП на БСК, (для mifare Classic)
     *
     * @param pdOnBsk
     * @return
     */
    public static byte[] createEcpData(@NonNull PD pdOnBsk) {
        // Версия ПД 1 byte[1]
        // Порядковый номер, Сроки, условия. ПД 1 byte[3]
        // Дата и время ПД 1 byte[4]
        // Тариф ПД 1 byte[4]
        // Код льготы ПД 1 byte[2]
        // Номер кристалла (UID) byte[4]
        // Внешний номер byte[12]

        // 05 580001 80B6BF54 43620300 0000 3F13780C 0000008f63ddec2200000000

        BscInformation bscInformation = pdOnBsk.getBscInformation();
        byte[] out = null;
        byte[] crystalSerialNumber = null;
        byte[] outerNumber = null;
        if (bscInformation != null) {
            crystalSerialNumber = bscInformation.getCrystalSerialNumber();
            outerNumber = bscInformation.getOuterNumberBytes();
        }

        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(pdOnBsk.getEcpDataForCheck());

            //добавим моказания хардварного счетчика если необходимо
            byte[] counterBytesForSign = getHwCounterData(pdOnBsk);
            byteArrayOutputStream.write(counterBytesForSign);

            if (crystalSerialNumber != null && outerNumber != null) {
                byteArrayOutputStream.write(crystalSerialNumber);
                byteArrayOutputStream.write(outerNumber);
            }
            out = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Logger.info(TAG, "Error while generate data for ecp sign");
            return null;
        } finally {
            if (byteArrayOutputStream != null)
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
        }

        Logger.trace(TAG, "createEcpData(pdOnBsk) return: " + CommonUtils.bytesToHexWithoutSpaces(out));

        return out;
    }
}
