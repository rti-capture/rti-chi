/*
 * MATLAB Compiler: 4.7 (R2007b)
 * Date: Thu Jan 29 01:19:44 2009
 * Arguments: "-B" "macro_default" "-m" "-W" "main" "-T" "link:exe" "calib"
 * "data_calib.m" "ima_read_calib.m" "click_calib.m" "go_calib_optim.m"
 * "ext_calib.m" "reproject_calib.m" "analyse_error.m" "recomp_corner_calib.m"
 * "add_suppress.m" "saving_calib.m" "loading_calib.m"
 * "extrinsic_computation.m" "undistort_image.m" "export_calib_data.m"
 * "show_calib_results.m" "planemat.m" "ext_calib2.m" 
 */

#include "mclmcr.h"

#ifdef __cplusplus
extern "C" {
#endif
const unsigned char __MCC_calib_session_key[] = {
    '3', '5', '0', '6', 'D', 'A', 'E', 'D', 'F', '6', '4', 'D', '1', 'C', '9',
    'D', '9', 'A', '8', '1', 'B', 'D', '6', '4', 'A', 'D', 'B', '8', '1', '6',
    '5', '7', 'F', '7', '9', '4', '7', '9', '7', '4', '5', 'D', 'B', '1', '2',
    'D', 'F', '8', '5', '3', 'A', 'C', '0', 'F', '7', '2', '8', 'A', '9', 'E',
    '7', '7', '5', '5', '5', '5', '2', '3', '0', '1', '2', 'D', '9', 'B', '2',
    '3', 'C', 'A', '8', '4', '0', 'B', 'A', 'C', '0', '4', '8', '0', '3', '3',
    '5', '8', '3', 'B', '4', '7', '1', 'C', '7', '1', 'F', 'A', '6', 'B', 'A',
    '7', '6', '7', 'D', 'C', 'F', 'E', 'C', '8', '7', '1', '9', 'A', '3', '2',
    '7', '0', 'C', '2', '6', '9', '7', 'B', '4', 'E', '7', 'C', 'A', '5', 'C',
    '1', '1', '9', '0', '7', 'C', '5', 'C', '2', 'C', 'E', 'A', '7', '0', '1',
    'E', '2', '0', '1', 'F', '9', 'D', '8', '5', '1', 'B', '9', '8', 'D', '8',
    '5', 'A', 'F', 'A', 'F', '0', '2', '0', 'D', 'A', 'B', '6', '7', 'A', '1',
    'E', '3', '8', 'A', 'D', '5', 'A', 'A', 'C', '4', 'B', '5', 'F', '9', '3',
    'C', 'B', 'F', '9', '9', '0', 'D', '6', '6', '9', 'C', '9', '9', '7', 'D',
    '6', 'B', 'E', '8', 'F', 'A', 'A', 'B', '2', '3', 'F', '4', '6', 'F', '4',
    '2', '1', '3', '2', 'D', '0', 'F', '5', '2', 'B', 'C', 'C', '9', '6', 'A',
    '6', 'A', 'D', 'A', 'E', 'A', 'A', 'F', '1', '0', '7', '3', 'B', '2', '9',
    '3', '\0'};

const unsigned char __MCC_calib_public_key[] = {
    '3', '0', '8', '1', '9', 'D', '3', '0', '0', 'D', '0', '6', '0', '9', '2',
    'A', '8', '6', '4', '8', '8', '6', 'F', '7', '0', 'D', '0', '1', '0', '1',
    '0', '1', '0', '5', '0', '0', '0', '3', '8', '1', '8', 'B', '0', '0', '3',
    '0', '8', '1', '8', '7', '0', '2', '8', '1', '8', '1', '0', '0', 'C', '4',
    '9', 'C', 'A', 'C', '3', '4', 'E', 'D', '1', '3', 'A', '5', '2', '0', '6',
    '5', '8', 'F', '6', 'F', '8', 'E', '0', '1', '3', '8', 'C', '4', '3', '1',
    '5', 'B', '4', '3', '1', '5', '2', '7', '7', 'E', 'D', '3', 'F', '7', 'D',
    'A', 'E', '5', '3', '0', '9', '9', 'D', 'B', '0', '8', 'E', 'E', '5', '8',
    '9', 'F', '8', '0', '4', 'D', '4', 'B', '9', '8', '1', '3', '2', '6', 'A',
    '5', '2', 'C', 'C', 'E', '4', '3', '8', '2', 'E', '9', 'F', '2', 'B', '4',
    'D', '0', '8', '5', 'E', 'B', '9', '5', '0', 'C', '7', 'A', 'B', '1', '2',
    'E', 'D', 'E', '2', 'D', '4', '1', '2', '9', '7', '8', '2', '0', 'E', '6',
    '3', '7', '7', 'A', '5', 'F', 'E', 'B', '5', '6', '8', '9', 'D', '4', 'E',
    '6', '0', '3', '2', 'F', '6', '0', 'C', '4', '3', '0', '7', '4', 'A', '0',
    '4', 'C', '2', '6', 'A', 'B', '7', '2', 'F', '5', '4', 'B', '5', '1', 'B',
    'B', '4', '6', '0', '5', '7', '8', '7', '8', '5', 'B', '1', '9', '9', '0',
    '1', '4', '3', '1', '4', 'A', '6', '5', 'F', '0', '9', '0', 'B', '6', '1',
    'F', 'C', '2', '0', '1', '6', '9', '4', '5', '3', 'B', '5', '8', 'F', 'C',
    '8', 'B', 'A', '4', '3', 'E', '6', '7', '7', '6', 'E', 'B', '7', 'E', 'C',
    'D', '3', '1', '7', '8', 'B', '5', '6', 'A', 'B', '0', 'F', 'A', '0', '6',
    'D', 'D', '6', '4', '9', '6', '7', 'C', 'B', '1', '4', '9', 'E', '5', '0',
    '2', '0', '1', '1', '1', '\0'};

static const char * MCC_calib_matlabpath_data[] = 
  { "calib/", "toolbox/compiler/deploy/",
    "prabath/projects/toolbox/matlab/", "$TOOLBOXMATLABDIR/general/",
    "$TOOLBOXMATLABDIR/ops/", "$TOOLBOXMATLABDIR/lang/",
    "$TOOLBOXMATLABDIR/elmat/", "$TOOLBOXMATLABDIR/elfun/",
    "$TOOLBOXMATLABDIR/specfun/", "$TOOLBOXMATLABDIR/matfun/",
    "$TOOLBOXMATLABDIR/datafun/", "$TOOLBOXMATLABDIR/polyfun/",
    "$TOOLBOXMATLABDIR/funfun/", "$TOOLBOXMATLABDIR/sparfun/",
    "$TOOLBOXMATLABDIR/scribe/", "$TOOLBOXMATLABDIR/graph2d/",
    "$TOOLBOXMATLABDIR/graph3d/", "$TOOLBOXMATLABDIR/specgraph/",
    "$TOOLBOXMATLABDIR/graphics/", "$TOOLBOXMATLABDIR/uitools/",
    "$TOOLBOXMATLABDIR/strfun/", "$TOOLBOXMATLABDIR/imagesci/",
    "$TOOLBOXMATLABDIR/iofun/", "$TOOLBOXMATLABDIR/audiovideo/",
    "$TOOLBOXMATLABDIR/timefun/", "$TOOLBOXMATLABDIR/datatypes/",
    "$TOOLBOXMATLABDIR/verctrl/", "$TOOLBOXMATLABDIR/codetools/",
    "$TOOLBOXMATLABDIR/helptools/", "$TOOLBOXMATLABDIR/winfun/",
    "$TOOLBOXMATLABDIR/demos/", "$TOOLBOXMATLABDIR/timeseries/",
    "$TOOLBOXMATLABDIR/hds/", "$TOOLBOXMATLABDIR/guide/",
    "$TOOLBOXMATLABDIR/plottools/", "toolbox/local/" };

static const char * MCC_calib_classpath_data[] = 
  { "" };

static const char * MCC_calib_libpath_data[] = 
  { "" };

static const char * MCC_calib_app_opts_data[] = 
  { "" };

static const char * MCC_calib_run_opts_data[] = 
  { "" };

static const char * MCC_calib_warning_state_data[] = 
  { "off:MATLAB:dispatcher:nameConflict" };


mclComponentData __MCC_calib_component_data = { 

  /* Public key data */
  __MCC_calib_public_key,

  /* Component name */
  "calib",

  /* Component Root */
  "",

  /* Application key data */
  __MCC_calib_session_key,

  /* Component's MATLAB Path */
  MCC_calib_matlabpath_data,

  /* Number of directories in the MATLAB Path */
  36,

  /* Component's Java class path */
  MCC_calib_classpath_data,
  /* Number of directories in the Java class path */
  0,

  /* Component's load library path (for extra shared libraries) */
  MCC_calib_libpath_data,
  /* Number of directories in the load library path */
  0,

  /* MCR instance-specific runtime options */
  MCC_calib_app_opts_data,
  /* Number of MCR instance-specific runtime options */
  0,

  /* MCR global runtime options */
  MCC_calib_run_opts_data,
  /* Number of MCR global runtime options */
  0,
  
  /* Component preferences directory */
  "calib_03AD716FED84D5F4B855E2B42F12724E",

  /* MCR warning status data */
  MCC_calib_warning_state_data,
  /* Number of MCR warning status modifiers */
  1,

  /* Path to component - evaluated at runtime */
  NULL

};

#ifdef __cplusplus
}
#endif


