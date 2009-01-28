// Copyright Cultural Heritage Imaging 2008
// HSH (Hemi-Spherical Harmonics) fitter

#include "cv.h"
#include "highgui.h"

#include <string.h>
#include <iostream>

#include "hsh_fitter.h"

using namespace std;

int main(int argc, char** argv)
{
	bool row_by_row = true;
	bool compressed = true;
	string filepath;
	string prefix;
	int order;
	string lamps_filename;
	string correction_filename;

	if (argc == 8)
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

		if (strcmp(argv[7],"true")==0)
			compressed = true;
		else
			compressed = false;
	}
	else 
	{
		cout << "Usage : hshfitc <path> <prefix> <order> <light_positions_file> <color_correction_file> <use_row_based_reader> " << endl;
		cout << "  Ex1  : hshfitc c:\chidata\ capture_ 3 lamps.lp lampcorrection.dat true " << endl;
		cout << "  Ex2  : hshfitc . test_ 2 lamps.lp x.dat false " << endl;
		return 0;
	}

	ostream *my = &cout;

	HshFitter hshfit(filepath, prefix, lamps_filename, correction_filename, order, row_by_row, (ofstream *) &cout);

	hshfit.set_compressed(compressed);
	hshfit.set_output_filename("simple.hsh");
	hshfit.read_inputs();
	hshfit.compute_loop();
	cin >> order;
}