package ru.ppr.core.dataCarrier.pd.base;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * NullObject для {@link Pd}.
 *
 * @author Aleksandr Brazhkin
 */
public class NullPd implements Pd {

    private final PdVersion version = null;
    private final int size;
    private final Date saleDateTime = new Date(0);
    private final long edsKeyNumber = -1L;

    public NullPd(int size) {
        this.size = size;
    }

    @Override
    public PdVersion getVersion() {
        return version;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Date getSaleDateTime() {
        return null;
    }

    @Override
    public long getEdsKeyNumber() {
        return 0;
    }
}
