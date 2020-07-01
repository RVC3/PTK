package ru.ppr.cppk.utils.mapper;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;

/**
 * Created by Артем on 26.02.2016.
 */
public final class MapperFactory {
    private MapperFactory(){}

    public static Mapper<PD, ParentTicketInfo> createPdToParentTicketMapper(){
        return new PdToParentTicketMapper();
    }
}
