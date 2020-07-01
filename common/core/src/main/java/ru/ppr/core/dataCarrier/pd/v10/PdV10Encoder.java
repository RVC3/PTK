package ru.ppr.core.dataCarrier.pd.v10;

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
public class PdV10Encoder implements PdEncoder {

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
        byte[] data = new byte[withEdsKeyNumber ? PdV10Structure.PD_SIZE : PdV10Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV10 pdV10 = (PdV10) pd;

        int orderNumber = pdV10.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV10Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV10Structure.ORDER_NUMBER_BIT_INDEX,
                PdV10Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int trainNumber = pdV10.getTrainNumber();
        DataCarrierUtils.writeInt(
                trainNumber,
                data,
                PdV10Structure.TRAIN_NUMBER_BYTE_INDEX,
                PdV10Structure.TRAIN_NUMBER_BIT_INDEX,
                PdV10Structure.TRAIN_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        String trainLetter = pdV10.getTrainLetter();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(trainLetter),
                data,
                PdV10Structure.TRAIN_LETTER_BYTE_INDEX
        );

        int placeNumber = pdV10.getPlaceNumber();
        DataCarrierUtils.writeBytes(
                new byte[]{(byte) placeNumber},
                data,
                PdV10Structure.PLACE_NUMBER_BYTE_INDEX
        );

        String placeLetter = pdV10.getPlaceLetter();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(placeLetter),
                data,
                PdV10Structure.PLACE_LETTER_BYTE_INDEX
        );

        final int carriageNumber = pdV10.getWagonNumber();
        final int departureDayOffset = pdV10.getDepartureDayOffset();
        final int departureTime = pdV10.getDepartureTime();
        final int carriageDepartureDateAndTime =
                (carriageNumber << PdV10Structure.CARRIAGE_NUMBER_BIT_SHIFT) +
                        (departureDayOffset << PdV10Structure.DEPARTURE_DAY_OFFSET_BIT_SHIFT) +
                        departureTime;
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(carriageDepartureDateAndTime);
        DataCarrierUtils.writeBytes(
                buffer.array(),
                data,
                PdV10Structure.CARRIAGE_DEPARTURE_DATE_AND_TIME_BYTE_INDEX
        );

        int documentTypeCode = pdV10.getDocumentTypeCode();
        DataCarrierUtils.writeBytes(
                new byte[]{(byte) documentTypeCode},
                data,
                PdV10Structure.DOCUMENT_TYPE_CODE_BYTE_INDEX
        );

        String documentNumber = pdV10.getDocumentNumber();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(documentNumber),
                data,
                PdV10Structure.DOCUMENT_NUMBER_BYTE_INDEX
        );

        long departureStationCode = pdV10.getDepartureStationCode();
        DataCarrierUtils.writeLong(
                departureStationCode,
                data,
                PdV10Structure.DEPARTURE_STATION_BYTE_INDEX,
                PdV10Structure.DEPARTURE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long destinationStationCode = pdV10.getDestinationStationCode();
        DataCarrierUtils.writeLong(
                destinationStationCode,
                data,
                PdV10Structure.DESTINATION_STATION_BYTE_INDEX,
                PdV10Structure.DESTINATION_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long ticketTypeCode = pdV10.getTicketTypeCode();
        DataCarrierUtils.writeLong(
                ticketTypeCode,
                data,
                PdV10Structure.TICKET_TYPE_BYTE_INDEX,
                PdV10Structure.TICKET_TYPE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        String lastName = pdV10.getLastName();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(lastName),
                data,
                PdV10Structure.LAST_NAME_BYTE_INDEX
        );

        String firstNameInitial = pdV10.getFirstNameInitial();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(firstNameInitial),
                data,
                PdV10Structure.FIRST_NAME_INITIAL_BYTE_INDEX
        );

        String secondNameInitial = pdV10.getSecondNameInitial();
        DataCarrierUtils.writeBytes(
                DataCarrierUtils.stringWin1251ToBytes(secondNameInitial),
                data,
                PdV10Structure.SECOND_NAME_INITIAL_BYTE_INDEX
        );

        int exemptionCode = pdV10.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV10Structure.EXEMPTION_BYTE_INDEX,
                PdV10Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        Date saleDateTime = pdV10.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV10Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV10Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int forDays = pdV10.getForDays();
        DataCarrierUtils.writeInt(
                forDays,
                data,
                PdV10Structure.FOR_DAYS_BYTE_INDEX,
                PdV10Structure.FOR_DAYS_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV10.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV10Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV10Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }

}
