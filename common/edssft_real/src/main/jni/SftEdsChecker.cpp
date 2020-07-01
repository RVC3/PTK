#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include "sftsdk3.h"

//#include <android/native_activity.h>
#include <errno.h>
#include <stdexcept>
#include <string>
#include <exception>
#include <iostream>
#include <sstream>

#define LOG_TAG "NATIVE"
#define LOGI(x...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,x)

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeOpenProcessor(JNIEnv *env, jclass clazz,
                                                             jstring workingPath,
                                                             jstring transportPath) {
    using std::string;
    //	string working_path1 = string("/storage/emulated/0/CPPKConnect/working");
    //	string transport_path1 = string("/storage/emulated/0/CPPKConnect/SftTransport");

    jsize lenWorking = env->GetStringUTFLength(workingPath);
    const char *workingChars = env->GetStringUTFChars(workingPath, (jboolean *) 0);
    string working_path(workingChars, lenWorking);

    jsize lenTransport = env->GetStringUTFLength(transportPath);
    const char *transportChars = env->GetStringUTFChars(transportPath, (jboolean *) 0);
    string transport_path(transportChars, lenTransport);

    return SFT_SDK3_OpenBarCodeProcessor(working_path.c_str(), working_path.length(),
                                         transport_path.c_str(), transport_path.length());
}

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeCloseProcessor(JNIEnv *env, jclass clazz) {
    return SFT_SDK3_CloseBarCodeProcessor();
}

extern "C" JNIEXPORT jstring

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeGetLastError(JNIEnv *env, jclass clazz) {
    const char *errInfo = "";
    unsigned int errInfoSize = 0;
    SFT_SDK3_GetLastErrorDescription(&errInfo, &errInfoSize);

    return env->NewStringUTF(errInfo);
}

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeSetUserId(JNIEnv *env, jclass clazz,
                                                         int userId) {
    const unsigned char *user_id = (unsigned char *) &userId;
    return SFT_SDK3_SetUserId(user_id, sizeof(userId));
}

extern "C" JNIEXPORT void JNICALL

writeLong(const char *name, uint64_t number, JNIEnv *env, jclass clazz, jobject obj) {

    jfieldID keyID = env->GetFieldID(clazz, name, "J");
    if (keyID != NULL)
        env->SetLongField(obj, keyID, (long long) number);
}

