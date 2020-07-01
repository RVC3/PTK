package ru.ppr.cppk.ui.activity.serviceticketcontrol.ticketinfo;


import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.localdb.model.ServiceZoneType;

/**
 * @author Aleksandr Brazhkin
 */
interface TicketInfoView extends MvpView {

    void setState(State state);

    void setServiceZones(List<ServiceZoneInfo> serviceZones);

    void setValidityTime(Date startDate, Date endDate);

    void setValidFromError(boolean error);

    void setValidToError(boolean error);

    void setCheckDocumentsLabelVisible(boolean visible);

    void setValid(boolean valid);

    void setTravelAllowed(boolean allowed);

    void setOutOfAreaBtnVisible(boolean visible);

    void setNoDocumentsBtnVisible(boolean visible);

    void setSaleNewPdBtnVisible(boolean visible);

    void setValidityErrorDesc(ValidityErrorDesc validityErrorDesc);

    void setDataErrorDesc(DataErrorDesc dataErrorDesc);

    void showSaleNewPdConfirmDialog();

    enum ValidityErrorDesc {
        INVALID_EDS_KEY,
        REVOKED_EDS_KEY
    }

    enum DataErrorDesc {
        INVALID_DEVICE_ID,
        NO_DATA
    }

    enum State {
        VALIDATING,
        DATA,
        ERROR
    }

    class ServiceZoneInfo {
        private final ServiceZoneType type;
        private final long code;
        private final String name;

        ServiceZoneInfo(ServiceZoneType type, long code, String name) {
            this.type = type;
            this.code = code;
            this.name = name;
        }

        public ServiceZoneType getType() {
            return type;
        }

        public long getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}
