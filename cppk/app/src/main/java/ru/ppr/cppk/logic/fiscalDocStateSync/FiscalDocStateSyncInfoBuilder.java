package ru.ppr.cppk.logic.fiscalDocStateSync;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.logic.fiscalDocStateSync.base.FiscalDocInfoBuilder;
import ru.ppr.cppk.logic.fiscalDocStateSync.builder.FineSaleEventInfoBuilder;
import ru.ppr.cppk.logic.fiscalDocStateSync.builder.TicketSaleEventInfoBuilder;

/**
 * @author Dmitry Nevolin
 */
public class FiscalDocStateSyncInfoBuilder {

    private final LocalDaoSession localDaoSession;
    private final String shiftId;

    public FiscalDocStateSyncInfoBuilder(LocalDaoSession localDaoSession, String shiftId) {
        this.localDaoSession = localDaoSession;
        this.shiftId = shiftId;
    }

    public Info build() {
        FiscalDocInfoBuilder<CPPKTicketSales> ticketSaleEventInfoBuilder = new TicketSaleEventInfoBuilder(localDaoSession, shiftId);
        FiscalDocInfoBuilder<FineSaleEvent> fineSaleEventInfoBuilder = new FineSaleEventInfoBuilder(localDaoSession, shiftId);

        FiscalDocInfoBuilder.Info ticketSaleEventInfo = ticketSaleEventInfoBuilder.build();
        FiscalDocInfoBuilder.Info fineSaleEventInfo = fineSaleEventInfoBuilder.build();
        Check check = localDaoSession.getCheckDao().getLastCheck();

        int number = check == null ? 1 : check.getSnpdNumber();
        BigDecimal totalValue = ticketSaleEventInfo.getTotal().add(fineSaleEventInfo.getTotal());

        return new Info(number, totalValue);
    }

    public static class Info {

        private int number;
        private BigDecimal total;

        private Info(int number, BigDecimal total) {
            this.number = number;
            this.total = total;
        }

        public int getNumber() {
            return number;
        }

        public BigDecimal getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "number=" + number +
                    ", total=" + total +
                    '}';
        }
    }

}
