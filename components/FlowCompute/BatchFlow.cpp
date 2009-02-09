#include "stdafx.h"

#include "Openvis3d.h"
#include "OpenCVImageAdapter.h"
#include <cv.h>
#include <highgui.h>
#include <fstream>

#include <vector>
#include <iostream>
#include <sstream>
#include "boost/filesystem.hpp"  
#include "BatchFlow.h"

using namespace std;
using namespace boost::filesystem;                                         
using namespace boost;



 inline std::string stringify(int x)
 {
   std::ostringstream o;
   o << x;
   return o.str();
 } 

int find_file( const string & str_path, const string & prefix, list<string> & filenames)          
{
  path dir_path(str_path);
  int filecount = 0;	
  if ( !exists( dir_path ) ) return 0;
  directory_iterator end_itr; 

  
  for ( directory_iterator itr( dir_path ); itr != end_itr; ++itr )
  {
    if ( !is_directory(itr->status()) )
	{
		if (itr->leaf().compare(0,prefix.length(),prefix) == 0) 
		{
			//cout << " ** ";
			filenames.push_back(itr->path().string());
			filecount++;
		}
      
	  //cout << itr->path() << endl;
    }
  }
  return filecount;
}

void find_min_max(IplImage * matrix, int row, float& min, float&max) 
{
	int height = matrix->height;
	int width = matrix->width;
	float* data_ptr = (float*)matrix->imageData+(width*row);
	min = FLT_MAX;
	max = -FLT_MAX;
	for (int i=0;i<width;i++) 
	{
		float current = (*data_ptr);
		if (current<min) min = current;
		if (current>max) max = current;
		data_ptr++;
	}
}


bool flow_save(string save_filename, IplImage *hsh_matrix, float scale, float bias) 
{
	ofstream savefile;
	savefile.open(save_filename.c_str(),ios::binary);
	int height = hsh_matrix->height;
	int width = hsh_matrix->width;

	float min_term, max_term;

	find_min_max(hsh_matrix,0,min_term,max_term);
	
	//write the header
	savefile << "#FLOW1.0\n";

	savefile << width << " " << height << "\r\n";
	savefile << 1 << " " << scale << " " << bias << "\r\n";   // element size, scale and bias

	//write the raw data
	//this loop goes from the first to the last pixel in an image, its in row-by-row order
	for (int i=0; i<height; i++)
	{
		for (int j=0; j<width; j++) 
		{
			float * f_ptr = (float*)hsh_matrix->imageData + width*i + j;
			float value = (*f_ptr);
			uchar char_value = (uchar)(value * 255);  // the float-value is in the range 0..1, make it a byte.
			savefile.write((char*)&char_value,1);
		}
	}

	return true;

	savefile.close();
}



