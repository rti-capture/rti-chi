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

#ifndef _IMAGE_H
#define	_IMAGE_H

#include <string>
#include <string.h>

extern "C"{
    #include "jpeglib.h"
}


using namespace std;

class Image {
public:
    Image(const string & str_path,int type);
    ~Image(){};

    bool LoadHeader();
    bool LoadImage();

    bool LoadImageRowByRow();

    bool PrepareRowByRow();
    bool DestroyRowByRow();

    int imgWidth;
    int imgHeight;
    int imgNChannels;
    int imgType;
    string imgPath;

    unsigned char * data;

    float * dataFloat;

    float * rowData;

    struct jpeg_decompress_struct cinfo_r;
    struct jpeg_error_mgr jerr_r;

    FILE * fp_r;

    int row_stride_r;

    JSAMPARRAY buffer; //All image

    JSAMPARRAY buffer_r; //Image row by row

};

#endif	/* _IMAGE_H */

