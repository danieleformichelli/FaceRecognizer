LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/../opencv-android-sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := facerecognizer
LOCAL_SRC_FILES := facerecognizer.cpp

include $(BUILD_SHARED_LIBRARY)
