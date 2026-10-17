cmake_minimum_required(VERSION 3.22.1)
project(audio_fingerprint C)

add_library(fingerprint_core STATIC
    fingerprint/afp.c
    fingerprint/afp_host.c
    fingerprint/runtime/wasm-rt-impl.c
    fingerprint/runtime/wasm-rt-exceptions-impl.c
    fingerprint/runtime/wasm-rt-mem-impl.c)
set_target_properties(fingerprint_core PROPERTIES POSITION_INDEPENDENT_CODE ON)
target_include_directories(fingerprint_core PUBLIC fingerprint fingerprint/runtime)
target_compile_features(fingerprint_core PRIVATE c_std_99)
# Explicit bounds checks avoid reserving a multi-GB guard region on watches.
target_compile_definitions(fingerprint_core PUBLIC WASM_RT_USE_MMAP=0 WASM_RT_ALLOW_SEGUE=0)
# Preserve Wasm floating-point behavior across ARM and x86.
target_compile_options(fingerprint_core PRIVATE -O2 -ffp-contract=off)
target_link_libraries(fingerprint_core PUBLIC m)

if(ANDROID)
    add_library(audio_fingerprint SHARED fingerprint/afp_jni.c)
    target_link_libraries(audio_fingerprint PRIVATE fingerprint_core)
    target_link_options(audio_fingerprint PRIVATE "-Wl,-z,max-page-size=16384")
endif()
