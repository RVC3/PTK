package ru.ppr.cppk.legacy;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ClearPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.cppk.dataCarrier.PassageMarkFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.PassageMarkToLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes.Result;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.logic.interactor.ToLegacyPdListConverter;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardReadErrorType;
import ru.ppr.rfid.WriteToCardResult;

/**
 * @author Aleksandr Brazhkin
 */
@Deprecated
public class BscReader {

    private static final String TAG = Logger.makeLogTag(BscReader.class);

    private static final int PD_INDEX_FOR_HW_COUNTER = 0;

    private final CardReader cardReader;

    public CardReader getCardReader() {
        return cardReader;
    }

    public BscReader(@NonNull CardReader cardReader) {
        this.cardReader = cardReader;
    }

    public Result<List<PD>> readPd(BscInformation bscInformation, ru.ppr.cppk.dataCarrier.entity.PassageMark passageMark) {

        if (!(cardReader instanceof ReadPdReader)) {
            return new Result<>(CardReadErrorType.OTHER, "readPd is not supported for " + cardReader.getClass().getSimpleName());
        }

        ReadPdReader readPdReader = (ReadPdReader) cardReader;

        Result<List<PD>> result;

        ReadCardResult<List<Pd>> pdListResult = readPdReader.readPdList();
        if (pdListResult.isSuccess()) {
            List<PD> legacyPdList = new ToLegacyPdListConverter().convert(pdListResult.getData(), bscInformation, passageMark);
            result = new Result<>(legacyPdList);
        } else {
            result = new Result<>(map(pdListResult.getReadCardErrorType()), pdListResult.getDescription());
        }

        return result;
    }

    public WriteToCardResult writePD(int pdPosition, byte[] fullPd, boolean needClearPdList) {

        if (!(cardReader instanceof WritePdReader)) {
            return WriteToCardResult.UNKNOWN_ERROR;
        }

        WriteToCardResult result;

        if (needClearPdList) {
            if (!(cardReader instanceof ClearPdReader)) {
                return WriteToCardResult.UNKNOWN_ERROR;
            }
            ClearPdReader clearPdReader = (ClearPdReader) cardReader;
            WriteCardResult res = clearPdReader.clearPdList();
            if (!res.isSuccess()) {
                return map(res.getWriteCardErrorType());
            }
        }

        WritePdReader writePdReader = (WritePdReader) cardReader;

        PdDecoder pdDecoder = Dagger.appComponent().pdDecoderFactory().create(fullPd);
        Pd pd = pdDecoder.decode(fullPd);

        if (pd != null) {
            List<Pd> pdList = new ArrayList<>(pdPosition + 1);
            boolean[] forWriteIndexes = new boolean[pdPosition + 1];

            if (pdPosition == 0) {
                pdList.add(pd);
                forWriteIndexes[0] = true;
            } else {
                pdList.add(null);
                pdList.add(pd);
                forWriteIndexes[1] = true;
            }

            WriteCardResult pdListResult = writePdReader.writePdList(pdList, forWriteIndexes);
            result = map(pdListResult.getWriteCardErrorType());
        } else {
            result = WriteToCardResult.UNKNOWN_ERROR;
        }

        return result;
    }

    public Result<ru.ppr.cppk.dataCarrier.entity.PassageMark> readPassageMark() {

        /*
         * В начале считываем показания аппратаного счетчики и информацию о метке прохода,
         * после чего сравниваем показания аппаратного счетчика и счетчика из метки,
         * если они не равны то перезаписываем метку с показанием аппаратного счетчика,
         * после чего возвращаем метку прохода.
         */


        Result<ru.ppr.cppk.dataCarrier.entity.PassageMark> result;

        //читаем метку с карт со счетчиком
        if (cardReader instanceof CppkNumberOfTripsOnePdReader) {

            CppkNumberOfTripsOnePdReader cppkNumberOfTripsOnePdReader = (CppkNumberOfTripsOnePdReader) cardReader;

            ReadCardResult<Integer> hardwareCounterResult = cppkNumberOfTripsOnePdReader.readHardwareCounter(PD_INDEX_FOR_HW_COUNTER);

            if (hardwareCounterResult.isSuccess()) {

                ReadCardResult<PassageMark> passageMarkResult = cppkNumberOfTripsOnePdReader.readPassageMark();
                if (passageMarkResult.isSuccess()) {
                    PassageMark passageMark = passageMarkResult.getData();
                    ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = null;
                    if (passageMark != null) {
                        boolean shouldRewriteCounterValueInMark;
                        if (passageMark instanceof PassageMarkV5Impl) {
                            PassageMarkV5Impl passageMarkV5 = (PassageMarkV5Impl) passageMark;
                            passageMarkV5.setHwCounterValue(hardwareCounterResult.getData());
                            shouldRewriteCounterValueInMark = hardwareCounterResult.getData() != passageMarkV5.getHwCounterValue();
                        } else {
                            shouldRewriteCounterValueInMark = false;
                        }

                        if (shouldRewriteCounterValueInMark) {

                            WriteCardResult writePassageMarkResult = cppkNumberOfTripsOnePdReader.writePassageMark(passageMark);
                            if (!writePassageMarkResult.isSuccess()) {
                                Logger.error(TAG, "Could not rewrite counter in passage mark");
                            }
                        }

                        legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(passageMarkResult.getData());
                    }
                    result = new Result<>(legacyPassageMark);
                } else {
                    result = new Result<>(map(passageMarkResult.getReadCardErrorType()), passageMarkResult.getDescription());
                }

            } else {
                final String message = "Error read hardware counter - " + hardwareCounterResult.getDescription();
                result = new Result<>(map(hardwareCounterResult.getReadCardErrorType()), message);
            }
        } else {
            // Читаем метку с карт без счетчика
            if (!(cardReader instanceof ReadPassageMarkReader)) {
                return new Result<>(CardReadErrorType.OTHER, "readPassageMark is not supported for " + cardReader.getClass().getSimpleName());
            }

            ReadPassageMarkReader readPassageMarkReader = (ReadPassageMarkReader) cardReader;

            ReadCardResult<PassageMark> passageMarkResult = readPassageMarkReader.readPassageMark();
            if (passageMarkResult.isSuccess()) {
                PassageMark passageMark = passageMarkResult.getData();
                ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = null;
                if (passageMark != null) {
                    legacyPassageMark = new PassageMarkToLegacyMapper().toLegacyPassageMark(passageMarkResult.getData());
                }
                result = new Result<>(legacyPassageMark);
            } else {
                result = new Result<>(map(passageMarkResult.getReadCardErrorType()), passageMarkResult.getDescription());
            }
        }

        return result;
    }

