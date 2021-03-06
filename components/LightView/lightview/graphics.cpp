#include "viewer.h"


unsigned char* g_i;
int basewindow;
bool button_down=false;
const float DEG2RAD = 3.14159/180;
int cx=0;
int cy=0;

int circle_x_origin = basewindow_xsize-150;
int circle_y_origin = basewindow_ysize-150;
int circle_radius = 140;

int square_x_origin = basewindow_xsize-150;
int square_y_origin = basewindow_ysize-200;
int square_x_size = 280;
int square_y_size = 140;

void drawSquare()
{
	double nx,ny,nz,mag;
	int index;
	glColor3f(0.1,0.0,0.0);
	glBegin(GL_POLYGON);
	{
		glVertex2f(square_x_origin - square_x_size/2,square_y_origin - square_y_size/2);
		glVertex2f(square_x_origin - square_x_size/2,square_y_origin + square_y_size/2);
		glVertex2f(square_x_origin + square_x_size/2,square_y_origin + square_y_size/2);
		glVertex2f(square_x_origin + square_x_size/2,square_y_origin - square_y_size/2);
	}
	glEnd();

	index = ((width)*cy + cx);
	if(g_mode == MODE_WARD || g_mode == MODE_PHONG || g_mode== MODE_PHOTOMETRIC)
	{
		nx = pixels[index].normal[0];
		ny = pixels[index].normal[1];
		nz = pixels[index].normal[2];
		mag = sqrt(nx*nx+nz*nz);
		nx = nx/mag;
		nz = nz/mag;
		glLineWidth(3.0);
		glColor3f(pixels[index].albedo[0],pixels[index].albedo[1],pixels[index].albedo[2]);
		glBegin(GL_LINES);
		{		
			glVertex2f(square_x_origin,square_y_origin-square_y_size/2);
			glVertex2f(square_x_origin + -nx*50,square_y_origin-square_y_size/2 + nz*50);
		}	
		glEnd();
		glLineWidth(1.0);
	}
}

void drawCircle()
{	

	glColor3f(0.1,0.0,0.0);
	glBegin(GL_POLYGON);
	float degInRad;
	for (int i=0; i < 360; i++)
	{
	  degInRad = i*DEG2RAD;
	  glVertex2f(cos(degInRad)*circle_radius+circle_x_origin,sin(degInRad)*circle_radius+circle_y_origin);
	}
	glEnd();

	glColor3f(0.0,0.0,1.0);
	glBegin(GL_POINTS);
	glVertex2f(circle_x_origin+lx*circle_radius,circle_y_origin+ly*circle_radius);
	glEnd();
}

void drawImage()
{
	double sx = (double)drawwindow_xsize/(double)width;
	double sy = (double)drawwindow_ysize/(double)height;
	
	glRasterPos2i(0,0);
	glPixelZoom(sx,sy);	
	glDrawPixels(width,height,GL_RGB,GL_UNSIGNED_BYTE,g_i);
}

void saveImage()
{
	printf("%i %i\n",width,height);
	FILE *outfp;
	int x,y;
	
	outfp = fopen("image.ppm","wb");
	if (outfp == NULL) {
		fprintf(stderr,"Unable to open file for writing\n");
		return;
	}
	
	fprintf(outfp,"P6\n");
	fprintf(outfp,"%i ",width);
	fprintf(outfp,"%i\n",height);
	fprintf(outfp,"255\n");
	
		
	for (y=height-1; y>=0; y--)
	{
		for (x=0; x<width; x++) 
		{
			fwrite(&g_i[3*(y*width+x)],sizeof(unsigned char),3,outfp);
		}
	}

	fclose(outfp);
}

void changeSize(int w, int h) 
{
	float ar; 
	float myar; 

	ar = (float)drawwindow_xsize/drawwindow_ysize;
	myar = (float)(w-300)/h;

	if(myar>ar)
		w=round((w-300)*(ar/myar))+300;
	else
		h=round(h*(myar/ar));

	if(w<=1)
		w=1;
	if(h<=1)
		h=1;
	glutReshapeWindow(w,h);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	current_width=w;
	current_height=h;
	drawwindow_xsize=w-300;
	drawwindow_ysize=h;
	circle_x_origin = current_width-150;
	circle_y_origin = current_height-150;

	square_x_origin = current_width-150;
	square_y_origin = current_height-360;
	// Set the viewport to be the entire window
	glViewport(0, 0, w, h);
	gluOrtho2D(0.0, (GLdouble) w, 0.0, (GLdouble) h);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
}

