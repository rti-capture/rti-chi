// Copyright Cultural Heritage Imaging 2008
// HSH (Hemi-Spherical Harmonics) fitter

#include "stdafx.h"

#include "cv.h"
#include "highgui.h"
#include <stdio.h>
#include <math.h>
#include <string.h>
#include <list>
#include <vector>

#include "row_jpeg.h"
#include <iostream>

#include "boost/filesystem.hpp"  

#include "hsh_fitter.h"

using namespace std;
using namespace boost::filesystem;                                         
using namespace boost;

const int MAX_TERMS = 30; // used for static array declarations inside convert_hsh_raw_compressed

/* HshFitter Class Constructors */
HshFitter::HshFitter(string str_main_path, string str_prefix, string str_lights_filename, string str_correction_filename,
					 int order, bool is_row_by_row,  ofstream *output) : 
		str_prefix(str_prefix), str_lights_filename(str_lights_filename), str_correction_filename(str_correction_filename)
			,is_row_by_row(is_row_by_row), order(order), output(output)
{
	this->str_main_path = path(str_main_path).string() + "/" ;
}

HshFitter::HshFitter(string str_main_path, string str_prefix, string str_lights_filename, string str_correction_filename
					 , ofstream *output) :
		str_prefix(str_prefix), str_lights_filename(str_lights_filename), str_correction_filename(str_correction_filename)
			,is_row_by_row(false), order(2), output(output)
{
	this->str_main_path = path(str_main_path).string() + "/" ;
}


/* find_file																			*/
/* return the number and names of the files matching the given prefix at the given path */
/* this is left as an independent utility method										*/

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

