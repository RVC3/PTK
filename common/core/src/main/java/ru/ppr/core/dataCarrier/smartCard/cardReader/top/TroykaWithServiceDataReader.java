package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceCardReader;

/**
 * Ридер карт Тройка 2K со служебными данными.
 *
 * @author Aleksandr Brazhkin
 */
public interface TroykaWithServiceDataReader extends TroykaReader, ServiceCardReader {
}
