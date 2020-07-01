package ru.ppr.core.dataCarrier.smartCard.passageMark.troyka;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;

import static ru.ppr.core.dataCarrier.smartCard.passageMark.troyka.PassageMarkTroykaStructure.*;

/**
 * Декодер метки прохода тройки.
 *
 * @author isedoi
 */
public class PassageMarkTroykaDecoder implements PassageMarkDecoder {

    public PassageMarkTroykaDecoder() {
    }

    @Nullable
    @Override
    public PassageMarkTroyka decode(@NonNull byte[] data) {
        if (data.length < PassageMarkTroykaStructure.PASSAGE_MARK_SIZE) return null;
        PassageMarkTroykaImpl passageMarkTroyka = new PassageMarkTroykaImpl();
        passageMarkTroyka.setTrainType(DataCarrierUtils.getValue(data,  PASSAGE_TRAIN_TYPE_BYTE_INDEX, PASSAGE_TRAIN_TYPE_BYTE_LENGTH));
        passageMarkTroyka.setTicketType(DataCarrierUtils.getValue(data,  PASSAGE_TICKET_TYPE_BYTE_INDEX, PASSAGE_TICKET_TYPE_BYTE_LENGTH));
        passageMarkTroyka.setPassMarkType(DataCarrierUtils.getValue(data,  PASSAGE_MARK_TYPE_BYTE_INDEX, PASSAGE_MARK_TYPE_BYTE_LENGTH));
        passageMarkTroyka.setPassageStationCode(DataCarrierUtils.getValue(data,  PASSAGE_STATION_INCOME_BYTE_INDEX, PASSAGE_STATION_INCOME_BYTE_LENGTH) + 2000000);
        passageMarkTroyka.setOutcomeStation(DataCarrierUtils.getValue(data,  PASSAGE_STATION_OUTCOME_BYTE_INDEX, PASSAGE_STATION_OUTCOME_BYTE_LENGTH) + 2000000);
        passageMarkTroyka.setIntersectionTime(DataCarrierUtils.getValue(data,  PASSAGE_TIME_INTERSECT_BYTE_INDEX, PASSAGE_TIME_INTERSECT_BYTE_LENGTH));
        passageMarkTroyka.setTurniketNum(DataCarrierUtils.getValue(data,  PASSAGE_TURNIKET_NUMBER_BYTE_INDEX, PASSAGE_TURNIKET_NUMBER_BYTE_LENGTH));
        final int imitovstavka = DataCarrierUtils.getValue(data,  PASSAGE_IMITOVSTAVKA_BYTE_INDEX, PASSAGE_IMITOVSTAVKA_BYTE_LENGTH);
        final byte[] data_code = Arrays.copyOf(data, 12);
        passageMarkTroyka.setValidImitovstavka(data_code, imitovstavka);
        return passageMarkTroyka;
    }

}
