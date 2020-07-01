package ru.ppr.chit.data;

import dagger.Binds;
import dagger.Module;
import ru.ppr.chit.data.repository.local.AuthInfoRepositoryImpl;
import ru.ppr.chit.data.repository.local.BoardingEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.BoardingExportEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.CarInfoRepositoryImpl;
import ru.ppr.chit.data.repository.local.CarSchemeElementRepositoryImpl;
import ru.ppr.chit.data.repository.local.CarSchemeRepositoryImpl;
import ru.ppr.chit.data.repository.local.ControlStationRepositoryImpl;
import ru.ppr.chit.data.repository.local.EventRepositoryImpl;
import ru.ppr.chit.data.repository.local.ExchangeEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.LocationRepositoryImpl;
import ru.ppr.chit.data.repository.local.OAuth2TokenRepositoryImpl;
import ru.ppr.chit.data.repository.local.PassengerPersonalDataRepositoryImpl;
import ru.ppr.chit.data.repository.local.PassengerRepositoryImpl;
import ru.ppr.chit.data.repository.local.PlaceLocationRepositoryImpl;
import ru.ppr.chit.data.repository.local.SmartCardRepositoryImpl;
import ru.ppr.chit.data.repository.local.StationInfoRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketBoardingRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketControlEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketControlExportEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketDataRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketIdRepositoryImpl;
import ru.ppr.chit.data.repository.local.TicketRepositoryImpl;
import ru.ppr.chit.data.repository.local.TrainInfoRepositoryImpl;
import ru.ppr.chit.data.repository.local.TripServiceEventRepositoryImpl;
import ru.ppr.chit.data.repository.local.UserRepositoryImpl;
import ru.ppr.chit.data.repository.local.base.LocalDbTransactionImpl;
import ru.ppr.chit.data.repository.nsi.AccessRuleRepositoryImpl;
import ru.ppr.chit.data.repository.nsi.AccessSchemeRepositoryImpl;
import ru.ppr.chit.data.repository.nsi.CredentialDocumentTypeRepositoryImpl;
import ru.ppr.chit.data.repository.nsi.StationRepositoryImpl;
import ru.ppr.chit.data.repository.nsi.TicketTypeRepositoryImpl;
import ru.ppr.chit.data.repository.nsi.VersionRepositoryImpl;
import ru.ppr.chit.data.repository.security.PtkDataContractsVersionRepositoryImpl;
import ru.ppr.chit.data.repository.security.SecurityStopListVersionRepositoryImpl;
import ru.ppr.chit.data.repository.security.TicketWhiteListItemRepositoryImpl;
import ru.ppr.chit.domain.repository.local.AuthInfoRepository;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.BoardingExportEventRepository;
import ru.ppr.chit.domain.repository.local.CarInfoRepository;
import ru.ppr.chit.domain.repository.local.CarSchemeElementRepository;
import ru.ppr.chit.domain.repository.local.CarSchemeRepository;
import ru.ppr.chit.domain.repository.local.ControlStationRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;
import ru.ppr.chit.domain.repository.local.LocationRepository;
import ru.ppr.chit.domain.repository.local.OAuth2TokenRepository;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.local.PassengerRepository;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.domain.repository.local.SmartCardRepository;
import ru.ppr.chit.domain.repository.local.StationInfoRepository;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.domain.repository.local.TicketControlExportEventRepository;
import ru.ppr.chit.domain.repository.local.TicketDataRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.repository.nsi.AccessRuleRepository;
import ru.ppr.chit.domain.repository.nsi.AccessSchemeRepository;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.domain.repository.nsi.TicketTypeRepository;
import ru.ppr.chit.domain.repository.nsi.VersionRepository;
import ru.ppr.chit.domain.repository.security.PtkDataContractsVersionRepository;
import ru.ppr.chit.domain.repository.security.SecurityStopListVersionRepository;
import ru.ppr.chit.domain.repository.security.TicketWhiteListItemRepository;

/**
 * @author Aleksandr Brazhkin
 */
@Module
public abstract class DataModule {

    @Binds
    public abstract LocalDbTransaction localDbTransaction(LocalDbTransactionImpl localDbTransaction);

    @Binds
    public abstract AuthInfoRepository authInfoRepository(AuthInfoRepositoryImpl authInfoRepository);

    @Binds
    public abstract BoardingEventRepository boardingEventRepository(BoardingEventRepositoryImpl boardingEventRepository);

