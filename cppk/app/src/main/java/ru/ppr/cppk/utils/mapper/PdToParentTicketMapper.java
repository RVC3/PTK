package ru.ppr.cppk.utils.mapper;

import android.support.annotation.NonNull;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;

/**
 * Created by Артем on 26.02.2016.
 */
final class PdToParentTicketMapper implements Mapper<PD, ParentTicketInfo> {

    @NonNull
    @Override
    public ParentTicketInfo mapTo(@NonNull PD pd) {
        ParentTicketInfo parentTicketInfo = new ParentTicketInfo();
        parentTicketInfo.setSaleDateTime(pd.getSaleDate());
        parentTicketInfo.setTicketNumber(pd.numberPD);
        parentTicketInfo.setWayType(pd.wayType);
        parentTicketInfo.setCashRegisterNumber(pd.deviceId);
        return parentTicketInfo;
    }
}
