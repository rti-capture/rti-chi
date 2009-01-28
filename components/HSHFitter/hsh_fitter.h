// Copyright Cultural Heritage Imaging 2008
// HSH (Hemi-Spherical Harmonics) fitter

#ifndef _HSH_FITTER_H_
#define _HSH_FITTER_H_

#include "cv.h"
#include <string.h>
#include <list>
#include <vector>
#include "row_jpeg.h"

using namespace std;


class LitImage {
public:
	bool used;						  // is this a valid image
	double lx, ly, lz;				  // lighting angle
	double scale_r, scale_g, scale_b; // color correction scales
	string filename;				  // image taken under this light
	RowJpegReader* reader;			  // row-by-row reader for handling large jpegs
};

class HshFitter
{
public:
	HshFitter(string str_main_path, string str_prefix, string str_lights_filename, string str_correction_filename,int order, bool is_row_by_row, ofstream *output);
	HshFitter(string str_main_path, string str_prefix, string str_lights_filename, string str_correction_filename, ofstream *output);
	~HshFitter(){};
	bool read_inputs();		// reads the input files at the given path & prefix. 
	void compute_loop();	// main computation process. solve for the HSH coefficients

	void set_order(int order) { this->order = order;};
	void set_output_filename (string str_output_filename) {this->str_output_filename = str_output_filename;};
	void set_row_by_row(bool is_row_by_row) {this->is_row_by_row = is_row_by_row;};
	void set_compressed(bool is_compressed) {this->is_compressed = is_compressed;};

	ostream *output;		//  stream for capturing console text output
private:
	list<string> filenames; // the list of files matching the prefix
	int num_files;			// number of files matching the given prefix at the path
	int num_used;			// number of files remaining after removing the ones indicated from color correction file

	bool is_row_by_row;		// is the 'reading and processing of images done on a row by row basis?'
	bool is_compressed;		// are we saving the data in compressed form?

	string str_main_path;		// the main working directory (where all the files are)
	string str_prefix;			// prefix of the files to be used
	string str_lights_filename;	// light positions filename
	string str_correction_filename; // color corrections filename

	string str_output_filename;  // output filename

	int order;					 // the order of the hsh fitting
	vector<LitImage> lit_images; // the information about all light positions, filenames, scale values etc.
	
	int fullheight;				// image height
	int width;					// image width	
	int height;					// 'block' height, number of rows for which the fitting is done per turn
	int channels;				// number of color channels

	int64 start_tick;

	string str_debug_output;	// debug/info output stored in this string

	int read_light_positions();	 // read the light positions file
	int read_color_correction(); // read the color correction file

	void stack_all_images();   // load all images into memory into one big matrix
	bool open_all_readers();   // open row-by-row readers for row processing
	void close_all_readers();  // close row-by-row readers
	void stack_all_rows();     // load all rows, used in place of 'stack_all_images' when row processing
	void matrix_char_to_float(); // convert the image byte matrix to a float

	bool hsh_save(ofstream &savefile, CvMat *hsh_matrix); // save hsh_file in single step, used if the whole output image is stored in memory
	void hsh_save_uncompressed_header(ofstream &savefile); // starts the save process for an uncompressed file by writing the header
	bool hsh_save_uncompressed(ofstream &savefile, CvMat *hsh_matrix); // saves the body part of an uncompressed file, normally called with a single row of the image
	bool convert_hsh_raw_to_compressed(const char *in_filename, const char * out_filename); // convert the saved uncompressed file to a compressed file, uses the min_max params

	CvMat* make_hsh_matrix(); // makes the coefficient matrix for hsh computation

	CvMat* mat_byte_images;
	CvMat* mat_float_images;
	CvMat* mat_solve;
	CvMat *mat_byte_row;

	vector<float *> term_ptr;
	vector<float> min_term, max_term; // (min, max) for each term, used for generating 'compressed' HSHs

};

#endif/*_HSH_FITTER_H_*/
