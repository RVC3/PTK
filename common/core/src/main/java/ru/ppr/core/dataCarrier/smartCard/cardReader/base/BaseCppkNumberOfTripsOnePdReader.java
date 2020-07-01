package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.HardwareCounterReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.rfid.IRfid;

/**
 * Базовый класс для ридеров карт ЦППК на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCppkNumberOfTripsOnePdReader extends CppkNumberOfTripsPayloadReaderImpl implements CppkNumberOfTripsOnePdReader {

    private static final byte MAX_PD_COUNT = 1;

    private static final byte PD_PAGE_NUMBER = 8;
    private static final byte PD_BYTE_NUMBER = 0;
    private static final byte PD_LENGTH = 22;

    private static final byte HW_COUNTER_INDEX = 0;

    private final ReadPdMifareUltralightReader readPdMifareUltralightReader;
    private final WritePdMifareUltralightReader writePdMifareUltralightReader;
    private final HardwareCounterReader hardwareCounterReader;

    public BaseCppkNumberOfTripsOnePdReader(IRfid rfid,
                                            CardInfo cardInfo,
                                            MifareUltralightReader mifareUltralightReader,
                                            OuterNumberReader outerNumberReader,
                                            ReadPdMifareUltralightReader readPdMifareUltralightReader,
                                            WritePdMifareUltralightReader writePdMifareUltralightReader,
                                            HardwareCounterReader hardwareCounterReader) {
        super(rfid, cardInfo, mifareUltralightReader, outerNumberReader);
        this.readPdMifareUltralightReader = readPdMifareUltralightReader;
        this.writePdMifareUltralightReader = writePdMifareUltralightReader;
        this.hardwareCounterReader = hardwareCounterReader;
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return readPdMifareUltralightReader.readPdList(PD_PAGE_NUMBER, PD_BYTE_NUMBER, PD_LENGTH, MAX_PD_COUNT);
    }

    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {
        int size = pdList.size();

        if (size > MAX_PD_COUNT) {
            throw new IllegalArgumentException("Invalid pd count = " + size);
        }

        WriteCardResult writeCardResult;
        if (size > 0) {
            if (forWriteIndexes[0]) {
                writeCardResult = writePdMifareUltralightReader.writePd(pdList.get(0), PD_PAGE_NUMBER);
                if (!writeCardResult.isSuccess()) {
                    return writeCardResult;
                }
            }
        }

        return new WriteCardResult();
    }

    @NonNull
    @Override
    public ReadCardResult<Integer> readHardwareCounter(int pdIndex) {
        if (pdIndex == 0) {
            ReadCardResult<Integer> readHardwareCounterResult = hardwareCounterReader.readHardwareCounter(HW_COUNTER_INDEX);
            if (readHardwareCounterResult.isSuccess()) {
                if (readHardwareCounterResult.getData() > 0xffff) {
                    // http://agile.srvdev.ru/browse/CPPKPP-30818
                    // http://agile.srvdev.ru/browse/CPPKPP-30356
                    // Для карт с одним билетом v.7 значение счетчика в метке прохода должно быть равным значению физического счетчика
                    // Проблема: размер счетчика на картах Ultralight - 3 байта, размер счетчика в метке прохода - 2 байта
                    // http://agile.srvdev.ru/browse/CPPKPP-38744
                    // Лебедев Сергей​, Корчак Александр:
                    // Если вдруг значение физического счетчика вышло за границы 2-х байт, считаем такую ситуацию некорректной
                    return new ReadCardResult<>(ReadCardErrorType.OTHER, "Hw counter value is too large: " + readHardwareCounterResult.getData());
                } else {
                    return readHardwareCounterResult;
                }
            } else {
                return readHardwareCounterResult;
            }
        } else {
            return new ReadCardResult<>(ReadCardErrorType.OTHER, "pdIndex is out of bounds");
        }
    }

    @NonNull
    @Override
    public WriteCardResult incrementHardwareCounter(int pdIndex, int incrementValue) {
        if (pdIndex == 0) {
            return hardwareCounterReader.incrementHardwareCounter(HW_COUNTER_INDEX, incrementValue);
        } else {
            return new WriteCardResult(WriteCardErrorType.NOT_SUPPORTED, "pdIndex is out of bounds");
        }
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}
