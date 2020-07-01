package ru.ppr.moebius;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.moebiusdrvr.KKMInfoStateData;

import java.io.File;

/**
 * Класс принтера, эммулирующий работу эклз
 * //https://aj.srvdev.ru/browse/CPPKPP-27557
 * Created by Artem Ushakov on 05.07.2016.
 */
public class PrinterZebraMoebiusVirtualEklz extends PrinterZebraMoebius {

    public static final int MASK_IS_FISCAL = 0b00000010;

    public PrinterZebraMoebiusVirtualEklz(@NonNull final Context context,
                                          @NonNull File logDir,
                                          @Nullable String printerMacAddress,
                                          @NonNull final BluetoothManager BluetoothManager) throws Exception {
        super(context, logDir, printerMacAddress, BluetoothManager);
    }

    @Override
    protected void openShiftImpl(int operatorCode, String operatorName) throws Exception {
        // В начале проверим фискализирован ли принтер или нет.
        // Если принтер фискализирован, то текущим драйвером пользоваться нельзя
        // и будет показана ошибка, в резульате чего смена не откроется.
        checkFiscalPrinter();
        super.openShiftImpl(operatorCode, operatorName);
    }

    /**
     * Проверяет фискализирован ли данный принтер. Если фискализирован, то бросает Exception
     */
    private void checkFiscalPrinter() throws Exception {
        final KKMInfoStateData kkmInfoStateData;
        kkmInfoStateData = kkmGetKKMInfoState((byte) 0x00);
        final byte state = kkmInfoStateData.State;
        if ((state & MASK_IS_FISCAL) == MASK_IS_FISCAL) {
            throw new Exception("Printer in fiscal mode!");
        }
    }

    @Override
    protected int getShiftNumImpl() throws Exception {
        return (int) kkmGetKKMInfoState((byte) 0x00).NumShift;
    }
}
