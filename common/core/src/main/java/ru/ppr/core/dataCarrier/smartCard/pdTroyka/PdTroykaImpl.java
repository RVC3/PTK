package ru.ppr.core.dataCarrier.smartCard.pdTroyka;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePd;
import ru.ppr.logger.Logger;

/**
 *
 * Комплексные и единые билеты
 *
 * @author Sergey Kolesnikov
 */

public class PdTroykaImpl extends BasePd implements MetroPd {

    private static final String TAG = Logger.makeLogTag(PdTroykaImpl.class);

    /**
     *   Тип билета №1 CRDCODE
     */
    private int typeTicket1;

    /**
     *   Тип билета №2 CRDCODE
     */
    private int typeTicket2;

    /**
     *   Дата и время последнего кодирования
     * DateTime.Now
     */
    private int dateTimeNow;

    /**
     *   Валидность формата данных
     */
    private boolean validFormatData;

    PdTroykaImpl() {
        super(PdVersion.V0, PdCommonTroykaStructure.PD_SIZE);
    }


    public void setTypeTicket1(int typeTicket1) {
        this.typeTicket1 = typeTicket1;
    }


    public void setTypeTicket2(int typeTicket2) {
        this.typeTicket2 = typeTicket2;
    }

    public void setDateTimeNow(int dateTimeNow) {
        this.dateTimeNow = dateTimeNow;
    }

    @Override
    public int getTypeTicket1() {
        return typeTicket1;
    }

    void setFormatData(int codeFormat, int extendNumFormat){
        validFormatData = codeFormat == PdCommonTroykaStructure.CODING_FORMAT_VALUE && extendNumFormat == PdCommonTroykaStructure.EXTEND_NUM_FORMAT_VALUE;
    }

    @Override
    public int getTypeTicket2() {
        return typeTicket2;
    }

    @Override
    public int getDateTimeNow() {
        return dateTimeNow;
    }

    @Override
    public boolean isValidFormatData() {
        return validFormatData;
    }


}
