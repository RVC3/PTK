#include <stdio.h>
//#include <string.h>
//#include <android/log.h>
//#include <sstream>

#include "omac1.h"


//constexpr char hexmap[] = {'0', '1', '2', '3', '4', '5', '6', '7',
//                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
//
//static std::string hexStr(unsigned char *data, int len) {
//    std::string s(len * 2, ' ');
//
//    for (int i = 0; i < len; ++i) {
//        s[2 * i] = hexmap[(data[i] & 0xF0) >> 4];
//        s[2 * i + 1] = hexmap[data[i] & 0x0F];
//    }
//
//    return s;
//}
//
//static void log(std::string string) {
//    __android_log_print(ANDROID_LOG_INFO, "ru.ppr.kuznyechik", "%s", string.c_str());
//}
//
//template<typename T>
//static std::string to_string(T value) {
//    std::ostringstream os;
//
//    os << value;
//
//    return os.str();
//}

static void _xor(uint8_t *out, const uint8_t *in) {
    for (int i = 0; i < BLOCK_SIZE; i++) {
        out[i] ^= in[i];
    }
}

static void pad(uint8_t *buf, int len) {
    for (int i = len; i < BLOCK_SIZE; i++) {
        buf[i] = (i == len) ? 0x80 : 0x00;
    }
}

static void left_shift(uint8_t *out, uint8_t *in, uint8_t *overflow) {
    for (int i = BLOCK_SIZE - 1; i >= 0; i--) {
        out[i] = (in[i] << 1) | (*overflow);
        (*overflow) = MSB(&in[i]);
    }
}

static void generate_sub_key(uint8_t *out, uint8_t *in) {
    uint8_t overflow = 0;

    left_shift(out, in, &overflow);

    if (overflow) {
        out[BLOCK_SIZE - 1] ^= 0x87;
    }
}

void
omac1_encrypt(container *_container, kuznyechik *_kuznyechik, const uint8_t *message, int message_length,
        uint8_t *out) {
    uint8_t M[BLOCK_SIZE];
    uint8_t *cursor = (uint8_t *) message;

    memcpy(out, ZEROS, BLOCK_SIZE);
    memset(M, 0, BLOCK_SIZE);

    int n = (message_length + (BLOCK_SIZE - 1)) / BLOCK_SIZE - 1;
    int k = (message_length % BLOCK_SIZE);

    for (int i = 0; i < n; i++) {
        _xor(out, cursor);
        _kuznyechik->encrypt_block(out, out);
        cursor += BLOCK_SIZE;
    }

    if (k == 0) {
        if (message != NULL && message_length != 0) {
            memcpy(M, cursor, BLOCK_SIZE);
            _xor(M, _container->K1);
        } else {
            pad(M, 0);
            _xor(M, _container->K2);
        }
    } else {
        memcpy(M, cursor, k);
        pad(M, k);
        _xor(M, _container->K2);
    }

    _xor(out, M);

    _kuznyechik->encrypt_block(out, out);
}

bool omac1_init(container *_container, kuznyechik *_kuznyechik) {
    uint8_t L[BLOCK_SIZE];

    memset((uint8_t *) _container, 0, sizeof(container));

    _kuznyechik->encrypt_block(ZEROS, L);

    generate_sub_key(_container->K1, L);
    generate_sub_key(_container->K2, _container->K1);

    return true;
}