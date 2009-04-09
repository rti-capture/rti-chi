#ifndef _AXIS_RECTIFY_H_
#define _AXIS_RECTIFY_H_

#include "hsh_fitter.h"


class AxisRectify
{
public:
	string str_main_path;
	string str_prefix;
	string str_correction_filename;
	string str_axis_filename;

	bool do_rectification;
	bool do_color_correction;

	AxisRectify(string instr_main_path, string instr_prefix, string instr_correction_filename, string instr_axis_filename);

	void read_axis_rot();
	void read_color_correction();

	bool full_rectification_loop();

	double rot_angle;

	double color_fix[3][200];

	int num_lights;
};


#endif /* _AXIS_RECTIFY_H_ */
