LOCAL_PATH := $(call my-dir)

# Includes
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Includes

include $(CLEAR_VARS)
LOCAL_MODULE := dobby
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libdetect

# Outros arquivos fonte
LOCAL_SRC_FILES := vx/libdetect.cpp\
                   Request/HttpRequest.cpp \
                   obfuscate.h \

LOCAL_STATIC_LIBRARIES := dobby
LOCAL_LDLIBS += -llog -landroid -lz -lEGL -lGLESv3 -lGLESv2

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive -DLOG_TAG=\"vxapp\"
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CPPFLAGS += -std=c++14

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libmain

LOCAL_SRC_FILES := vx/libmain.cpp\
                   Request/HttpRequest.cpp \
                   obfuscate.h \

LOCAL_STATIC_LIBRARIES := dobby
LOCAL_LDLIBS += -llog -landroid -lz -lEGL -lGLESv3 -lGLESv2

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive -DLOG_TAG=\"vxapp\"
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CPPFLAGS += -std=c++14

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libapp
LOCAL_SRC_FILES := vx/libapp.cpp\
                    obfuscate.h \

LOCAL_LDLIBS += -llog -landroid
LOCAL_CFLAGS := -Wno-error=format-security -fpermissive -DLOG_TAG=\"vxapp\"
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CPPFLAGS += -std=c++14

include $(BUILD_SHARED_LIBRARY)