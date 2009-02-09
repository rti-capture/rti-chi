#pragma once
#include <list>
#include <string.h>

using namespace std;

class BatchFlow
{
public:
	list<string> filenames; // the list of files matching the prefix
	int num_files;			// number of files matching the given prefix at the path
	int num_used;			// number of files remaining after removing the ones indicated from color correction file

	int min_shiftx, max_shiftx;

	float deg_seperation;

	string str_main_path;		// the main working directory (where all the files are)
	string str_prefix;			// prefix of the files to be used

	string str_descriptor_filename;

	int get_files();
	void batch_process();
	void save_descriptor_file();
};