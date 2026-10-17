#ifndef AFP_HOST_H
#define AFP_HOST_H
#include <stddef.h>
#include <stdint.h>

/* Each call owns and releases a fresh Wasm instance. The caller frees output. */
int afp_generate(const float* samples, size_t count, uint8_t** output, size_t* size);
#endif
