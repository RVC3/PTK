/*
This code is written by kerukuro for cppcrypto library (http://cppcrypto.sourceforge.net/)
and released into public domain.
*/

#include "block_cipher.h"

block_cipher::~block_cipher()
{
}

void block_cipher::encrypt_blocks(const uint8_t* in, uint8_t* out, size_t n)
{
	size_t bs = blocksize();
	for (size_t i = 0; i < n; i++)
	{
		encrypt_block(in, out);
		in += bs;
		out += bs;
	}
}

void block_cipher::decrypt_blocks(const uint8_t* in, uint8_t* out, size_t n)
{
	size_t bs = blocksize();
	for (size_t i = 0; i < n; i++)
	{
		decrypt_block(in, out);
		in += bs;
		out += bs;
	}
}