/* Reads in the light positions from the 'str_lights_filename'.				*/
/*  also uses num_files which should match the number of lines in the light */
/*  positions file.															*/
/*  Results are saved in the lit_images list.								*/
int HshFitter::read_light_positions() 
{
	ifstream lights_file((str_main_path + str_lights_filename).c_str());

	double lx, ly, lz, mag;
	lights_file >> lx >> ly >> lz; // get the first dummy line out of the way.
	for (int i=0; i<num_files; i++)
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


/* Reads in color correction scaling values from 'str_correction_filename'  */
/* The special lines with 1 1 1 in the file means that the light should not */
/* be used. The variable 'num_used' is updated with the actual number of    */
/* lights used.	*/
int HshFitter::read_color_correction() 
{
	ifstream fix_file((str_main_path + str_correction_filename).c_str());

	if (!fix_file) return num_files; // no corrections file, use all lighting positions

	int count_used = 0;
	for (int i=0; i<num_files; i++)
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

bool HshFitter::read_inputs()
{
	num_files = find_file(str_main_path,str_prefix,filenames);

	if (!num_files) 
		return false;

	lit_images.resize(num_files);
	
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

	read_light_positions();
	
	num_used = read_color_correction();
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

	// load single image to get the height and width of images 
	IplImage * sample = cvLoadImage(lit_images[0].filename.c_str(),1);
	
	if (!sample)
		return false;

	height = sample->height;
	width = sample->width;			// do something if width is not a multiple of 4, otherwise the byte alignment is going to mess you up.
	channels = sample->nChannels;
	
	cvReleaseImage(&sample);

	start_tick = cvGetTickCount();

	fullheight = height;
	float megapix = ((int)height*width/100000)/10.0; 
	*output << "hshfitter for prefix '" << str_prefix << "', using " << num_used << " (out of " << num_files << ") files \r\n";
	*output << " hsh order " << order << ", image size " << height << " x " << width << " (" << megapix << " MP), color channels " << channels << "\r\n";
	*output << " with " << height << " rows largest matrix size will be ( " << num_used << " , " << height*width*channels << " ) , memory requirement " 
		<<  ceil((sizeof(float)*height*width*channels*num_used)/1000000.0) << "MB+ \r\n";

	return true;
}



void HshFitter::stack_all_images() 
{

	int size = height*width;
	
	uchar* matrix_ptr = mat_byte_images->data.ptr;

	for (int i=0; i<num_used; i++) 
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
}

bool HshFitter::open_all_readers()
{
	for (int i=0; i<num_used; i++) 
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

void HshFitter::close_all_readers()
{
	for (int i=0; i<num_used; i++) 
	{
		lit_images[i].reader->FinishReading();
	}
}

void HshFitter::stack_all_rows() 
{

	int size = height*width;
	
	uchar* matrix_ptr = mat_byte_images->data.ptr;

	for (int i=0; i<num_used; i++) 
	{
		for (int j=0; j<height; j++) 
		{
			if( !lit_images[i].reader->ReadRow( mat_byte_row->data.ptr ))
			{
				cout << "Error reading data! Row offset " << j << " File " << lit_images[i].filename << endl;
				//return 0;
			} 

			cvConvertImage(mat_byte_row,mat_byte_row, CV_CVTIMG_SWAP_RB ); // swap the BGR byte order, this is the funtion that needs CV_8UC format!
			memcpy(matrix_ptr,mat_byte_row->data.ptr,width*channels);
			matrix_ptr+=width*channels;
		}
	}

	//return matrix;
}

void HshFitter::matrix_char_to_float() 
{
	int height = mat_byte_images->height;
	int width = mat_byte_images->width;
	int type  = mat_byte_images->type;

	//CvMat* float_matrix = cvCreateMat(height,width,CV_32FC1);

	uchar* src_ptr = mat_byte_images->data.ptr;
	float* dst_ptr = mat_float_images->data.fl;

	for (int i=0; i<height; i++) 
	{
		for (int j=0; j<width; j++)
		{
			(*dst_ptr) = ((*src_ptr)+1)/255.0;
			src_ptr++;
			dst_ptr++;
		}
	}

	//return float_matrix;
}



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

static const double pi = 3.141592653589793238462643383279502884197; 

CvMat* HshFitter::make_hsh_matrix()
{
	int terms = order * order;
	CvMat * a = cvCreateMat(terms,num_used,CV_32FC1);

	for (int i=0; i<num_used; i++) 
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

bool HshFitter::convert_hsh_raw_to_compressed(const char *in_filename, const char * out_filename)
{
	int height, width, channels, order;
	float tmpf[30];
	unsigned char tmpuc[30];
	char buffer[500];


	ofstream savefile(out_filename,ios::binary);

	ifstream infile(in_filename,ios::binary);

	infile.getline(buffer,500);

	int file_type, terms, basis_type, element_size;
	float dummy_scale, dummy_bias;
	infile >> file_type;
	infile >> width >> height >> channels;
	infile >> terms >> basis_type >> element_size;
	infile.getline(buffer,10);

	for (int i=0; i<terms; i++) 
		infile.read((char *)&dummy_scale,sizeof(float)); // scale
	for (int i=0; i<terms; i++) 
		infile.read((char *)&dummy_bias,sizeof(float)); // scale



	//write header
	savefile << "#HSH1.2\r\n";

	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << width << " " << height << " " << channels << "\r\n";
	savefile << terms << " " << 2 << " " << 1 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 1 byte

	//write scaling values for each term
	for (int i=0; i<terms; i++) 
	{
		float diff = max_term[i]-min_term[i];
		savefile.write((char *)&diff,sizeof(float)); // scale
	}
	for (int i=0; i<terms; i++) 
		savefile.write((char *)&min_term[i],sizeof(float)); // bias


	for (int i=0; i<height; i++)
		for (int j=0; j<width; j++)
			for (int c=0; c<channels; c++)
			{
				infile.read((char *)&tmpf,sizeof(float)*terms);
				for (int t=0; t<terms; t++) 
				{
					float value = tmpf[t];
					value = ((value-min_term[t])/(max_term[t]-min_term[t]))*255;
					tmpuc[t] = value;
				}
				savefile.write((char *)&tmpuc,sizeof(unsigned char)*terms);
			}

	infile.close();
	savefile.close();
	return true;
}


bool HshFitter::hsh_save(ofstream &savefile, CvMat *hsh_matrix) 
{
	int terms = hsh_matrix->height;
	int pixels = hsh_matrix->width;

	for (int i=0; i<terms; i++) 
	{
		find_min_max(hsh_matrix,i,min_term[i],max_term[i]);

		float * float_ptr = hsh_matrix->data.fl + pixels*i;		
		//uchar* char_ptr = hsh_matrix->data.ptr + hsh_matrix->step*i;
		float diff = max_term[i]-min_term[i];

		//this loop goes from the first to the last pixel in an image, it's in row-by-row order
		for (int j=0; j<pixels; j++)
		{
			float value = (*float_ptr);
			value = ((value-min_term[i])/diff)*255;
			(*float_ptr) = value;
			float_ptr++;
		}
	}
	savefile << "#HSH1.2\r\n";

	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << width << " " << height << " " << channels << "\r\n";
	savefile << terms << " " << 2 << " " << 1 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 1 byte


	//write scaling values for each term
	for (int i=0; i<terms; i++) 
	{
		float diff = max_term[i]-min_term[i];
		savefile.write((char *)&diff,sizeof(float)); // scale
	}
	for (int i=0; i<terms; i++) 
		savefile.write((char *)&min_term[i],sizeof(float)); // bias


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

	return true;
}


void HshFitter::hsh_save_uncompressed_header(ofstream &savefile) 
{
	//write the header
	savefile << "#HSH1.2\r\n";

	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << width << " " << fullheight << " " << channels << "\r\n";
	savefile << order*order << " " << 2 << " " << 4 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 4 bytes (float)

	//write scaling values for each term
	float scale = 1, bias = 0;
	for (int i=0; i<order*order; i++) 
		savefile.write((char *)&scale,sizeof(float));
	for (int i=0; i<order*order; i++) 
		savefile.write((char *)&bias,sizeof(float));
}

bool HshFitter::hsh_save_uncompressed(ofstream &savefile, CvMat *hsh_matrix) 
{
	int terms = hsh_matrix->height;
	int pixels = hsh_matrix->width;


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
			if (is_compressed) // compressed in row-by-row saving means we have to track min-max and do a second pass
			{
				float value = (*term_ptr[t]);
				if (value<min_term[t]) min_term[t]=value;
				if (value>max_term[t]) max_term[t]=value;
			}
			term_ptr[t]++;
		}
	}

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




void HshFitter::compute_loop() 
{
	ofstream outfile;
	if (is_row_by_row) 
	{
		if (is_compressed) // row-by-row and compressed, we'll save to a temporary file and then convert
			outfile.open("temp.hsh",ios::binary);
		else			// no temporary files, the uncompressed file is the output
			outfile.open(str_output_filename.c_str(),ios::binary);
		height = 1;
		open_all_readers();
		hsh_save_uncompressed_header(outfile);
		//outfile.close();
		//		convert_hsh_raw_to_compressed("temp.hsh",str_output_filename.c_str());
	}
	else
	{
		// the 'all in memory' case
		outfile.open(str_output_filename.c_str(),ios::binary);
	}

	if (is_compressed)  // initialize minimum and maximum bounds for 'compressed' format
	{
		min_term.resize(order*order);
		max_term.resize(order*order);
		for (int i=0;i<order*order;i++) 
		{
			min_term[i] = FLT_MAX;
			max_term[i] = -FLT_MAX;
		}
	}

	term_ptr.resize(order*order); // used to track term blocks when saving HSHs

	// do calculations once to compute the HSH coefficient matrix
	CvMat* mat_Afirst = make_hsh_matrix();

	CvMat* mat_A = cvCreateMat(order*order, order*order, CV_32FC1);
	cvMulTransposed(mat_Afirst,mat_A,0);

	CvMat* mat_B = cvCreateMat(order*order, height*width*channels, CV_32FC1);

	// preallocate some large and / or reusable matrices
	mat_byte_row = cvCreateMat( 1, width, CV_8UC3 );  // there is a 3-channel assumption here!
	mat_byte_images = cvCreateMat(num_used, height*width*channels, CV_8UC1);;
	mat_float_images = cvCreateMat(num_used, height*width*channels, CV_32FC1);;
	mat_solve = cvCreateMat(order*order, height*width*channels, CV_32FC1);


	for (int row=0; row<fullheight; row+=height)
	{
		if (is_row_by_row) 
		{
			stack_all_rows();
			//cout << "Row " << row << " image matrix created!" << endl;
		}
		else
		{
			stack_all_images();
			//cout << "Single large image matrix created! " << endl;
		}

		matrix_char_to_float();

		//save_single_image(blarge_float,63,height,width);
		//return 0;

		cvMatMul(mat_Afirst,mat_float_images,mat_B);

		int singular = cvSolve(mat_A,mat_B,mat_solve);

		if (singular == 0)
			*output << "Singular matrix at row " << row << "/r/n";

		
		if (is_row_by_row)
			hsh_save_uncompressed(outfile,mat_solve);
		else
			hsh_save(outfile,mat_solve);	

	}
	outfile.close();

	if (is_row_by_row)
	{
		close_all_readers();
		if (is_compressed) // if row-by-row and compressed, we have to resave the file as compressed.
			convert_hsh_raw_to_compressed("temp.hsh",str_output_filename.c_str());
	}

	cvReleaseMat(&mat_byte_images);
	cvReleaseMat(&mat_float_images);
	cvReleaseMat(&mat_solve);
	cvReleaseMat(&mat_Afirst);
	cvReleaseMat(&mat_A);
	cvReleaseMat(&mat_B);

	int64 end_tick = cvGetTickCount();

	int seconds_spent = (end_tick-start_tick)/(cvGetTickFrequency()*1000000);
	*output << "done. time spent " << seconds_spent << "s \r\n";

}