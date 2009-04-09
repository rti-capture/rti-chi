#ifndef LV_H
#define LV_H

#include "viewer.h"

extern int use_left_image;
extern int use_right_image;
extern int save_movie;
extern int use_color_interp;
extern int snap_nearest;
extern int keep_continuity;

extern int cylindrical_warp;

void stereoLinearInterp(double pos, unsigned char* imgleft, unsigned char* imgright, short* displeft, short* dispright);
void readInputFiles(char *baseimages);
void lowMemoryBatchMode(char *baseimages, char *disparitymaps);

void clearUp(bool clear_views);
void readSingleHSHfile(char *baseimage);


#endif