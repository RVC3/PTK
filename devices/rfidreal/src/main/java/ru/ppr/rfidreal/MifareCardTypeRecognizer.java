package ru.ppr.rfidreal;

import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.MifareCardType;

/**
 * Определитель физического типа карты.
 * https://docs.google.com/spreadsheets/d/1qV5gaDi9HjPpkP4pTHZFZse7nrBIF-XgcpndQqe0RKM/edit#gid=0
 *
 * @author Aleksandr Brazhkin
 */
public class MifareCardTypeRecognizer {

    private static final String TAG = Logger.makeLogTag(MifareCardTypeRecognizer.class);

    public MifareCardTypeRecognizer() {
    }

    /**
     * Вернет тип карты а основе данных объекта CardData
     *
     * @param cardData
     * @return
     */
    public MifareCardType getMifareCardType(CardData cardData) {

        MifareCardType out = MifareCardType.Unknown;

        if (cardData != null && cardData.getAtqa() != null) {


            switch (cardData.getAtqaInt()) {
                case 0x0004:
                    out = MifareCardType.Mifare_Classic_1K;
                    break;
                case 0x0002:
                    out = MifareCardType.Mifare_Classic_4K;
                    break;
                case 0x0044: //ultralight or classic 4k with 7byte uid or plus 2k
                case 0x0042: //classic 4k with 7 byte uid or plus 4k
                case 0x0048: // mifare plus S 2k with 7 byteK uid
                    if (cardData.getSak() == null)
                        break;
                    switch (cardData.getSakInt()) {
                        case 0x08: //PICC not compliant with ISO/IEC 14443-4
                        case 0x28: //PICC compliant with ISO/IEC 14443-4
                            out = MifareCardType.Mifare_Plus_2K;
                            break;
                        case 0x18: //PICC not compliant with ISO/IEC 14443-4
                        case 0x38: //PICC compliant with ISO/IEC 14443-4
                            out = MifareCardType.Mifare_Plus_4K;
                            break;
                        case 0x00: {  //Ultralight
                            out = isEv1(cardData) ? MifareCardType.UltralightEV1 : MifareCardType.UltralightC;
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

        }

        Logger.trace(TAG, "getMifareCardType: " + out.toString());

        return out;
    }

    /**
     * Определяет принадлежность Ultralight карты к семейству Ev1
     *
     * @param cardData
     * @return
     */
    private boolean isEv1(CardData cardData) {
        //0x22 - Mifare UltraLight EV1 640 bits 0x23 - Mifare UltraLight EV1 1312 bits,
        //другой способ определния пока не подходит
        //ибо после вызова mifareUlEv1GetVersion любое последующее чтение проваливается,
        //надо подбирать команды драйверу. С таким типом определения всё пока в порядке.

        //этот метод не сработал на карте Ev1 которая вернула 0x35
        //return cardData.getMifareUlIdentifyType() == 0x22 || cardData.getMifareUlIdentifyType() == 0x23;

        return cardData.isEv1();
    }
}
