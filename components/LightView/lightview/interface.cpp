#include <GL/glui.h>
#include <fstream>

#include "interface.h"
#include "lv.h"

const float DEG2RAD = 3.14159/180;

int viewpoints;
int windowheight, windowwidth;
unsigned char *imgshow;

int mousedownX, mousedownY;
int mousemoveX, mousemoveY;
bool mousedown = false;
float postrackbar = 50;
bool alreadyWorking = false;
bool batch_mode = false;
bool low_mem = false;
bool change_lighting = false;
bool one_file_only = false;

int scale = 1;
int cx = 0;
int cy = 0;

int circle_x_origin;
int circle_y_origin;
int circle_radius;

int movie_frames = 10;
int total_movie_frames = 0;

GLUI *glui;

int light_theta = 45;
int light_turns = 2;


void recalcImage() {
	if (alreadyWorking) {
		cout << "!";
		return;
	}
	alreadyWorking = true;
	double pos = (double)postrackbar/(double)windowwidth;

	int step1 = floor(pos * (viewpoints-1));
	int step2 = ceil(pos * (viewpoints-1));

	//cout << postrackbar << " " << pos << " : " << step1 << " " << step2 << endl;
	if (snap_nearest || step1==step2)  
	{ // Just snap to the nearest view and do all calculations from there.
		int step = round(pos * (viewpoints-1));
		
		if (!view[step].lightingcached) {
			if (g_mode == MODE_HEMISPHERICAL)
				renderImageHSH(view[step].floatpixels,view[step].Image);
			view[step].lightingcached = true;
		}

		imgshow = view[step].Image;
	}
	else
	{
		double inbetweenpos = pos * (viewpoints-1) - step1;

		if (!view[step1].lightingcached) {
			if (g_mode == MODE_HEMISPHERICAL)
				renderImageHSH(view[step1].floatpixels,view[step1].Image);
			view[step1].lightingcached = true;
		}

		if (!view[step2].lightingcached) {
			if (g_mode == MODE_HEMISPHERICAL)
				renderImageHSH(view[step2].floatpixels,view[step2].Image);
			view[step2].lightingcached = true;
		}

		stereoLinearInterp(inbetweenpos, view[step1].Image,view[step2].Image,view[step1].imgdispleft,view[step1].imgdispright);
	}

	if (!batch_mode) glutPostRedisplay();
	alreadyWorking = false;
}

void clearLightingCache() {
	for (int i=0; i<viewpoints; i++)
		view[i].lightingcached = false;
}

void drawSquare()
{
	int square_x_origin = windowwidth;
	int square_y_origin = windowheight;
	int square_x_size = 300;
	int square_y_size = 300;
	glColor3f(0.4,0.4,0.4);
	glBegin(GL_POLYGON);
		glVertex2f(square_x_origin, square_y_origin);
		glVertex2f(square_x_origin + square_x_size,square_y_origin);
		glVertex2f(square_x_origin + square_x_size,square_y_origin - square_y_size);
		glVertex2f(square_x_origin, square_y_origin - square_y_size);
	glEnd();
}

void drawCircle()
{	
	circle_x_origin = windowwidth+150;
	circle_y_origin = windowheight-150;
	circle_radius = 140;

	glColor3f(0.1,0.1,0.1);
	glBegin(GL_POLYGON);
	float degInRad;
	for (int i=0; i < 360; i++)
	{
	  degInRad = i*DEG2RAD;
	  glVertex2f(cos(degInRad)*circle_radius+circle_x_origin,sin(degInRad)*circle_radius+circle_y_origin);
	}
	glEnd();

	glColor3f(0.0,1.0,0.0);
	glBegin(GL_LINES);
		glVertex2f(circle_x_origin+lx*circle_radius-5,circle_y_origin+ly*circle_radius-5);
		glVertex2f(circle_x_origin+lx*circle_radius+5,circle_y_origin+ly*circle_radius+5);
		glVertex2f(circle_x_origin+lx*circle_radius-5,circle_y_origin+ly*circle_radius+5);
		glVertex2f(circle_x_origin+lx*circle_radius+5,circle_y_origin+ly*circle_radius-5);
	glEnd();
}


void drawTrackbar(void) {
	float pixelgap = windowwidth/(viewpoints-1);
	float pixeltrackbar = postrackbar;

	glColor3f(0.0,0.0,1.0);
	glBegin(GL_LINES);
		glVertex2f(0, windowheight-10);
		glVertex2f(windowwidth, windowheight-10);
	glEnd();

	glColor3f(0.4,0.4,0.4);
	for (int linex=0; linex<=windowwidth; linex+=pixelgap) {
		glBegin(GL_TRIANGLES);
			glVertex2f(linex-5,windowheight);
			glVertex2f(linex+5,windowheight);
			glVertex2f(linex,windowheight-10);
		glEnd();
	}

	glColor3f(0.0,1.0,0.0);
	glBegin(GL_TRIANGLES);
		glVertex2f(pixeltrackbar-5,windowheight-20);
		glVertex2f(pixeltrackbar+5,windowheight-20);
		glVertex2f(pixeltrackbar,windowheight-10);
	glEnd();

}