    public WriteToCardResult writePassageMark(byte[] cardUID, ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {

        if (!(cardReader instanceof WritePassageMarkReader)) {
            return WriteToCardResult.UNKNOWN_ERROR;
        }

        WritePassageMarkReader writePassageMarkReader = (WritePassageMarkReader) cardReader;

        WriteToCardResult result;

        PassageMark passageMark = new PassageMarkFromLegacyMapper().fromLegacyPassageMark(legacyPassageMark);

        if (passageMark != null) {
            WriteCardResult passageMarkResult = writePassageMarkReader.writePassageMark(passageMark);
            result = map(passageMarkResult.getWriteCardErrorType());
        } else {
            result = WriteToCardResult.UNKNOWN_ERROR;
        }

        return result;
    }

    public Result<byte[]> readECP() {

        if (!(cardReader instanceof ReadEdsReader)) {
            return new Result<>(CardReadErrorType.OTHER, "readECP is not supported for " + cardReader.getClass().getSimpleName());
        }

        ReadEdsReader readEdsReader = (ReadEdsReader) cardReader;

        Result<byte[]> result;

        ReadCardResult<byte[]> edsResult = readEdsReader.readEds();
        if (edsResult.isSuccess()) {
            byte[] eds = edsResult.getData();
            result = new Result<>(eds);
        } else {
            result = new Result<>(map(edsResult.getReadCardErrorType()), edsResult.getDescription());
        }

        return result;
    }

    public WriteToCardResult writeEcp(byte[] ecp, byte[] cardUID) {

        if (!(cardReader instanceof WriteEdsReader)) {
            return WriteToCardResult.UNKNOWN_ERROR;
        }

        WriteEdsReader writeEdsReader = (WriteEdsReader) cardReader;

        WriteToCardResult result;

        WriteCardResult edsResult = writeEdsReader.writeEds(ecp);
        if (edsResult.isSuccess()) {
            result = WriteToCardResult.SUCCESS;
        } else {
            result = map(edsResult.getWriteCardErrorType());
        }

        return result;
    }

    public int getMaxPdCount() {
        if (!(cardReader instanceof WithMaxPdCountReader)) {
            throw new UnsupportedOperationException("getMaxPdCount is not supported for " + cardReader.getClass().getSimpleName());
        }
        WithMaxPdCountReader withMaxPdCountReader = (WithMaxPdCountReader) cardReader;
        return withMaxPdCountReader.getMaxPdCount();
    }

    public boolean canReadPd() {
        return cardReader instanceof ReadPdReader;
    }

    public boolean canWritePd() {
        return cardReader instanceof WritePdReader;
    }

    /**
     * Конвертирует {@link ReadCardErrorType} в {@link CardReadErrorType}
     *
     * @param readCardErrorType Тип ошибки чтения карты с нижнего уровня
     * @return Тип ошибки чтения карты
     */
    public CardReadErrorType map(ReadCardErrorType readCardErrorType) {
        switch (readCardErrorType) {
            case NONE:
                return CardReadErrorType.NONE;
            case AUTHORIZATION:
                return CardReadErrorType.AUTHORIZATION;
            case OTHER:
                return CardReadErrorType.OTHER;
            case UID_DOES_NOT_MATCH:
                return CardReadErrorType.UID_DOES_NOT_MATCH;
            default:
                return CardReadErrorType.OTHER;
        }
    }

    /**
     * Конвертирует {@link WriteCardErrorType} в {@link WriteToCardResult}
     *
     * @param writeCardErrorType Тип ошибки записи на карту с нижнего уровня
     * @return Тип ошибки записи на карту
     */
    private WriteToCardResult map(WriteCardErrorType writeCardErrorType) {
        switch (writeCardErrorType) {
            case SUCCESS:
                return WriteToCardResult.SUCCESS;
            case UID_DOES_NOT_MATCH:
                return WriteToCardResult.UID_DOES_NOT_MATCH;
            case CAN_NOT_SEARCH_CARD:
                return WriteToCardResult.CAN_NOT_SEARCH_CARD;
            case UNKNOWN_ERROR:
                return WriteToCardResult.UNKNOWN_ERROR;
            case WRITE_ERROR:
                return WriteToCardResult.WRITE_ERROR;
            case NOT_SUPPORTED:
                return WriteToCardResult.NOT_SUPPORTED;
            default:
                return WriteToCardResult.UNKNOWN_ERROR;
        }
    }
}
