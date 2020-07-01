package ru.ppr.kuznyechik;

/**
 * Реализация "Кузнечика" взята целиком у cppcrypto (http://cppcrypto.sourceforge.net)
 * Реализация OMAC1 частично переделана, оригинал (https://github.com/louismullie/cmac-rb)
 *
 * @author Dmitry Nevolin
 */
final class NativeCppcryptoKuznyechik {

    static {
        System.loadLibrary("kuznyechik");
    }

    native boolean init(byte[] key, int direction);

    native void encryptBlock(byte[] in, byte[] out);

    native void decryptBlock(byte[] in, byte[] out);

    native void omac1(byte[] in, byte[] out);

}
