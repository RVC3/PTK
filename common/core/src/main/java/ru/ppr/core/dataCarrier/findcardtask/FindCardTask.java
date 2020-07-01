package ru.ppr.core.dataCarrier.findcardtask;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

import ru.ppr.core.BuildConfig;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.statickey.DefaultStaticKeyAuthorizationStrategy;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsPayloadReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsPayloadReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.CoverageAreaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.CoverageAreaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.HardwareCounterReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceDataReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm1kReadWritePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm1kReadWritePdReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm4kReadWritePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.Skm4kReadWritePdReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.SkmoReadWritePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.SkmoReadWritePdReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.UltralightCHardwareCounterReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.UltralightEV1HardwareCounterReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareUltralightReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.BigMoscowReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.BigMoscowReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsUltralightCReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsUltralightEv1OnePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsUltralightEv1TwoPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReaderBMImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.EttReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.EttReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.IpkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.IpkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.Skm1kReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.Skm4kReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmNoPdPlaceReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmNoPdPlaceReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmoNoPdPlaceReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmoNoPdPlaceReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmoReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.SkmoReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaReaderBMImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaTroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaTroykaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.TroykaWithBMPdReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.TroykaWithPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.TroykaWithPdReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.TroykaWithServiceDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.TroykaWithServiceDataReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.checker.SkmSkmoIpkRecognizer;
import ru.ppr.core.dataCarrier.smartCard.checker.TroykaRecognizer;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.CppkNumberOfTripsOuterNumberParser;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.DefaultOuterNumberParser;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.OuterNumberParser;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.StrelkaOuterNumberParser;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoderFactory;
import ru.ppr.core.logic.interactor.UltralighrCardTypeChecker;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.MifareCardType;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Команда поиска смарт-карты.
 *
 * @author Aleksandr Brazhkin
 */
public class FindCardTask {

    private static final String TAG = Logger.makeLogTag(FindCardTask.class);

    /**
     * Флаг, что команда отменена извне.
     */
    private volatile boolean canceled;
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
    private final StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy;
    /**
     * Алгоритм авторизации в секторах карт Mifare Classic c использованием SAM-модуля.
     */
    private final SamAuthorizationStrategy samAuthorizationStrategy;

    private final PdDecoderFactory pdDecoderFactory;
    private final PdEncoderFactory pdEncoderFactory;
    private final PassageMarkDecoderFactory passageMarkDecoderFactory;
    private final PassageMarkEncoderFactory passageMarkEncoderFactory;
    private final PdVersionDetector pdVersionDetector;
    private final ServiceDataDecoderFactory serviceDataDecoderFactory;
    private final CoverageAreaListDecoderFactory coverageAreaListDecoderFactory;
    private final UltralighrCardTypeChecker ultralighrCardTypeChecker;

    public FindCardTask(IRfid rfid,
                        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                        SamAuthorizationStrategy samAuthorizationStrategy,
                        PdDecoderFactory pdDecoderFactory,
                        PdEncoderFactory pdEncoderFactory,
                        PassageMarkDecoderFactory passageMarkDecoderFactory,
                        PassageMarkEncoderFactory passageMarkEncoderFactory,
                        PdVersionDetector pdVersionDetector,
                        ServiceDataDecoderFactory serviceDataDecoderFactory,
                        CoverageAreaListDecoderFactory coverageAreaListDecoderFactory,
                        UltralighrCardTypeChecker ultralighrCardTypeChecker) {
        this.rfid = rfid;
        this.staticKeyAuthorizationStrategy = staticKeyAuthorizationStrategy;
        this.samAuthorizationStrategy = samAuthorizationStrategy;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdEncoderFactory = pdEncoderFactory;
        this.passageMarkDecoderFactory = passageMarkDecoderFactory;
        this.passageMarkEncoderFactory = passageMarkEncoderFactory;
        this.pdVersionDetector = pdVersionDetector;
        this.serviceDataDecoderFactory = serviceDataDecoderFactory;
        this.coverageAreaListDecoderFactory = coverageAreaListDecoderFactory;
        this.ultralighrCardTypeChecker = ultralighrCardTypeChecker;
    }

