package ru.ppr.core.dataCarrier.pd.v10;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;

/**
 * Декодер ПД v.10.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV10Decoder implements PdDecoder {

    @Nullable
    @Override
    public PdV10 decode(@NonNull byte[] data) {

        if (data.length < PdV10Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV10Structure.ORDER_NUMBER_BYTE_INDEX, PdV10Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV10Structure.ORDER_NUMBER_BIT_INDEX, PdV10Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte[] trainNumberData = DataCarrierUtils.subArray(data, PdV10Structure.TRAIN_NUMBER_BYTE_INDEX, PdV10Structure.TRAIN_NUMBER_BYTE_LENGTH);
        int trainNumber = DataCarrierUtils.bytesToInt(trainNumberData, PdV10Structure.TRAIN_NUMBER_BIT_INDEX, PdV10Structure.TRAIN_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte trainLetterData = data[PdV10Structure.TRAIN_LETTER_BYTE_INDEX];
        String trainLetter = DataCarrierUtils.bytesToStringWin1251(new byte[]{trainLetterData});

        byte placeNumberData = data[PdV10Structure.PLACE_NUMBER_BYTE_INDEX];
        int placeNumber = DataCarrierUtils.byteToInt(placeNumberData);

        byte placeLetterData = data[PdV10Structure.PLACE_LETTER_BYTE_INDEX];
        String placeLetter = DataCarrierUtils.bytesToStringWin1251(new byte[]{placeLetterData});

        final byte[] carriageDepartureDateAndTimeData = DataCarrierUtils.subArray(data, PdV10Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_INDEX, PdV10Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_LENGTH);
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(carriageDepartureDateAndTimeData);
        buffer.put((byte) 0); // Дополняем до 4 байт
        final int carriageDepartureDateAndTime = buffer.getInt(0);
        final int carriageNumber = carriageDepartureDateAndTime >> PdV10Structure.CARRIAGE_NUMBER_BIT_SHIFT;
        final int departureDayOffset = (carriageDepartureDateAndTime >> PdV10Structure.DEPARTURE_DAY_OFFSET_BIT_SHIFT) & PdV10Structure.DEPARTURE_DAY_OFFSET_BIT_MASK;
        final int departureTime = carriageDepartureDateAndTime & PdV10Structure.DEPARTURE_TIME_BIT_MASK;

        byte documentTypeCodeData = data[PdV10Structure.DOCUMENT_TYPE_CODE_BYTE_INDEX];
        int documentTypeCode = DataCarrierUtils.byteToInt(documentTypeCodeData);

        byte[] documentNumberData = DataCarrierUtils.subArray(data, PdV10Structure.DOCUMENT_NUMBER_BYTE_INDEX, PdV10Structure.DOCUMENT_NUMBER_BYTE_LENGTH);
        String documentNumber = DataCarrierUtils.bytesToStringWin1251(documentNumberData);

        byte[] departureStationCodeData = DataCarrierUtils.subArray(data, PdV10Structure.DEPARTURE_STATION_BYTE_INDEX, PdV10Structure.DEPARTURE_STATION_BYTE_LENGTH);
        long departureStationCode = DataCarrierUtils.bytesToLong(departureStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] destinationStationCodeData = DataCarrierUtils.subArray(data, PdV10Structure.DESTINATION_STATION_BYTE_INDEX, PdV10Structure.DESTINATION_STATION_BYTE_LENGTH);
        long destinationStationCode = DataCarrierUtils.bytesToLong(destinationStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] ticketTypeCodeData = DataCarrierUtils.subArray(data, PdV10Structure.TICKET_TYPE_BYTE_INDEX, PdV10Structure.TICKET_TYPE_BYTE_LENGTH);
        long ticketTypeCode = DataCarrierUtils.bytesToLong(ticketTypeCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] lastNameData = DataCarrierUtils.subArray(data, PdV10Structure.LAST_NAME_BYTE_INDEX, PdV10Structure.LAST_NAME_BYTE_LENGTH);
        String lastName = DataCarrierUtils.bytesToStringWin1251(lastNameData);

        byte firstNameInitialData = data[PdV10Structure.FIRST_NAME_INITIAL_BYTE_INDEX];
        String firstNameInitial = DataCarrierUtils.bytesToStringWin1251(new byte[]{firstNameInitialData});

        byte secondNameInitialData = data[PdV10Structure.SECOND_NAME_INITIAL_BYTE_INDEX];
        String secondNameInitial = DataCarrierUtils.bytesToStringWin1251(new byte[]{secondNameInitialData});

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV10Structure.EXEMPTION_BYTE_INDEX, PdV10Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV10Structure.SALE_DATE_TIME_BYTE_INDEX, PdV10Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] forDaysData = DataCarrierUtils.subArray(data, PdV10Structure.FOR_DAYS_BYTE_INDEX, PdV10Structure.FOR_DAYS_BYTE_LENGTH);
        int forDays = DataCarrierUtils.bytesToInt(forDaysData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV10Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV10Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV10Impl pdV10 = new PdV10Impl();
        pdV10.setOrderNumber(orderNumber);
        pdV10.setTrainNumber(trainNumber);
        pdV10.setTrainLetter(trainLetter);
        pdV10.setPlaceNumber(placeNumber);
        pdV10.setPlaceLetter(placeLetter);
        pdV10.setWagonNumber(carriageNumber);
        pdV10.setDepartureDayOffset(departureDayOffset);
        pdV10.setDepartureTime(departureTime);
        pdV10.setDocumentTypeCode(documentTypeCode);
        pdV10.setDocumentNumber(documentNumber);
        pdV10.setDepartureStationCode(departureStationCode);
        pdV10.setDestinationStationCode(destinationStationCode);
        pdV10.setTicketTypeCode(ticketTypeCode);
        pdV10.setLastName(lastName);
        pdV10.setFirstNameInitial(firstNameInitial);
        pdV10.setSecondNameInitial(secondNameInitial);
        pdV10.setExemptionCode(exemptionCode);
        pdV10.setSaleDateTime(saleDateTime);
        pdV10.setForDays(forDays);
        pdV10.setEdsKeyNumber(edsKeyNumber);

        return pdV10;
    }

}
