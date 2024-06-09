#include "jni.h"
#include <iostream>
#include <fstream>
#include <string>
#include <unistd.h>
#include <algorithm>
#include <cstring>
#include "Includes/Logger.h"
#include "obfuscate.h"

using namespace std;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

	return JNI_VERSION_1_6;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_victor_vx_app_fragments_AccountFragment_urlAuthorization(JNIEnv *env, jobject thiz) {
	const char * url = OBFUSCATE("https://api-app-vx.glitch.me/authorization-token");
	return env->NewStringUTF(url);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_victor_vx_app_connections_ServerConnectionApp_nativeUrlApi(JNIEnv *env, __attribute__((unused)) jobject thiz) {
	const char * url = OBFUSCATE("https://api-app-vx.glitch.me");
	return env->NewStringUTF(url);
}