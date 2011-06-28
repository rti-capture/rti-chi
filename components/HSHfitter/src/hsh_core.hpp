/*  HSHFitter
 *  Copyright (C) 2009-11 UC Santa Cruz and Cultural Heritage Imaging
 *    
 *  Portions Copyright (C) 2010-11 Univ. do Minho and Cultural Heritage Imaging
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3 as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef _HSH_CORE_HPP
#define	_HSH_CORE_HPP

#include <stdio.h>
#include <math.h>
#include <string.h>
#include <list>
#include <vector>
#include <time.h>

#include <ostream>
#include <fstream>
#include <iostream>
#include "input.h"
#include "image.h"

using namespace std;

class HshCore{
public:
    HshCore(Input * data);
    ~HshCore(){};

    void compute_loop();

    ostream *output;		//  stream for capturing console text output


private:

	Input * in;

    void make_hsh_matrix();
    bool convert_hsh_raw_to_compressed(const char *infilename,const char *outfilename);
    bool hsh_save(ofstream &savefile, float *hsh_matrix);
    void hsh_save_uncompressed_header(ofstream &savefile);
    bool hsh_save_uncompressed(ofstream &savefile, float *hsh_matrix);
    void stack_all_images();
    void stack_all_images2();
    void stack_all_rows();

    void prepareRowByRow();
    void destroyRowByRow();

    void find_min_max(float * matrix, int row, float& min, float&max);

    void delColumn(float * mat_float_b_coluna,int colunaI,int height,int width);
    void addColumn(float * mat_float_b_coluna,int colunaI,int height,int width);

    unsigned char * mat_all_images;
    float * mat_float;
    float * hsh_matrix;
    float * mat_float_b;
    float * mat_float_b_backup;
    //float * mat_float_a;
    //float * mat_float_a_backup;

    vector<float *> term_ptr;
    vector<float> min_term, max_term; // (min, max) for each term, used for generating 'compressed' HSHs

    string str_debug_output;	// debug/info output stored in this string

};

#endif	/* _HSH_CORE_HPP */

