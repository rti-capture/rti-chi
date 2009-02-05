#!/bin/bash
# this is a script shell for setting up the application bundle for the mac
# it should be run in the rtiviewer/src/install dir.
# it moves plugins and frameworks into the package and runs the 
# install_tool on them to change the linking path to the local version of qt
# the build was issued with
# qmake "CONFIG += debug_and_release warn_off" rtiviewer.pro -spec macx-g++
# make clean
# make release
# Note that sometimes you have to copy by hand the icons in the rtiviewer.app/Contents/Resources directory


cd ../src
QTPATH="/Library/Frameworks"
APPNAME="RTIViewer.app"

BUNDLE="RTIViewerBundle"

QTCOMPONENTS="QtCore QtGui QtOpenGL QtNetwork QtXml"

QTCORE=QtCore.framework/Versions/4.0/QtCore
QTGUI=QtGui.framework/Versions/4.0/QtGui

if [ -e $APPNAME -a -d $APPNAME ]
then
  echo "------------------"
else
  echo "Started in the wrong dir"
  exit 0
fi

echo "Starting to copying stuff in the bundle"

rm -r -f $BUNDLE

mkdir $BUNDLE
cp -r rtiviewer.app $BUNDLE

# copy the files icons into the app.
# cp images/rtiviewer_obj.icns $BUNDLE/$APPNAME/Contents/Resources

mkdir $BUNDLE/$APPNAME/Contents/Frameworks   
mkdir $BUNDLE/$APPNAME/Contents/plugins   
mkdir $BUNDLE/$APPNAME/Contents/plugins/imageformats 
  
#cp ../../docs/gpl.txt $BUNDLE
#cp ../../docs/readme.txt $BUNDLE

for x in $QTCOMPONENTS
do
#    cp -R $QTPATH/$x.framework $BUNDLE/$APPNAME/Contents/Frameworks 
rsync -avu --exclude='*debug*' $QTPATH/$x.framework $BUNDLE/$APPNAME/Contents/Frameworks
done

echo "now trying to change the paths in the rtiviewer executable"

for x in $QTCOMPONENTS
  do
   install_name_tool -id  @executable_path/../Frameworks/$x.framework/Versions/4.0/$x $BUNDLE/rtiviewer.app/Contents/Frameworks/$x.framework/Versions/4.0/$x
  done

install_name_tool -change QtCore.framework/Versions/4/QtCore @executable_path/../Frameworks/$QTCORE $BUNDLE/rtiviewer.app/Contents/Frameworks/QtGui.framework/Versions/4.0/QtGui
install_name_tool -change QtCore.framework/Versions/4/QtCore @executable_path/../Frameworks/$QTCORE $BUNDLE/rtiviewer.app/Contents/Frameworks/QtXml.framework/Versions/4.0/QtXml
install_name_tool -change QtCore.framework/Versions/4/QtCore @executable_path/../Frameworks/$QTCORE $BUNDLE/rtiviewer.app/Contents/Frameworks/QtNetwork.framework/Versions/4.0/QtNetwork
install_name_tool -change QtCore.framework/Versions/4/QtCore @executable_path/../Frameworks/$QTCORE $BUNDLE/rtiviewer.app/Contents/Frameworks/QtOpenGL.framework/Versions/4.0/QtOpenGL
install_name_tool -change QtCore.framework/Versions/4/QtCore @executable_path/../Frameworks/$QTCORE $BUNDLE/rtiviewer.app/Contents/Frameworks/QtOpenGL.framework/Versions/4.0/QtOpenGL
install_name_tool -change QtGui.framework/Versions/4/QtGui   @executable_path/../Frameworks/$QTGUI  $BUNDLE/rtiviewer.app/Contents/Frameworks/QtOpenGL.framework/Versions/4.0/QtOpenGL


IMAGEFORMATSPLUGINS="libqjpeg.dylib libqgif.dylib libqtiff.dylib"
for x in $IMAGEFORMATSPLUGINS
do
cp /Developer/Applications/Qt/plugins/imageformats/$x $BUNDLE/rtiviewer.app/Contents/plugins/imageformats
install_name_tool -change QtCore.framework/Versions/4/QtCore  @executable_path/../Frameworks/QtCore.framework/Versions/4/QtCore  $BUNDLE/rtiviewer.app/Contents/plugins/imageformats/$x 
install_name_tool -change QtGui.framework/Versions/4/QtGui    @executable_path/../Frameworks/QtGui.framework/Versions/4/QtGui    $BUNDLE/rtiviewer.app/Contents/plugins/imageformats/$x 
done

echo "Now Changing " #--------------------------

EXECNAMES="MacOS/rtiviewer " 
QTLIBPATH="/usr/local/Trolltech/Qt-4.3.3/lib"
for x in $EXECNAMES
do
  install_name_tool -change QtCore.framework/Versions/4/QtCore       @executable_path/../Frameworks/QtCore.framework/Versions/4/QtCore       $BUNDLE/rtiviewer.app/Contents/$x
  install_name_tool -change QtGui.framework/Versions/4/QtGui         @executable_path/../Frameworks/QtGui.framework/Versions/4/QtGui         $BUNDLE/rtiviewer.app/Contents/$x
  install_name_tool -change QtNetwork.framework/Versions/4/QtNetwork @executable_path/../Frameworks/QtNetwork.framework/Versions/4/QtNetwork $BUNDLE/rtiviewer.app/Contents/$x
  install_name_tool -change QtOpenGL.framework/Versions/4/QtOpenGL   @executable_path/../Frameworks/QtOpenGL.framework/Versions/4/QtOpenGL   $BUNDLE/rtiviewer.app/Contents/$x
  install_name_tool -change QtXml.framework/Versions/4/QtXml         @executable_path/../Frameworks/QtXml.framework/Versions/4/QtXml         $BUNDLE/rtiviewer.app/Contents/$x
done

cd ../install

#Suppose you have:
#1) Folder of content
#2) Background image file for that folder, in the folder.
#3) Icon for dmg, anywhere.

#To make disk image (with background, and with custom image icon):

# 1) Open Disk Utility
# 2) Images-->New-->Blank Image
#   a) leave it on read/write disk image
#   b) leave it on Encryption: none
#   c) Size: select a size somewhat larger than the size of the folder (since the available space is less when formatted).
#   d) Save As: This is the name for the mounted Image. Choose a good name for your disk image. It can contains spaces, etc., but you will not be # able to change this later (You will only be able to change name of .dmg file).
# 3) Move your folder contents into the empty disk image that you just created (and it automatically mounts after the previous step).
# 4) From the finder while you are viewing the opened image, Apple-J (View-->Show View Options)
#   a) Use the icon view
#   b) Select: This Window Only
#   c) Select desired icon size
#   d) Select Background: Picture, then choose the file that is *INSIDE* your disk image. There are a variety of ways to hide this file, such as # in hidden folders beginning with ".", or using blank or camoflauged icons and space " " file names with hidden extensions in Get Info.
# 5) Apply the custom icon to your disk image, if desired. (Get info, and paste it in... 128x128).
# 6) Arrange icons & window size as desired, and close window.
# 7) Go back to Disk Utility, and in the left window click to select the .dmg (not the disk image that is hanging off of it).
# 8 ) Select Images--> Convert...
# 9) Choose a new filename for the converted (final) .dmg file. Note that the name of the mounted image itself will remain unchanged.
#   a) Select Compressed, then go ahead & convert.
# 10) Voila, you are finished.

