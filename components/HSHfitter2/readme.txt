Build Instructions

JPEG lib dependencies:

Goto http://www.ijg.org/ and download the 6b version of the JPEG library
Compile for your operating system following the instruction on the official site. On linux you can install using the package manager.

On MacOs copy the libjpeg.a to the directory make file and follow the instructions bellow.

Windows requirements:
   - Install the latest version of MinGw, including the LAPACK Libraries (http://www.mingw.org/)


MacOs Requirements:
   - XCode 3.X or higher
   - Download from 
Linux Requirements:
   - GCC 4.x.x + Blas Library


Build HSHfitter
1. Edit the Makefile
2. Replace the OS by the correct OS. (Note: Leave no spaces before or after)
3. Save and Quit
4. Run make clean; make

