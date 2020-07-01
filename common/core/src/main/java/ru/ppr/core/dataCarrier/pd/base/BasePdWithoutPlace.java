package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базовый класс для ПД без места.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdWithoutPlace extends BaseRealPd implements PdWithoutPlace {

    /**
     * Дата начала действия ПД: количество дней с дня продажи. От 0 (в день продажи) до 31.
     */
    private int startDayOffset;
    /**
     * Код тарифа для ПД с местом, с использованием которого сформирован ПД, в соответствии с НСИ
     */
    private long tariffCode;

    public BasePdWithoutPlace(PdVersion version, int size) {
        super(version, size);
    }

    @Override
    public int getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(int startDayOffset) {
        this.startDayOffset = startDayOffset;
    }

    @Override
    public long getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(long tariffCode) {
        this.tariffCode = tariffCode;
    }
}
