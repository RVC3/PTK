package ru.ppr.core.dataCarrier.pd.v9;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Декодер ПД версии 9.
 *
 * @see PdV9Impl
 */
public class PdV9Decoder implements PdDecoder {

    @Nullable
    @Override
    public PdV9 decode(@NonNull final byte[] data) {

        if (data.length < PdV9Structure.PD_SIZE)
            return null;

        final byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV9Structure.ORDER_NUMBER_BYTE_INDEX, PdV9Structure.ORDER_NUMBER_BYTE_LENGTH);
        final int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV9Structure.ORDER_NUMBER_BIT_INDEX, PdV9Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        final byte[] trainNumberData = DataCarrierUtils.subArray(data, PdV9Structure.TRAIN_NUMBER_BYTE_INDEX, PdV9Structure.TRAIN_NUMBER_BYTE_LENGTH);
        final int trainNumber = DataCarrierUtils.bytesToInt(trainNumberData, PdV9Structure.TRAIN_NUMBER_BIT_INDEX, PdV9Structure.TRAIN_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        final byte trainLetterData = data[PdV9Structure.TRAIN_LETTER_BYTE_INDEX];
        final String trainLetter = DataCarrierUtils.bytesToStringWin1251(new byte[]{trainLetterData});

        final byte placeNumberData = data[PdV9Structure.PLACE_NUMBER_BYTE_INDEX];
        final int placeNumber = DataCarrierUtils.byteToInt(placeNumberData);

        final byte placeLetterData = data[PdV9Structure.PLACE_LETTER_BYTE_INDEX];
        final String placeLetter = DataCarrierUtils.bytesToStringWin1251(new byte[]{placeLetterData});

        final byte[] carriageDepartureDateAndTimeData = DataCarrierUtils.subArray(data, PdV9Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_INDEX, PdV9Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_LENGTH);
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(carriageDepartureDateAndTimeData);
        buffer.put((byte) 0); // Дополняем до 4 байт
        final int carriageDepartureDateAndTime = buffer.getInt(0);
        final int carriageNumber = carriageDepartureDateAndTime >> PdV9Structure.CARRIAGE_NUMBER_BIT_SHIFT;
        final int departureDayOffset = (carriageDepartureDateAndTime >> PdV9Structure.DEPARTURE_DAY_OFFSET_BIT_SHIFT) & PdV9Structure.DEPARTURE_DAY_OFFSET_BIT_MASK;
        final int departureTime = carriageDepartureDateAndTime & PdV9Structure.DEPARTURE_TIME_BIT_MASK;

        final byte documentTypeCodeData = data[PdV9Structure.DOCUMENT_TYPE_CODE_BYTE_INDEX];
        final int documentTypeCode = DataCarrierUtils.byteToInt(documentTypeCodeData);

        final byte[] documentNumberData = DataCarrierUtils.subArray(data, PdV9Structure.DOCUMENT_NUMBER_BYTE_INDEX, PdV9Structure.DOCUMENT_NUMBER_BYTE_LENGTH);
        final String documentNumber = DataCarrierUtils.bytesToStringWin1251(documentNumberData);

        byte[] departureStationCodeData = DataCarrierUtils.subArray(data, PdV9Structure.DEPARTURE_STATION_BYTE_INDEX, PdV9Structure.DEPARTURE_STATION_BYTE_LENGTH);
        long departureStationCode = DataCarrierUtils.bytesToLong(departureStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] destinationStationCodeData = DataCarrierUtils.subArray(data, PdV9Structure.DESTINATION_STATION_BYTE_INDEX, PdV9Structure.DESTINATION_STATION_BYTE_LENGTH);
        long destinationStationCode = DataCarrierUtils.bytesToLong(destinationStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] ticketTypeCodeData = DataCarrierUtils.subArray(data, PdV9Structure.TICKET_TYPE_BYTE_INDEX, PdV9Structure.TICKET_TYPE_BYTE_LENGTH);
        long ticketTypeCode = DataCarrierUtils.bytesToLong(ticketTypeCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] lastNameData = DataCarrierUtils.subArray(data, PdV9Structure.LAST_NAME_BYTE_INDEX, PdV9Structure.LAST_NAME_BYTE_LENGTH);
        String lastName = DataCarrierUtils.bytesToStringWin1251(lastNameData);

        byte firstNameInitialData = data[PdV9Structure.FIRST_NAME_INITIAL_BYTE_INDEX];
        String firstNameInitial = DataCarrierUtils.bytesToStringWin1251(new byte[]{firstNameInitialData});

        byte secondNameInitialData = data[PdV9Structure.SECOND_NAME_INITIAL_BYTE_INDEX];
        String secondNameInitial = DataCarrierUtils.bytesToStringWin1251(new byte[]{secondNameInitialData});

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV9Structure.EXEMPTION_BYTE_INDEX, PdV9Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV9Structure.SALE_DATE_TIME_BYTE_INDEX, PdV9Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV9Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV9Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        final byte[] eds = DataCarrierUtils.subArray(data, PdV9Structure.EDS_KEY_BYTE_INDEX, PdV9Structure.EDS_KEY_BYTE_LENGTH);

        PdV9Impl pdV9 = new PdV9Impl();
        pdV9.setOrderNumber(orderNumber);
        pdV9.setTrainNumber(trainNumber);
        pdV9.setTrainLetter(trainLetter);
        pdV9.setPlaceNumber(placeNumber);
        pdV9.setPlaceLetter(placeLetter);
        pdV9.setWagonNumber(carriageNumber);
        pdV9.setDepartureDayOffset(departureDayOffset);
        pdV9.setDepartureTime(departureTime);
        pdV9.setDocumentTypeCode(documentTypeCode);
        pdV9.setDocumentNumber(documentNumber);
        pdV9.setDepartureStationCode(departureStationCode);
        pdV9.setDestinationStationCode(destinationStationCode);
        pdV9.setTicketTypeCode(ticketTypeCode);
        pdV9.setLastName(lastName);
        pdV9.setFirstNameInitial(firstNameInitial);
        pdV9.setSecondNameInitial(secondNameInitial);
        pdV9.setExemptionCode(exemptionCode);
        pdV9.setSaleDateTime(saleDateTime);
        pdV9.setEdsKeyNumber(edsKeyNumber);
        pdV9.setEds(eds);

        return pdV9;
    }

}
