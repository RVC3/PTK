package ru.ppr.rfidreal;

import fr.coppernic.cpcframework.cpcask.Commands;
import fr.coppernic.cpcframework.cpcask.Defines;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Вспомогательный класс, позволяющий получить параметры, необходимые для определения физического типа карты.
 */
public class CardTypeParcer {

    private static String TAG = Logger.makeLogTag(CardTypeParcer.class);
    private static int mFuncTimeout = 2000;

    /**
     * Возвращает ATQA карты. Обладает свойством чинить функцию:  mifareUlIdentifyType(byte[] status)
     *
     * @param mReader
     * @return
     */
    public static byte[] getATQA(AscReader mReader) {

        String prefix = "getATQA() ";
        addLog(prefix + "START");
        long timer = System.currentTimeMillis();

        byte[] atqa = null;

        if (!mReader.isTimeoutError()) {

            int vRet; // return value


            // prepares the command buffer for a SwitchOffAntenna command
            Commands.iCSC_SwitchOffAntenna((byte) 0x01); // antenna 1

            // Send a command frame to the CSC, and waits for the answer
            vRet = mReader.cscSendReceive(mFuncTimeout, Commands.giCSCTrame, Commands.giCSCTrameLn);

            if (vRet != Defines.RCSC_Ok) {
                addLog(prefix + "Error: SwitchOffAntenna 1");
            }

            if (!mReader.isTimeoutError()) {

                Commands.giCSCTrame[0] = Defines.CSC_CMD_EXEC; // EXEC Command
                Commands.giCSCTrame[1] = 0x05; // Length
                Commands.giCSCTrame[2] = Defines.CSC_CLA_MIFARE; // System class
                Commands.giCSCTrame[3] = 0x01;
                Commands.giCSCTrame[4] = 0x02;
                Commands.giCSCTrame[5] = 0x0C;
                Commands.giCSCTrame[6] = 0x01;
                Commands.giCSCTrame[7] = 0x00;
                Commands.giCSCTrameLn = 8;
                Commands.icsc_SetCRC();

                // Send a command frame to the CSC, and waits for the answer
                vRet = mReader.cscSendReceive(mFuncTimeout, Commands.giCSCTrame, Commands.giCSCTrameLn);

                if (!mReader.isTimeoutError()) {

                    if (vRet != Defines.RCSC_Ok) {
                        addLog(prefix + "Error: SwitchOffAntenna 2");
                    }

                    byte[] configISO = new byte[1];
                    byte[] configAddCRC = new byte[1];
                    byte[] configCheckCRC = new byte[1];
                    byte[] configField = new byte[1];

                    vRet = mReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x01, configISO, configAddCRC, configCheckCRC, configField);

                    if (!mReader.isTimeoutError()) {

                        if (vRet != Defines.RCSC_Ok)
                            addLog(prefix + "Error: cscTransparentCommandConfig");

                        byte[] status = new byte[1];
                        int[] lnOut = new int[1];
                        byte[] bufOut = new byte[255];

                        mReader.cscTransparentCommand(new byte[]{(byte) 0x26}, 1, status, lnOut, bufOut);

                        if (!mReader.isTimeoutError()) {
                            if (lnOut[0] > 0) {
                                // Builds the string ATQA
                                StringBuilder sbAtr = new StringBuilder();
                                for (int i = 0; i < lnOut[0]; i++) {
                                    sbAtr.append(String.format("%02X ", bufOut[i]));
                                }
                                addLog(prefix + "ATQA : " + sbAtr.toString());
                                atqa = CommonUtils.getByteFromData(bufOut, 0, lnOut[0]);
                            } else
                                addLog(prefix + "Error: lnOut[0]=" + lnOut[0]);
                        } else
                            addLog(prefix + "Error: TimeOut (15)");
                    } else
                        addLog(prefix + "Error: TimeOut (14)");
                } else
                    addLog(prefix + "Error: TimeOut (13)");
            } else
                addLog(prefix + "Error: TimeOut (12)");
        } else
            addLog(prefix + "Error: TimeOut (11)");
        addLog(prefix + "FINISH atqa=" + CommonUtils.bytesToHexWithoutSpaces(atqa) + getTimeString(timer));
        return atqa;
    }

    /**
     * Определяет SAK кары.
     * Обладает свойством ломать функцию:  mifareUlIdentifyType(byte[] status)
     * Не срабатывает если перед ней не вызвать функцию  getATQA
     *
     * @param ascReader
     * @return
     */
    public static byte[] getSAK(AscReader ascReader) {

        String prefix = "getSAK() ";
        addLog(prefix + "START");
        long timer = System.currentTimeMillis();

        byte[] sak = null;

        if (!ascReader.isTimeoutError()) {

            // из-за бага сдк если не вызвать эту функцию getSAK всегда возвращает null
            // ANTICOLLISION CASCADE LEVEL 1 : 01
            // error getting SAK2
            // getATQA(mReader);

            int vRet;

            byte[] configISO = new byte[1];
            byte[] configAddCRC = new byte[1];
            byte[] configCheckCRC = new byte[1];
            byte[] configField = new byte[1];

            // Transparent Mode A Parity On
            vRet = ascReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x88, (byte) 0x08, (byte) 0x00, configISO, configAddCRC, configCheckCRC, configField);

            if (!ascReader.isTimeoutError()) {

                if (vRet != Defines.RCSC_Ok) {
                    addLog(prefix + "Error: cscTransparentCommandConfig (11)");
                }

                byte[] status = new byte[1];
                int[] lnOut = new int[1];
                byte[] bufOut = new byte[255];

                // ANTICOLLISION CASCADE LEVEL 1
                vRet = ascReader.cscTransparentCommand(new byte[]{(byte) 0x93, (byte) 0x20}, 2, status, lnOut, bufOut);

                if (vRet == Defines.RCSC_Ok) {

                    if (lnOut[0] > 0) {
                        // Builds the string ANTICOLLISION CASCADE LEVEL 1
                        StringBuilder sbUID = new StringBuilder();
                        for (int i = 0; i < lnOut[0]; i++) {
                            sbUID.append(String.format("%02X ", bufOut[i]));
                        }
                        addLog(prefix + "ANTICOLLISION CASCADE LEVEL 1 : " + sbUID.toString());


                        // Transparent Mode A Parity On CRC On
                        vRet = ascReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x89, (byte) 0x09, (byte) 0x00, configISO, configAddCRC, configCheckCRC, configField);

                        if (vRet == Defines.RCSC_Ok) {

                            byte[] command = new byte[2 + lnOut[0]];
                            command[0] = (byte) 0x93;
                            command[1] = (byte) 0x70;

                            System.arraycopy(bufOut, 0, command, 2, lnOut[0]);

                            // SELECT CASCADE LEVEL 1
                            vRet = ascReader.cscTransparentCommand(command, 2 + lnOut[0], status, lnOut, bufOut);

                            if (vRet == Defines.RCSC_Ok) {

                                boolean cascadeLevel1AndBuffOutRes = true;
                                if (lnOut[0] > 0) {
                                    // Builds the string SLECT Cascade Level1
                                    StringBuilder sbSelect = new StringBuilder();

                                    for (int i = 0; i < lnOut[0]; i++) {
                                        sbSelect.append(String.format("%02X ", bufOut[i]));
                                    }

                                    addLog(prefix + "SAK Cascade level 1 : " + sbSelect.toString());

                                    // Bit 3 indicate if UID is
                                    // close (0=close; 1= not
                                    // close)
                                    if ((bufOut[0] & (byte) 0x04) == 0) {
                                        cascadeLevel1AndBuffOutRes = false;
                                    }
                                }

                                if (cascadeLevel1AndBuffOutRes) {

                                    // Transparent Mode A Parity On
                                    vRet = ascReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x88, (byte) 0x08, (byte) 0x00, configISO, configAddCRC, configCheckCRC, configField);

                                    if (vRet == Defines.RCSC_Ok) {

                                        // ANTICOLLISION CASCADE LEVEL 2
                                        vRet = ascReader.cscTransparentCommand(new byte[]{(byte) 0x95, (byte) 0x20}, 2, status, lnOut, bufOut);

                                        if (vRet == Defines.RCSC_Ok) {

                                            if (lnOut[0] > 0) {
                                                // Builds the string ANticollision
                                                sbUID = new StringBuilder();

                                                for (int i = 0; i < lnOut[0]; i++) {
                                                    sbUID.append(String.format("%02X ", bufOut[i]));
                                                }

                                                addLog(prefix + "ANTICOLLISION 2 : " + sbUID.toString());
                                            }

                                            // Transparent Mode A Parity On CRC On
                                            vRet = ascReader.cscTransparentCommandConfig((byte) 0x02, (byte) 0x89, (byte) 0x09, (byte) 0x00, configISO, configAddCRC, configCheckCRC, configField);

                                            if (vRet == Defines.RCSC_Ok) {

                                                // SELECT CASCADE LEVEL 2
                                                command = new byte[2 + lnOut[0]];
                                                command[0] = (byte) 0x95;
                                                command[1] = (byte) 0x70;

                                                System.arraycopy(bufOut, 0, command, 2, lnOut[0]);

                                                vRet = ascReader.cscTransparentCommand(command, 2 + lnOut[0], status, lnOut, bufOut);

                                                if (vRet == Defines.RCSC_Ok) {

                                                    if (lnOut[0] > 0) {
                                                        // Builds the string ATQA
                                                        StringBuilder sbSelect = new StringBuilder();
                                                        for (int i = 0; i < lnOut[0]; i++) {
                                                            sbSelect.append(String.format("%02X ", bufOut[i]));
                                                        }
                                                        addLog(prefix + "SAK2 : " + sbSelect.toString());
                                                        sak = CommonUtils.getByteFromData(bufOut, 0, lnOut[0]);
                                                    } else
                                                        addLog(prefix + "Error: getting SAK2 (21)");
                                                } else
                                                    addLog(prefix + "Error: cscTransparentCommand (20)");
                                            } else
                                                addLog(prefix + "Error: cscTransparentCommandConfig (19)");
                                        } else
                                            addLog(prefix + "Error: cscTransparentCommand 2 (18)");
                                    } else
                                        addLog(prefix + "Error: cscTransparentCommandConfig (17)");
                                } else
                                    addLog(prefix + "Error: '(bufOut[0] & (byte) 0x04) != 0' (16)");
                            } else
                                addLog(prefix + "Error: cscTransparentCommand (15)");
                        } else
                            addLog(prefix + "Error: cscTransparentCommandConfig 2 (14)");
                    } else
                        addLog(prefix + "Error: getting ANTICOLLISION CASCADE LEVEL 1 (13)");
                } else
                    addLog(prefix + "Error: cscTransparentCommand (12)");
            } else
                addLog(prefix + "Error: TimeOut (11)");
        } else
            addLog(prefix + "Error: TimeOut (10)");
        addLog(prefix + "FINISH sak=" + CommonUtils.bytesToHexWithoutSpaces(sak) + getTimeString(timer));
        return sak;
    }

    private static void addLog(String log) {
        Logger.trace(TAG, log);
    }

    private static String getTimeString(long startTimeStamp) {
        return " - " + (System.currentTimeMillis() - startTimeStamp) + "mc";
    }

}
