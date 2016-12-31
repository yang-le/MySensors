//
// Created by yangle on 16-12-30.
//
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "oclHelper.h"

static void oclHello()
{
    __android_log_write(ANDROID_LOG_INFO, "oclHelper", "Hello");
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNINativeMethod method_table[] = {
            { "Hello", "()V", oclHello },
    };

    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass clazz = (*env)->FindClass(env, "me/yangle/myphone/OclHelper");
    if (NULL == clazz) {
        return -1;
    }

    if ((*env)->RegisterNatives(env, clazz, method_table, sizeof(method_table) / sizeof(method_table[0])) < 0) {
        return -1;
    }

    return JNI_VERSION_1_6;
}
