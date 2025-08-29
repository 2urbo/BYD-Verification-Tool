#include <jni.h>
#include <string>
#include <iostream>
#include "include/SystemPropertyUtils.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_turbo2k_bydverificationtool_MainActivity_getSystemProperty(
        JNIEnv* env,
        jobject,
        jstring propertyName) {

    const char* propertyNameChars = env->GetStringUTFChars(propertyName, nullptr);
    std::string propertyNameString(propertyNameChars);
    env->ReleaseStringUTFChars(propertyName, propertyNameChars);

    std::string value = getSystemProperty(propertyNameString);

    return env->NewStringUTF(value.c_str());
}