void doOpticalFlow(const char*imgfilename1, const char*imgfilename2, double minshiftX, double maxshiftX, double minshiftY, double maxshiftY, string outprefix1, string outprefix2)
{
  //read input images
  IplImage*img1 = cvLoadImage(imgfilename1);
  IplImage*img2 = cvLoadImage(imgfilename2);

  //create output images
  CvSize sz;
  sz.height = img1->height;
  sz.width  = img1->width;
  IplImage*imgU1 = cvCreateImage(sz,IPL_DEPTH_64F,1);
  IplImage*imgV1 = cvCreateImage(sz,IPL_DEPTH_64F,1);
  IplImage*imgO1 = cvCreateImage(sz,IPL_DEPTH_64F,1);
  IplImage*imgU2 = cvCreateImage(sz,IPL_DEPTH_64F,1);
  IplImage*imgV2 = cvCreateImage(sz,IPL_DEPTH_64F,1);
  IplImage*imgO2 = cvCreateImage(sz,IPL_DEPTH_64F,1);

  if(img1 && img2 && imgU1 && imgV1 && imgO1 && imgU2 && imgV2 && imgO2)
  {
    //wrap all the input and output images in OpenCVImageAdapter, so that they can be
    //accessed by OpenVis3D
    OpenCVImageAdapter*ovaImg1 = new OpenCVImageAdapter(img1);
    OpenCVImageAdapter*ovaImg2 = new OpenCVImageAdapter(img2);
    OpenCVImageAdapter*ovaImgU1 = new OpenCVImageAdapter(imgU1);
    OpenCVImageAdapter*ovaImgV1 = new OpenCVImageAdapter(imgV1);
    OpenCVImageAdapter*ovaImgO1 = new OpenCVImageAdapter(imgO1);
    OpenCVImageAdapter*ovaImgU2 = new OpenCVImageAdapter(imgU2);
    OpenCVImageAdapter*ovaImgV2 = new OpenCVImageAdapter(imgV2);
    OpenCVImageAdapter*ovaImgO2 = new OpenCVImageAdapter(imgO2);

    //create Birchfield-Tomasi local matcher and set its default parameter alpha to 20.0
    BTLocalMatcherT<double> btmatcher;
    double alpha[] = {20.0};
    btmatcher.setParams(1,alpha);

    //create global diffusion-based optical flow algorithm instance
    OvFlowDiffuseMatcherT<double> flowDiffuseMatcher;

    //create general optical flow algorithm execution manager instance
    OvFlowT<double> flowManager;
    flowManager.setLocalImageMatcher(btmatcher);
    flowManager.setGlobalMatcher(flowDiffuseMatcher);

	cout << "Running optical flow ..." << imgfilename1 << " and " << imgfilename2 << "\r\n";

    //EXECUTE optical flow estimation
    flowManager.doOpticalFlow(*ovaImg1, *ovaImg2, minshiftX, maxshiftX, minshiftY, maxshiftY, *ovaImgU1, *ovaImgV1, *ovaImgO1, *ovaImgU2, *ovaImgV2, *ovaImgO2);



    //DISPLAY RESULTS

    //BEGIN: rescale flow maps to range (0,1) so that they can be displayed by OpenCV
    OvImageT<double> ovtU1, ovtV1, ovtU2, ovtV2;

    ovtU1.copyFromAdapter(*ovaImgU1);
    ovtV1.copyFromAdapter(*ovaImgV1);
    ovtU2.copyFromAdapter(*ovaImgU2);
    ovtV2.copyFromAdapter(*ovaImgV2);

    ovtU1 = (ovtU1-minshiftX)/(maxshiftX-minshiftX);
    ovtV1 = (ovtV1-minshiftY)/(maxshiftY-minshiftY);
    ovtU2 = (ovtU2+maxshiftX)/(-minshiftX+maxshiftX);
    ovtV2 = (ovtV2+maxshiftY)/(-minshiftY+maxshiftY);

    ovtU1.copyToAdapter(*ovaImgU1);
    ovtV1.copyToAdapter(*ovaImgV1);
    ovtU2.copyToAdapter(*ovaImgU2);
    ovtV2.copyToAdapter(*ovaImgV2);
    //END: rescale flow maps to range (0,1) so that they can be displayed by OpenCV

	/*
    cvNamedWindow("U1", CV_WINDOW_AUTOSIZE);
    cvNamedWindow("V1", CV_WINDOW_AUTOSIZE);
    cvNamedWindow("O1", CV_WINDOW_AUTOSIZE);
    cvNamedWindow("U2", CV_WINDOW_AUTOSIZE);
    cvNamedWindow("V2", CV_WINDOW_AUTOSIZE);
    cvNamedWindow("O2", CV_WINDOW_AUTOSIZE);

    cvShowImage("U1", imgU1);
    cvShowImage("V1", imgV1);
    cvShowImage("O1", imgO1);
    cvShowImage("U2", imgU2);
    cvShowImage("V2", imgV2);
    cvShowImage("O2", imgO2);
	*/

	// Save results
	string outfile1 = outprefix1 + "_to_" + outprefix2;
	string outfile2 = outprefix2 + "_to_" + outprefix1;

	flow_save(outfile1,imgU1,(maxshiftX-minshiftX),minshiftX);
	flow_save(outfile2,imgU2,(minshiftX-maxshiftX),minshiftX);

    //release adaptors
    if(ovaImg1) delete ovaImg1;
    if(ovaImg2) delete ovaImg2;
    if(ovaImgU1) delete ovaImgU1;
    if(ovaImgV1) delete ovaImgV1;
    if(ovaImgO1) delete ovaImgO1;
    if(ovaImgU2) delete ovaImgU2;
    if(ovaImgV2) delete ovaImgV2;
    if(ovaImgO2) delete ovaImgO2;
  }

  //release opencv images
  if(img1) cvReleaseImage(&img1);
  if(img2) cvReleaseImage(&img2);
  if(imgU1) cvReleaseImage(&imgU1);
  if(imgV1) cvReleaseImage(&imgV1);
  if(imgO1) cvReleaseImage(&imgO1);
  if(imgU2) cvReleaseImage(&imgU2);
  if(imgV2) cvReleaseImage(&imgV2);
  if(imgO2) cvReleaseImage(&imgO2);
}



int BatchFlow::get_files()
{
	num_files = find_file(str_main_path,str_prefix,filenames);

	if (num_files < 2) 
		return 0;

	filenames.sort();

	return num_files;
}

void BatchFlow::batch_process()
{
	list<string>::iterator it;
	string firstfile, secondfile;
	int count;
	for ( it=filenames.begin(),count=1; it != filenames.end(); it++, count++ )
	{
		secondfile = *it;
		if (count>1)
		{
			doOpticalFlow(firstfile.c_str(),secondfile.c_str(),min_shiftx,max_shiftx,0,0,stringify(count),stringify(count+1));
		}
		firstfile = *it;
	}
}


void BatchFlow::save_descriptor_file()
{
	ofstream descfile(str_descriptor_filename.c_str());
	descfile << num_files  << " " << 1 << " " << num_files << "\r\n";  // <max_views_x> <max_views_y> <actual_viewpoints>
	descfile << 1 << " " << 1 << "\r\n"; // <start_x> <start_y>
	descfile << 1 << " " << 1 << " " << deg_seperation << " " << 0 << "\r\n"; // <use_flow> <turntable_based> <separation_x> <separation_y>

	list<string>::iterator it;
	int count;
	for ( it=filenames.begin(),count = 1; it != filenames.end(); it++, count++ )
	{
		descfile << count << " " << (*it) << "\r\n";
	}

	for (int i=1; i<=num_files; i++)
		descfile << i << " ";

	descfile << "\r\n";

	
	for (int i=1; i<=num_files; i++)
	{
		if (i==1)
			descfile << i << " " << 0 << " " << 0 << " " << 0 << " " << i << "_to_" << i+1 << ".flow" << "\r\n";
		else if (i==num_files)
			descfile << i << " " << 0 << " " << 0 << " " << " " << i << "_to_" << i-1 << ".flow" << 0 << "\r\n";
		else
			descfile << i << " " << 0 << " " << 0 << " " << i << "_to_" << i-1 << ".flow" << " " << i << "_to_" << i+1 << ".flow" << "\r\n";

	}
	
	descfile.close();
}

int main(int argc, char **argv)
{
	BatchFlow b;
	b.str_main_path = argv[1];
	b.str_prefix = argv[2];
	b.max_shiftx = 10;
	b.min_shiftx = -10;
	b.str_descriptor_filename = "descriptor.txt";

	b.get_files();
	b.save_descriptor_file();
	b.batch_process();	

	return 0;
}

