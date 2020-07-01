LOCAL_PATH := $(call my-dir) 

APP_ALLOW_MISSING_DEPS=true

# libftd2xx-jni.so
include $(CLEAR_VARS)
LOCAL_MODULE := libftd2xx-jni
LOCAL_SRC_FILES := libftd2xx-jni.so
include $(PREBUILT_SHARED_LIBRARY)   
    
# cryptoc
include $(CLEAR_VARS)
LOCAL_MODULE := cryptoc
LOCAL_SRC_FILES := libcryptoc.so
include $(PREBUILT_SHARED_LIBRARY)

# safetickets
include $(CLEAR_VARS)
LOCAL_MODULE := safetickets-sdk4
LOCAL_SRC_FILES := libsafetickets-sdk4.so
LOCAL_EXPORT_C_INCLUDES := sftsdk3.h
LOCAL_SHARED_LIBRARIES := cryptoc
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := SftEdsChecker
LOCAL_SRC_FILES := SftEdsChecker.cpp
LOCAL_STATIC_LIBRARIES := cpufeatures 
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := safetickets-sdk4
LOCAL_CPP_FEATURES += exceptions
include $(BUILD_SHARED_LIBRARY)


