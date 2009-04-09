#ifndef VIEWER_H
#define VIEWER_H

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <vector>
#include <algorithm>

#include <GL\glut.h>
#include <math.h>
#include <iostream>


//#define max(a,b) a>b?a:b
//#define min(a,b) a<b?a:b
#define round(a) (a<0?ceil((a)-0.5):floor((a)+0.5))

const int MAX = 360; // max number of viewpoints

#define IPI 0.31830988618379
#define PI 3.14159265358979

using namespace std;

enum type {MODE_PHONG,MODE_WARD,MODE_SPHERICAL,MODE_POLY,MODE_PHOTOMETRIC,MODE_HEMISPHERICAL,MODE_IMAGE};

struct pixel
{
	double n;
	double kd, ks, ka;
	double albedo[3];
	double normal[3];
};

struct shpixel
{
	double data_r[9];
	double data_g[9];
	double data_b[9];
};

struct Cview
{
	short *imgdispleft;
	short *imgdispright;
	unsigned char* Image;
	float * floatpixels;
	vector<pixel> pixels;
	vector<shpixel> shpixels;
	vector<shpixel> popixels;
	bool lightingcached;
};

extern Cview view[MAX];

void	swap(pixel*,pixel*);
void	swapsh(shpixel*,shpixel*);
void	SetupGlutWindows(int argc, char **argv);
void	idle(void);
void	display(void);
void	keys(unsigned char key, int x, int y);

void renderImage(vector<pixel> &pixels,vector<shpixel> &shpixels,vector<shpixel> &popixels, unsigned char* Image);
void generateImages(vector<pixel> &pixels);
void load(char* fn,vector<pixel> &pixels,vector<shpixel> &shpixels,vector<shpixel> &popixels, int scale);
float * loadHSH(char* fn);
float * loadHSHuncompressed(char* fn);
void renderImageHSH(float * hshimage, unsigned char* Image);

extern int basewindow;

extern int basewindow_xsize;
extern int basewindow_ysize;

extern int drawwindow_xsize;
extern int drawwindow_ysize;	

extern int current_width;
extern int current_height;

extern int width;
extern int height;
extern int bands;
extern int order;

extern int num_to_show;

extern unsigned char* albedoImage;
extern unsigned char* Image;
extern unsigned char* normalImage;
extern unsigned char* ksImage;
extern unsigned char* kdImage;
extern unsigned char* kaImage;

extern type g_mode;

extern float lx;
extern float ly;
extern float lz;

extern int showMode;

inline int getindex(int h, int w, int b, int o) 
{
	return h * (width*bands*order*order) + w * (bands*order*order) + b*(order*order) + o;
}
//extern vector<pixel> pixels;
//extern vector<shpixel> shpixels;
//extern vector<shpixel> popixels;

#endif