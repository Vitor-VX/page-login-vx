#include "jni.h"
#include "obfuscate.h"
#include <algorithm>
#include <cstring>
#include <dirent.h>
#include <dlfcn.h>
#include <fstream>
#include <iostream>
#include <linux/unistd.h>
#include <string>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <thread>
#include <sys/ptrace.h>
#include "Includes/Logger.h"
#include <dlfcn.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <vector>

#define MAX_BUF_SIZE 1024
#define MAX_LIB 2

using namespace std;

static JavaVM *globalJavaVM = nullptr;
static bool isRoot = true;

const char* GetLibDir(JNIEnv *env) {
	jclass activity_thread_clz = env->FindClass("android/app/ActivityThread");
	if (activity_thread_clz != nullptr) {
		jmethodID currentApplicationId = env->GetStaticMethodID(activity_thread_clz, "currentApplication", "()Landroid/app/Application;");
		if (currentApplicationId) {
			jobject application = env->CallStaticObjectMethod(activity_thread_clz, currentApplicationId);
			jclass application_clazz = env->GetObjectClass(application);
			if (application_clazz) {
				jmethodID get_application_info = env->GetMethodID(application_clazz, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
				if (get_application_info) {
					jobject application_info = env->CallObjectMethod(application, get_application_info);
					jfieldID native_library_dir_id = env->GetFieldID(env->GetObjectClass(application_info), "nativeLibraryDir","Ljava/lang/String;");
					if (native_library_dir_id) {
						auto native_library_dir_jstring = (jstring) env->GetObjectField(application_info, native_library_dir_id);
						auto path = env->GetStringUTFChars(native_library_dir_jstring, nullptr);
						const char *lib_dir(path);
						env->ReleaseStringUTFChars(native_library_dir_jstring, path);
						return lib_dir;
					} else {
						LOGI("nativeLibraryDir not found");
					}
				} else {
					LOGI("getApplicationInfo not found");
				}
			} else {
				LOGI("application class not found");
			}
		} else {
			LOGI("currentApplication not found");
		}
	} else {
		LOGI("ActivityThread not found");
	}
	return {};
}

JNIEnv *GetJniEnv() {
	JNIEnv *env;
	if (globalJavaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
		if (globalJavaVM->AttachCurrentThread(&env, nullptr) != JNI_OK) {
			LOGI("Failed to attach current thread to JVM");
			return nullptr;
		}
	}
	return env;
}

static void processKill() {
	abort();
}

std::vector<std::string> getLibPaths() {
	JNIEnv *env = GetJniEnv();
	const char* nativeLib = GetLibDir(env);
	char directory_app[256] = "";

	snprintf(directory_app, sizeof(directory_app), "%s/", nativeLib);

	DIR* directory = opendir(directory_app);
	if (directory == NULL) {
		LOGI("%s", (const char*) OBFUSCATE("Erro ao abrir o diretório"));
		return {};
	}

	std::vector<std::string> lib_paths;
	struct dirent* entry;
	while ((entry = readdir(directory)) != NULL) {
		if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
			continue;
		}

		if (strcmp(entry->d_name, OBFUSCATE("libdetect.so"))) {
			std::string lib_path = std::string(directory_app) + entry->d_name;
			lib_paths.push_back(lib_path);
		}
	}

	closedir(directory);
	return lib_paths;
}

void *checkLibCount(void *) {
	std::vector<std::string> lib = getLibPaths();
	int value = lib.size();

	if (value > MAX_LIB) processKill();

	return NULL;
}

void *checkFridaPort(void *) {
	while (true) {
		int socket_fd = socket(AF_INET, SOCK_STREAM, 0);
		if (!socket_fd || socket_fd < 0) {
			return NULL;
		}

		struct sockaddr_in server_address;
		memset(&server_address, 0, sizeof(server_address));

		server_address.sin_family = AF_INET;
		server_address.sin_addr.s_addr = inet_addr(OBFUSCATE("127.0.0.1"));
		server_address.sin_port = htons(static_cast<uint16_t>(strtoul(OBFUSCATE("27042"), nullptr, 10)));

		int result = connect(socket_fd, (struct sockaddr *)&server_address, sizeof(server_address));

		if (result == 0) processKill();

		sleep(5);
	}

	return NULL;
}

void *searchGadget(void *) {
	if (!isRoot) return NULL;

	while (true) {
		const char *commandGetPid = (const char*) OBFUSCATE("(toolbox ps; toolbox ps -A; toybox ps; toybox ps -A) | grep \" Gadget \"");
		const char* commandOutputFile = (const char*) OBFUSCATE("/data/data/victor.vx.app/files/output.txt");

		char fullCommand[MAX_BUF_SIZE];
		snprintf(fullCommand, sizeof(fullCommand), OBFUSCATE("su -c \"%s > %s\""), commandGetPid, commandOutputFile);
		system(fullCommand);

		FILE *fp = fopen(commandOutputFile, "r");
		if (fp == NULL) {
			LOGI("%s", (const char*) OBFUSCATE("Error: opening output file"));

			return NULL;
		}

		int pid = 0;
		char outputLine[MAX_BUF_SIZE];
		while (fgets(outputLine, sizeof(outputLine), fp) != NULL) {
			if (strstr(outputLine, OBFUSCATE("Gadget")) != NULL) {
				sscanf(outputLine, OBFUSCATE("%*s %d"), &pid);
				break;
			}
		}

		fclose(fp);

		if (pid != 0) processKill();

		sleep(5);
	}

	return NULL;
}

void *searchFridaLib(void *) {
	// frida -> 66 72 69 64 61
	vector<unsigned char> frd = { 0x66, 0x72, 0x69, 0x64, 0x61 };

	while (true) {
		std::vector<std::string> libs = getLibPaths();

		for (const auto &lib : libs) {
			std::ifstream file(lib, std::ios::binary);
			if (!file) {
				LOGI("%s", (const char *) OBFUSCATE("Erro ao abrir o arquivo: %s"), lib.c_str());
				continue;
			}

			std::vector<unsigned char> file_bytes;

			char byte;
			while (file.get(byte)) {
				file_bytes.push_back(static_cast<unsigned char>(byte));
			}

			bool found_frida = false;

			for (size_t i = 0; i <= file_bytes.size() - frd.size(); i++) {
				bool match = true;
				for (size_t j = 0; j < frd.size(); j++) {
					if (file_bytes[i + j] != frd[j]) {
						match = false;
						break;
					}
				}

				if (match) {
					found_frida = true;
					processKill();
				}
			}

			if (!found_frida) continue;
		}
		sleep(5);
	}

	return NULL;
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	globalJavaVM = vm;

	pthread_t pid_port;
	pthread_create(&pid_port, NULL, checkFridaPort, NULL);

	pthread_t pid_gadget;
	pthread_create(&pid_gadget, NULL, searchGadget, NULL);

	pthread_t pid_lib;
	pthread_create(&pid_lib, NULL, searchFridaLib, NULL);

	pthread_t pid_app;
	pthread_create(&pid_app, NULL, checkLibCount, NULL);

	return JNI_VERSION_1_6;
}