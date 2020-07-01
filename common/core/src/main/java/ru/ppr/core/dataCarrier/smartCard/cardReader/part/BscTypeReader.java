package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;
import android.util.Pair;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;

/**
 * Ридер типа карты.
 *
 * @author Aleksandr Brazhkin
 */
public interface BscTypeReader extends CardReader {

    int TYPE_BSC_START_INDEX = 4;
    int TYPE_BSC_BYTES_COUNT = 1;

    /**
     * Считывает данные содержащие тип карты и вычисляет тип карты.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<Pair<BscType, byte[]>> readBscTypeWithRawData();
}
