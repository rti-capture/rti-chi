#ifndef INTERFACE_H
#define INTERFACE_H

#include "viewer.h"

const int TRACKBAR_GAP = 100;
const int VIEWSPAN = 360;

const int MENU_CALIBRATE = 20;
const int MENU_RECTIFY = 30;
const int MENU_STEREO = 40;
const int MENU_INTERP = 50;
const int MENU_INTERP_LINEAR = 55;

extern int viewpoints;
extern int windowheight, windowwidth;
extern unsigned char *imgshow;

extern int movie_frames;
extern int total_movie_frames;
extern bool batch_mode;

extern int mousedownX, mousedownY;
extern int mousemoveX, mousemoveY;
extern bool mousedown;
extern float postrackbar;
extern int scale;

void saveMovie(int frame_offset);
void saveMovieReverse(int frame_offset);


#endif