//return 0 if get info is successful, else error code
extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeGetKeyInfo(JNIEnv *env, jobject thisObj,
                                                          int keyNumber) {
    uint32_t key = keyNumber;
    uint64_t keyValidSince = 0;
    uint64_t keyValidTill = 0;
    uint64_t keyWhenRevocated = 0;
    const unsigned char *userDeviceId = 0;
    unsigned int userDeviceSize = 0;

    long result = SFT_SDK3_GetKeyInfo(keyNumber, &keyValidSince, &keyValidTill, &keyWhenRevocated,
                                      &userDeviceId, &userDeviceSize);
    if (result == 0) {
        jclass thisClass = env->GetObjectClass(thisObj);
        /*
         jfieldID keyValidSinceID = env->GetFieldID(thisClass, "keyValidSince", "J");
         if(keyValidSinceID != NULL)
         env->SetLongField(thisObj, keyValidSinceID, (long long) keyValidSince);*/

        writeLong("keyValidSince", keyValidSince, env, thisClass, thisObj);
        writeLong("keyValidTill", keyValidTill, env, thisClass, thisObj);
        writeLong("keyWhenRevocated", keyWhenRevocated, env, thisClass, thisObj);

        // �������� ����� Java-������ ��� ������������� ������, ��������� �������� deviceId
        jmethodID method = env->GetMethodID(thisClass, "setDeviceIdBuffer", "(I)V");
        if (method != NULL) {
            env->CallVoidMethod(thisObj, method, userDeviceSize);
        }

        // ���������� �������� ������� � ���������� ����������� Java-������
        jfieldID signDataField = env->GetFieldID(thisClass, "deviceId", "[B");
        if (signDataField != NULL) {
            jobject sdata = env->GetObjectField(thisObj, signDataField);
            jbyteArray *array = reinterpret_cast<jbyteArray *>(&sdata);
            jbyte *data = env->GetByteArrayElements(*array, 0);
            memcpy(data, userDeviceId, userDeviceSize * sizeof(jbyte));
            env->ReleaseByteArrayElements(*array, data, JNI_ABORT);
        }

    }

    return result;
}

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeSignData(JNIEnv *env, jobject thisObj,
                                                        jbyteArray data, long time) {
    const unsigned char *rawData = (unsigned char *) env->GetByteArrayElements(data, 0);
    unsigned int dataSize = env->GetArrayLength(data);

    const unsigned char *signBuffer = 0;
    unsigned int signSize = 0;
    uint32_t keyNumber = 0;

    long result = SFT_SDK3_SignData(rawData, dataSize, time, &signBuffer, &signSize, &keyNumber);

    if (signSize > 0) {
        jclass thisClass = env->GetObjectClass(thisObj);

        // ���������� �������� ����� � ���������� ����������� Java-������
        jfieldID keyNumberField = env->GetFieldID(thisClass, "keyNumber", "J");
        if (keyNumberField != NULL)
            env->SetLongField(thisObj, keyNumberField, (long long) keyNumber);

        // �������� ����� Java-������ ��� ������������� ������, ��������� �������� �������
        jmethodID method = env->GetMethodID(thisClass, "setSignDataSize", "(I)V");
        if (method != NULL) {
            env->CallVoidMethod(thisObj, method, signSize);
        }

        // ���������� �������� ������� � ���������� ����������� Java-������
        jfieldID signDataField = env->GetFieldID(thisClass, "signData", "[B");
        if (signDataField != NULL) {
            jobject sdata = env->GetObjectField(thisObj, signDataField);
            jbyteArray *array = reinterpret_cast<jbyteArray *>(&sdata);
            jbyte *data = env->GetByteArrayElements(*array, 0);
            memcpy(data, signBuffer, signSize * sizeof(jbyte));
            env->ReleaseByteArrayElements(*array, data, JNI_ABORT);
        }
    }

    env->ReleaseByteArrayElements(data, (jbyte *) rawData, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeVerifySign(JNIEnv *env, jobject thisObj,
                                                          jbyteArray data, jbyteArray sign,
                                                          long keyNumber) {
    const unsigned char *rawData = (unsigned char *) env->GetByteArrayElements(data, 0);
    unsigned int dataSize = env->GetArrayLength(data);

    const unsigned char *rawSign = (unsigned char *) env->GetByteArrayElements(sign, 0);
    unsigned int signSize = env->GetArrayLength(sign);

    uint32_t keyNumberUnsignde = keyNumber;

    unsigned int isSignValid;

    long result = SFT_SDK3_VerifySign(rawData, dataSize, rawSign, signSize, keyNumberUnsignde,
                                      &isSignValid);

    // ���������� ��������� �������� � ���������� ����������� Java-������
    jclass thisClass = env->GetObjectClass(thisObj);
    jfieldID validField = env->GetFieldID(thisClass, "isSignValid", "Z");
    if (validField != NULL)
        env->SetBooleanField(thisObj, validField, (isSignValid != 0));

    return result;
}

extern "C" JNIEXPORT jint

JNICALL Java_ru_ppr_edssft_real_NativeSft_nativeGetState(JNIEnv *env, jobject thisObj) {
    unsigned int state;
    long result = SFT_SDK3_GetState(&state);

    // ���������� ��������� �������� � ���������� ����������� Java-������
    jclass thisClass = env->GetObjectClass(thisObj);
    jfieldID validField = env->GetFieldID(thisClass, "state", "I");
    if (validField != NULL)
        env->SetIntField(thisObj, validField, state);

    return result;
}
