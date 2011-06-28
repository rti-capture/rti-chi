/*
 * File:   input.h
 * Author: matheus
 *
 * Created on March 19, 2009, 3:49 PM
 */

#ifndef _INPUT_H
#define	_INPUT_H

#include <list>
#include <string.h>
#include <ostream>
#include <fstream>
#include <vector>
#include "image.h"

using namespace std;

class LitImage {
public:
	bool used;						  // is this a valid image
	double lx, ly, lz;				  // lighting angle
	double scale_r, scale_g, scale_b; // color correction scales
	string filename;				  // image taken under this light
	Image * img;
};

class Input {
public:
    Input(string str_main_path, string str_prefix, string str_lights_filename,
    		string str_correction_filename,int order, bool is_row_by_row,ofstream *output);
    ~Input(){};

    void set_order(int order) { this->order = order;};
    void set_output_filename (string str_output_filename) {this->str_output_filename = str_output_filename;};

    void set_row_by_row(bool is_row_by_row) {this->is_row_by_row = is_row_by_row;};
    void set_compressed(bool is_compressed) {this->is_compressed = is_compressed;};
    void setMaxThreads(int maxThreads) {this->numberOfThreads = maxThreads;}
    bool read_inputs();
    bool read_inputs2();


    ostream *output;		//  stream for capturing console text output

    int find_file(const string & str_path, const string & prefix, list<string> & filenames);
    int read_light_positions();
    int read_lp();
    int read_color_correction() ;

    int numberOfThreads; //Used to set the OpenMP number of threads

    string str_main_path;		// the main working directory (where all the files are)
    string str_prefix;			// prefix of the files to be used
    string str_lights_filename;	// light positions filename
    string str_correction_filename; // color corrections filename

    string str_output_filename;  // output filename

    vector<LitImage> lit_images; // the information about all light positions, filenames, scale values etc.
    list<string> filenames; // the list of files matching the prefix
    int num_files;			// number of files matching the given prefix at the path
    int num_used;			// number of files remaining after removing the ones indicated from color correction file

    bool is_row_by_row;		// is the 'reading and processing of images done on a row by row basis?'
    bool is_compressed;		// are we saving the data in compressed form?

    int order;					 // the order of the hsh fitting
    int fullheight;				// image height
    int width;					// image width
    int height;					// 'block' height, number of rows for which the fitting is done per turn
    int channels;				// number of color channels


};

#endif	/* _INPUT_H */

