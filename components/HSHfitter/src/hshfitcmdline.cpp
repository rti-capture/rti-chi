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

#include <string.h>
#include <iostream>
#include <stdlib.h>
#include<omp.h>
#include "input.h"
#include "hsh_core.hpp"

using namespace std;

int main(int argc, char** argv)
{
	/**
	 * Slower algorithm but it consumes little memory.
	 *
	 */
	bool row_by_row = true;

	/**
	 * Generates compresssed output file.
	 *
	 */
	bool compressed = true;


	string filepath;

	/**
	 * Used to deal with specific .lp files.
	 *
	 */
	string prefix;

	/**
	 * Algorithm specific parameter.
	 *
	 */

	int order;

	/**
	 * Used in the hsh_core to set the number of threads of the parallel section.
	 *
	 */
	int maxThreads;

	string lamps_filename;

	string color_correction_filename;

	string correction_filename;

	/**
	 * Output filename.
	 *
	 */
	string outputfn;


	switch(argc){

		case 4: filepath = argv[1];
			prefix = "deprecated";
			order = atoi(argv[2]);
			maxThreads = omp_get_max_threads();
			outputfn = argv[3];
			break;

		case 5: filepath = argv[1];
			prefix = "deprecated";
			order = atoi(argv[2]);
			maxThreads = atoi(argv[3]);
			if(maxThreads < 1) {
				maxThreads = omp_get_max_threads();
				cout << "Invalid number of threads : " << atoi(argv[3]) << ". " <<
						"Using " << omp_get_max_threads() << " threads" << endl;
			}

			outputfn = argv[4];
			break;

		case 8:
			filepath = argv[1];
			prefix = argv[2];
			order = atoi(argv[3]);
			//cout << "Order : " << order << endl;
			if((order < 1)||(order > 3)){
				cout << "Invalid Order. Values must be between 1 and 3" << endl;
				return 1;
			}
			lamps_filename = argv[4];
			color_correction_filename = argv[5];

			maxThreads = omp_get_max_threads();

			if (strcmp(argv[6],"true")==0)
				row_by_row = true;
			else
				row_by_row = false;

			if (strcmp(argv[7],"true")==0)
				compressed = true;
			else
				compressed = false;

			break;

		case 9:
				filepath = argv[1];
				prefix = argv[2];
				order = atoi(argv[3]);
				//cout << "Order : " << order << endl;
				if((order < 1)||(order > 3)){
					cout << "Invalid Order. Values must be between 1 and 3" << endl;
					return 1;
				}
				lamps_filename = argv[4];
				color_correction_filename = argv[5];

				if (strcmp(argv[6],"true")==0)
					row_by_row = true;
				else
					row_by_row = false;

				if (strcmp(argv[7],"true")==0)
					compressed = true;
				else
					compressed = false;


				maxThreads = atoi(argv[8]);
				if(maxThreads < 1) {
					maxThreads = omp_get_max_threads();
					cout << "Invalid number of threads : " << atoi(argv[8]) << ". " <<
							"Using " << omp_get_max_threads() << " threads" << endl;
				}

				break;

		default: cout << "Usage : hshfitter <path_to_light_positions_file> <order> <output_file_name>" << endl;
				 cout << "Usage : hshfitter <path_to_light_positions_file> <order> <MaxNumberOfThreads> <output_file_name>" << endl;
		         cout << "Usage : hshfitter <path> <prefix> <order> <light_positions_file> <color_correction_file> "
						 << "<use_row_based_reader> <compressed>" << endl;
		         cout << "Usage : hshfitter <path> <prefix> <order> <light_positions_file> <color_correction_file> "
		         						 << "<use_row_based_reader> <compressed> <MaxNumberOfThreads>" << endl;
		         cout << "    Example 1 : ./hshfitter /home/matheus/snooker2/assembly-files/teste.lp 2 2 /home/matheus/Desktop/partilhaVB/snooker.hsh" << endl;
		         cout << "    Example 2 : ./hshfitter /home/matheus/snookerPrabath/jpeg-exports/ snooker-test-1-_00 2 teste.lp nofile.txt true true" << endl;
				 return 0;
	}

	Input * input = new Input(filepath, prefix, lamps_filename, color_correction_filename, order, row_by_row,(ofstream *) &cout);

	/**
	 * Read Inputs
	 *
	 */

	bool result;
	switch(argc){

		case 4:
		case 5:
			result = input->read_inputs2();
			if(!result){
				cout << "Problem while reading command line arguments" << endl;
				return 0;
			}else{
				cout << "Parameters read successfully" << endl;
			}
			break;

		case 8:
		case 9:
			result = input->read_inputs();
			if(!result){
				cout << "Problem while reading command line arguments" << endl;
				return 0;
			}else{
				cout << "Parameters read successfully" << endl;
			}
			break;
	}


	input->set_compressed(compressed);
	input->set_output_filename(outputfn);
	input->setMaxThreads(maxThreads);

	HshCore * core = new HshCore(input);
	core->compute_loop();
	//cin >> order;
}
