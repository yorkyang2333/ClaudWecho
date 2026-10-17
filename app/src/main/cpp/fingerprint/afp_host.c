#include "afp_host.h"
#include "afp.h"
#include "wasm-rt-impl.h"
#include "wasm-rt-exceptions.h"
#include <pthread.h>

struct w2c_a {
    w2c_afp module;
    u32 invoker;
    u32 target;
    bool registering;
};

static pthread_mutex_t runtime_lock = PTHREAD_MUTEX_INITIALIZER;

static uint8_t* memory(struct w2c_a* host, u32 offset, size_t size) {
    wasm_rt_memory_t* mem = w2c_afp_B(&host->module);
    if ((uint64_t)offset + size > mem->size) wasm_rt_trap(WASM_RT_TRAP_OOB);
    return mem->data + offset;
}

static void registration(struct w2c_a* host) {
    if (!host->registering) wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE);
}

/* Only type registration is skipped. The algorithm itself runs unchanged. */
#define REG(name, ...) void w2c_a_##name(struct w2c_a* h, __VA_ARGS__) { registration(h); }
REG(A, u32 a, u32 b, u32 c, u32 d, u32 e, u32 f, u32 g, u32 i, u32 j, u32 k, u32 l, u32 m, u32 n)
REG(a, u32 a, u32 b, u32 c)
REG(b, u32 a, u32 b, u32 c, u32 d, u32 e)
REG(c, u32 a, u32 b, u32 c, u32 d, u32 e, u32 f, u32 g, u32 i)
REG(e, u32 a, u32 b, u32 c)
REG(j, u32 a, u32 b, u32 c)
REG(k, u32 a, u32 b)
REG(p, u32 a, u32 b, u32 c, u32 d, u32 e, u32 f, u32 g)
REG(w, u32 a, u32 b, u32 c, u32 d, u32 e, u32 f)
REG(x, u32 a, u32 b)
REG(y, u32 a, u32 b, u32 c, u32 d, u32 e)
REG(z, u32 a, u32 b)

void w2c_a_l(struct w2c_a* h, u32 name, u32 count, u32 types, u32 signature, u32 invoker, u32 target) {
    registration(h);
    if (!memcmp(memory(h, name, 15), "ExtractQueryFP\0", 15)) {
        if (count != 2 || memcmp(memory(h, signature, 4), "iii\0", 4))
            wasm_rt_trap(WASM_RT_TRAP_CALL_INDIRECT);
        h->invoker = invoker;
        h->target = target;
    }
}

void w2c_a_d(struct w2c_a* h, u32 a, u32 b, u32 c, u32 d) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
void w2c_a_f(struct w2c_a* h, u32 a, u32 b, u32 c) { wasm_rt_trap(WASM_RT_TRAP_UNCAUGHT_EXCEPTION); }
u32 w2c_a_g(struct w2c_a* h, u32 a) { wasm_rt_trap(WASM_RT_TRAP_UNCAUGHT_EXCEPTION); }
void w2c_a_h(struct w2c_a* h) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
void w2c_a_m(struct w2c_a* h, u32 a) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
void w2c_a_n(struct w2c_a* h, u32 a) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
u32 w2c_a_o(struct w2c_a* h, u32 a, u32 b) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
void w2c_a_q(struct w2c_a* h, u32 a) {}
u32 w2c_a_s(struct w2c_a* h, u32 a) { wasm_rt_trap(WASM_RT_TRAP_EXHAUSTION); }
u32 w2c_a_t(struct w2c_a* h, u32 a, u32 b, u32 c, u32 d, u32 e) { wasm_rt_trap(WASM_RT_TRAP_UNREACHABLE); }
u32 w2c_a_u(struct w2c_a* h, u32 a, u32 b) { registration(h); return 0; }
u32 w2c_a_v(struct w2c_a* h, u32 a, u32 b) { registration(h); return 0; }

u32 w2c_a_r(struct w2c_a* h, u32 to, u32 from, u32 size) {
    memmove(memory(h, to, size), memory(h, from, size), size);
    return to;
}

u32 w2c_a_i(struct w2c_a* h, u32 fd, u32 vectors, u32 count, u32 written) {
    /* Discard algorithm diagnostics, but report a successful fd_write. */
    u32 total = 0;
    for (u32 i = 0; i < count; ++i) {
        u32 size;
        memcpy(&size, memory(h, vectors + i * 8 + 4, 4), 4);
        total += size;
    }
    memcpy(memory(h, written, 4), &total, 4);
    return 0;
}

int afp_generate(const float* samples, size_t count, uint8_t** output, size_t* size) {
    *output = NULL;
    *size = 0;
    if (!samples || count != 24000) return 1;
    struct w2c_a* host = calloc(1, sizeof(*host));
    if (!host) return 1;
    pthread_mutex_lock(&runtime_lock);
    wasm_rt_init();
    int error = wasm_rt_impl_try();
    if (!error) {
        host->registering = true;
        wasm2c_afp_instantiate(&host->module, host);
        w2c_afp_C(&host->module);
        host->registering = false;
        wasm_rt_funcref_table_t* table = w2c_afp_D(&host->module);
        if (!host->invoker || host->invoker >= table->size || host->target >= table->size)
            wasm_rt_trap(WASM_RT_TRAP_CALL_INDIRECT);
        u32 bytes = (u32)(count * sizeof(float));
        u32 input = w2c_afp_E(&host->module, bytes + 5);
        if (!input) wasm_rt_trap(WASM_RT_TRAP_EXHAUSTION);
        memcpy(memory(host, input, 4), &bytes, 4);
        memcpy(memory(host, input + 4, bytes), samples, bytes);
        *memory(host, input + 4 + bytes, 1) = 0;
        wasm_rt_funcref_t function = table->data[host->invoker];
        u32 vector = ((u32 (*)(void*, u32, u32))function.func)(function.module_instance, host->target, input);
        u32 begin, end;
        memcpy(&begin, memory(host, vector, 4), 4);
        memcpy(&end, memory(host, vector + 4, 4), 4);
        if (end <= begin || end - begin > 65536) wasm_rt_trap(WASM_RT_TRAP_OOB);
        uint8_t* data = memory(host, begin, end - begin);
        *output = malloc(end - begin);
        if (!*output) wasm_rt_trap(WASM_RT_TRAP_EXHAUSTION);
        *size = end - begin;
        memcpy(*output, data, *size);
    }
    /* Freeing linear memory also releases all algorithm allocations, including traps. */
    wasm2c_afp_free(&host->module);
    free(host);
    pthread_mutex_unlock(&runtime_lock);
    return error;
}
