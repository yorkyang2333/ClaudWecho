#include "afp_host.h"
#include <jni.h>
#include <stdlib.h>

JNIEXPORT jbyteArray JNICALL
Java_com_yorkyang2333_claudwecho_data_recognition_NativeAudioFingerprint_generate(
        JNIEnv* env, jobject receiver, jfloatArray samples) {
    if (!samples || (*env)->GetArrayLength(env, samples) != 24000) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IllegalArgumentException"), "Expected 3 seconds of 8 kHz mono audio");
        return NULL;
    }
    float* input = malloc(24000 * sizeof(float));
    if (!input) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "Cannot allocate audio buffer");
        return NULL;
    }
    (*env)->GetFloatArrayRegion(env, samples, 0, 24000, input);
    if ((*env)->ExceptionCheck(env)) { free(input); return NULL; }
    uint8_t* output = NULL;
    size_t size = 0;
    int error = afp_generate(input, 24000, &output, &size);
    free(input);
    if (error) {
        free(output);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IllegalStateException"), "Audio fingerprint generation failed");
        return NULL;
    }
    jbyteArray result = (*env)->NewByteArray(env, (jsize)size);
    if (result) (*env)->SetByteArrayRegion(env, result, 0, (jsize)size, (jbyte*)output);
    free(output);
    return result;
}