    @Binds
    public abstract BoardingExportEventRepository boardingExportEventRepository(BoardingExportEventRepositoryImpl boardingExportEventRepository);

    @Binds
    public abstract CarInfoRepository carInfoRepository(CarInfoRepositoryImpl carInfoRepository);

    @Binds
    public abstract CarSchemeElementRepository carSchemeElementRepository(CarSchemeElementRepositoryImpl carSchemeElementRepository);

    @Binds
    public abstract CarSchemeRepository carSchemeRepository(CarSchemeRepositoryImpl carSchemeRepository);

    @Binds
    public abstract ControlStationRepository controlStationRepository(ControlStationRepositoryImpl controlStationRepository);

    @Binds
    public abstract EventRepository eventRepository(EventRepositoryImpl eventRepository);

    @Binds
    public abstract ExchangeEventRepository exchangeEventRepository(ExchangeEventRepositoryImpl exchangeEventRepository);

    @Binds
    public abstract OAuth2TokenRepository oAuth2TokenRepository(OAuth2TokenRepositoryImpl oAuth2TokenRepository);

    @Binds
    public abstract PassengerPersonalDataRepository passengerPersonalDataRepository(PassengerPersonalDataRepositoryImpl passengerPersonalDataRepository);

    @Binds
    public abstract PlaceLocationRepository placeLocationRepository(PlaceLocationRepositoryImpl placeLocationRepository);

    @Binds
    public abstract StationInfoRepository stationInfoRepository(StationInfoRepositoryImpl stationInfoRepository);

    @Binds
    public abstract TicketIdRepository ticketIdRepository(TicketIdRepositoryImpl ticketIdRepository);

    @Binds
    public abstract TicketRepository ticketRepository(TicketRepositoryImpl ticketRepository);

    @Binds
    public abstract TrainInfoRepository trainInfoRepository(TrainInfoRepositoryImpl trainInfoRepository);

    @Binds
    public abstract TripServiceEventRepository tripServiceRepository(TripServiceEventRepositoryImpl tripServiceRepository);

    @Binds
    public abstract UserRepository userRepository(UserRepositoryImpl userRepository);

    @Binds
    public abstract StationRepository stationRepository(StationRepositoryImpl stationRepository);

    @Binds
    public abstract VersionRepository versionRepository(VersionRepositoryImpl versionRepository);

    @Binds
    public abstract TicketWhiteListItemRepository ticketWhiteListItemRepository(TicketWhiteListItemRepositoryImpl ticketWhiteListItemRepository);

    @Binds
    public abstract PtkDataContractsVersionRepository ptkDataContractsVersionRepository(PtkDataContractsVersionRepositoryImpl ptkDataContractsVersionRepository);

    @Binds
    public abstract SecurityStopListVersionRepository SecurityStopListVersionRepository(SecurityStopListVersionRepositoryImpl securityStopListVersionRepository);

    @Binds
    public abstract TicketTypeRepository ticketTypeRepository(TicketTypeRepositoryImpl ticketTypeRepository);

    @Binds
    public abstract AccessRuleRepository accessRuleRepository(AccessRuleRepositoryImpl accessRuleRepository);

    @Binds
    public abstract AccessSchemeRepository accessSchemeRepository(AccessSchemeRepositoryImpl accessSchemeRepository);

    @Binds
    public abstract CredentialDocumentTypeRepository credentialDocumentTypeRepository(CredentialDocumentTypeRepositoryImpl credentialDocumentTypeRepository);

    @Binds
    public abstract TicketControlEventRepository ticketControlEventRepository(TicketControlEventRepositoryImpl ticketControlEventRepository);

    @Binds
    public abstract TicketControlExportEventRepository ticketControlExportEventRepository(TicketControlExportEventRepositoryImpl ticketControlExportEventRepository);

    @Binds
    public abstract TicketBoardingRepository ticketBoardingRepository(TicketBoardingRepositoryImpl ticketBoardingRepository);

    @Binds
    public abstract TicketDataRepository ticketDataRepository(TicketDataRepositoryImpl ticketDataRepository);

    @Binds
    public abstract PassengerRepository passengerRepository(PassengerRepositoryImpl passengerRepository);

    @Binds
    public abstract LocationRepository locationRepository(LocationRepositoryImpl locationRepository);

    @Binds
    public abstract SmartCardRepository smartCardRepository(SmartCardRepositoryImpl smartCardRepository);

}