    /**
     * Выполняет поиск карты.
     *
     * @return Cоответствующий найденной карте ридер.
     */
    @Nullable
    public CardReader  find() {


        Logger.trace(TAG, "find started");

        while (true) {
            final boolean canceled1 = isCanceled();
            if (canceled1) break;
            CardData cardData = rfid.getRfidAtr();
            if (cardData == null) {
                // Не удалось найти карту
                continue;
            }
            final boolean canceled = isCanceled();
            if (canceled) {
                continue;
            }
            CardInfo cardInfo = CardInfoMapper.map(cardData);

            if (cardInfo.getMifareCardType().isClassic()) {
                MifareClassicReader mifareClassicReader = new MifareClassicReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy);
                ReadPdMifareClassicReader readPdMifareClassicReader = new ReadPdMifareClassicReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, pdDecoderFactory);
                WritePdMifareClassicReader writePdMifareClassicReader = new WritePdMifareClassicReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, pdEncoderFactory);
                ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader = new ReadPassageMarkMifareClassicReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, passageMarkDecoderFactory);
                WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader = new WritePassageMarkMifareClassicReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, passageMarkEncoderFactory);
                ReadCardResult<Pair<BscType, byte[]>> bscTypeResult = mifareClassicReader.readBscTypeWithRawData();
                if (bscTypeResult.isSuccess()) {
                    final boolean canceled2 = isCanceled();
                    if (canceled2) {
                        continue;
                    }
                    BscType bscType = bscTypeResult.getData().first;
                    byte[] rawDataWithBscType = bscTypeResult.getData().second;
                    Logger.trace(TAG, "Тип карты:" +bscType );
                    switch (bscType) {
                        case SKM_SKMO_IPK: {
                            BscInformationReader bscInformationReader = new BscInformationReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    rawDataWithBscType
                            );
                            EmissionDataReader emissionDataReader = new EmissionDataReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader
                            );
                            PersonalDataReader personalDataReader = new PersonalDataReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader
                            );
                            SkmSkmoIpkReader skmSkmoIpkReader = new SkmSkmoIpkReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    bscInformationReader,
                                    emissionDataReader,
                                    personalDataReader
                            );
                            ReadCardResult<EmissionData> emissionDataResult = skmSkmoIpkReader.readEmissionData();

                            if (emissionDataResult.isSuccess()) {
                                EmissionData emissionData = emissionDataResult.getData();
                                SkmSkmoIpkRecognizer skmSkmoIpkRecognizer = new SkmSkmoIpkRecognizer();
                                if (skmSkmoIpkRecognizer.isSkm(emissionData.getCardNumber())) {
                                    if (cardInfo.getMifareCardType().isClassic1k()) {
                                        Logger.trace(TAG, "Skm1kReadWritePdReader created");
                                        Skm1kReadWritePdReader skm1kReadWritePdReader = new Skm1kReadWritePdReaderImpl(
                                                rfid,
                                                cardInfo,
                                                staticKeyAuthorizationStrategy,
                                                samAuthorizationStrategy,
                                                mifareClassicReader,
                                                readPdMifareClassicReader,
                                                writePdMifareClassicReader
                                        );

                                        //прихраним состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                        //http://agile.srvdev.ru/browse/CPPKPP-34434
                                        samAuthorizationStrategy.saveState();

                                        ReadCardResult<List<Pd>> pdList = skm1kReadWritePdReader.readPdList();
                                        if (pdList.isSuccess()) {
                                            ReadCardResult<byte[]> edsResult = skm1kReadWritePdReader.readEds();
                                            if (edsResult.isSuccess()) {
                                                Logger.trace(TAG, "Skm1kReader created");
                                                Skm1kReader skm1kReader = new Skm1kReader(
                                                        rfid,
                                                        cardInfo,
                                                        staticKeyAuthorizationStrategy,
                                                        samAuthorizationStrategy,
                                                        bscInformationReader,
                                                        emissionDataReader,
                                                        personalDataReader,
                                                        mifareClassicReader,
                                                        skm1kReadWritePdReader
                                                );
                                                return skm1kReader;
                                            }
                                        } else {
                                            //восстановим состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                            //http://agile.srvdev.ru/browse/CPPKPP-34434
                                            samAuthorizationStrategy.restoreState();
                                        }
                                    } else {
                                        Logger.trace(TAG, "Skm4kReadWritePdReader created");
                                        ((DefaultStaticKeyAuthorizationStrategy) staticKeyAuthorizationStrategy).addSKM4KKeys();
                                        Skm4kReadWritePdReader skm4kReadWritePdReader = new Skm4kReadWritePdReaderImpl(
                                                rfid,
                                                cardInfo,
                                                staticKeyAuthorizationStrategy,
                                                samAuthorizationStrategy,
                                                mifareClassicReader,
                                                readPdMifareClassicReader,
                                                writePdMifareClassicReader
                                        );

                                        //прихраним состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                        //http://agile.srvdev.ru/browse/CPPKPP-34434
                                        if (samAuthorizationStrategy != null)
                                            samAuthorizationStrategy.saveState();

                                        ReadCardResult<List<Pd>> pdList = skm4kReadWritePdReader.readPdList();
                                        if (pdList.isSuccess()) {
                                            ReadCardResult<byte[]> edsResult = skm4kReadWritePdReader.readEds();
                                            if (edsResult.isSuccess()) {
                                                Logger.trace(TAG, "Skm4kReader created");
                                                Skm4kReader skm4kReader = new Skm4kReader(
                                                        rfid,
                                                        cardInfo,
                                                        staticKeyAuthorizationStrategy,
                                                        samAuthorizationStrategy,
                                                        bscInformationReader,
                                                        emissionDataReader,
                                                        personalDataReader,
                                                        mifareClassicReader,
                                                        skm4kReadWritePdReader
                                                );
                                                return skm4kReader;
                                            }
                                        } else {
                                            //восстановим состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                            //http://agile.srvdev.ru/browse/CPPKPP-34434
                                            if (samAuthorizationStrategy != null)
                                                samAuthorizationStrategy.restoreState();
                                        }
                                    }
                                    //Это карта СКМ но мы не смогли прочитать с нее ПД
                                    Logger.trace(TAG, "SkmNoPdPlaceReader created");
                                    SkmNoPdPlaceReader skmNoPdPlaceReader = new SkmNoPdPlaceReaderImpl(
                                            rfid,
                                            cardInfo,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            mifareClassicReader,
                                            bscInformationReader,
                                            emissionDataReader,
                                            personalDataReader
                                    );
                                    return skmNoPdPlaceReader;

                                } else if (skmSkmoIpkRecognizer.isSkmo(emissionData.getCardNumber())) {
                                    Logger.trace(TAG, "SkmoReadWritePdReader created");
                                    SkmoReadWritePdReader skmoReadWritePdReader = new SkmoReadWritePdReaderImpl(
                                            rfid,
                                            cardInfo,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            mifareClassicReader,
                                            readPdMifareClassicReader,
                                            writePdMifareClassicReader
                                    );

                                    //прихраним состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                    //http://agile.srvdev.ru/browse/CPPKPP-34434
                                    if (samAuthorizationStrategy != null)
                                        samAuthorizationStrategy.saveState();

                                    ReadCardResult<List<Pd>> pdList = skmoReadWritePdReader.readPdList();
                                    if (pdList.isSuccess()) {
                                        ReadCardResult<byte[]> edsResult = skmoReadWritePdReader.readEds();
                                        if (edsResult.isSuccess()) {
                                            Logger.trace(TAG, "SkmoReader created");
                                            SkmoReader skmoReader = new SkmoReaderImpl(
                                                    rfid,
                                                    cardInfo,
                                                    staticKeyAuthorizationStrategy,
                                                    samAuthorizationStrategy,
                                                    bscInformationReader,
                                                    emissionDataReader,
                                                    personalDataReader,
                                                    mifareClassicReader,
                                                    skmoReadWritePdReader
                                            );
                                            return skmoReader;
                                        }
                                    } else {
                                        //восстановим состояние стратегии из-за возможных проблем с ключами для секторов с ПД
                                        //http://agile.srvdev.ru/browse/CPPKPP-34434
                                        if (samAuthorizationStrategy != null)
                                            samAuthorizationStrategy.restoreState();
                                    }
                                    //Это карта СКМО но мы не смогли прочитать с нее ПД
                                    Logger.trace(TAG, "SkmoNoPdPlaceReader created");
                                    SkmoNoPdPlaceReader skmoNoPdPlaceReader = new SkmoNoPdPlaceReaderImpl(
                                            rfid,
                                            cardInfo,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            mifareClassicReader,
                                            bscInformationReader,
                                            emissionDataReader,
                                            personalDataReader
                                    );
                                    return skmoNoPdPlaceReader;
                                } else if (skmSkmoIpkRecognizer.isIpk(emissionData.getCardNumber())) {
                                    Logger.trace(TAG, "IpkReader created");
                                    IpkReader ipkReader = new IpkReaderImpl(
                                            rfid,
                                            cardInfo,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            bscInformationReader,
                                            emissionDataReader,
                                            personalDataReader,
                                            mifareClassicReader,
                                            readPdMifareClassicReader,
                                            writePdMifareClassicReader,
                                            readPassageMarkMifareClassicReader,
                                            writePassageMarkMifareClassicReader
                                    );
                                    return ipkReader;
                                } else {
                                    Logger.error(TAG, "Unknown card number for skm, skmo, ipk card: " + emissionData.getCardNumber());
                                    return null;
                                }
                            } else {
                                Logger.error(TAG, "Could not read emission data for skm, skmo, ipk card: " + emissionDataResult);
                                return null;
                            }
                        }
                        case CPPK:
                        case CPPK_PLUS: {
                            ((DefaultStaticKeyAuthorizationStrategy) staticKeyAuthorizationStrategy).addCPPKKeys();
                            OuterNumberParser outerNumberParser = new DefaultOuterNumberParser();
                            OuterNumberReader outerNumberReader = new OuterNumberClassicReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberParser,
                                    rawDataWithBscType
                            );

                            BigMoscowReader bigMoscowReader = new BigMoscowReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberReader,
                                    writePdMifareClassicReader,
                                    readPassageMarkMifareClassicReader,
                                    writePassageMarkMifareClassicReader,
                                    pdDecoderFactory,
                                    pdVersionDetector);

                            CppkReader cppkReader;
                            if (bigMoscowReader.checkForPD(6, 15)) {
                                cppkReader = new CppkReaderBMImpl(
                                        rfid,
                                        cardInfo,
                                        staticKeyAuthorizationStrategy,
                                        samAuthorizationStrategy,
                                        mifareClassicReader,
                                        outerNumberReader,
                                        writePdMifareClassicReader,
                                        readPassageMarkMifareClassicReader,
                                        writePassageMarkMifareClassicReader,
                                        pdDecoderFactory,
                                        pdVersionDetector,
                                        bigMoscowReader.getPds()
                                );
                            } else {
                                cppkReader = new CppkReaderImpl(
                                        rfid,
                                        cardInfo,
                                        staticKeyAuthorizationStrategy,
                                        samAuthorizationStrategy,
                                        mifareClassicReader,
                                        outerNumberReader,
                                        writePdMifareClassicReader,
                                        readPassageMarkMifareClassicReader,
                                        writePassageMarkMifareClassicReader,
                                        pdDecoderFactory,
                                        pdVersionDetector);
                            }

                            return cppkReader;
                        }
                        case TROIKA_STRELKA:
                        case TROIKA: {
                            ((DefaultStaticKeyAuthorizationStrategy) staticKeyAuthorizationStrategy).addTroykaKeys();
                            OuterNumberParser outerNumberParser = new DefaultOuterNumberParser();
                            OuterNumberReader outerNumberReader = new OuterNumberClassicReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberParser,
                                    rawDataWithBscType);
                            BigMoscowReader bigMoscowReader = new BigMoscowReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberReader,
                                    writePdMifareClassicReader,
                                    readPassageMarkMifareClassicReader,
                                    writePassageMarkMifareClassicReader,
                                    pdDecoderFactory,
                                    pdVersionDetector);
                            TroykaReader troykaReader = new TroykaReaderImpl(rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberReader,
                                    pdDecoderFactory
                            );
                            ReadCardResult<byte[]> payloadBlockResult = troykaReader.readFirstPayloadBlock();
                            final boolean payloadBlockResultSuccess = payloadBlockResult.isSuccess();
                            if (payloadBlockResultSuccess) {
                                TroykaRecognizer troykaRecognizer = new TroykaRecognizer();
                                final boolean troykaWithServiceData = troykaRecognizer.isTroykaWithServiceData(payloadBlockResult.getData());
                                if (troykaWithServiceData) {
                                    ServiceDataReader serviceDataReader = new ServiceDataReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, serviceDataDecoderFactory);
                                    CoverageAreaReader coverageAreaReader = new CoverageAreaReaderImpl(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, coverageAreaListDecoderFactory);
                                    TroykaWithServiceDataReader troykaWithServiceDataReader = new TroykaWithServiceDataReaderImpl(
                                            rfid,
                                            cardInfo,
                                            outerNumberReader,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            mifareClassicReader,
                                            serviceDataReader,
                                            coverageAreaReader
                                    );
                                    Logger.trace(TAG, "TroykaWithServiceDataReader created");
                                    return troykaWithServiceDataReader;
                                } else {
                                    final boolean checkForPD = bigMoscowReader.checkForPD(6, 15);
                                    if (checkForPD) {
                                        TroykaWithBMPdReaderImpl reader = new TroykaWithBMPdReaderImpl(
                                                rfid,
                                                cardInfo,
                                                staticKeyAuthorizationStrategy,
                                                samAuthorizationStrategy,
                                                mifareClassicReader,
                                                outerNumberReader,
                                                readPdMifareClassicReader,
                                                writePdMifareClassicReader,
                                                pdDecoderFactory,
                                                bigMoscowReader.getPds(),
                                                readPassageMarkMifareClassicReader,
                                                writePassageMarkMifareClassicReader
                                        );
                                        Logger.trace(TAG, "Troyka Big Moscow Reader created");
                                        return reader;
                                    }
                                    TroykaWithPdReader troyka2KWithPdReader = new TroykaWithPdReaderImpl(
                                            rfid,
                                            cardInfo,
                                            staticKeyAuthorizationStrategy,
                                            samAuthorizationStrategy,
                                            mifareClassicReader,
                                            outerNumberReader,
                                            readPdMifareClassicReader,
                                            writePdMifareClassicReader,
                                            new MifareUltralightReaderImpl(rfid, cardInfo)
                                    );
                                    Logger.trace(TAG, "TroykaWithPdReader created");
                                    return troyka2KWithPdReader;
                                }
                            } else {
                                Logger.error(TAG, "Could not read first payload block for Troyka card: " + payloadBlockResult);
                                return null;
                            }
                        }
                        case STRELKA_TROIKA_VOLD:
                        case STRELKA_TROIKA:
                        case STRELKA: {
                            ((DefaultStaticKeyAuthorizationStrategy) staticKeyAuthorizationStrategy).addTroykaKeys();
                            OuterNumberParser strelkaOuterNumberParser = new StrelkaOuterNumberParser();
                            OuterNumberReader strelkaOuterNumberReader = new OuterNumberClassicReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    strelkaOuterNumberParser,
                                    rawDataWithBscType
                            );
                            Logger.trace(TAG, "StrelkaReader created");

                            BigMoscowReader bigMoscowReader = new BigMoscowReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    strelkaOuterNumberReader,
                                    writePdMifareClassicReader,
                                    readPassageMarkMifareClassicReader,
                                    writePassageMarkMifareClassicReader,
                                    pdDecoderFactory,
                                    pdVersionDetector);

                            if (bigMoscowReader.checkForPD(5, 6)) {

                                StrelkaReader strelkaReader = new StrelkaReaderBMImpl(
                                        rfid,
                                        cardInfo,
                                        staticKeyAuthorizationStrategy,
                                        samAuthorizationStrategy,
                                        mifareClassicReader,
                                        strelkaOuterNumberReader,
                                        readPdMifareClassicReader,
                                        writePdMifareClassicReader,
                                        pdDecoderFactory,
                                        bigMoscowReader.getPds()
                                );
                                return strelkaReader;
                            }

                            StrelkaTroykaReader strelkaReader = new StrelkaTroykaReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    strelkaOuterNumberReader,
                                    readPdMifareClassicReader,
                                    writePdMifareClassicReader
                            );
                            return strelkaReader;
                        }
                        case ETT: {
                            BscInformationReader bscInformationReader = new BscInformationReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    rawDataWithBscType
                            );
                            Logger.trace(TAG, "EttReader created");
                            EttReader ettReader = new EttReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    bscInformationReader,
                                    writePdMifareClassicReader,
                                    pdDecoderFactory,
                                    pdVersionDetector
                            );
                            return ettReader;
                        }
                        case SERVICE:
                        case SERVICE_26: {
                            OuterNumberParser outerNumberParser = new DefaultOuterNumberParser();
                            OuterNumberReader outerNumberReader = new OuterNumberClassicReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberParser,
                                    rawDataWithBscType
                            );
                            Logger.trace(TAG, "AuthCardReader created");
                            ServiceDataReader serviceDataReader = new ServiceDataReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    serviceDataDecoderFactory
                            );
                            CoverageAreaReader coverageAreaReader = new CoverageAreaReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    coverageAreaListDecoderFactory
                            );
                            AuthCardReader authCardReader = new AuthCardReaderImpl(
                                    rfid,
                                    cardInfo,
                                    staticKeyAuthorizationStrategy,
                                    samAuthorizationStrategy,
                                    mifareClassicReader,
                                    outerNumberReader,
                                    readPassageMarkMifareClassicReader,
                                    writePassageMarkMifareClassicReader,
                                    serviceDataReader,
                                    coverageAreaReader
                            );
                            return authCardReader;
                        }

                        default: {
                            Logger.error(TAG, "Reader is not implemented for bscType = " + bscType);
                            return null;
                        }
                    }
                } else {
                    Logger.error(TAG, "Could not read bsc type: " + bscTypeResult);
                    return null;
                }
            } else if (cardData.getMifareCardType().isUltralight()) {
                MifareUltralightReader mifareUltralightReader = new MifareUltralightReaderImpl(rfid, cardInfo);
                ReadCardResult<Pair<BscType, byte[]>> bscTypeResult = mifareUltralightReader.readBscTypeWithRawData();
                if (bscTypeResult.isSuccess()) {
                    BscType bscType = bscTypeResult.getData().first;
                    if (!ultralighrCardTypeChecker.check(bscType, cardData.getMifareCardType())) {
                        Logger.error(TAG, "Физический тип карты (" + cardData.getMifareCardType() + ") не соответствует BscType = " + bscType);
                        return null;
                    }
                    byte[] rawDataWithBscType = bscTypeResult.getData().second;
                    switch (bscType) {
                        case CPPK_COUNTER:
                        case EV_1_CPPK_COUNTER: {
                            OuterNumberParser outerNumberParser = new CppkNumberOfTripsOuterNumberParser();
                            OuterNumberReader outerNumberReader = new OuterNumberUltralightReaderImpl(
                                    rfid,
                                    cardInfo,
                                    mifareUltralightReader,
                                    outerNumberParser,
                                    rawDataWithBscType
                            );
                            ReadPdMifareUltralightReader readPdMifareUltralightReader = new ReadPdMifareUltralightReaderImpl(
                                    rfid,
                                    cardInfo,
                                    mifareUltralightReader,
                                    pdDecoderFactory
                            );
                            WritePdMifareUltralightReader writePdMifareClassicReader = new WritePdMifareUltralightReaderImpl(
                                    rfid,
                                    cardInfo,
                                    mifareUltralightReader,
                                    pdEncoderFactory
                            );
                            ReadPassageMarkMifareUltralightReader readPassageMarkMifareUltralightReader = new ReadPassageMarkMifareUltralightReaderImpl(
                                    rfid,
                                    cardInfo,
                                    mifareUltralightReader,
                                    passageMarkDecoderFactory);
                            WritePassageMarkMifareUltralightReader writePassageMarkMifareUltralightReader = new WritePassageMarkMifareUltralightReaderImpl(
                                    rfid,
                                    cardInfo,
                                    mifareUltralightReader,
                                    passageMarkEncoderFactory);
                            if (cardInfo.getMifareCardType() == MifareCardType.UltralightEV1) {
                                CppkNumberOfTripsPayloadReader cppkNumberOfTripsPayloadReader = new CppkNumberOfTripsPayloadReaderImpl(
                                        rfid,
                                        cardInfo,
                                        mifareUltralightReader,
                                        outerNumberReader
                                );

                                ReadCardResult<byte[]> payloadDataResult = cppkNumberOfTripsPayloadReader.readFirstPayloadBytes();
                                if (payloadDataResult.isSuccess()) {
                                    PdVersion pdVersion = pdVersionDetector.getVersion(payloadDataResult.getData());

                                    HardwareCounterReader hardwareCounterReader = new UltralightEV1HardwareCounterReaderImpl(
                                            rfid,
                                            cardInfo,
                                            mifareUltralightReader
                                    );

                                    Logger.trace(TAG, "Detected pd version on ev1: " + pdVersion);

                                    if (pdVersion == PdVersion.V19 || pdVersion == PdVersion.V20 || pdVersion == PdVersion.V23 || pdVersion == PdVersion.V24) {
                                        Logger.trace(TAG, "CppkNumberOfTripsUltralightEv1TwoPdReader created");
                                        return new CppkNumberOfTripsUltralightEv1TwoPdReader(
                                                rfid,
                                                cardInfo,
                                                mifareUltralightReader,
                                                outerNumberReader,
                                                readPdMifareUltralightReader,
                                                writePdMifareClassicReader,
                                                readPassageMarkMifareUltralightReader,
                                                writePassageMarkMifareUltralightReader,
                                                hardwareCounterReader);
                                    } else {
                                        Logger.trace(TAG, "CppkNumberOfTripsUltralightEv1OnePdReader created");
                                        return new CppkNumberOfTripsUltralightEv1OnePdReader(
                                                rfid,
                                                cardInfo,
                                                mifareUltralightReader,
                                                outerNumberReader,
                                                readPdMifareUltralightReader,
                                                writePdMifareClassicReader,
                                                readPassageMarkMifareUltralightReader,
                                                writePassageMarkMifareUltralightReader,
                                                hardwareCounterReader);
                                    }
                                } else {
                                    Logger.error(TAG, "Could not read first payload block for Troyka card: " + payloadDataResult);
                                    return null;
                                }
                            } else {
                                HardwareCounterReader hardwareCounterReader = new UltralightCHardwareCounterReaderImpl(
                                        rfid,
                                        cardInfo,
                                        mifareUltralightReader
                                );
                                Logger.trace(TAG, "CppkNumberOfTripsUltralightCReader created");
                                return new CppkNumberOfTripsUltralightCReader(
                                        rfid,
                                        cardInfo,
                                        mifareUltralightReader,
                                        outerNumberReader,
                                        readPdMifareUltralightReader,
                                        writePdMifareClassicReader,
                                        readPassageMarkMifareUltralightReader,
                                        writePassageMarkMifareUltralightReader,
                                        hardwareCounterReader);
                            }
                        }
                        default: {
                            Logger.error(TAG, "Reader is not implemented for bscType = " + bscType);
                            return null;
                        }
                    }
                } else {
                    Logger.error(TAG, "Could not read bsc type: " + bscTypeResult);
                    return null;
                }
            } else {
                Logger.error(TAG, "Reader is not implemented for mifareCardType = " + cardInfo.getMifareCardType());
                return null;
            }
        }

        Logger.error(TAG, "Task was canceled");
        return null;
    }

    /**
     * Проверяет, прервана ли комада поиска карты извне.
     *
     * @return {@code true}, если прервана, {@code false} иначе
     */
    private boolean isCanceled() {
        return BuildConfig.BUILD_TYPE.equals("debug") ? false : (canceled || Thread.currentThread().isInterrupted());
    }

    /**
     * Прерывает команду поиска карты.
     */
    public void cancel() {
        canceled = BuildConfig.BUILD_TYPE.equals("debug") ? false : true;
    }
}
