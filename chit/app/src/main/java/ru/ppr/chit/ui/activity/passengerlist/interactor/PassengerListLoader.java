package ru.ppr.chit.ui.activity.passengerlist.interactor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.PassengerWithTicketId;
import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.logic.FioFormatter;

/**
 * @author Aleksandr Brazhkin
 */
public class PassengerListLoader {

    private final PassengerPersonalDataRepository passengerPersonalDataRepository;
    private final CredentialDocumentTypeRepository credentialDocumentTypeRepository;
    private final FioFormatter fioFormatter;
    private final NsiVersionProvider nsiVersionProvider;
    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    PassengerListLoader(PassengerPersonalDataRepository passengerPersonalDataRepository,
                        CredentialDocumentTypeRepository credentialDocumentTypeRepository,
                        FioFormatter fioFormatter,
                        NsiVersionProvider nsiVersionProvider,
                        TripServiceInfoStorage tripServiceInfoStorage) {
        this.passengerPersonalDataRepository = passengerPersonalDataRepository;
        this.credentialDocumentTypeRepository = credentialDocumentTypeRepository;
        this.fioFormatter = fioFormatter;
        this.nsiVersionProvider = nsiVersionProvider;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
    }

    public List<PassengerInfo> load(String filter, int recordsOffset, int pageLimit) {
        long currentStationCode = tripServiceInfoStorage.getControlStation() != null ? tripServiceInfoStorage.getControlStation().getCode() : 0;

        int versionId = nsiVersionProvider.getCurrentNsiVersion();
        List<PassengerWithTicketId> passengersWithTicketId = passengerPersonalDataRepository.loadPassengersWithTicketId(filter, null, recordsOffset, pageLimit, currentStationCode);
        // Если по фамилии ничего не найдено, то возможо это номер документа, попробуем с ним
        if (passengersWithTicketId.isEmpty()) {
            passengersWithTicketId = passengerPersonalDataRepository.loadPassengersWithTicketId(null, filter, recordsOffset, pageLimit, currentStationCode);
        }
        // Затем в любом случае формируем список
        List<PassengerInfo> passengerInfoList = new ArrayList<>();
        for (PassengerWithTicketId passengerWithTicketId : passengersWithTicketId) {
            PassengerInfo passengerInfo = new PassengerInfo();
            passengerInfo.setFio(buildFio(passengerWithTicketId));
            passengerInfo.setDocumentNumber(passengerWithTicketId.getDocumentNumber());
            passengerInfo.setDocumentType(loadDocumentType(passengerWithTicketId, versionId));
            passengerInfo.setTicketId(passengerWithTicketId.getTicketId());
            passengerInfo.setDepartureStationCode(passengerWithTicketId.getDepartureStationCode());
            passengerInfo.setWasBoarded(passengerWithTicketId.getWasBoarded());
            passengerInfo.setIsCurrentStationBoarding(passengerWithTicketId.getIsCurrentStationBoarding());
            passengerInfoList.add(passengerInfo);
        }

        return passengerInfoList;
    }

    private String buildFio(PassengerWithTicketId passengerWithTicketId) {
        return fioFormatter.getFullNameAsSurnameWithInitials(
                passengerWithTicketId.getLastName(),
                passengerWithTicketId.getFirstName(),
                passengerWithTicketId.getMiddleName()
        );
    }

    private String loadDocumentType(PassengerWithTicketId passengerWithTicketId, int versionId) {
        CredentialDocumentType credentialDocumentType = credentialDocumentTypeRepository.load(passengerWithTicketId.getDocumentTypeCode(), versionId);
        return credentialDocumentType == null ? "" : credentialDocumentType.getName();
    }
}
