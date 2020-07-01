package ru.ppr.rfidreal;

public class BCommands {

        public static final int kiCSC_CRCINIT = 3911;
        private static int[] kiCSC_CRCTABLE = new int[]{61560, 57841, 54122, 49891, 46684, 42965, 38222, 33991, 31792, 28089, 24354, 20139, 14868, 11165, 6406, 2191, 57593, 61808, 50155, 53858, 42717, 46932, 34255, 37958, 27825, 32056, 20387, 24106, 10901, 15132, 2439, 6158, 53626, 49395, 62056, 58337, 38750, 34519, 46156, 42437, 23858, 19643, 32288, 28585, 6934, 2719, 14340, 10637, 49659, 53362, 58089, 62304, 34783, 38486, 42189, 46404, 19891, 23610, 28321, 32552, 2967, 6686, 10373, 14604, 45692, 41973, 37230, 32999, 62552, 58833, 55114, 50883, 15924, 12221, 7462, 3247, 30736, 27033, 23298, 19083, 41725, 45940, 33263, 36966, 58585, 62800, 51147, 54850, 11957, 16188, 3495, 7214, 26769, 31000, 19331, 23050, 37758, 33527, 45164, 41445, 54618, 50387, 63048, 59329, 7990, 3775, 15396, 11693, 22802, 18587, 31232, 27529, 33791, 37494, 41197, 45412, 50651, 54354, 59081, 63296, 4023, 7742, 11429, 15660, 18835, 22554, 27265, 31496, 29808, 26105, 22370, 18155, 12884, 9181, 4422, 207, 63544, 59825, 56106, 51875, 48668, 44949, 40206, 35975, 25841, 30072, 18403, 22122, 8917, 13148, 455, 4174, 59577, 63792, 52139, 55842, 44701, 48916, 36239, 39942, 21874, 17659, 30304, 26601, 4950, 735, 12356, 8653, 55610, 51379, 64040, 60321, 40734, 36503, 48140, 44421, 17907, 21626, 26337, 30568, 983, 4702, 8389, 12620, 51643, 55346, 60073, 64288, 36767, 40470, 44173, 48388, 13940, 10237, 5478, 1263, 28752, 25049, 21314, 17099, 47676, 43957, 39214, 34983, 64536, 60817, 57098, 52867, 9973, 14204, 1511, 5230, 24785, 29016, 17347, 21066, 43709, 47924, 35247, 38950, 60569, 64784, 53131, 56834, 6006, 1791, 13412, 9709, 20818, 16603, 29248, 25545, 39742, 35511, 47148, 43429, 56602, 52371, 65032, 61313, 2039, 5758, 9445, 13676, 16851, 20570, 25281, 29512, 35775, 39478, 43181, 47396, 52635, 56338, 61065, 65280};
        public static int kiCSCMaxTrame = 270;
        public static int giCSCTrameLn = 0;
        public static byte[] giCSCTrame;
        public static byte gCurrentSAM;
        public static byte[] gSAM_Prot;

        public BCommands() {
        }

        public static void icsc_SetCRC() {
            int CRCVal = 0;

            for(int i = 0; i < giCSCTrameLn; ++i) {
                CRCVal = kiCSC_CRCTABLE[(CRCVal ^= giCSCTrame[i] & 255) & 255] ^ CRCVal >> 8;
            }

            giCSCTrame[giCSCTrameLn] = (byte)(CRCVal % 256);
            giCSCTrame[giCSCTrameLn + 1] = (byte)(CRCVal / 256);
            giCSCTrameLn += 2;
        }

        public static boolean iCSC_TestCRC() {
            int lg = 0;
            int CRCVal = 0;
            if (giCSCTrameLn > 1) {
                if (giCSCTrame[1] != 255) {
                    if ((lg = (giCSCTrame[1] & 255) + 5) > giCSCTrameLn) {
                        return false;
                    }
                } else if ((lg = (giCSCTrame[2] & 255) + 6 + 255) > giCSCTrameLn) {
                    return false;
                }
            }

            for(int i = 0; i < lg; ++i) {
                CRCVal = kiCSC_CRCTABLE[(CRCVal ^= giCSCTrame[i] & 255) & 255] ^ CRCVal >> 8;
            }

            return CRCVal == 3911;
        }

