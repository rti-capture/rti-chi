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

#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include  <istream>

#include "image.h"

using namespace std;

Image::Image(const string & str_path, int type) : imgPath(str_path), imgType(type){

}

bool Image::LoadHeader(){
	struct jpeg_decompress_struct cinfo;
	struct jpeg_error_mgr jerr;

	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_decompress(&cinfo);

	FILE * fp;
	fp = fopen(imgPath.c_str(),"rb");
	if(!fp) return false;

	jpeg_stdio_src(&cinfo,fp);

	jpeg_read_header(&cinfo,TRUE);

	imgHeight = cinfo.image_height;
	imgWidth = cinfo.image_width;
	imgNChannels = cinfo.num_components;

	jpeg_abort_decompress(&cinfo);
return true;

}

bool Image::LoadImage(){
	struct jpeg_decompress_struct cinfo;
	struct jpeg_error_mgr jerr;

	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_decompress(&cinfo);

	FILE * fp;
	fp = fopen(imgPath.c_str(),"rb");
	if(!fp) return false;

	jpeg_stdio_src(&cinfo,fp);

	jpeg_read_header(&cinfo,TRUE);

	jpeg_start_decompress(&cinfo);

	cout << "Output Components : " << cinfo.output_components << endl;

	int row_stride = cinfo.output_width*cinfo.output_components;

	JSAMPARRAY buffer = (*cinfo.mem->alloc_sarray)
	      ((j_common_ptr) &cinfo, JPOOL_IMAGE,
	       row_stride , (JDIMENSION) 1);

	//data = (unsigned char *)malloc(cinfo.output_height * cinfo.output_width * cinfo.output_components);
	dataFloat = (float *)malloc(sizeof(float) * cinfo.output_height * cinfo.output_width * cinfo.output_components);


	while(cinfo.output_scanline < cinfo.output_height){
		jpeg_read_scanlines(&cinfo,buffer,1);

		for(int j = 0; j < row_stride; j++){
			//data[cinfo.output_scanline*row_stride+j] = (*buffer)[j];
			dataFloat[cinfo.output_scanline*row_stride+j] = ((*buffer)[j] + 1)/255.0;
		}

	}

	jpeg_finish_decompress(&cinfo);

	fclose(fp);

	jpeg_destroy_decompress(&cinfo);
return true;

}

bool Image::PrepareRowByRow(){
	cinfo_r.err = jpeg_std_error(&jerr_r);
	jpeg_create_decompress(&cinfo_r);

	fp_r = fopen(imgPath.c_str(),"rb");
	if(!fp_r) return false;

	jpeg_stdio_src(&cinfo_r,fp_r);

	jpeg_read_header(&cinfo_r,TRUE);

	jpeg_start_decompress(&cinfo_r);

	row_stride_r = cinfo_r.output_width*cinfo_r.output_components;

	buffer_r = (*cinfo_r.mem->alloc_sarray)
	      ((j_common_ptr) &cinfo_r, JPOOL_IMAGE,
	       row_stride_r , (JDIMENSION) 1);

	rowData = (float *)malloc(sizeof(float) * cinfo_r.output_width * cinfo_r.output_components);
	return true;
}

bool Image::LoadImageRowByRow(){

		jpeg_read_scanlines(&cinfo_r,buffer_r,1);

		for(int j = 0; j < row_stride_r; j++){
			//data[cinfo.output_scanline*row_stride+j] = (*buffer)[j];
			rowData[j] = ((*buffer_r)[j] + 1)/255.0;
		}

	return true;

}

bool Image::DestroyRowByRow(){
	jpeg_finish_decompress(&cinfo_r);

	fclose(fp_r);

	jpeg_destroy_decompress(&cinfo_r);

	return true;
}

