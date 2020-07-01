package ru.ppr.inpas.lib.protocol.model;

/**
 * Поддерживаемые сигналы согласно протоколу SA в HEX представлении.
 */
public enum PosSignalType {
    SOH(0x01),
    STX(0x02),
    EOT(0x04),
    ENQ(0x05),
    ACK(0x06),
    NAK(0x15);

    private final int mValue;

    PosSignalType(final int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public byte getAsByte() {
        return (byte) mValue;
    }

}