        public static void iCSC_EnterHuntPhaseParameters(byte maxNbCards, byte req, byte nbSlots, byte afi, byte autoSelDiv, byte deselect, byte selectAppli, byte dataLength, byte[] data, short felicaAfi, byte felicaNbSlots) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 0;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 23;
            giCSCTrame[4] = maxNbCards;
            giCSCTrame[5] = req;
            giCSCTrame[6] = nbSlots;
            giCSCTrame[7] = afi;
            giCSCTrame[8] = autoSelDiv;
            giCSCTrame[9] = deselect;
            giCSCTrame[10] = selectAppli;
            giCSCTrame[11] = 0;
            giCSCTrameLn = 12;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iCSC_EnterHuntPhase4(byte Antenna, byte MONO, byte OTH, byte CONT, byte ISOA, byte MIFARE, byte ISOB, byte TICK, byte INNO, byte MV4k, byte MV5k, byte Forget, byte TimeOut) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 10;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 3;
            giCSCTrame[4] = (byte)(MONO << 6 | Antenna);
            giCSCTrame[5] = (byte)(OTH << 4 | CONT);
            giCSCTrame[6] = (byte)(MV5k << 6 | MV4k << 4 | ISOB);
            giCSCTrame[7] = (byte)(ISOA << 4 | MIFARE);
            giCSCTrame[8] = (byte)(TICK << 4 | INNO);
            giCSCTrame[9] = 1;
            giCSCTrame[10] = Forget;
            giCSCTrame[11] = TimeOut;
            giCSCTrame[12] = 0;
            giCSCTrameLn = 13;
            icsc_SetCRC();
        }

        public static void iCSC_SwitchOnAntenna(byte Antenna) {
            giCSCTrame[0] = -128;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 14;
            giCSCTrame[4] = Antenna;
            giCSCTrame[5] = 1;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCSC_SwitchOffAntenna(byte Antenna) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 14;
            giCSCTrame[4] = Antenna;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCSC_TransparentCommandConfig(byte ISO, byte addCRC, byte checkCRC, byte field) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 32;
            giCSCTrame[4] = ISO;
            giCSCTrame[5] = addCRC;
            giCSCTrame[6] = checkCRC;
            giCSCTrame[7] = field;
            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iCSC_TransparentCommand(byte frameLength, byte[] frame) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(frameLength + 2);
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 33;

            for(int i = 0; i < (frameLength & 255); ++i) {
                giCSCTrame[i + 4] = frame[i];
            }

            giCSCTrame[frameLength + 4] = 0;
            giCSCTrameLn = frameLength + 5;
            icsc_SetCRC();
        }

        public static void iCSC_SendToAntenna(byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 4);
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 18;
            giCSCTrame[4] = (byte)(DataLen + 1);
            giCSCTrame[5] = (byte)(DataLen + 1);

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 6] = Data[i];
            }

            giCSCTrame[DataLen + 6] = 0;
            giCSCTrameLn = DataLen + 7;
            icsc_SetCRC();
        }

        public static void iCSC_ISOCommandContact(byte[] BufIN, byte LnIN, byte Case) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(LnIN + 5);
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 20;
            giCSCTrame[4] = 0;
            giCSCTrame[5] = (byte)(LnIN + 1);

            for(int i = 0; i < (LnIN & 255); ++i) {
                giCSCTrame[i + 6] = BufIN[i];
            }

            giCSCTrame[LnIN + 6] = Case;
            giCSCTrame[LnIN + 7] = 0;
            giCSCTrameLn = LnIN + 8;
            icsc_SetCRC();
        }

        public static void iCSC_SelectSAM(byte N_SAM, byte Type) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 25;
            giCSCTrame[4] = N_SAM;
            giCSCTrame[5] = Type;
            gCurrentSAM = N_SAM;
            gSAM_Prot[gCurrentSAM] = Type;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCSC_ResetSAM(byte N_SAM) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 19;
            giCSCTrame[4] = N_SAM;
            giCSCTrame[5] = (byte)(gSAM_Prot[gCurrentSAM] == 0 ? 1 : 0);
            giCSCTrame[6] = (byte)(gSAM_Prot[gCurrentSAM] == 1 ? 1 : (gSAM_Prot[gCurrentSAM] == 2 ? 2 : 0));
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        public static void iCSC_SendToSAM(byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 4);
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 20;
            giCSCTrame[4] = 1;
            giCSCTrame[5] = (byte)(DataLen + 1);

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 6] = Data[i];
            }

            giCSCTrame[DataLen + 7 & 255] = 0;
            giCSCTrameLn = DataLen + 8 & 255;
            icsc_SetCRC();
        }

        public static void iCSC_SendISO7816ToSAM(byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 5);
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 20;
            giCSCTrame[4] = 0;
            giCSCTrame[5] = (byte)(DataLen + 1);

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 6] = Data[i];
            }

            giCSCTrame[DataLen + 6 & 255] = 3;
            giCSCTrame[DataLen + 7 & 255] = 0;
            giCSCTrameLn = DataLen + 8 & 255;
            icsc_SetCRC();
        }

        public static void iCSC_SwitchSignals(byte signals) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 24;
            giCSCTrame[4] = signals;
            giCSCTrame[5] = 0;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCSC_ConfigIoExt(byte inputMask, byte enablePullUp, byte enableFilter, byte outputMask, byte outputDefaultValue, byte outputEnableOpenDrain, byte outputEnablePullUp) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 9;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 36;
            giCSCTrame[4] = inputMask;
            giCSCTrame[5] = enablePullUp;
            giCSCTrame[6] = enableFilter;
            giCSCTrame[7] = outputMask;
            giCSCTrame[8] = outputDefaultValue;
            giCSCTrame[9] = outputEnableOpenDrain;
            giCSCTrame[10] = outputEnablePullUp;
            giCSCTrame[11] = 0;
            giCSCTrameLn = 12;
            icsc_SetCRC();
        }

        public static void iCSC_ReadIoExt(byte ioToRead) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 37;
            giCSCTrame[4] = ioToRead;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCSC_WriteIoExt(byte ioToWrite, byte newVal) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 38;
            giCSCTrame[4] = ioToWrite;
            giCSCTrame[5] = newVal;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCSC_SamPps(byte proProt, byte paramFd) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 1;
            giCSCTrame[3] = 39;
            giCSCTrame[4] = proProt;
            giCSCTrame[5] = paramFd;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCSC_WriteEeprom(byte index, byte value) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 0;
            giCSCTrame[3] = 7;
            giCSCTrame[4] = index;
            giCSCTrame[5] = value;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCSC_ReadEeprom(byte index) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 0;
            giCSCTrame[3] = 8;
            giCSCTrame[4] = index;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iMIFARE_Authenticate(byte NumSector, byte KeyAorB, byte KeyIndex) {
            int j = 0;
             j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 5;
            giCSCTrame[j++] = 3;
            giCSCTrame[j++] = KeyAorB;
            giCSCTrame[j++] = NumSector;
            giCSCTrame[j++] = KeyIndex;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_Halt() {
            int j = 0;
             j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 9;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_LoadReaderKeyIndex(byte KeyIndex, byte[] KeyVal) {
            int j = 0;
            int i;
               if (KeyIndex < 31) {
                j = j + 1;
                giCSCTrame[j] = -128;
                giCSCTrame[j++] = 0;
                giCSCTrame[j++] = 16;
                giCSCTrame[j++] = 1;
                giCSCTrame[j++] = 8;
                giCSCTrame[j++] = 6;
                giCSCTrame[j++] = KeyIndex;

                for(i = 0; i < 6; ++j) {
                    giCSCTrame[j] = KeyVal[i];
                    ++i;
                }
            } else {
                j = j + 1;
                giCSCTrame[j] = -128;
                giCSCTrame[j++] = 0;
                giCSCTrame[j++] = 16;
                giCSCTrame[j++] = 1;
                giCSCTrame[j++] = 7;
                giCSCTrame[j++] = 11;

                for(i = 0; i < 6; ++j) {
                    giCSCTrame[j] = KeyVal[5 - i];
                    ++i;
                }
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_ReadBlock(byte NumBlock) {
            int j = 0;
             j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 1;
            giCSCTrame[j++] = NumBlock;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_ReadSector(byte NumSector, byte KeyAorB, byte KeyIndex) {
            int j = 0;
             j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 7;
            giCSCTrame[j++] = 3;
            giCSCTrame[j++] = KeyAorB;
            giCSCTrame[j++] = NumSector;
            giCSCTrame[j++] = KeyIndex;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_WriteBlock(byte NumBlock, byte[] DataToWrite) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 8;
            giCSCTrame[j++] = 17;
            giCSCTrame[j++] = NumBlock;

            for(int i = 0; i < 16; ++j) {
                giCSCTrame[j] = DataToWrite[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_DecrementValue(byte NumBlock, byte[] Substract) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 11;
            giCSCTrame[j++] = 5;
            giCSCTrame[j++] = NumBlock;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = Substract[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_IncrementValue(byte NumBlock, byte[] Addition) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 10;
            giCSCTrame[j++] = 5;
            giCSCTrame[j++] = NumBlock;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = Addition[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_BackUpRestoreValue(byte Origine, byte Destination) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 12;
            giCSCTrame[j++] = 2;
            giCSCTrame[j++] = Origine;
            giCSCTrame[j++] = Destination;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_Send_Nxp_Write_RF_Chip_Register_in_EEPROM(byte addr, byte data) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 16;
            giCSCTrame[j++] = 1;
            giCSCTrame[j++] = 3;
            giCSCTrame[j++] = 18;
            giCSCTrame[j++] = addr;
            giCSCTrame[j++] = data;
            giCSCTrame[j++] = 0;
            giCSCTrameLn = j;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_Authenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock, byte lgDiversifier, byte blockDiversifier) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 0;
            giCSCTrame[2] = 20;
            giCSCTrame[3] = 1;
            giCSCTrame[4] = numKey;
            giCSCTrame[5] = versionKey;
            giCSCTrame[6] = keyAorB;
            giCSCTrame[7] = numBlock;
            giCSCTrame[8] = lgDiversifier;
            giCSCTrame[9] = blockDiversifier;
            giCSCTrame[10] = 0;
            giCSCTrameLn = 11;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_ReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock, byte lgDiversifier, byte blockDiversifier) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 2;
            giCSCTrame[j++] = numKey;
            giCSCTrame[j++] = versionKey;
            giCSCTrame[j++] = keyAorB;
            giCSCTrame[j++] = numBlock;
            giCSCTrame[j++] = lgDiversifier;
            giCSCTrame[j++] = blockDiversifier;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_ReadBlock(byte numBlock) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 3;
            giCSCTrame[j++] = numBlock;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_WriteBlock(byte numBlock, byte[] dataToWrite) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 4;
            giCSCTrame[j++] = numBlock;

            for(int i = 0; i < 16; ++j) {
                giCSCTrame[j] = dataToWrite[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_ChangeKey(byte numKey, byte versionKeyA, byte versionKeyB, byte[] defaultAccess, byte numBlock, byte lgDiversifier, byte blockDiversifier) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 5;
            giCSCTrame[j++] = numKey;
            giCSCTrame[j++] = versionKeyA;
            giCSCTrame[j++] = versionKeyB;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = defaultAccess[i];
                ++i;
            }

            giCSCTrame[j++] = numBlock;
            giCSCTrame[j++] = lgDiversifier;
            giCSCTrame[j++] = blockDiversifier;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_Increment(byte numBlock, byte[] increment) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = numBlock;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = increment[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_Decrement(byte numBlock, byte[] decrement) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 7;
            giCSCTrame[j++] = numBlock;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = decrement[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_BackUpValue(byte source, byte destination) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 8;
            giCSCTrame[j++] = source;
            giCSCTrame[j++] = destination;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_SAMNXP_KillAuthentication() {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 20;
            giCSCTrame[j++] = 9;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_Identify_Type() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 0;
            giCSCTrame[4] = 0;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_Read(byte add, byte nb) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 34;
            giCSCTrame[4] = add;
            giCSCTrame[5] = nb;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_Write(byte add, byte dataLen, byte[] dataToWrite) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(dataLen + 4);
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 35;
            giCSCTrame[4] = add;
            giCSCTrame[5] = dataLen;

            for(int i = 0; i < (dataLen & 255); ++i) {
                giCSCTrame[i + 6] = dataToWrite[i];
            }

            giCSCTrame[6 + dataLen] = 0;
            giCSCTrameLn = 6 + dataLen + 1;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_C_Authenticate(byte keyNo, byte keyV, byte divLength, byte divInput) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 36;
            giCSCTrame[4] = keyNo;
            giCSCTrame[5] = keyV;
            giCSCTrame[6] = divLength;
            giCSCTrame[7] = divInput;
            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_C_Write_Key_From_Sam(byte keyNo, byte keyV, byte divLength, byte divInput) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 37;
            giCSCTrame[4] = keyNo;
            giCSCTrame[5] = keyV;
            giCSCTrame[6] = divLength;
            giCSCTrame[7] = divInput;
            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Pass_Authenticat(byte[] pwd) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 38;

            for(int i = 0; i < 4; ++i) {
                giCSCTrame[4 + i] = pwd[i];
            }

            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Create_Divers_Password_Pack(byte keyNo, byte keyV, byte divLength, byte divInput) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 43;
            giCSCTrame[4] = keyNo;
            giCSCTrame[5] = keyV;
            giCSCTrame[6] = divLength;
            giCSCTrame[7] = divInput;
            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Read_Counter(byte add) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 39;
            giCSCTrame[4] = add;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Increment_Counter(byte add, byte[] incrementValue) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 40;
            giCSCTrame[4] = add;

            for(int i = 0; i < 3; ++i) {
                giCSCTrame[5 + i] = incrementValue[i];
            }

            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Get_Version() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 2;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 41;
            giCSCTrame[4] = 0;
            giCSCTrameLn = 5;
            icsc_SetCRC();
        }

        public static void iMIFARE_UL_EV1_Check_Tearing_Effect(byte add) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 22;
            giCSCTrame[3] = 42;
            giCSCTrame[4] = add;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iGEN_AppendRecord(byte AccMode, byte SID, long LID, byte NKEY, byte RUF, byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 9);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 1;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 7] = Data[i];
            }

            giCSCTrame[DataLen + 7] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[DataLen + 8] = (byte)((int)(LID & 255L));
            giCSCTrame[DataLen + 9] = NKEY;
            giCSCTrame[DataLen + 10] = RUF;
            giCSCTrame[DataLen + 11] = 0;
            giCSCTrameLn = DataLen + 12;
            icsc_SetCRC();
        }

        public static void iGEN_CancelPurchase(byte Type, byte[] DataLog, byte[] Disp) {
            giCSCTrame[0] = -128;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 19;
            giCSCTrame[4] = Type;

            int i;
            for(i = 0; i < 7; ++i) {
                giCSCTrame[i + 5] = DataLog[i];
            }

            if (Type == 0) {
                giCSCTrame[1] = 10;
                giCSCTrame[12] = 0;
                giCSCTrameLn = 13;
            } else {
                for(i = 0; i < 6; ++i) {
                    giCSCTrame[i + 12] = Disp[i];
                }

                giCSCTrame[1] = 16;
                giCSCTrame[18] = 0;
                giCSCTrameLn = 19;
            }

            icsc_SetCRC();
        }

        public static void iCD97_SelectFile(byte SelectMode, byte[] IdPath, byte IdPathLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(IdPathLen + 4);
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 8;
            giCSCTrame[4] = SelectMode;
            giCSCTrame[5] = IdPathLen;

            for(int i = 0; i < (IdPathLen & 255); ++i) {
                giCSCTrame[i + 6] = IdPath[i];
            }

            giCSCTrame[IdPathLen + 6] = 0;
            giCSCTrameLn = IdPathLen + 7;
            icsc_SetCRC();
        }

        public static void iCD97_StatusFile(byte SelectMode, byte[] IdPath, byte IdPathLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(IdPathLen + 4);
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 9;
            giCSCTrame[4] = SelectMode;
            giCSCTrame[5] = IdPathLen;

            for(int i = 0; i < (IdPathLen & 255); ++i) {
                giCSCTrame[i + 6] = IdPath[i];
            }

            giCSCTrame[IdPathLen + 6] = 0;
            giCSCTrameLn = IdPathLen + 7;
            icsc_SetCRC();
        }

        public static void iCD97_Invalidate(byte AccMode) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 5;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCD97_Rehabilitate(byte AccMode) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 7;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCD97_ChangeKey(byte KeyIndex, byte NewVersion) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 2;
            giCSCTrame[4] = KeyIndex;
            giCSCTrame[5] = NewVersion;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCD97_ChangePIN(byte[] OldPIN, byte[] NewPIN) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 2;
            giCSCTrame[4] = 4;
            giCSCTrame[5] = 0;

            int i;
            for(i = 0; i < 4; ++i) {
                giCSCTrame[i + 6] = OldPIN[i];
            }

            for(i = 0; i < 4; ++i) {
                giCSCTrame[i + 10] = NewPIN[i];
            }

            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iCD97_VerifyPIN(byte[] PIN) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 7;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 11;
            giCSCTrame[4] = 1;

            for(int i = 0; i < 4; ++i) {
                giCSCTrame[i + 5] = PIN[i];
            }

            giCSCTrame[9] = 0;
            giCSCTrameLn = 10;
            icsc_SetCRC();
        }

        public static void iCD97_Increase(byte AccMode, byte SID, long Value) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 4;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)((int)(Value / 65536L & 255L));
            giCSCTrame[7] = (byte)((int)(Value / 256L & 255L));
            giCSCTrame[8] = (byte)((int)(Value & 255L));

            for(int i = 0; i < 5; ++i) {
                giCSCTrame[i + 9] = 0;
            }

            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iCD97_Decrease(byte AccMode, byte SID, int Value) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 3;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)(Value / 65536 & 255);
            giCSCTrame[7] = (byte)(Value / 256 & 255);
            giCSCTrame[8] = (byte)(Value & 255);

            for(int i = 0; i < 5; ++i) {
                giCSCTrame[i + 9] = 0;
            }

            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iCD97_ReadRecord(byte AccMode, byte SID, byte NuRec, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 6;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 6;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;
            giCSCTrame[8] = 0;
            giCSCTrameLn = 9;
            icsc_SetCRC();
        }

        public static void iCD97_AppendRecord(byte AccMode, byte SID, byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 5);
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 1;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 7] = Data[i];
            }

            giCSCTrame[DataLen + 7] = 0;
            giCSCTrameLn = DataLen + 8;
            icsc_SetCRC();
        }

        public static void iCD97_UpdateRecord(byte AccMode, byte SID, byte NuRec, byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 6);
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 10;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 8] = Data[i];
            }

            giCSCTrame[DataLen + 8] = 0;
            giCSCTrameLn = DataLen + 9;
            icsc_SetCRC();
        }

        public static void iCD97_WriteRecord(byte AccMode, byte SID, byte NuRec, byte[] Data, byte DataLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 6);
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 12;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 8] = Data[i];
            }

            giCSCTrame[DataLen + 8] = 0;
            giCSCTrameLn = DataLen + 9;
            icsc_SetCRC();
        }

        public static void iCD97_OpenSecuredSession(byte Type, byte SID, byte NREC) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 16;
            giCSCTrame[4] = Type;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NREC;
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        public static void iCD97_CloseSecuredSession() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 2;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 17;
            giCSCTrame[4] = 0;
            giCSCTrameLn = 5;
            icsc_SetCRC();
        }

        public static void iCD97_Purchase(byte Type, byte[] DataLog, byte[] Disp) {
            giCSCTrame[0] = -128;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 13;
            giCSCTrame[4] = Type;

            int i;
            for(i = 0; i < 7; ++i) {
                giCSCTrame[i + 5] = DataLog[i];
            }

            if (Type == 0) {
                giCSCTrame[1] = 10;
                giCSCTrame[12] = 0;
                giCSCTrameLn = 13;
            } else {
                for(i = 0; i < 6; ++i) {
                    giCSCTrame[i + 12] = Disp[i];
                }

                giCSCTrame[1] = 16;
                giCSCTrame[18] = 0;
                giCSCTrameLn = 19;
            }

            icsc_SetCRC();
        }

        public static void iCD97_GetEPStatus(byte Type) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 14;
            giCSCTrame[4] = Type;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCD97_ReloadEP(byte[] ChargLog1, byte[] ChargLog2) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 15;

            int i;
            for(i = 0; i < 5; ++i) {
                giCSCTrame[i + 4] = ChargLog1[i];
            }

            for(i = 0; i < 5; ++i) {
                giCSCTrame[i + 9] = ChargLog2[i];
            }

            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iCD97_CancelPurchase(byte Type, byte[] DataLog, byte[] Disp) {
            giCSCTrame[0] = -128;
            giCSCTrame[2] = 3;
            giCSCTrame[3] = 19;
            giCSCTrame[4] = Type;

            int i;
            for(i = 0; i < 7; ++i) {
                giCSCTrame[i + 5] = DataLog[i];
            }

            if (Type == 0) {
                giCSCTrame[1] = 10;
                giCSCTrame[12] = 0;
                giCSCTrameLn = 13;
            } else {
                for(i = 0; i < 6; ++i) {
                    giCSCTrame[i + 12] = Disp[i];
                }

                giCSCTrame[1] = 16;
                giCSCTrame[18] = 0;
                giCSCTrameLn = 19;
            }

            icsc_SetCRC();
        }

        public static void iCSC_SelectCID(byte CID) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 1;
            giCSCTrame[j++] = 21;
            giCSCTrame[j++] = CID;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iCSC_SelectDIV(byte Slot, byte Prot, byte[] DIV) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 1;
            giCSCTrame[j++] = 22;
            giCSCTrame[j++] = Slot;
            giCSCTrame[j++] = Prot;

            for(int i = 0; i < 4; ++j) {
                giCSCTrame[j] = DIV[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iCSC_WriteSAMNumber(byte N_SAM) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 0;
            giCSCTrame[3] = 6;
            giCSCTrame[4] = N_SAM;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCSC_ChangeCSCSpeed(byte RS232Speed) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 0;
            giCSCTrame[3] = 4;
            giCSCTrame[4] = RS232Speed;
            giCSCTrame[5] = 12;
            giCSCTrame[6] = 12;
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        public static void iCTx_Active() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 2;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 1;
            giCSCTrame[4] = 0;
            giCSCTrameLn = 5;
            icsc_SetCRC();
        }

        public static void iCTx_Read(byte ADD, byte NB) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 2;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCTx_Update(byte ADD, byte NB, byte[] Data, byte[] DataInCTS) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(2 * NB + 4);
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 3;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;

            int i;
            for(i = 0; i < (NB & 255); ++i) {
                giCSCTrame[i + 6] = Data[i];
            }

            for(i = 0; i < (NB & 255); ++i) {
                giCSCTrame[i + NB + 6] = DataInCTS[i];
            }

            giCSCTrame[NB + NB + 6] = 0;
            giCSCTrameLn = 2 * NB + 7;
            icsc_SetCRC();
        }

        public static void iCTx_Release(byte Param) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 4;
            giCSCTrame[4] = Param;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCTx_512B_Halt(byte Param) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 36;
            giCSCTrame[4] = Param;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCTX_512B_List(byte RFU) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 32;
            giCSCTrame[4] = RFU;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        static void iCTX_512B_Select(byte[] serialNumber) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 33;
            giCSCTrame[4] = serialNumber[0];
            giCSCTrame[5] = serialNumber[1];
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCTx_512B_Read(byte ADD, byte NB) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 34;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCTx_512B_Update(byte ADD, byte NB, byte[] data) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(NB + 4);
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 35;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;

            for(int i = 0; i < (NB & 255); ++i) {
                giCSCTrame[i + 6] = data[i];
            }

            giCSCTrame[NB + 6] = 0;
            giCSCTrameLn = NB + 7;
            icsc_SetCRC();
        }

        public static void iCTX_512X_List(byte RFU) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 32;
            giCSCTrame[4] = RFU;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        static void iCTX_512X_Select(byte[] serialNumber) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 33;
            giCSCTrame[4] = serialNumber[0];
            giCSCTrame[5] = serialNumber[1];
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCTx_512X_Read(byte ADD, byte NB) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 34;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iCTx_512X_Update(byte ADD, byte NB, byte[] data) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(NB + 4);
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 35;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;

            for(int i = 0; i < (NB & 255); ++i) {
                giCSCTrame[i + 6] = data[i];
            }

            giCSCTrame[NB + 6] = 0;
            giCSCTrameLn = NB + 7;
            icsc_SetCRC();
        }

        public static void iCTx_512X_Halt(byte Param) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 36;
            giCSCTrame[4] = Param;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iCTx_512X_Write(byte ADD, byte NB, byte[] data) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(NB + 4);
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 38;
            giCSCTrame[4] = ADD;
            giCSCTrame[5] = NB;

            for(int i = 0; i < (NB & 255); ++i) {
                giCSCTrame[i + 6] = data[i];
            }

            giCSCTrame[NB + 6] = 0;
            giCSCTrameLn = NB + 7;
            icsc_SetCRC();
        }

        public static void iCTx_512X_Authenticate(byte address, byte kif_kref, byte kvc_zero) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 39;
            giCSCTrame[4] = address;
            giCSCTrame[5] = kif_kref;
            giCSCTrame[6] = kvc_zero;
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        public static void iCTx_512X_WriteKey(byte kif_kref, byte kvc_zero) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 4;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 40;
            giCSCTrame[4] = kif_kref;
            giCSCTrame[5] = kvc_zero;
            giCSCTrame[6] = 0;
            giCSCTrameLn = 7;
            icsc_SetCRC();
        }

        public static void iGEN_Decrease(byte AccMode, byte SID, long lID, byte ICount, int Value, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 3;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)(Value / 65536 & 255);
            giCSCTrame[7] = (byte)(Value / 256 & 255);
            giCSCTrame[8] = (byte)(Value & 255);
            giCSCTrame[9] = (byte)((int)(lID / 256L & 255L));
            giCSCTrame[10] = (byte)((int)(lID & 255L));
            giCSCTrame[11] = ICount;
            giCSCTrame[12] = NKEY;
            giCSCTrame[13] = RUF;
            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iGEN_GetEPStatus(byte Type, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 14;
            giCSCTrame[4] = Type;
            giCSCTrame[5] = NKEY;
            giCSCTrame[6] = RUF;
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        static void iGEN_GiveCertificate(byte KeyType, byte Param, byte LngBuffer, byte[] Buffer, byte LngCertificat) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(6 + LngBuffer);
            giCSCTrame[2] = 4;
            giCSCTrame[3] = 5;
            giCSCTrame[4] = KeyType;
            giCSCTrame[5] = Param;
            giCSCTrame[6] = LngBuffer;

            for(int i = 0; i < (LngBuffer & 255); ++i) {
                giCSCTrame[7 + i] = Buffer[i];
            }

            giCSCTrame[7 + LngBuffer] = LngCertificat;
            giCSCTrame[8 + LngBuffer] = 0;
            giCSCTrameLn = 9 + LngBuffer;
            icsc_SetCRC();
        }

        public static void iCD97_ToGTML() {
            if (giCSCTrameLn >= 5) {
                giCSCTrame[2] = 2;
                giCSCTrameLn -= 2;
                icsc_SetCRC();
            }

        }

        public static void iGEN_Increase(byte AccMode, byte SID, long lID, byte ICount, int Value, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 4;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)(Value / 65536 & 255);
            giCSCTrame[7] = (byte)(Value / 256 & 255);
            giCSCTrame[8] = (byte)(Value & 255);
            giCSCTrame[9] = (byte)((int)(lID / 256L & 255L));
            giCSCTrame[10] = (byte)((int)(lID & 255L));
            giCSCTrame[11] = ICount;
            giCSCTrame[12] = NKEY;
            giCSCTrame[13] = RUF;
            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iGEN_Invalidate(byte AccMode, long lID, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 7;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 5;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = (byte)((int)(lID / 256L & 255L));
            giCSCTrame[6] = (byte)((int)(lID & 255L));
            giCSCTrame[7] = NKEY;
            giCSCTrame[8] = RUF;
            giCSCTrame[9] = 0;
            giCSCTrameLn = 10;
            icsc_SetCRC();
        }

        public static void iGEN_Lock_Unlock(byte Type) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 3;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 22;
            giCSCTrame[4] = Type;
            giCSCTrame[5] = 0;
            giCSCTrameLn = 6;
            icsc_SetCRC();
        }

        public static void iGEN_MultiDecrease(byte AccMode, byte SID, long LID, byte NKEY, byte RUF, byte NbCnt, byte[] Data) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(NbCnt * 4 + 9);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 20;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[7] = (byte)((int)(LID & 255L));
            giCSCTrame[8] = NKEY;
            giCSCTrame[9] = RUF;
            giCSCTrame[10] = NbCnt;

            for(int i = 0; i < (NbCnt & 255) * 4; ++i) {
                giCSCTrame[i + 11] = Data[i];
            }

            giCSCTrame[NbCnt * 4 + 11] = 0;
            giCSCTrameLn = NbCnt * 4 + 12;
            icsc_SetCRC();
        }

        public static void iGEN_MultiIncrease(byte AccMode, byte SID, long LID, long NKEY, byte RUF, byte NbCnt, byte[] Data) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(NbCnt * 4 + 9);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 21;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[7] = (byte)((int)(LID & 255L));
            giCSCTrame[8] = (byte)((int)NKEY);
            giCSCTrame[9] = RUF;
            giCSCTrame[10] = NbCnt;

            for(int i = 0; i < (NbCnt & 255) * 4; ++i) {
                giCSCTrame[i + 11] = Data[i];
            }

            giCSCTrame[NbCnt * 4 + 11] = 0;
            giCSCTrameLn = NbCnt * 4 + 12;
            icsc_SetCRC();
        }

        public static void iGEN_OpenSecuredSession(byte Type, byte SID, byte NREC, byte NKEY, byte RUF, byte Mode) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 8;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 16;
            giCSCTrame[4] = Type;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NREC;
            giCSCTrame[7] = NKEY;
            giCSCTrame[8] = RUF;
            giCSCTrame[9] = Mode;
            giCSCTrame[10] = 0;
            giCSCTrameLn = 11;
            icsc_SetCRC();
        }

        public static void iGEN_PINStatus() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 9;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 11;
            giCSCTrame[4] = 0;

            for(int i = 0; i < 4; ++i) {
                giCSCTrame[i + 5] = 0;
            }

            giCSCTrame[9] = 0;
            giCSCTrame[10] = 0;
            giCSCTrame[11] = 0;
            giCSCTrameLn = 12;
            icsc_SetCRC();
        }

        public static void iGEN_Purchase(byte Type, byte[] DataLog, byte[] Disp) {
            giCSCTrame[0] = -128;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 13;
            giCSCTrame[4] = Type;

            int i;
            for(i = 0; i < 7; ++i) {
                giCSCTrame[i + 5] = DataLog[i];
            }

            if (Type == 0) {
                giCSCTrame[1] = 10;
                giCSCTrame[12] = 0;
                giCSCTrameLn = 13;
            } else {
                for(i = 0; i < 6; ++i) {
                    giCSCTrame[i + 12] = Disp[i];
                }

                giCSCTrame[1] = 16;
                giCSCTrame[18] = 0;
                giCSCTrameLn = 19;
            }

            icsc_SetCRC();
        }

        public static void iGEN_ReadRecord(byte AccMode, byte SID, byte NuRec, byte DataLen, long LID, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 10;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 6;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;
            giCSCTrame[8] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[9] = (byte)((int)(LID & 255L));
            giCSCTrame[10] = NKEY;
            giCSCTrame[11] = RUF;
            giCSCTrame[12] = 0;
            giCSCTrameLn = 13;
            icsc_SetCRC();
        }

        public static void iGEN_Rehabilitate(byte AccMode, long LID, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 7;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 7;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[6] = (byte)((int)(LID & 255L));
            giCSCTrame[7] = NKEY;
            giCSCTrame[8] = RUF;
            giCSCTrame[9] = 0;
            giCSCTrameLn = 10;
            icsc_SetCRC();
        }

        public static void iGEN_ReloadEP(byte[] ChargLog1, byte[] ChargLog2) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 12;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 15;

            int i;
            for(i = 0; i < 5; ++i) {
                giCSCTrame[i + 4] = ChargLog1[i];
            }

            for(i = 0; i < 5; ++i) {
                giCSCTrame[i + 9] = ChargLog2[i];
            }

            giCSCTrame[14] = 0;
            giCSCTrameLn = 15;
            icsc_SetCRC();
        }

        public static void iGEN_SelectFile(byte SelectMode, byte[] IdPath, byte IdPathLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(IdPathLen + 4);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 8;
            giCSCTrame[4] = SelectMode;
            giCSCTrame[5] = IdPathLen;

            for(int i = 0; i < (IdPathLen & 255); ++i) {
                giCSCTrame[i + 6] = IdPath[i];
            }

            giCSCTrame[IdPathLen + 6] = 0;
            giCSCTrameLn = IdPathLen + 7;
            icsc_SetCRC();
        }

        public static void iSRx_Active() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 2;
            giCSCTrame[2] = 6;
            giCSCTrame[3] = 49;
            giCSCTrame[4] = 0;
            giCSCTrameLn = 5;
            icsc_SetCRC();
        }

        public static void iSRx_ReadBlocks(byte NumBlock, byte NbBlocks) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 50;
            giCSCTrame[j++] = NumBlock;
            giCSCTrame[j++] = NbBlocks;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iSRx_WriteBlocks(byte NumBlock, byte NbBlocks, byte dataLen, byte[] DataToWrite) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 51;
            giCSCTrame[j++] = NumBlock;
            giCSCTrame[j++] = NbBlocks;

            for(int i = 0; i < (dataLen & 255); ++j) {
                giCSCTrame[j] = DataToWrite[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iSRx_Release(byte pParam) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 52;
            giCSCTrame[j++] = pParam;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iSRx_Read(short pAdd, byte pNumBytes) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 53;
            giCSCTrame[j++] = (byte)(pAdd & 255);
            giCSCTrame[j++] = (byte)((pAdd & '\uff00') >> 8);
            giCSCTrame[j++] = pNumBytes;
            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iSRx_Write(short pAdd, byte pNumBytes, byte[] pDataToWrite) {
            int j = 0;
            j = j + 1;
            giCSCTrame[j] = -128;
            giCSCTrame[j++] = 0;
            giCSCTrame[j++] = 6;
            giCSCTrame[j++] = 54;
            giCSCTrame[j++] = (byte)(pAdd & 255);
            giCSCTrame[j++] = (byte)((pAdd & '\uff00') >> 8);
            giCSCTrame[j++] = pNumBytes;

            for(int i = 0; i < (pNumBytes & 255); ++j) {
                giCSCTrame[j] = pDataToWrite[i];
                ++i;
            }

            giCSCTrame[j] = 0;
            giCSCTrameLn = j + 1;
            giCSCTrame[1] = (byte)(giCSCTrameLn - 3);
            icsc_SetCRC();
        }

        public static void iGEN_StatusFile(byte SelectMode, byte[] IdPath, byte IdPathLen) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(IdPathLen + 4);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 9;
            giCSCTrame[4] = SelectMode;
            giCSCTrame[5] = IdPathLen;

            for(int i = 0; i < (IdPathLen & 255); ++i) {
                giCSCTrame[i + 6] = IdPath[i];
            }

            giCSCTrame[IdPathLen + 6] = 0;
            giCSCTrameLn = IdPathLen + 7;
            icsc_SetCRC();
        }

        public static void iGEN_UpdateRecord(byte AccMode, byte SID, byte NuRec, byte[] Data, byte DataLen, long LID, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 10);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 10;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 8] = Data[i];
            }

            giCSCTrame[DataLen + 8] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[DataLen + 9] = (byte)((int)(LID & 255L));
            giCSCTrame[DataLen + 10] = NKEY;
            giCSCTrame[DataLen + 11] = RUF;
            giCSCTrame[DataLen + 12] = 0;
            giCSCTrameLn = DataLen + 13;
            icsc_SetCRC();
        }

        public static void iGEN_WriteRecord(byte AccMode, byte SID, byte NuRec, byte[] Data, byte DataLen, long LID, byte NKEY, byte RUF) {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = (byte)(DataLen + 10);
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 12;
            giCSCTrame[4] = AccMode;
            giCSCTrame[5] = SID;
            giCSCTrame[6] = NuRec;
            giCSCTrame[7] = DataLen;

            for(int i = 0; i < (DataLen & 255); ++i) {
                giCSCTrame[i + 8] = Data[i];
            }

            giCSCTrame[DataLen + 8] = (byte)((int)(LID / 256L & 255L));
            giCSCTrame[DataLen + 9] = (byte)((int)(LID & 255L));
            giCSCTrame[DataLen + 10] = NKEY;
            giCSCTrame[DataLen + 11] = RUF;
            giCSCTrame[DataLen + 12] = 0;
            giCSCTrameLn = DataLen + 13;
            icsc_SetCRC();
        }

        public static void iGEN_VerifyPIN(byte[] PIN, byte NKEY, byte RUF) {
            byte mode;
            if (NKEY == 0) {
                mode = 2;
            } else {
                mode = 1;
            }

            giCSCTrame[0] = -128;
            giCSCTrame[1] = 9;
            giCSCTrame[2] = 5;
            giCSCTrame[3] = 11;
            giCSCTrame[4] = mode;

            for(int i = 0; i < 4; ++i) {
                giCSCTrame[i + 5] = PIN[i];
            }

            giCSCTrame[9] = NKEY;
            giCSCTrame[10] = RUF;
            giCSCTrame[11] = 0;
            giCSCTrameLn = 12;
            icsc_SetCRC();
        }

        public static void csc_loadIsoAConfig() {
            giCSCTrame[0] = -128;
            giCSCTrame[1] = 5;
            giCSCTrame[2] = 16;
            giCSCTrame[3] = 1;
            giCSCTrame[4] = 2;
            giCSCTrame[5] = 12;
            giCSCTrame[6] = 1;
            giCSCTrame[7] = 0;
            giCSCTrameLn = 8;
            icsc_SetCRC();
        }

        static {
            giCSCTrame = new byte[kiCSCMaxTrame];
            gSAM_Prot = new byte[5];
        }
    }
