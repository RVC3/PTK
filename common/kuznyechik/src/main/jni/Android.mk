LOCAL_PATH := $(call my-dir)

APP_ALLOW_MISSING_DEPS=true

include  $(CLEAR_VARS)

LOCAL_MODULE := kuznyechik
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_SRC_FILES := kuznyechik.cpp \
    block_cipher.cpp \
    omac1.cpp
include $(BUILD_SHARED_LIBRARY)
