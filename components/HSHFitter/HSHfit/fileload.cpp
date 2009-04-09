//#define BOOST_ALL_DYN_LINK 
//#define BOOST_THREAD_USE_DLL
//#define BOOST_LIB_DIAGNOSTIC


#include "cv.h"
#include "highgui.h"
#include <stdio.h>
#include <math.h>
#include <string.h>
#include <list>
#include <vector>
//#include <boost/algorithm/string.hpp>


#include "row_jpeg.h"
#include <iostream>

#include "boost/filesystem.hpp"   // includes all needed Boost.Filesystem declarations

using namespace std;
using namespace boost::filesystem;                                         
using namespace boost;

#define MAX_HSH_TERMS 25
IplImage* limg = 0;
int height, width; // globals, where should these go?

class LitImage {
public:
	bool used;		   // is this a valid image
	double lx, ly, lz; // lighting angle
	double scale_r, scale_g, scale_b; // color correction scales
	string filename;
	RowJpegReader* reader;
};

int find_file( const path & dir_path,         // in this directory,
                const std::string & prefix, list<string> & filenames)          
{
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

int read_light_positions(const path& filepath, const string & lights_filename, int n, vector<LitImage> & lit_images) 
{
	path lights_file_path(filepath / lights_filename);
	ifstream lights_file(lights_file_path.string().c_str());
	//lights_file.open(lights_filename.c_str());
	double lx, ly, lz, mag;
	lights_file >> lx >> ly >> lz; // get the first dummy line out of the way.
	for (int i=0; i<n; i++)
	{
		lights_file >> lx >> ly >> lz;
		mag = sqrt(lx*lx + ly*ly + lz*lz);
		lit_images[i].lx = lx/mag;
		lit_images[i].ly = ly/mag;
		lit_images[i].lz = lz/mag;
	}
	lights_file.close();
	return 0;
}


int read_color_correction(const path & filepath, const string & fix_filename, int n, vector<LitImage> & lit_images) 
{
	path fix_file_path(filepath / fix_filename);
	ifstream fix_file(fix_file_path.string().c_str());
	//fix_file.open(fix_filename.c_str());

	if (!fix_file) return n; // no corrections file, use all lighting positions

	int count_used = 0;
	for (int i=0; i<n; i++)
	{
		fix_file >> lit_images[i].scale_r >> lit_images[i].scale_g >> lit_images[i].scale_b;
		if (lit_images[i].scale_r == 1 && lit_images[i].scale_g == 1 &&  lit_images[i].scale_b == 1)
		{
			lit_images[i].used = false;
		}
		else
		{
			count_used++;
		}
		//cout << i << ": " << lit_images[i].filename << " (" << lit_images[i].lx << "," << lit_images[i].ly << "," << lit_images[i].lz 
		//	 << ") (" << lit_images[i].scale_r << "," << lit_images[i].scale_g << "," << lit_images[i].scale_b << ")" << endl;
	}
	fix_file.close();
	return count_used;
}

CvMat* stack_all_images(int n, vector<LitImage> & lit_images, int height, int width, int channels) 
{
	CvMat * matrix = cvCreateMat(n, height*width*channels, CV_8UC1);

	int size = height*width;
	
	uchar* matrix_ptr = matrix->data.ptr;

	for (int i=0; i<n; i++) 
	{
		IplImage *image = cvLoadImage(lit_images[i].filename.c_str(),1);
		cvConvertImage(image,image, CV_CVTIMG_SWAP_RB );  // has a 3-channel assumption for the BGR swap!

		memcpy(matrix_ptr,image->imageData,size*channels);

		matrix_ptr+=size*channels;

		/*
		cvSplit( image, dstB, dstG, dstR, NULL );

		memcpy(matrix_ptr,dstR->data.ptr,size);
		matrix_ptr+=size;
		memcpy(matrix_ptr,dstG->data.ptr,size);
		matrix_ptr+=size;
		memcpy(matrix_ptr,dstB->data.ptr,size);
		matrix_ptr+=size;		
		*/

		cvReleaseImage(&image);
	}

	return matrix;
}

bool open_all_readers(int n, vector<LitImage> & lit_images)
{
	for (int i=0; i<n; i++) 
	{
		lit_images[i].reader = new RowJpegReader(lit_images[i].filename.c_str());
		if (lit_images[i].reader->ReadHeader())
		{
			lit_images[i].reader->StartReading(lit_images[i].reader->IsColor());
		}
		else
		{
			cout << "Error opening " << lit_images[i].filename << endl;
			return false;
		}
	}
	return true;
}

void close_all_readers(int n, vector<LitImage> & lit_images)
{
	for (int i=0; i<n; i++) 
	{
		lit_images[i].reader->FinishReading();
	}
}

CvMat* stack_all_rows(int n, vector<LitImage> & lit_images, int height, int width, int channels) 
{
	CvMat * matrix = cvCreateMat(n, height*width*channels, CV_8UC1);

	int size = height*width;
	
	uchar* matrix_ptr = matrix->data.ptr;

	CvMat *row = cvCreateMat( 1, width, CV_8UC3 );  // there is a 3-channel assumption here!

	for (int i=0; i<n; i++) 
	{
		for (int j=0; j<height; j++) 
		{
			if( !lit_images[i].reader->ReadRow( row->data.ptr ))
			{
				cout << "Error reading data! Row offset " << j << " File " << lit_images[i].filename << endl;
				return 0;
			} 

			cvConvertImage(row,row, CV_CVTIMG_SWAP_RB ); // swap the BGR byte order, this is the funtion that needs CV_8UC format!
			memcpy(matrix_ptr,row->data.ptr,width*channels);
			matrix_ptr+=width*channels;
		}
	}

	return matrix;
}

CvMat* matrix_char_to_float(CvMat* char_matrix) 
{
	int height = char_matrix->height;
	int width = char_matrix->width;
	int type  = char_matrix->type;

	CvMat* float_matrix = cvCreateMat(height,width,CV_32FC1);

	uchar* src_ptr = char_matrix->data.ptr;
	float* dst_ptr = float_matrix->data.fl;

	for (int i=0; i<height; i++) 
	{
		for (int j=0; j<width; j++)
		{
			(*dst_ptr) = ((*src_ptr)+1)/255.0;
			src_ptr++;
			dst_ptr++;
		}
	}

	return float_matrix;
}

static const double pi = 3.141592653589793238462643383279502884197; 

void show_matrix(CvMat* mat) 
{
	cout << " --------------------------------------------------- " << endl;
	for (int i=0; i<mat->height; i++) 
	{
		for (int j=0; j<mat->width; j++)
		{
			cout <<  cvmGet(mat,i,j) << "  " ;
		}
		cout << endl;
	}
	cout << endl;
}

CvMat* make_hsh_matrix(int n, vector<LitImage> & lit_images, int order)
{
	int terms = order * order;
	CvMat * a = cvCreateMat(terms,n,CV_32FC1);

	for (int i=0; i<n; i++) 
	{
		double& lx = lit_images[i].lx;
		double& ly = lit_images[i].ly;
		double& lz = lit_images[i].lz;
		double phi = atan2(ly,lx);
		if (phi<0) phi = 2*pi+phi;
		double theta = acos(lz);

		// depending on the specified order of HSH, fill in the number of terms
		// based on lx, ly and lz
		cvmSet(a,0,i, 1/sqrt(2*pi));
		cvmSet(a,1,i, sqrt(6/pi)      *  (cos(phi)*sqrt(cos(theta)-cos(theta)*cos(theta))));
		cvmSet(a,2,i, sqrt(3/(2*pi)) *  (-1 + 2*cos(theta)));
		cvmSet(a,3,i, sqrt(6/pi)      *  (sqrt(cos(theta) - pow(cos(theta),2))*sin(phi)));

		if(order>2)
		{
			cvmSet(a,4,i, sqrt(30/pi)     *  (cos(2*phi)*(-cos(theta) + pow(cos(theta),2))));
			cvmSet(a,5,i, sqrt(30/pi)     *  (cos(phi)*(-1 + 2*cos(theta))*sqrt(cos(theta) - pow(cos(theta),2))));
			cvmSet(a,6,i, sqrt(5/(2*pi)) *  (1 - 6*cos(theta) + 6*pow(cos(theta),2)));
			cvmSet(a,7,i, sqrt(30/pi)     *  ((-1 + 2*cos(theta))*sqrt(cos(theta) - pow(cos(theta),2))*sin(phi)));
			cvmSet(a,8,i, sqrt(30/pi)     *  ((-cos(theta) + pow(cos(theta),2))*sin(2*phi)));
		}

		if(order>3)                    
		{
			cvmSet(a, 9,i, 2*sqrt(35/pi)  * cos(3*phi)*pow((cos(theta) - pow(cos(theta),2)),(3/2)));
			cvmSet(a,10,i, (sqrt(210/pi)  * cos(2*phi)*(-1 + 2*cos(theta))*(-cos(theta) + pow(cos(theta),2))));
			cvmSet(a,11,i, 2*sqrt(21/pi)  * cos(phi)*sqrt(cos(theta) - pow(cos(theta),2))*(1 - 5*cos(theta) + 5*pow(cos(theta),2)));
			cvmSet(a,12,i, sqrt(7/(2*pi)) * (-1 + 12*cos(theta) - 30*pow(cos(theta),2) + 20*pow(cos(theta),3)));
			cvmSet(a,13,i, 2*sqrt(21/pi)  * sqrt(cos(theta) - pow(cos(theta),2))*(1 - 5*cos(theta) + 5*pow(cos(theta),2))*sin(phi));
			cvmSet(a,14,i, (sqrt(210/pi)  * (-1 + 2*cos(theta))*(-cos(theta) + pow(cos(theta),2))*sin(2*phi)));
			cvmSet(a,15,i, 2*sqrt(35/pi)  * pow((cos(theta) - pow(cos(theta),2)),(3/2))*sin(3*phi));
		}
	}
/*
	cout << " --------------------------------------------------- " << endl;
	for (int i=0; i<n; i++) 
	{
		for (int j=0; j<terms; j++)
		{
			cout <<  cvmGet(a,j,i) << "  " ;
		}
		cout << endl;
	}
	*/
	return a;

}

void find_min_max(CvMat * matrix, int row, float& min, float&max) 
{
	int height = matrix->height;
	int width = matrix->width;
	float* data_ptr = matrix->data.fl+(width*row);
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

bool hsh_save(char * save_filename, CvMat *hsh_matrix, int height,int width, int channels,int order) 
{
	cout << "Saving " << save_filename << "...";

	int terms = hsh_matrix->height;
	int pixels = hsh_matrix->width;

	vector<float> min_term(terms), max_term(terms);

	for (int i=0; i<terms; i++) 
	{
		find_min_max(hsh_matrix,i,min_term[i],max_term[i]);

		float * float_ptr = hsh_matrix->data.fl + pixels*i;		
		//uchar* char_ptr = hsh_matrix->data.ptr + hsh_matrix->step*i;
		float diff = max_term[i]-min_term[i];

		//this loop goes from the first to the last pixel in an image, its in row-by-row order
		for (int j=0; j<pixels; j++)
		{
			float value = (*float_ptr);
			value = ((value-min_term[i])/diff)*255;
			(*float_ptr) = value;
			float_ptr++;
		}
	}

	ofstream savefile(save_filename,ios::binary);
	//write the header
	savefile << "#HSH1.1\n";
	savefile << "#data format: (minvals)*order^2 (maxvals)*order^2 (uchar)*bands*w*h \n";

	//write the dimensions of the HSH
	savefile.write((char *)&width,sizeof(int));
	savefile.write((char *)&height,sizeof(int));
	savefile.write((char *)&channels,sizeof(int));
	savefile.write((char *)&order,sizeof(int));

	//write scaling values for each term
	for (int i=0; i<terms; i++) 
		savefile.write((char *)&min_term[i],sizeof(float));
	for (int i=0; i<terms; i++) 
		savefile.write((char *)&max_term[i],sizeof(float));


	vector<float *> term_ptr(terms);
	for (int t=0; t<terms; t++) 
	{
		term_ptr[t] = hsh_matrix->data.fl + pixels*t;
	}

	//write the raw data
	//this loop goes from the first to the last pixel in an image, its in row-by-row order
	for (int i=0; i<pixels; i++)
	{
		for (int t=0; t<terms; t++) 
		{
			uchar char_value = (*term_ptr[t]);
			savefile.write((char*)&char_value,1);
			term_ptr[t]++;
		}
	}
	savefile.close();

	cout << ".done." << endl;
	return true;
}

void hsh_save_uncompressed_header(ofstream &savefile, int height,int width, int channels,int order) 
{
	//write the header
	savefile << "#HSH1.1\n";
	savefile << "#data format: (float)*terms*channels*w*h \n";

	//write the dimensions of the HSH
	savefile.write((char *)&width,sizeof(int));
	savefile.write((char *)&height,sizeof(int));
	savefile.write((char *)&channels,sizeof(int));
	savefile.write((char *)&order,sizeof(int));
}

bool hsh_save_uncompressed(ofstream &savefile, CvMat *hsh_matrix, int height,int width, int channels,int order) 
{
	//cout << "writing to file...";

	int terms = hsh_matrix->height;
	int pixels = hsh_matrix->width;


	vector<float *> term_ptr(terms);
	for (int t=0; t<terms; t++) 
	{
		term_ptr[t] = hsh_matrix->data.fl + pixels*t;
	}

	//write the raw data
	//this loop goes from the first to the last pixel in an image, its in row-by-row order
	for (int i=0; i<pixels; i++)
	{
		for (int t=0; t<terms; t++) 
		{
			savefile.write((char *)term_ptr[t],sizeof(float));
			term_ptr[t]++;
		}
	}

	//cout << ".done." << endl;
	return true;
}



void save_single_image(CvMat* large_float, int row, int height, int width)
{
	IplImage *image = cvCreateImage(cvSize(width,height),IPL_DEPTH_8U,3);
	float * large_ptr = large_float->data.fl + large_float->width*row;
	uchar * char_ptr = (uchar *) image->imageData;

	for (int i=0; i<height; i++) 
		for (int j=0; j<width; j++)
			for (int c=0; c<3; c++) 
			{
				float val = (*large_ptr);
				(*char_ptr) = val;
				large_ptr++;
				char_ptr++;
			}

	//memcpy(char_ptr,large_ptr,width*height*3);
	cvSaveImage("test.png",image);
	cvReleaseImage(&image);
}

int main(int argc, char** argv)
{
	int n,c; 
	list<string> filenames;
	bool row_by_row = false;
	string filepath = ".";
	string prefix = "capture_";
	int order = 3;
	string lamps_filename = "lamps.lp";
	string correction_filename = "lampcorrection.dat";

	if (argc == 7)
	{
		filepath = argv[1];
		prefix = argv[2];
		order = atoi(argv[3]);
		lamps_filename = argv[4];
		correction_filename = argv[5];
		if (strcmp(argv[6],"true")==0)
			row_by_row = true;
		else
			row_by_row = false;
	}
	else 
	{
		cout << "Usage : hshfitc <path> <prefix> <order> <light_positions_file> <color_correction_file> <use_row_based_reader> " << endl;
		cout << "  Ex1  : hshfitc c:\chidata\ capture_ 3 lamps.lp lampcorrection.dat true " << endl;
		cout << "  Ex2  : hshfitc . test_ 2 lamps.lp x.dat false " << endl;
		return 0;
	}

	path current(filepath);
	n = find_file(current,prefix,filenames);

	vector<LitImage> lit_images(n);
	
	filenames.sort();
	list<string>::iterator it;
	int count = 0;
	for ( it=filenames.begin() ; it != filenames.end(); it++ )
	{
		//cout << " " << *it <<endl;
		lit_images[count].filename.assign(*it);
		lit_images[count].used = true;
		count++;
	}

	read_light_positions(current,lamps_filename,n,lit_images);
	

	int num_used = read_color_correction(current,correction_filename,n,lit_images);
	//cout << "Number used : " << num_used << endl;

	vector<LitImage>::iterator li_it;
	for (li_it=lit_images.begin(); li_it != lit_images.end(); ) 
	{
		if (!(*li_it).used) 
			li_it = lit_images.erase(li_it);
		else 
			li_it++;
	}

	//cout << "Vector count : " << lit_images.size() << endl;

	IplImage * sample = cvLoadImage(lit_images[0].filename.c_str(),1);
	int height = sample->height;
	int width = sample->width;
	int channels = sample->nChannels;
	// do something if width is not a multiple of 4, otherwise the byte alignment is going to mess you up.
	cvReleaseImage(&sample);

	int64 start_tick = cvGetTickCount();

	int realheight = height;
	if (row_by_row) 
	{
		height = 1;
		open_all_readers(num_used,lit_images);
	}
	cout << "hshfitter for prefix '" << prefix << "', using " << num_used << " (out of " << n << ") files" << endl;
	cout << " hsh order " << order << ", image height " << realheight << ", image width " << width << ", color channels " << channels << endl;
	cout << " reading " << height << " row(s) at a time" << endl;
	cout << " largest matrix size ( " << num_used << " , " << height*width*channels << " ) , memory requirement " 
		<<  ceil((sizeof(float)*height*width*channels*num_used)/1000000.0) << "MB+ " << endl;


	// do calculations once to compute the HSH coefficient matrix
	CvMat* a2 = make_hsh_matrix(num_used,lit_images,order);

	CvMat* a3 = cvCreateMat(order*order, order*order, CV_32FC1);
	cvMulTransposed(a2,a3,0);
	//cout << "A3 made." << endl;
	//show_matrix(a3);

	CvMat* b3 = cvCreateMat(order*order, height*width*channels, CV_32FC1);

	ofstream outfile("output.hsh",ios::binary);
	hsh_save_uncompressed_header(outfile,realheight,width,channels,order);

	for (int row=0; row<realheight; row+=height)
	{
		CvMat* blarge;
		if (row_by_row) 
		{
			blarge = stack_all_rows(num_used,lit_images,height,width,channels);
			//cout << "Row " << row << " image matrix created!" << endl;
		}
		else
		{
			blarge = stack_all_images(num_used,lit_images,height,width,channels);
			//cout << "Single large image matrix created! " << endl;
		}

		CvMat* blarge_float = matrix_char_to_float(blarge);
		cvReleaseMat(&blarge);
		//cout << "Doubled. Memory usage estimate = " << sizeof(float)*height*width*channels*num_used << endl;

		//save_single_image(blarge_float,63,height,width);
		//return 0;

		//cout << " ( " << a2->height << "," << a2->width << ") . ( " << blarge_float->height << "," << blarge_float->width << ") " << endl;
		cvMatMul(a2,blarge_float,b3);
		cvReleaseMat(&blarge_float);
		//cout << "b3 made." << endl;

		CvMat* x = cvCreateMat(order*order, height*width*channels, CV_32FC1);
		int singular = cvSolve(a3,b3,x);

		if (singular == 0)
			cout << "Singular matrix!!!!!! " << endl;

		//hsh_save("output.hsh",x,height,width,channels,order);
		hsh_save_uncompressed(outfile,x,height,width,channels,order);
		cvReleaseMat(&x);
	}
	outfile.close();

	int64 end_tick = cvGetTickCount();
	int seconds_spent = (end_tick-start_tick)/(cvGetTickFrequency()*1000000);
	cout << "done. time spent " << seconds_spent << "s" << endl;

	if (row_by_row)
		close_all_readers(num_used,lit_images);

	cvReleaseMat(&b3);
	cvReleaseMat(&a3);
	cvReleaseMat(&a2);

	cin >> c;
	return 0;
}







/*


	
    RowJpegReader* reader = new RowJpegReader("pic1.jpg");
    RowJpegReader* reader2 = new RowJpegReader("pic2.jpg");
    //CvMat hdr, *matrix = 0;
    int depth = 8;

    CvSize size;
    int iscolor;
    int cn;
	bool load_as_matrix = true;

    if( !reader->ReadHeader() )
		cout << " Error reading header \n ";
    if( !reader2->ReadHeader() )
		cout << " Error reading header Im2 \n ";

    size.width = reader->GetWidth();
    size.height = reader->GetHeight()/2;

    iscolor = reader->IsColor();

    cn = iscolor ? 3 : 1;

	int type = CV_8U;

	IplImage* image = cvCreateImage( size, IPL_DEPTH_8U, cn );
	IplImage* image2 = cvCreateImage( size, IPL_DEPTH_8U, cn );

	if (!reader->StartReading(iscolor))
		cout << " Error starting \n ";
	if (!reader2->StartReading(iscolor))
		cout << " Error starting Im2 \n ";

	CvMat *row = cvCreateMat( 1, size.width, CV_MAKETYPE(type, cn) );

	for (int i=0; i<size.height; i++) {

		if( !reader->ReadRow( row->data.ptr ))
		{
			cout << "Error reading data! Row " << i << endl;
		} 
		
		int p = 0;
		for (int j=0; j<size.width; j++) {
			uchar* temp_ptr = &((uchar*)(image->imageData + image->widthStep*i))[j*3];
			temp_ptr[0]=row->data.ptr[p++];	
			temp_ptr[1]=row->data.ptr[p++];	
			temp_ptr[2]=row->data.ptr[p++];
		}


		if( !reader2->ReadRow( row->data.ptr ))
		{
			cout << "Error reading data! Image 2 Row " << i << endl;
		} 
		
		p = 0;
		for (int j=0; j<size.width; j++) {
			uchar* temp_ptr = &((uchar*)(image2->imageData + image2->widthStep*i))[j*3];
			temp_ptr[0]=row->data.ptr[p++];	
			temp_ptr[1]=row->data.ptr[p++];	
			temp_ptr[2]=row->data.ptr[p++];
		}
	}


	cvNamedWindow( "Image1", 1 );
	cvShowImage("Image1",image);


	cvNamedWindow( "Image2", 1 );
	cvShowImage("Image2",image2);

	c = cvWaitKey(0);
	cvDestroyWindow("Image1");
	cvDestroyWindow("Image2");

	reader->FinishReading();
	reader2->FinishReading();

	delete reader;
	delete reader2;

    cvReleaseImage( &image );
    cvReleaseImage( &image2 );

	return 0;
*/





/*

	IplImage * im = cvLoadImage("pic1.png",1);
	CvMat * dstR = cvCreateMat(h,w,CV_8UC1 );
	CvMat * dstG = cvCreateMat(h,w,CV_8UC1 );
	CvMat * dstB = cvCreateMat(h,w,CV_8UC1 );

	CvMat * imW = cvCreateMat(h,w,CV_8UC1);
	IplImage *imI = cvCreateImage(cvSize(w,h*3),IPL_DEPTH_8U,1);

	cvSplit( im, dstB, dstG, dstR, NULL );


	CvMat gray_mat_hdr;
	IplImage gray_img_hdr, *gray_img;
	cvReshape( dstG, &gray_mat_hdr, 0,h*w );
	gray_img = cvGetImage( &gray_mat_hdr, &gray_img_hdr );

	cout << " (h,w) " << h << " " << w << " ws " << im->widthStep << endl;

	cout << " new (h,w) " << gray_img->height << " " << gray_img->width << " ws " << gray_img->widthStep << endl;


	//CvMat gray_mat_hdr;
	//IplImage gray_img_hdr, *gray_img;
	//cvReshape( im, &gray_mat_hdr, 1 ,1 );
	//cvReshapeND( im, &gray_img_hdr, 1, 0, 0 );
	//gray_img = cvGetImage( &gray_mat_hdr, &gray_img_hdr );
	//gray_img = (IplImage*)cvReshapeND( im, &gray_img_hdr, 1, 0, 0 );

	cvNamedWindow( "Image1", 1 );
	cvShowImage("Image1",imI);

	int cc = cvWaitKey(0);
	cvDestroyWindow("Image1");


*/