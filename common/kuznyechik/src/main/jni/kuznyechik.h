//
// Created by nevolin on 11.07.2017.
//

#ifndef PROJECT_KUZNYECHIK_H
#define PROJECT_KUZNYECHIK_H

#include "block_cipher.h"

class kuznyechik : public block_cipher {
	public:
		~kuznyechik();

		size_t blocksize() const override { return 128; }
		size_t keysize() const override { return 256; }
		kuznyechik* clone() const override { return new kuznyechik; }
		void clear() override;

		bool init(const uint8_t* key, block_cipher::direction direction) override;
		void encrypt_block(const uint8_t* in, uint8_t* out) override;
		void decrypt_block(const uint8_t* in, uint8_t* out) override;

	private:
		uint64_t rk[10][2];
};


#endif //PROJECT_KUZNYECHIK_H
