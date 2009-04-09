//
#include "cv.h"
#include "highgui.h"

#include <string.h>
#include <iostream>

#include "hsh_fitter.h"
#include "axis_rectify.h"

using namespace std;

int main(int argc, char** argv)
{

	// TESTING

	AxisRectify a("c:/prabath/data/vase/","v_0300","lampcorrection.dat","axis.txt");
	a.read_axis_rot();
	a.read_color_correction();
	a.full_rectification_loop();

	/*
	IplImage* I = cvLoadImage("test.jpg");
	IplImage* J = cvCreateImage(cvSize(I->width,I->height),I->depth,I->nChannels);
	CvMat* R = cvCreateMat(2,3,CV_32FC1);

	CvPoint2D32f center = cvPoint2D32f(I->width/2,I->height/2);
	double angle = 10;
	double scale = 0.5;

	//cvConvertScale( I, J, 2);
	IplImage* IdstR = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	IplImage* IdstG = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	IplImage* IdstB = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	IplImage* OdstR = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	IplImage* OdstG = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	IplImage* OdstB = cvCreateImage(cvSize(I->width,I->height),I->depth,1);
	cvSplit( I, IdstB, IdstG, IdstR, NULL );
	cvConvertScale( IdstR, OdstR, 1);
	cvConvertScale( IdstG, OdstG, 1);
	cvConvertScale( IdstB, OdstB, 4);
	cvMerge(OdstB, OdstG, OdstR, NULL, J);

	//R =  cv2DRotationMatrix(center, angle, scale, R );


	//cvWarpAffine( I, J, R);

	cvSaveImage("output.jpg",J);
	*/
	return 0;
	// TESTING END;
	bool row_by_row = true;
	bool compressed = true;
	string filepath;
	string prefix;
	int order;
	string lamps_filename;
	string correction_filename;

	if (argc == 8)
	{
		filepath = argv[1];
		prefix = argv[2];
		order = atoi(argv[3]);
		cout << "order " << order ;
		lamps_filename = argv[4];
		correction_filename = argv[5];
		if (strcmp(argv[6],"true")==0)
			row_by_row = true;
		else
			row_by_row = false;

		if (strcmp(argv[7],"true")==0)
			compressed = true;
		else
			compressed = false;
	}
	else 
	{
		cout << "Usage : hshfitc <path> <prefix> <order> <light_positions_file> <color_correction_file> <use_row_based_reader> " << endl;
		cout << "  Ex1  : hshfitc c:\chidata\ capture_ 3 lamps.lp lampcorrection.dat true " << endl;
		cout << "  Ex2  : hshfitc . test_ 2 lamps.lp x.dat false " << endl;
		return 0;
	}

	ostream *my = &cout;

	HshFitter hshfit(filepath, prefix, lamps_filename, correction_filename, order, row_by_row, (ofstream *) &cout);

	hshfit.set_compressed(compressed);
	hshfit.set_output_filename("simple.hsh");
	hshfit.read_inputs();
	hshfit.compute_loop();
	cin >> order;
}