void processMouse1(int button, int state, int x, int y) 
{
	float dist;
	int index;
	y = current_height-y;
	if (button == GLUT_LEFT_BUTTON) 
	{
		if(x>circle_x_origin-circle_radius && x<circle_x_origin+circle_radius && 
		   y>circle_y_origin-circle_radius && y<circle_y_origin+circle_radius)
		{
			dist = sqrt(pow((float)x-circle_x_origin,2)+pow((float)y-circle_y_origin,2));
			if(dist<circle_radius)
			{
				lx = (x-circle_x_origin)/(float)circle_radius;
				ly = (y-circle_y_origin)/(float)circle_radius;
				lz = sqrt(1-lx*lx-ly*ly);	
				renderImage();
			}
		}
		else if(x >= 0 && x < drawwindow_xsize && y >= 0 && y < drawwindow_ysize)
		{			
			float sx = (float)drawwindow_xsize/(float)width;
			float sy = (float)drawwindow_ysize/(float)height;

			index = ((width)*cy + cx);
			cx = x/sx;
			cy = y/sy;
			if(state==GLUT_UP)
				cout<<cx<<","<<cy<<": ["<<(int)g_i[index*3]<<","<<(int)g_i[index*3+1]<<","<<(int)g_i[index*3+2]<<"]"<<endl;

		}
	}
}

void processMouseActiveMotion1(int x, int y) 
{
	y = current_height-y;
	float dist;
	if(x>circle_x_origin-circle_radius && x<circle_x_origin+circle_radius && y>circle_y_origin-circle_radius && y<circle_y_origin+circle_radius)
	{
		dist = sqrt(pow((float)x-circle_x_origin,2)+pow((float)y-circle_y_origin,2));
		if(dist<circle_radius)
		{
			lx = (x-circle_x_origin)/(float)circle_radius;
			ly = (y-circle_y_origin)/(float)circle_radius;
			lz = sqrt(1-lx*lx-ly*ly);	
			renderImage();
		}
	}
	else if(x >= 0 && x < drawwindow_xsize && y >= 0 && y < drawwindow_ysize)
	{			
		float sx = (float)drawwindow_xsize/(float)width;
		float sy = (float)drawwindow_ysize/(float)height;

		cx = x/sx;
		cy = y/sy;
	}
}
 
void processMousePassiveMotion1(int x, int y) {
}


void SetupGlutWindows(int argc, char **argv)
{
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
	
	glutInitWindowSize(basewindow_xsize,basewindow_ysize);
	glutInitWindowPosition(30, 100);

	basewindow = glutCreateWindow("PHL Viewer");

	glShadeModel(GL_FLAT);
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

	glPointSize(3.0);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(0.0, (GLdouble) basewindow_xsize, 0.0, (GLdouble) basewindow_ysize);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	glutDisplayFunc(display);
	glutIdleFunc(idle);
	glutKeyboardFunc(keys);
	glutReshapeFunc(changeSize);
	
	glutMouseFunc(processMouse1);
	glutMotionFunc(processMouseActiveMotion1);
	glutPassiveMotionFunc(processMousePassiveMotion1);

	g_i=Image;
}

void	idle(void)
{
	glutSetWindow(basewindow);
	glClearColor (0.3, 0.3, 0.3, 1.0);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glDisable(GL_LIGHTING);
	glEnable(GL_BLEND);
	glDisable(GL_DEPTH_TEST);


	drawCircle();
	drawSquare();
	drawImage();
	glutSwapBuffers();
}

void	display(void)
{
	glutSetWindow(basewindow);
	glutSwapBuffers();
}

void keys(unsigned char key, int x, int y)
{
	switch(key)
	{
		case('.'):
		break;
		case(','):
		break;
		case('x'):
			exit(0);
		break;
		case('1'):
			showMode=0;
			g_i=Image;
			renderImage();
		break;
		case('2'):
			showMode=1;
			g_i=normalImage;
		break;
		case('3'):
			showMode=2;
			g_i=kaImage;
		break;
		case('4'):
			showMode=3;
			g_i=kdImage;
		break;
		case('5'):
			showMode=4;
			g_i=ksImage;
		break;
		case('6'):
			showMode=5;
			g_i=albedoImage;
		break;
		case('7'):
			showMode=6;
			g_i=Image;
			renderImage();
		break;
		case('s'):
			saveImage();
		break;
	}
}