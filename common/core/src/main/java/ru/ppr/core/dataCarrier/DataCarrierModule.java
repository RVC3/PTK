package ru.ppr.core.dataCarrier;

import dagger.Binds;
import dagger.Module;
import ru.ppr.core.dataCarrier.coupon.CouponDecoder;
import ru.ppr.core.dataCarrier.coupon.CouponDecoderImpl;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactoryImpl;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.SamAuthorizationStrategyFactory;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.SamAuthorizationStrategyFactoryImpl;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.StaticKeyAuthorizationStrategyFactory;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.StaticKeyAuthorizationStrategyFactoryImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.CouponBarcodeDetectorImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.PdBarcodeDetectorImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeDetector;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeDetector;
import ru.ppr.core.dataCarrier.pd.DefaultPdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.DefaultPdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactoryImpl;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.DefaultCoverageAreaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.DefaultCoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.DefaultPassageMarkDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.DefaultPassageMarkEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.DefaultServiceDataDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.DefaultServiceDataEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataEncoderFactory;

/**
 * @author Aleksandr Brazhkin
 */
@Module
public abstract class DataCarrierModule {
    @Binds
    abstract FindCardTaskFactory findCardTaskFactory(FindCardTaskFactoryImpl findCardTaskFactory);

    @Binds
    abstract ReadBarcodeTaskFactory readBarcodeTaskFactory(ReadBarcodeTaskFactoryImpl readBarcodeTaskFactory);

    @Binds
    abstract StaticKeyAuthorizationStrategyFactory staticKeyAuthorizationStrategyFactory(StaticKeyAuthorizationStrategyFactoryImpl staticKeyAuthorizationStrategyFactory);

    @Binds
    abstract SamAuthorizationStrategyFactory samAuthorizationStrategyFactory(SamAuthorizationStrategyFactoryImpl samAuthorizationStrategyFactory);

    @Binds
    abstract PdDecoderFactory pdDecoderFactory(DefaultPdDecoderFactory pdDecoderFactory);

    @Binds
    abstract PdEncoderFactory pdEncoderFactory(DefaultPdEncoderFactory pdEncoderFactory);

    @Binds
    abstract PassageMarkDecoderFactory passageMarkDecoderFactory(DefaultPassageMarkDecoderFactory passageMarkDecoderFactory);

    @Binds
    abstract PassageMarkEncoderFactory passageMarkEncoderFactory(DefaultPassageMarkEncoderFactory passageMarkEncoderFactory);

    @Binds
    abstract ServiceDataDecoderFactory serviceDataDecoderFactory(DefaultServiceDataDecoderFactory serviceDataDecoderFactory);

    @Binds
    abstract ServiceDataEncoderFactory serviceDataEncoderFactory(DefaultServiceDataEncoderFactory serviceDataEncoderFactory);

    @Binds
    abstract CoverageAreaListDecoderFactory coverageAreaListDecoderFactory(DefaultCoverageAreaListDecoderFactory coverageAreaListDecoderFactory);

    @Binds
    abstract CoverageAreaDecoderFactory coverageAreaDecoderFactory(DefaultCoverageAreaDecoderFactory coverageAreaDecoderFactory);

    @Binds
    abstract CouponBarcodeDetector couponBarcodeDetector(CouponBarcodeDetectorImpl couponBarcodeDetector);

    @Binds
    abstract PdBarcodeDetector pdBarcodeDetector(PdBarcodeDetectorImpl pdBarcodeDetector);

    @Binds
    abstract CouponDecoder couponDecoder(CouponDecoderImpl couponDecoder);
}
