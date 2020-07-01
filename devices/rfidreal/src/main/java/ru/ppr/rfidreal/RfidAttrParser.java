package ru.ppr.rfidreal;

import java.util.Arrays;

import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Вспомогательный класс, позволяющий получить серийный номер карты по rfidAtr.
 *
 * @author Aleksandr Brazhkin
 */
public class RfidAttrParser {

    private static String TAG = Logger.makeLogTag(RfidAttrParser.class);

    public static byte[] getSerialNumber(byte com, byte atrLength, byte[] atr) {
        byte[] serialNumber = null;
        Logger.trace(TAG, "com: " + CommonUtils.byteToHex(com));
        Logger.trace(TAG, "atr: " + CommonUtils.bytesToHexWithSpaces(atr));
        Logger.trace(TAG, "atrLength: " + CommonUtils.byteToHex(atrLength));
        switch (com) {
            //$01 : Card recognized with Felica® protocol
            case 0x01: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$02 : Card recognized with ISO 14443 type A protocol (ISO level 4 compliant but not Calypso)
            case 0x02: {
                serialNumber = parseIso14443TypeAProtocol(com, atr);
                break;
            }
            //$03 : Card recognized with INNOVATRON protocol
            case 0x03: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$04 : Card recognized with ISO 14443 type B protocol (Calypso Card)
            case 0x04: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$14 : ISO 14443 type B protocol asked but an unwanted collision occurred.
            case 0x14: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$05 : Card recognized with ISO 14443 type MIFARE protocol
            case 0x05: {
                serialNumber = parseMifareProtocol(com, atr);
                break;
            }
            //$15 : ISO 14443 type MIFARE protocol asked but an unwanted collision occurred.
            case 0x15: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$06 : CTS or CTM Ticket recognized
            case 0x06: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$07: Card recognized in contact mode
            case 0x07: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$08 : Card recognized with ISO 14443 type A part 3 but not compliant with 14443 type A part 4
            case 0x08: {
                serialNumber = parseIso14443TypeAProtocol(com, atr);
                break;
            }
            //$18 : ISO 14443 type A protocol asked but an unwanted collision occurred.
            case 0x18: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$09 : Card recognized with ISO 14443 type B protocol (Non-Calypso card)
            case 0x09: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$0B : Card recognized with MV5000 protocol ( no data format control)
            case 0x0B: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$1B : MV5000 protocol asked but an unwanted collision occurred.
            case 0x1B: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$0C : Card recognized with ISO 14443 type A protocol ((Calypso Card)
            case 0x0C: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$0D : SRI detected
            case 0x0D: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$0E : NFC Felica detected
            case 0x0E: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$0F : NFC Mifare detected
            case 0x0F: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$6F : Timeout expired.
            case 0x6F: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
            //$7F: Response (before tag research)
            case 0x7F: {
                Logger.error(TAG, "Unsupported com value");
                break;
            }
        }
        Logger.trace(TAG, "serialNumber: " + CommonUtils.bytesToHexWithSpaces(serialNumber));
        return serialNumber;
    }

    /**
     * INNOVATRON protocol
     * <p>
     * - The serial number (4 bytes) followed by 2 bytes
     * - The Answer to reset (17 bytes for CD97 and GTML)
     * - The Status words (2 bytes)
     */
    private static void parseInnovatronProtocol(byte com, byte[] atr) {

    }

    /**
     * ISO 14443 Type B protocol
     * <p>
     * The communication channel number CID (1 Byte)
     * - The Serial Number if the card is Calypso-Compliant, otherwise the first 4 bytes are filled at 00 and the
     * 4 others are the PUPI (8 bytes).
     * - 1 byte optional length (1 to 16) of application name (if bit 3 of byte 6 of EnterHuntPhaseParameter = 1)
     * - n byte optional application name (if bit 3 of byte 6 of EnterHuntPhaseParameter = 1)
     * - The Historical Bytes if the card is Calypso-Compliant, otherwise the last 3 are filled at 00(7bytes for
     * GTML2) same order as in the SelectApplication command
     * - The Status words is returned if the card is Calypso-Compliant, filled at 00 otherwise (2 bytes)
     */
    private static void parseIso14443TypeBProtocol(byte com, byte[] atr) {

    }

    /**
     * MIFARE protocol
     * <p>
     * The answer in this mode depends on two parameters: for the request to be really sent in MIFARE mode,
     * the presence of the MIFARE chip in the reader is mandatory AND only one card must be looked for.
     * Otherwise the answer is formatted just as an ISO 14443 Type A (14443-4 compliant or not). But if the
     * search is a true MIFARE search, the format is as follows:
     * - Communication Status (1 Byte): set to 0x00: OK.
     * - Type of the card (e.g.: 0x08 for the Mifare Classic, 0x18 for the Mifare 4k, 0x28 for the Mifare Classic
     * implementation in ProX) (1 Byte)
     * - The 4 Serial Number bytes
     */
    private static byte[] parseMifareProtocol(byte com, byte[] atr) {
        byte communicationStatus = atr[0];
        byte cardType = atr[1];
        byte[] serialNumber = Arrays.copyOfRange(atr, 2, 6);
        return serialNumber;
    }

    /**
     * ISO 14443 Type A protocol
     * <p>
     * ISO 14443-4 compliant:
     * - The communication channel number CID (1 Byte).
     * - Length of the Serial Number (1 Byte).
     * - The Serial Number if the card is Calypso-Compliant, otherwise the UID (4, 7 or 10 bytes).
     * - 1 byte optional length (1 to 16) of application name (if bit 3 of byte 6 of
     * EnterHuntPhaseParameter = 1)
     * - n byte optional application name (if bit 3 of byte 6 of EnterHuntPhaseParameter = 1)
     * - Length of the Information (1 Byte).
     * - Information given back in the ATS, including:
     * Maximum size of a frame accepted by the card. (1 Byte)
     * Warning: 0xFF means 256 bytes!
     * The bit rate from the coupler to the card. (1 Byte)
     * The bit rate from the card to the coupler. (1 Byte)
     * Only the same baud rate for both direction is supported? -> 1 if TRUE. (1 Byte)
     * The Frame Waiting Time Integer which defines the Frame Waiting Time between the
     * end of the frame sent by the coupler and the beginning of the answer of the card. (1
     * Byte)
     * The Specific Guard Time Integer which defines the time needed by the card before
     * answering after sending the ATS. (1 Byte)
     * NAD supported? -> 1 if TRUE. (1 Byte)
     * CID supported? -> 1 if TRUE. (1 Byte)
     * Information of the Application (historical bytes).
     * ISO 14443-4 non-compliant: (for instance the MIFAREUltraLight)
     * - Set to 00 (1 Byte) (means non ISO–4 compliant).
     * - Length of the following Serial Number (1 Byte).
     * - Serial Number (Length Bytes)
     * • CTS or C
     */
    private static byte[] parseIso14443TypeAProtocol(byte com, byte[] atr) {
        byte channelNumber = atr[0];
        byte serialNumberLength = atr[1];
        byte[] serialNumberOrUid = Arrays.copyOfRange(atr, 2, 2 + serialNumberLength);
        return serialNumberOrUid;
    }

    /**
     * CTS or CTM Ticket
     */
    private static void parseCtsOrCtmTicket() {

    }

    /**
     * SRI
     */
    private static void parseSri() {

    }

    /**
     * ISO7816 Contact mode
     */
    private static void parseIso7816ContactMode() {

    }

    /**
     * Motorola cards
     */
    private static void parseMotorolaCards() {

    }

    /**
     * FELICA protocol
     */
    private static void parseFelicaProtocol() {

    }
}
