package ru.ppr.cppk.entity.event.base34;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Класс-обертка над ивентами продажи/возврата
 *
 * Created by Артем on 13.01.2016.
 */
public class SaleReturnWrapper {

    private final List<Object> saleReturnEvent;

    public static SaleReturnWrapper create(@NonNull List<CPPKTicketSales> sales,
                                           @NonNull List<CPPKTicketReturn> returns) {

        ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
        return new SaleReturnWrapper(listBuilder.addAll(sales).addAll(returns).build());
    }

    private SaleReturnWrapper(List<Object> saleReturnEvent) {
        this.saleReturnEvent = saleReturnEvent;
    }

    public List<Object> getSaleReturnEvent() {
        return saleReturnEvent;
    }
}
