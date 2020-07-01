package ru.ppr.cppk.helpers.controlbscstorage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.logic.servicedatacontrol.ValidityChecker;

/**
 * Данные, считанные с карты при контроле БСК с СТУ.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceTicketControlCardData {

    private CardInformation cardInformation;
    private ServiceData serviceData;
    private byte[] rawServiceData;
    private List<CoverageArea> coverageAreaList;
    private byte[] rawCoverageAreaList;
    private byte[] eds;
    private PassageMark passageMark;
    private ServiceTicketControlEvent serviceTicketControlEvent;
    private ValidityChecker.Result checkResult;

    public ServiceTicketControlCardData() {
    }

    @NonNull
    public CardInformation getCardInformation() {
        return cardInformation;
    }

    public void setCardInformation(CardInformation cardInformation) {
        this.cardInformation = cardInformation;
    }

    @NonNull
    public ServiceData getServiceData() {
        return serviceData;
    }

    public void setServiceData(ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    @NonNull
    public byte[] getRawServiceData() {
        return rawServiceData;
    }

    public void setRawServiceData(byte[] rawServiceData) {
        this.rawServiceData = rawServiceData;
    }

    @NonNull
    public List<CoverageArea> getCoverageAreaList() {
        return coverageAreaList;
    }

    public void setCoverageAreaList(List<CoverageArea> coverageAreaList) {
        this.coverageAreaList = coverageAreaList;
    }

    @NonNull
    public byte[] getRawCoverageAreaList() {
        return rawCoverageAreaList;
    }

    public void setRawCoverageAreaList(byte[] rawCoverageAreaList) {
        this.rawCoverageAreaList = rawCoverageAreaList;
    }

    @Nullable
    public byte[] getEds() {
        return eds;
    }

    public void setEds(byte[] eds) {
        this.eds = eds;
    }

    @Nullable
    public PassageMark getPassageMark() {
        return passageMark;
    }

    public void setPassageMark(PassageMark passageMark) {
        this.passageMark = passageMark;
    }

    @NonNull
    public ServiceTicketControlEvent getServiceTicketControlEvent() {
        return serviceTicketControlEvent;
    }

    public void setServiceTicketControlEvent(ServiceTicketControlEvent serviceTicketControlEvent) {
        this.serviceTicketControlEvent = serviceTicketControlEvent;
    }

    @NonNull
    public ValidityChecker.Result getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(ValidityChecker.Result checkResult) {
        this.checkResult = checkResult;
    }
}