void idleFunc(void)
{
}

void renderBitmapString(float x, float y,  char *string) 
{  
  char *c;
  glRasterPos2f(x, y);
  for (c=string; *c != '\0'; c++) 
  {
    glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, *c);
  }
}

void drawTextBox() 
{
	int lh = 20;
	//renderBitmapString(width+50,height-320-lh,"To change viewpoint click on the track bar / click and drag on the image area.");
	//renderBitmapString(width+50,height-320-lh*2,"To change lighting direction click on the circle.");
	glColor3f(0.5,0.5,0.5);
	renderBitmapString(width+50,height-320-lh*3,"l : interpolate from left disparity.");
	renderBitmapString(width+50,height-320-lh*4,"r : interpolate from right disparity.");
	renderBitmapString(width+50,height-320-lh*5,"c : use interp/n.n. for color.");
}

void renderScene(void) 
{
	glClear(GL_COLOR_BUFFER_BIT);
	
	glRasterPos2i(0,0);
	//glPixelZoom(1,1);

	glDrawPixels(width,height,GL_RGB,GL_UNSIGNED_BYTE,imgshow);

	drawTrackbar();
	drawSquare();
	drawCircle();
	drawTextBox();
	glFlush();

	glutSwapBuffers();

}




void processMouse(int button, int state, int x, int y) {
	y = windowheight - y;
	if (button==GLUT_LEFT_BUTTON && y>windowheight-20 && state == GLUT_DOWN) 
	{
		int oldpos = postrackbar;
		postrackbar = x;
		if (postrackbar < 0) postrackbar = 0;
		if (postrackbar > windowwidth) postrackbar = windowwidth;
		if (oldpos != postrackbar) recalcImage();
	} 
	if (button==GLUT_LEFT_BUTTON) 
	{
		if (x>circle_x_origin-circle_radius && x<circle_x_origin+circle_radius && 
			y>circle_y_origin-circle_radius && y<circle_y_origin+circle_radius) 
		{
			// left clicks on the circle for relighting			
			float dist = sqrt(pow((float)x-circle_x_origin,2)+pow((float)y-circle_y_origin,2));
			if(dist<circle_radius)
			{
				lx = (x-circle_x_origin)/(float)circle_radius;
				ly = (y-circle_y_origin)/(float)circle_radius;
				lz = sqrt(1-lx*lx-ly*ly);	
				clearLightingCache();
				recalcImage();
			}
		}
		else if (x>=0 && y>=0 && x<windowwidth && y<windowheight) 
		{			
			// left clicks on the image area for dragging
			if (state == GLUT_DOWN) 
			{
				mousedown = true;
				mousemoveX = mousedownX = x;
				mousemoveY = mousedownY = y;
				cout << "down ( " << x << "," << y << ") ";
			} 
			else if (state == GLUT_UP) 
			{
				mousedown = false;
				//cout << " up. ";
			}
		}
	} 
	else if (button==GLUT_RIGHT_BUTTON)
	{
		if  (x>=0 && y>=0 && x<windowwidth && y<windowheight) 
		{
			// right clicks inside the image area for relighting
			float sx = 1.0; //(float)drawwindow_xsize/(float)width;
			float sy = 1.0; //(float)drawwindow_ysize/(float)height;

			cx = x/sx;
			cy = y/sy;

			clearLightingCache();
			recalcImage();
		}
	}
}

void processMouseActiveMotion(int x, int y) {
	y = windowheight - y;
	if (mousedown && x>0 && y>0 && x<windowwidth && y<windowheight) {
		mousemoveX = x;
		mousemoveY = y;
		int dragX = mousemoveX - mousedownX;
		int oldpostrackbar = postrackbar;
		postrackbar += dragX;
		if (postrackbar <= 0 || postrackbar >= windowwidth) 
			postrackbar = oldpostrackbar;
		else 
			recalcImage();
		mousedownX = x;
		mousedownY = y;
		//cout << "move ( " << x << "," << y << ") ";
	}
}


