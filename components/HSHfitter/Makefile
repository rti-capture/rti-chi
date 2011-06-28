# source files.


OS=MacOs
# Linux / Windows

MAC_OS_FRAMEWORK += -framework Accelerate

MAC_OS_LIBS = libjpeg.a -lm -L/System/Library/Frameworks/Accelerate.framework -framework Accelerate
WINDOWS_LIBS = -ljpeg -lblas -lm
LINUX_LIBS = -ljpeg -lblas -lm


SRC = src/hshfitcmdline.cpp src/input.cpp src/image.cpp src/hsh_core.cpp
OBJ = $(SRC:.cpp=.o)

OUT = hshfitter

# include directories
INCLUDES = -I. 
ifeq ($(OS), MacOs)
   INCLUDE += $(FRAMEWORKS)
endif


# C++ compiler flags (-g -O2 -Wall)
#CCFLAGS = -O2 -Wall -static
CCFLAGS = -O3 -Wall -fopenmp
# compiler
CCC = g++

# library paths
LIBS = 

# compile flags
LDFLAGS = -O3 -fopenmp

.SUFFIXES: .cpp .c 


ifeq ($(OS), Linux)
   LIBS = $(LINUX_LIBS)
endif
ifeq ($(OS), Windows)
   LIBS = $(WINDOWS_LIBS)
endif
ifeq ($(OS), MacOs)
   LIBS = $(MAC_OS_LIBS) $(FRAMEWORKS)
endif

default: $(OUT)

.cpp.o:
	$(CCC) $(INCLUDES)  -c $< -o $@

.c.o:
	$(CCC) $(INCLUDES) -c $< -o $@

$(OUT): $(OBJ)

	$(CCC) $(LDFLAGS) -o $(OUT) $(OBJ) $(LIBS) 

depend:  dep
#
#dep:
#	makedepend -- $(CFLAGS) -- $(INCLUDES) $(SRC)

clean:
	rm -f $(OUT) src/*.o src/*.a src/Makefile.bak 
