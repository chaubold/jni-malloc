#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>

#include "com_example_jnimemory_MemoryTestNative.h"

JNIEXPORT jlong JNICALL Java_com_example_jnimemory_MemoryTestNative_allocateMemory
  (JNIEnv *env, jclass cls, jlong size) 
{
    void* ptr = malloc((size_t)size);
    if (!ptr) {
        // Could throw OutOfMemoryError in Java
        jclass oomError = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
        (*env)->ThrowNew(env, oomError, "Native malloc failed");
        return 0;
    }
    return (jlong) (uintptr_t) ptr;  // cast pointer to 64-bit integer
}

JNIEXPORT void JNICALL Java_com_example_jnimemory_MemoryTestNative_freeMemory
  (JNIEnv *env, jclass cls, jlong address)
{
    if (address == 0) {
        return; 
    }
    void* ptr = (void*) (uintptr_t) address;
    free(ptr);
}

JNIEXPORT void JNICALL Java_com_example_jnimemory_MemoryTestNative_setMemory
  (JNIEnv *env, jclass cls, jlong address, jlong size, jbyte value)
{
    if (address == 0) {
        return; 
    }
    void* ptr = (void*) (uintptr_t) address;
    memset(ptr, (int)value, (size_t)size);
}

#if defined(__APPLE__) && defined(__MACH__)
#include <mach/mach.h>
#endif

JNIEXPORT jlong JNICALL Java_com_example_jnimemory_MemoryTestNative_getResidentSize
  (JNIEnv *env, jclass cls)
{
#if defined(__APPLE__) && defined(__MACH__)
    // macOS
    struct task_basic_info_64 info;
    mach_msg_type_number_t count = TASK_BASIC_INFO_64_COUNT;
    kern_return_t kr = task_info(mach_task_self(), TASK_BASIC_INFO_64,
                                 (task_info_t)&info, &count);
    if (kr != KERN_SUCCESS) {
        return (jlong)-1;
    }
    return (jlong)info.resident_size;
#elif defined(__linux__)
    // Linux
    FILE* fp = fopen("/proc/self/statm", "r");
    if (!fp) {
        return (jlong)-1;
    }
    long size, resident;
    if (fscanf(fp, "%ld %ld", &size, &resident) != 2) {
        fclose(fp);
        return (jlong)-1;
    }
    fclose(fp);

    long pageSize = sysconf(_SC_PAGESIZE);
    if (pageSize < 1) {
        return (jlong)-1;
    }
    return (jlong)(resident * pageSize);
#else
    // Unsupported platform
    return (jlong)-1;
#endif
}