// copies a layer from the image stack onto an opencv matrix
void copyArraytoImage(IplImage * img, unsigned char *stack)
{
	for (int y=0; y<height; y++)
		for (int x=0; x<width; x++) {
			unsigned char * ptr = &((unsigned char*)(img->imageData + img->widthStep*y))[x * img->nChannels];
			ptr[0] = stack[y*width*3 + x*3];
			ptr[1] = stack[y*width*3 + x*3+1];
			ptr[2] = stack[y*width*3 + x*3+2];
		}
}

void saveImage(void) {
	IplImage *frame = cvCreateImage(cvSize(width, height),IPL_DEPTH_8U,3);
	copyArraytoImage(frame,imgshow);
	cvConvertImage(frame,frame, CV_CVTIMG_FLIP );
	cvConvertImage(frame,frame, CV_CVTIMG_SWAP_RB );
	cvSaveImage("capture.png",frame);
	printf("capture saved.\n");
	cvReleaseImage(&frame);
}

void processNormalKeys(unsigned char key, int x, int y) {
	if (key == 27) 
		exit(0);
	else if (key == 'l')
		use_left_image = !use_left_image;
	else if (key == 'r') 
		use_right_image = !use_right_image;
	else if (key == 'c')
		use_color_interp = !use_color_interp;
	else if (key == 's')
		saveImage();

	glui->sync_live();
	recalcImage();
}




void processMenuOptions(int option) {
}



void saveMovieReverse(int frame_offset) {
	float oldpostrackbar = postrackbar;
	//CvVideoWriter* video = cvCreateVideoWriter("movie.avi",-1,25,cvSize(width, height),1);
	char savegeneratedfiles[250];

	clearLightingCache();
	save_movie = true;
	IplImage *frame = cvCreateImage(cvSize(width, height),IPL_DEPTH_8U,3);
	for (int deg = movie_frames; deg>=0; deg--) {
		postrackbar = round(windowwidth*((double)deg/(double)movie_frames));
		if (change_lighting) {

			lz = cos(DEG2RAD * light_theta);
			double r_ = sin(DEG2RAD * light_theta);
			double theta = light_turns * (double)((movie_frames-deg)+frame_offset)*(2*PI)/total_movie_frames;
			lx = r_ * sin(theta);
			ly = r_ * cos(theta);

			cout << lx << " " << ly << " " << lz <<endl;
			
			double mag = sqrt(lx*lx+ly*ly+lz*lz);
			lx /= mag; 
			ly /= mag;
			lz /= mag;
			
			clearLightingCache();
		}
		recalcImage();
		copyArraytoImage(frame,imgshow);
		cvConvertImage(frame,frame, CV_CVTIMG_FLIP );
		cvConvertImage(frame,frame, CV_CVTIMG_SWAP_RB );
		
		sprintf_s(savegeneratedfiles,"movie\\frame_%05d.png",(movie_frames-deg)+frame_offset);
		cvSaveImage(savegeneratedfiles,frame);
		//cvWriteFrame( video, frame );
		printf(".");
	}
	cvReleaseImage(&frame);
	//cvReleaseVideoWriter(&video);
	save_movie = false;
	postrackbar = oldpostrackbar;
	printf("done.\n");
	recalcImage();
}

void saveMovie(int frame_offset) {
	float oldpostrackbar = postrackbar;
	//CvVideoWriter* video = cvCreateVideoWriter("movie.avi",-1,25,cvSize(width, height),1);
	char savegeneratedfiles[250];

	clearLightingCache();
	save_movie = true;
	IplImage *frame = cvCreateImage(cvSize(width, height),IPL_DEPTH_8U,3);
	for (int deg = 0; deg<=movie_frames; deg++) {
		postrackbar = round(windowwidth*((double)deg/(double)movie_frames));
		if (change_lighting) {

			lz = cos(DEG2RAD * light_theta);
			double r_ = sin(DEG2RAD * light_theta);
			double theta = light_turns * (double)(deg+frame_offset)*(2*PI)/total_movie_frames;
			lx = r_ * sin(theta);
			ly = r_ * cos(theta);

			cout << lx << " " << ly << " " << lz <<endl;
			
			double mag = sqrt(lx*lx+ly*ly+lz*lz);
			lx /= mag; 
			ly /= mag;
			lz /= mag;
			
			clearLightingCache();
		}
		recalcImage();
		copyArraytoImage(frame,imgshow);
		cvConvertImage(frame,frame, CV_CVTIMG_FLIP );
		cvConvertImage(frame,frame, CV_CVTIMG_SWAP_RB );
		
		sprintf_s(savegeneratedfiles,"movie\\frame_%05d.png",deg+frame_offset);
		cvSaveImage(savegeneratedfiles,frame);
		//cvWriteFrame( video, frame );
		printf(".");
	}
	cvReleaseImage(&frame);
	//cvReleaseVideoWriter(&video);
	save_movie = false;
	postrackbar = oldpostrackbar;
	printf("done.\n");
	recalcImage();
}

