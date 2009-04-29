% Intrinsic and Extrinsic Camera Parameters
%
% This script file can be directly excecuted under Matlab to recover the camera intrinsic and extrinsic parameters.
% IMPORTANT: This file contains neither the structure of the calibration objects nor the image coordinates of the calibration points.
%            All those complementary variables are saved in the complete matlab data file Calib_Results.mat.
% For more information regarding the calibration model visit http://www.vision.caltech.edu/bouguetj/calib_doc/


%-- Focal length:
fc = [ 4001.894289257255000 ; 4014.275703569434900 ];

%-- Principal point:
cc = [ 633.276870838591890 ; 945.631928093683090 ];

%-- Skew coefficient:
alpha_c = 0.000000000000000;

%-- Distortion coefficients:
kc = [ 0.148396551607303 ; 0.378469600774664 ; 0.011092869865149 ; 0.002084726126863 ; 0.000000000000000 ];

%-- Focal length uncertainty:
fc_error = [ 123.149414203508570 ; 117.304779963532850 ];

%-- Principal point uncertainty:
cc_error = [ 140.669563708025490 ; 171.364874111838730 ];

%-- Skew coefficient uncertainty:
alpha_c_error = 0.000000000000000;

%-- Distortion coefficients uncertainty:
kc_error = [ 0.592278162003298 ; 33.901119581392784 ; 0.022880368307034 ; 0.019528553392238 ; 0.000000000000000 ];

%-- Image size:
nx = 1152;
ny = 1728;


%-- Various other variables (may be ignored if you do not use the Matlab Calibration Toolbox):
%-- Those variables are used to control which intrinsic parameters should be optimized

n_ima = 6;						% Number of calibration images
est_fc = [ 1 ; 1 ];					% Estimation indicator of the two focal variables
est_aspect_ratio = 1;				% Estimation indicator of the aspect ratio fc(2)/fc(1)
center_optim = 1;					% Estimation indicator of the principal point
est_alpha = 0;						% Estimation indicator of the skew coefficient
est_dist = [ 1 ; 1 ; 1 ; 1 ; 0 ];	% Estimation indicator of the distortion coefficients


%-- Extrinsic parameters:
%-- The rotation (omc_kk) and the translation (Tc_kk) vectors for every calibration image and their uncertainties

%-- Image #1:
omc_1 = [ -1.858446e+000 ; -1.894330e+000 ; 5.664237e-001 ];
Tc_1  = [ -3.631638e+001 ; -4.368531e+001 ; 6.812510e+002 ];
omc_error_1 = [ 3.445852e-002 ; 2.258943e-002 ; 5.513620e-002 ];
Tc_error_1  = [ 2.399582e+001 ; 2.910267e+001 ; 1.894379e+001 ];

%-- Image #2:
omc_2 = [ -1.973620e+000 ; -2.017944e+000 ; 3.982735e-001 ];
Tc_2  = [ -3.717553e+001 ; -4.377274e+001 ; 6.738223e+002 ];
omc_error_2 = [ 3.237048e-002 ; 2.302260e-002 ; 5.926913e-002 ];
Tc_error_2  = [ 2.373133e+001 ; 2.878233e+001 ; 1.910755e+001 ];

%-- Image #3:
omc_3 = [ -2.079925e+000 ; -2.132769e+000 ; 2.163645e-001 ];
Tc_3  = [ -3.665470e+001 ; -4.386124e+001 ; 6.663462e+002 ];
omc_error_3 = [ 3.039071e-002 ; 2.559163e-002 ; 6.578220e-002 ];
Tc_error_3  = [ 2.346148e+001 ; 2.845947e+001 ; 1.946727e+001 ];

%-- Image #4:
omc_4 = [ 2.137171e+000 ; 2.201964e+000 ; 1.360165e-001 ];
Tc_4  = [ -3.236133e+001 ; -4.394330e+001 ; 6.542617e+002 ];
omc_error_4 = [ 3.426956e-002 ; 3.068282e-002 ; 6.245540e-002 ];
Tc_error_4  = [ 2.304099e+001 ; 2.795074e+001 ; 1.887398e+001 ];

%-- Image #5:
omc_5 = [ 1.879037e+000 ; 1.948495e+000 ; 5.417373e-001 ];
Tc_5  = [ -2.162260e+001 ; -4.402226e+001 ; 6.418177e+002 ];
omc_error_5 = [ 3.527467e-002 ; 2.357533e-002 ; 5.387124e-002 ];
Tc_error_5  = [ 2.260421e+001 ; 2.740461e+001 ; 1.907189e+001 ];

%-- Image #6:
omc_6 = [ 1.513969e+000 ; 1.585665e+000 ; 9.412712e-001 ];
Tc_6  = [ -2.469228e+000 ; -4.406959e+001 ; 6.345537e+002 ];
omc_error_6 = [ 3.861138e-002 ; 2.748078e-002 ; 4.453421e-002 ];
Tc_error_6  = [ 2.234828e+001 ; 2.709249e+001 ; 1.937683e+001 ];

