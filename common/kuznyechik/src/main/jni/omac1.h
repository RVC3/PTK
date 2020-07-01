#ifndef _OMAC1_H_
#define _OMAC1_H_

#define BLOCK_SIZE 16

#include "kuznyechik.h"

typedef struct _container {
  uint8_t K1[BLOCK_SIZE];
  uint8_t K2[BLOCK_SIZE];
} container;

#define MSB(x) (((x)[0] & 0x80) ? 1 : 0)

static uint8_t ZEROS[BLOCK_SIZE] = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

bool omac1_init(container * _container, kuznyechik* _kuznyechik);
void omac1_encrypt(container * _container, kuznyechik* _kuznyechik, const uint8_t * msg, int msg_len, uint8_t * ct);

#endif