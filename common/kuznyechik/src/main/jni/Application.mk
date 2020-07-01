APP_MODULES := kuznyechik
# без этой строчки никого STL (включая string) мы не дождемся
APP_STL := stlport_static
APP_ABI := armeabi
APP_CPPFLAGS += -std=c++11
