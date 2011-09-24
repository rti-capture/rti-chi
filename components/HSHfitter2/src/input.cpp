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

#include <vector>


#include <string>
#include <list>
#include <iostream>

/*Besides not being part of the C standard library,
it is considered "pseudo-standard" and reliable/portable between distinct OS(s).*/
#include <dirent.h>
#include <stdio.h>
#include <math.h>
#include "input.h"
#include "image.h"
#include "hsh_core.hpp"

using namespace std;

Input::Input(string str_main_path, string str_prefix, string str_lights_filename, string str_correction_filename,
					 int order, bool is_row_by_row, ofstream *output) :
		str_prefix(str_prefix), str_lights_filename(str_lights_filename), str_correction_filename(str_correction_filename)
			,is_row_by_row(is_row_by_row), order(order), output(output)
{
	this->str_main_path = str_main_path;
}

/**
 * Function that counts the number of images in a directory given a prefix and path
 *
 */
int Input::find_file(const string & str_path, const string & prefix, list<string> & filenames){
    DIR *d;
    struct dirent *dir;
    int file_count = 0;
    int size_prefix = prefix.length();
    d = opendir(str_path.c_str());

    if(d){
        while(dir = readdir(d)){
            string aux(dir->d_name);
            if(!aux.compare(0,size_prefix,prefix)){
                filenames.push_back(aux);
                file_count++;
            }
        }
    }

    return file_count;
}

/* Reads in the light positions from the 'str_lights_filename'.				*/
/*  also uses num_files which should match the number of lines in the light */
/*  positions file.															*/
/*  Results are saved in the lit_images list.								*/
int Input::read_light_positions()
{
	ifstream lights_file((str_main_path + str_lights_filename).c_str());

	if (lights_file.is_open())
	{
		double lx, ly, lz, mag;
		int n;
		string s;
		lights_file >> n;
		//lights_file >> lx >> ly >> lz; // get the first dummy line out of the way.
		int i;
		for (i=0; i<num_files && !lights_file.eof(); i++)
		{
			//lights_file >> lx >> ly >> lz;
			lights_file >> s >> lx >> ly >> lz;
			mag = sqrt(lx*lx + ly*ly + lz*lz);
			lit_images[i].lx = lx/mag;
			lit_images[i].ly = ly/mag;
			lit_images[i].lz = lz/mag;
		}
		lights_file.close();
		return i;
	}
	else
		return 0;
}

/**
 * This functions reads an HP LP file.
 *
 */
int Input::read_lp()
{
    int aux_num_files;
    double lx,ly,lz,mag;
    string aux_path;

    ifstream lp_file((str_main_path + str_lights_filename).c_str());

    if(!lp_file.is_open()) return 0;

    lp_file >> aux_num_files; //the first line indicates the number of files used.

    lit_images.resize(aux_num_files);

    //cout << "\n" << aux_num_files << "\n";
    for(int i=0; i< aux_num_files;i++)
    {
        lp_file >> aux_path >> lx >> ly >> lz;
        //cout << "\n" << aux_path + " " << lx << " " << ly << " "<< lz << " " << "\n";
        mag = sqrt(lx*lx + ly*ly + lz*lz);
		lit_images[i].lx = lx/mag;
		lit_images[i].ly = ly/mag;
		lit_images[i].lz = lz/mag;
        lit_images[i].filename.assign(aux_path.c_str());

        //All the images are used in this step.
        //Some images can be discarded when dealing with color correction?
        lit_images[i].used = true;
    }
    lp_file.close();
    return aux_num_files;
}

/* Reads in color correction scaling values from 'str_correction_filename'  */
/* The special lines with 1 1 1 in the file means that the light should not */
/* be used. The variable 'num_used' is updated with the actual number of    */
/* lights used.	*/
int Input::read_color_correction()
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

bool Input::read_inputs2(){

    num_files = read_lp();

    if(!num_files) return false;

    num_used = read_color_correction();

    vector<LitImage>::iterator li_it;
    for (li_it=lit_images.begin(); li_it != lit_images.end(); )
    {
            if (!(*li_it).used)
                    li_it = lit_images.erase(li_it);
            else
                    li_it++;
    }

    //Read an arbitrary image to get some information properties
    Image * img = new Image(lit_images[0].filename,0);
    if(!img) return false;

    img->LoadHeader();

    height = img->imgHeight;
    width = img->imgWidth;
    channels = img->imgNChannels;
    fullheight = height;

    cout << "Sample Image -> W: " << width << " H: " << height << " Channels: " << channels << endl;

return true;
}

/* 1) Finds the list of files that are in str_main_path and have the prefix str_prefix		*/
/* 2) Reads the light positions file and the color corrections file							*/
/* 3) Filters out the files that are unused, after checking the color corrections file		*/
/* 4) Loads a single image (first one in list) to get the image dimensions					*/
bool Input::read_inputs()
{
	num_files = this->find_file(str_main_path,str_prefix,filenames);

	cout << "Number of files found given prefix : " << num_files << endl;

	if (!num_files)
		return false;

	lit_images.resize(num_files);

	filenames.sort();
	list<string>::iterator it;
	int count = 0;
	for ( it=filenames.begin() ; it != filenames.end(); it++ )
	{
		//cout << "Assign *it " << *it <<endl;
		lit_images[count].filename.assign(str_main_path + (*it));
		lit_images[count].used = true;
		count++;
	}

	int num_light_positions = this->read_light_positions();

	if (num_light_positions != num_files)
	{
		*output << " Light positions on file (" << num_light_positions << ") does not match the number of files for viewpoint ("
			<< num_files << ") \r\n";
		return false;
	}


	num_used = this->read_color_correction();
	//cout << "Number used : " << num_used << endl;

	vector<LitImage>::iterator li_it;
	for (li_it=lit_images.begin(); li_it != lit_images.end(); )
	{
		if (!(*li_it).used)
			li_it = lit_images.erase(li_it);
		else
			li_it++;
	}

	//Read an arbitrary image to get some information properties
	Image * img = new Image(lit_images[0].filename,0);
	if(!img) {
		*output << " Error opening sample image " << lit_images[0].filename.c_str() << "! \r\n";
		return false;
	}

	img->LoadHeader();

	height = img->imgHeight;
	width = img->imgWidth;
	channels = img->imgNChannels;
	fullheight = height;

	cout << "Sample Image -> W: " << width << " H: " << height << " Channels: " << channels << endl;

	float megapix = ((int)height*width/100000)/10.0;
	*output << "hshfitter for prefix '" << str_prefix << "', using " << num_used << " (out of " << num_files << ") files \r\n";
	*output << " hsh order " << order << ", image size " << height << " x " << width << " (" << megapix << " MP), color channels " << channels << "\r\n";
	*output << " with " << height << " rows largest matrix size will be ( " << num_used << " , " << height*width*channels << " ) , memory requirement "
		<<  ceil((sizeof(float)*height*width*channels*num_used)/1000000.0) << "MB+ \r\n";

	return true;
}