void controlCB( int control ) {
	if (control==1) {
		saveMovie(0);
	}
}

void batchTesting(char *testfile) {
	const int num_cases = 10;
	const double testpos[num_cases][3] = 
	{{	 0.930285, -2.86312, 11.6026},
	{	 3.01047,   0,       11.6026},
	{	 4.33335,  -3.14836, 10.7126},
	{	 6.29935,   0,      10.1926},
	{	 2.79085,  -8.58937, 7.88151},
	{	 7.64373,  -3.14836, 8.6667},
	{	 5.80132,  -8.58937, 6.02094},
	{	 9.96168,  -2.86313, 6.02094},
	{	-4.33335,  -7.03995, 8.6667},
	{	-8.03447,  -1.94579, 8.6667}};

	/*
	ifstream testin(testfile);
	if (testin == NULL) {
		cout << testfile << " not found for test light positions! \n";
		return;
	}
	int num_cases;
	testin >> num_cases;
	*/
	char savegeneratedfiles[250];
	IplImage *frame = cvCreateImage(cvSize(width, height),IPL_DEPTH_8U,3);

	for (int i=0; i<num_cases;i++) {
		//testin >> lx >> ly >> lz;
		lx = testpos[i][0];
		ly = testpos[i][1];
		lz = testpos[i][2];
		cout << lx << " " << ly << " " << lz <<endl;
		double mag = sqrt(lx*lx+ly*ly+lz*lz);
		lx /= mag;
		ly /= mag;
		lz /= mag;
		postrackbar = windowwidth/2;
		clearLightingCache();
		recalcImage();
		copyArraytoImage(frame,imgshow);
		cvConvertImage(frame,frame, CV_CVTIMG_FLIP );
		cvConvertImage(frame,frame, CV_CVTIMG_SWAP_RB );
		
		sprintf_s(savegeneratedfiles,"out_%02d.png",i+1);
		cvSaveImage(savegeneratedfiles,frame);
		printf("%d.",i+1);
		
	}
	cvReleaseImage(&frame);
}



void main(int argc, char **argv) {

	char basefiles[250] = "base_images.txt", disparityfiles[250] = "disparity_maps.txt";

	if (argc == 2) {
		int	strcount=strlen(argv[1]);
		char * ext=&argv[1][strcount-3];		
		if(strcmp(ext,"rti")==0 || strcmp(ext,"hsh")==0) {
			one_file_only = true;
			snap_nearest = true;
			strcpy_s(basefiles,argv[1]);
		}
		ext=&argv[1][strcount-5];
		if(strcmp(ext,"mview")==0) { 
			snap_nearest = false;
			strcpy_s(basefiles,argv[1]);
		}
	}
	else {
		cout << " Usage : lightview.exe [RTI filename / MVIEW filename] " << endl;
		return;
	}

	if (one_file_only)
		readSingleHSHfile(basefiles);
	else
		readInputFiles(basefiles);
	
	windowheight = height;
	windowwidth = width;

	if (batch_mode) {
		batchTesting("test_lights.txt");
		clearUp(true);
		return;
	}

	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_DEPTH | GLUT_DOUBLE | GLUT_RGBA);
	glutInitWindowPosition(100,100);
	glutInitWindowSize(windowwidth+300, windowheight);
	int window_id = glutCreateWindow("lview");

	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	glPixelStorei(GL_PACK_ALIGNMENT, 1);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(0.0, (GLdouble) windowwidth+300, 0.0, (GLdouble) windowheight);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	//glutAttachMenu(GLUT_RIGHT_BUTTON);
	glutDisplayFunc(renderScene);
	GLUI_Master.set_glutIdleFunc(NULL);
	glui = GLUI_Master.create_glui("lview menu");
	glui->add_checkbox("Use left disparity",&use_left_image);
	glui->add_checkbox("Use right disparity",&use_right_image);
	glui->add_checkbox("Warp hole filling",&keep_continuity);
	glui->add_checkbox("Use interpolation for warping",&use_color_interp);
	glui->add_checkbox("Snap to nearest viewpoint",&snap_nearest);
	glui->add_separator();
	glui->add_edittext("Movie Frames :",GLUI_EDITTEXT_INT, &movie_frames);
	glui->add_button("Save Movie",1,controlCB);
	//glui->add_rotation("Ball");

	//glutIdleFunc(idleFunc);
	glutMouseFunc(processMouse);
	glutMotionFunc(processMouseActiveMotion);
	glutKeyboardFunc(processNormalKeys);

	clearLightingCache();
	recalcImage();

	glui->set_main_gfx_window(window_id);
	glutMainLoop();

	clearUp(true);
}