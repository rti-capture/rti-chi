TEMPLATE       = app
TARGET         = rtibuilder
LANGUAGE       = C++
CONFIG         += qt debug network console warn_off
QT             += xml

MOC_DIR        = moc

INCLUDEPATH    += ../../../../vcglib \
                  ../../rtiviewer/src \
                  ../../compression/src/ \


SOURCES        =  \
               main.cpp \
               ../../rtiviewer/src/ptm.cpp \
               ../../rtiviewer/src/loadingdlg.cpp \
               ../../rtiviewer/src/diffusegain.cpp \
               ../../rtiviewer/src/specularenhanc.cpp \
               ../../rtiviewer/src/normalenhanc.cpp \
               ../../rtiviewer/src/unsharpmasking.cpp \
               ../../rtiviewer/src/coeffenhanc.cpp\
			   ../../rtiviewer/src/detailenhanc.cpp\
			   ../../rtiviewer/src/dyndetailenhanc.cpp\

HEADERS        = \
               zorder.h \
               ../../rtiviewer/src/rti.h \
               ../../rtiviewer/src/ptm.h \
               ../../rtiviewer/src/loadingdlg.h \               
               ../../rtiviewer/src/diffusegain.h \
               ../../rtiviewer/src/specularenhanc.h \
               ../../rtiviewer/src/normalenhanc.h \
               ../../rtiviewer/src/unsharpmasking.h \
               ../../rtiviewer/src/coeffenhanc.h\
			   ../../rtiviewer/src/detailenhanc.h\
			   ../../rtiviewer/src/dyndetailenhanc.h\


mac: LIBS       += ../../compression/src/lib/libjpeg2000.a
win32-msvc2005: LIBS  += ../../compression/src/lib/jpeg2000.lib
win32-msvc2008: LIBS  += ../../compression/src/lib/jpeg2000.lib
win32-g++: LIBS += ../../compression/src/lib/libjpeg2000.a