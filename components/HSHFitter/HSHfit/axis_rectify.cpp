#include "stdafx.h"

#include "axis_rectify.h"
#include <iostream>
#include <fstream>
#include <string.h>

#include <cv.h>
#include "highgui.h"

#include "boost/filesystem.hpp"  

using namespace std;

AxisRectify::AxisRectify(string instr_main_path, string instr_prefix, string instr_correction_filename, string instr_axis_filename)
: str_main_path(instr_main_path),str_prefix(instr_prefix),
str_correction_filename(instr_correction_filename),str_axis_filename(instr_axis_filename),
do_color_correction(false),do_rectification(false)
{
}

void AxisRectify::read_axis_rot()
{
	ifstream axis_file((str_main_path + str_axis_filename).c_str());

	if (!axis_file)
	{
		do_rectification = false;
		return;
	}

	double axis_x, axis_y, axis_z;

	axis_file >> axis_x >> axis_y >> axis_z;

	rot_angle =90.0 + (atan( axis_y/axis_x) * 180 / pi);
	axis_file.close();

	do_rectification = true;
}


void AxisRectify::read_color_correction()
{
	ifstream fix_file((str_main_path + str_correction_filename).c_str());

	if (!fix_file) 
	{
		do_color_correction = false;
		return;
	}

	for (num_lights=0; !fix_file.eof(); num_lights++)
	{
		fix_file >> color_fix[0][num_lights] >>  color_fix[1][num_lights] >>  color_fix[2][num_lights];
	}
	fix_file.close();

	do_color_correction = true;
}

bool AxisRectify::full_rectification_loop()
{
	if (!do_rectification && !do_color_correction)
		return false;

	list<string> allfiles; // the list of files matching the prefix
	int num_files = find_file(str_main_path,str_prefix, allfiles,true);

	if (num_files % num_lights != 0)
	{
		// Does not divide!!!
		return false;
	}

	int light_index = 0;

	for (list<string>::iterator it=allfiles.begin() ; it != allfiles.end(); it++ )
	{
		string currentfilename  = (*it);

		IplImage* imgIn = cvLoadImage(currentfilename.c_str());
		IplImage* imgOut = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,imgIn->nChannels);

		if (do_color_correction)
		{
			IplImage* IdstR = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			IplImage* IdstG = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			IplImage* IdstB = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			IplImage* OdstR = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			IplImage* OdstG = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			IplImage* OdstB = cvCreateImage(cvSize(imgIn->width,imgIn->height),imgIn->depth,1);
			
			cvSplit( imgIn, IdstB, IdstG, IdstR, NULL );
			cvConvertScale( IdstR, OdstR, color_fix[0][light_index]);
			cvConvertScale( IdstG, OdstG, color_fix[1][light_index]);
			cvConvertScale( IdstB, OdstB, color_fix[2][light_index]);
			cvMerge(OdstB, OdstG, OdstR, NULL, imgIn);
			
			cvCopyImage(imgIn,imgOut);
		}

		if (do_rectification)
		{
			CvMat* R = cvCreateMat(2,3,CV_32FC1);
			CvPoint2D32f center = cvPoint2D32f(imgIn->width/2,imgIn->height/2);
			R =  cv2DRotationMatrix(center, rot_angle, 1, R );
			cvWarpAffine( imgIn, imgOut, R);
		}


		light_index++;
		if (light_index>=num_lights)
			light_index = 0;

		cvSaveImage(currentfilename.c_str(),imgOut); // overwrite existing image
	}

	return true;
}