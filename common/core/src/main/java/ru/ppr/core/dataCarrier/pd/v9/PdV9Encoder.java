package ru.ppr.core.dataCarrier.pd.v9;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Энкодер ПД v.11.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV9Encoder implements PdEncoder {

    @NonNull
    @Override
    public byte[] encode(@NonNull Pd pd) {
        return internalEncode(pd, true);
    }

    @NonNull
    @Override
    public byte[] encodeWithoutEdsKeyNumber(@NonNull Pd pd) {
        return internalEncode(pd, false);
    }

    private byte[] internalEncode(@NonNull Pd pd, boolean withEdsKeyNumber) {
        byte[] data = new byte[withEdsKeyNumber ? PdV9Structure.PD_SIZE : PdV9Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV9 pdV9 = (PdV9) pd;

        int orderNumber = pdV9.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV9Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV9Structure.ORDER_NUMBER_BIT_INDEX,
                PdV9Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int trainNumber = pdV9.getTrainNumber();
        DataCarrierUtils.writeInt(
                trainNumber,
                data,
                PdV9Structure.TRAIN_NUMBER_BYTE_INDEX,
                PdV9Structure.TRAIN_NUMBER_BIT_INDEX,
                PdV9Structure.TRAIN_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        String trainLetter = pdV9.getTrainLetter();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(trainLetter),
                data,
                PdV9Structure.TRAIN_LETTER_BYTE_INDEX
        );

        int placeNumber = pdV9.getPlaceNumber();
        DataCarrierUtils.writeBytes(
                new byte[]{(byte) placeNumber},
                data,
                PdV9Structure.PLACE_NUMBER_BYTE_INDEX
        );

        String placeLetter = pdV9.getPlaceLetter();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(placeLetter),
                data,
                PdV9Structure.PLACE_LETTER_BYTE_INDEX
        );

        final int carriageNumber = pdV9.getWagonNumber();
        final int departureDayOffset = pdV9.getDepartureDayOffset();
        final int departureTime = pdV9.getDepartureTime();
        final int carriageDepartureDateAndTime =
                (carriageNumber << PdV9Structure.CARRIAGE_NUMBER_BIT_SHIFT) +
                        (departureDayOffset << PdV9Structure.DEPARTURE_DAY_OFFSET_BIT_SHIFT) +
                        departureTime;
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(carriageDepartureDateAndTime);
        DataCarrierUtils.writeBytes(
                buffer.array(),
                data,
                PdV9Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_INDEX
        );

        int documentTypeCode = pdV9.getDocumentTypeCode();
        DataCarrierUtils.writeBytes(
                new byte[]{(byte) documentTypeCode},
                data,
                PdV9Structure.DOCUMENT_TYPE_CODE_BYTE_INDEX
        );

        String documentNumber = pdV9.getDocumentNumber();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(documentNumber),
                data,
                PdV9Structure.DOCUMENT_NUMBER_BYTE_INDEX
        );

        long departureStationCode = pdV9.getDepartureStationCode();
        DataCarrierUtils.writeLong(
                departureStationCode,
                data,
                PdV9Structure.DEPARTURE_STATION_BYTE_INDEX,
                PdV9Structure.DEPARTURE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long destinationStationCode = pdV9.getDestinationStationCode();
        DataCarrierUtils.writeLong(
                destinationStationCode,
                data,
                PdV9Structure.DESTINATION_STATION_BYTE_INDEX,
                PdV9Structure.DESTINATION_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long ticketTypeCode = pdV9.getTicketTypeCode();
        DataCarrierUtils.writeLong(
                ticketTypeCode,
                data,
                PdV9Structure.TICKET_TYPE_BYTE_INDEX,
                PdV9Structure.TICKET_TYPE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        String lastName = pdV9.getLastName();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(lastName),
                data,
                PdV9Structure.LAST_NAME_BYTE_INDEX
        );

        String firstNameInitial = pdV9.getFirstNameInitial();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(firstNameInitial),
                data,
                PdV9Structure.FIRST_NAME_INITIAL_BYTE_INDEX
        );

        String secondNameInitial = pdV9.getSecondNameInitial();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(secondNameInitial),
                data,
                PdV9Structure.SECOND_NAME_INITIAL_BYTE_INDEX
        );

        int exemptionCode = pdV9.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV9Structure.EXEMPTION_BYTE_INDEX,
                PdV9Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        Date saleDateTime = pdV9.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV9Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV9Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV9.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV9Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV9Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        byte[] eds = pdV9.getEds();
        DataCarrierUtils.writeBytes(eds, data, PdV9Structure.EDS_KEY_BYTE_INDEX);

        return data;
    }

}
