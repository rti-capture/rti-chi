TEMPLATE       = app
TARGET         = rtiviewer
LANGUAGE       = C++
CONFIG        += qt debug xml network opengl console warn_off
QT			  += opengl xml network

UI_DIR         = ui
MOC_DIR        = moc

INCLUDEPATH    += ../../../../vcglib \
                  ../../compression/src/ \
				  ../../rtibuilder/src

SOURCES        =  \
				ptm.cpp \
				gui.cpp \
				main.cpp \
				rtiBrowser.cpp \
				lightControl.cpp\
				renderingdialog.cpp\
				diffusegain.cpp\
				specularenhanc.cpp\
				navigator.cpp\
				loadingdlg.cpp\
				openremotedlg.cpp\
				httpthread.cpp\
				normalenhanc.cpp\
				unsharpmasking.cpp\
				coeffenhanc.cpp\
				detailenhanc.cpp\
				dyndetailenhanc.cpp\
				hsh.cpp\
				universalrti.cpp
				

HEADERS        = \
				rti.h \
				ptm.h \
				gui.h \
				../../rtibuilder/src/zorder.h \
				rtiBrowser.h \
				lightControl.h\
				renderingdialog.h\
				renderingmode.h\
				diffusegain.h\
				specularenhanc.h\
				navigator.h\
				loadingdlg.h\
				openremotedlg.h\
				httpthread.h\
				util.h\
				normalenhanc.h\
				unsharpmasking.h\
				coeffenhanc.h\
				detailenhanc.h\
				dyndetailenhanc.h\
				configdlg.h\
				pyramid.h\
				hsh.h\
				universalrti.h
				
               

FORMS          = rtiviewer.ui \
                 about.ui

RESOURCES     =  rtiviewer.qrc
# to add MacOS icon
ICON = images/rtiviewer.icns

DEFINES += PRINT_DEBUG
mac: LIBS  += ../../compression/src/lib/libjpeg2000.a
win32-msvc2005: LIBS  += ../../compression/src/lib/jpeg2000.lib
win32-msvc2008: LIBS  += ../../compression/src/lib/jpeg2000.lib
win32-g++: LIBS += ../../compression/src/lib/libjpeg2000.a