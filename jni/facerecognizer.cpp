#include <jni.h>
// #include "../opencv-android-sdk/native/jni/include/opencv2/contrib/contrib.hpp"
#include "opencv2/contrib/contrib.hpp"

//
// the create****FaceRecognizer functions return a cv::Ptr<FaceRecognizer>.
// if this thing leaves the scope(our function returns), it will destroy the instance.
// so we have to something about that (adding a refcount):
//

#ifdef __cplusplus
extern "C" {
#endif

/**
 * EigenFaceRecognizer constructors
 */

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_10(JNIEnv* env, jclass);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_10(JNIEnv* env, jclass) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createEigenFaceRecognizer();
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_11(JNIEnv* env, jclass, jint num_components);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_11(JNIEnv* env, jclass, jint num_components) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createEigenFaceRecognizer((int)num_components);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_12(JNIEnv* env, jclass, jint num_components, jdouble threshold);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_EigenFaceRecognizer_createEigenFaceRecognizer_12(JNIEnv* env, jclass, jint num_components, jdouble threshold) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createEigenFaceRecognizer((int)num_components,(double)threshold);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

/**
 * FisherFaceRecognizer constructors
 */

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_10(JNIEnv* env, jclass);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_10(JNIEnv* env, jclass) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createFisherFaceRecognizer();
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_11(JNIEnv* env, jclass, jint num_components);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_11(JNIEnv* env, jclass, jint num_components) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createFisherFaceRecognizer((int)num_components);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_12(JNIEnv* env, jclass, jint num_components, jdouble threshold);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_FisherFaceRecognizer_createFisherFaceRecognizer_12(JNIEnv* env, jclass, jint num_components, jdouble threshold) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createFisherFaceRecognizer((int)num_components,(double)threshold);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

/**
 * LBPHFaceRecognizer constructors
 */

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_10(JNIEnv* env, jclass);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_10(JNIEnv* env, jclass) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createLBPHFaceRecognizer();
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_11(JNIEnv* env, jclass, jint radius);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_11(JNIEnv* env, jclass, jint radius) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createLBPHFaceRecognizer((int)radius);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_12(JNIEnv* env, jclass, jint radius, jint neighbours);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_12(JNIEnv* env, jclass, jint radius, jint neighbours) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createLBPHFaceRecognizer((int)radius,(int)neighbours);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_13(JNIEnv* env, jclass, jint radius, jint neighbours, jint grid_x, jint grid_y, jdouble threshold);
JNIEXPORT jlong JNICALL Java_com_eim_facerecognition_LBPHFaceRecognizer_createLBPHFaceRecognizer_13(JNIEnv* env, jclass, jint radius, jint neighbours, jint grid_x, jint grid_y, jdouble threshold) {
    try {
    	cv::Ptr<cv::FaceRecognizer> pfr = cv::createLBPHFaceRecognizer((int)radius,(int)neighbours,(int)grid_x,(int)grid_y,(double)threshold);
    	pfr.addref();
        return (jlong) pfr.obj;
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "sorry, dave..");
    }
    return 0;
}

#ifdef __cplusplus
}
#endif
