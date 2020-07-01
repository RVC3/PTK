package ru.ppr.kuznyechik;

/**
 * Обёртка для NativeCppcryptoKuznyechik
 *
 * @author Dmitry Nevolin
 */
public class CppcryptoKuznyechik implements Kuznyechik {

    private final NativeCppcryptoKuznyechik kuznyechik;

    public CppcryptoKuznyechik() {
        kuznyechik = new NativeCppcryptoKuznyechik();
    }

    @Override
    public boolean init(byte[] key, Direction direction) {
        return kuznyechik.init(key, direction.getCode());
    }

    @Override
    public void encryptBlock(byte[] in, byte[] out) {
        kuznyechik.encryptBlock(in, out);
    }

    @Override
    public void decryptBlock(byte[] in, byte[] out) {
        kuznyechik.decryptBlock(in, out);
    }

    @Override
    public void omac1(byte[] in, byte[] out) {
        kuznyechik.omac1(in, out);
    }

}
