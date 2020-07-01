package ru.ppr.core.dataCarrier.findcardtask;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.SamAuthorizationStrategyFactory;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.StaticKeyAuthorizationStrategyFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.logic.interactor.UltralighrCardTypeChecker;
import ru.ppr.rfid.IRfid;

/**
 * Фабрика {@link FindCardTask}.
 *
 * @author Aleksandr Brazhkin
 */
public class FindCardTaskFactoryImpl implements FindCardTaskFactory {
    /**
     * Считываель RFID
     */
    private final IRfid rfid;
    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием конкретных ключей.
     * В большинстве случаев этот флаг должет быть null,
     * поскольку сейчас все classic карты читаются по схемам доступа из НСИ.
     * Однако для дебажной сборки авторизация по статичным ключам без SAM еще используется.
     */
    private final StaticKeyAuthorizationStrategyFactory staticKeyAuthorizationStrategyFactory;
    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием SAM-модуля.
     */
    private final SamAuthorizationStrategyFactory samAuthorizationStrategyFactory;

    private final PdDecoderFactory pdDecoderFactory;
    private final PdEncoderFactory pdEncoderFactory;
    private final PassageMarkDecoderFactory passageMarkDecoderFactory;
    private final PassageMarkEncoderFactory passageMarkEncoderFactory;
    private final PdVersionDetector pdVersionDetector;
    private final ServiceDataDecoderFactory serviceDataDecoderFactory;
    private final CoverageAreaListDecoderFactory coverageAreaListDecoderFactory;
    private final UltralighrCardTypeChecker ultralighrCardTypeChecker;

    @Inject
    FindCardTaskFactoryImpl(IRfid rfid,
                            StaticKeyAuthorizationStrategyFactory staticKeyAuthorizationStrategyFactory,
                            SamAuthorizationStrategyFactory samAuthorizationStrategyFactory,
                            PdDecoderFactory pdDecoderFactory,
                            PdEncoderFactory pdEncoderFactory,
                            PassageMarkDecoderFactory passageMarkDecoderFactory,
                            PassageMarkEncoderFactory passageMarkEncoderFactory,
                            PdVersionDetector pdVersionDetector,
                            ServiceDataDecoderFactory serviceDataDecoderFactory,
                            CoverageAreaListDecoderFactory coverageAreaListDecoderFactory,
                            UltralighrCardTypeChecker ultralighrCardTypeChecker) {
        this.rfid = rfid;
        this.staticKeyAuthorizationStrategyFactory = staticKeyAuthorizationStrategyFactory;
        this.samAuthorizationStrategyFactory = samAuthorizationStrategyFactory;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdEncoderFactory = pdEncoderFactory;
        this.passageMarkDecoderFactory = passageMarkDecoderFactory;
        this.passageMarkEncoderFactory = passageMarkEncoderFactory;
        this.pdVersionDetector = pdVersionDetector;
        this.serviceDataDecoderFactory = serviceDataDecoderFactory;
        this.coverageAreaListDecoderFactory = coverageAreaListDecoderFactory;
        this.ultralighrCardTypeChecker = ultralighrCardTypeChecker;
    }

    @Override
    public FindCardTask create() {
        return new FindCardTask(
                rfid,
                staticKeyAuthorizationStrategyFactory.create(),
                samAuthorizationStrategyFactory.create(),
                pdDecoderFactory,
                pdEncoderFactory,
                passageMarkDecoderFactory,
                passageMarkEncoderFactory,
                pdVersionDetector,
                serviceDataDecoderFactory,
                coverageAreaListDecoderFactory,
                ultralighrCardTypeChecker);
    }
}
