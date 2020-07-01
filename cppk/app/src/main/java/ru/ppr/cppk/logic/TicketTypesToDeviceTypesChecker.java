package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.DeviceType;
import ru.ppr.nsi.entity.TicketType;

/**
 * Класс, выполняющий проверку возможности продажи ПД определенного типа на ПТК
 *
 * @author Grigoriy Kashka
 */
public class TicketTypesToDeviceTypesChecker {

    private final NsiDaoSession nsiDaoSession;
    private final int nsiVersion;

    public TicketTypesToDeviceTypesChecker(NsiDaoSession nsiDaoSession, int nsiVersion) {
        this.nsiDaoSession = nsiDaoSession;
        this.nsiVersion = nsiVersion;
    }

    /**
     * Выполняет проверку на возможность продажи ПД на ПТК
     *
     * @param ticketType - тип билета
     * @return разрешение на продажу
     */
    public boolean checkForPtk(@NonNull TicketType ticketType) {
        return check(ticketType.getCode(), DeviceType.Ptk.getCode());
    }

    /**
     * Выполняет проверку на возможность записи ПД на конкретный носитель
     *
     * @param ticketTypeCode - тип билета
     * @param deviceTypeCode - тип устройства
     * @return - разрешение на продажу
     */
    public boolean check(long ticketTypeCode, long deviceTypeCode) {
        return nsiDaoSession.getTicketTypeToDeviceTypeDao().canTicketTypeBeSoldOnDeviceType(ticketTypeCode, deviceTypeCode, nsiVersion);
    }
}
