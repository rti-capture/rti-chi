How to compile RTIViewer
-------------------------------------------------------------------------------------

To compile RTIViewer you need a C++ compiling environment and the following libraries:
- Qt 4.4 (http://www.qtsoftware.com/downloads).
- the VCG libraries; this library is only accessible through anonymous svn (svn string:
  svn co https://vcg.svn.sourceforge.net/svnroot/vcg vcg ). The library must be at the
  same level of the folder 'components' and its root should be named 'vcglib'

The code tree must have the following structure:
~/components/RTIViewer/
~/vcglib/vcg/space/
 
The compiling step depends on the compiling environment. Using GCC (both under linux and
using the mingw gcc provided with the free Qt distribution) you should just type, from
the '~/components/RTIViewer/' directory:

qmake rtiviewerv10.pro
make

Under windows the suggested platform is the one formed by the open source version of Qt
with the mingw gcc compiler that is kindly included in the open source Qt distribution
available from TrollTech.
If you want to use Visual Studio, please buy the commercial version of Qt that offers a
nice integration of the Qt tools into the Visual Studio IDE. In that case you simply have
to import the top level pro ( ~/components/RTIViewer/rtiviewerv10.pro ) into VisualStudio.
