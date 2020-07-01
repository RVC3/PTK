package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;

/**
 * Ридер смарт карт СКМ, СКМО, ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public interface SkmSkmoIpkReader extends CardReader, BscInformationReader, EmissionDataReader, PersonalDataReader {
}
