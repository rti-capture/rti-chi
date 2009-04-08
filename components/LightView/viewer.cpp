// viewer.cpp : Defines the entry point for the console application.
//

#include "viewer.h"

int basewindow_xsize;
int basewindow_ysize;

int drawwindow_xsize;
int drawwindow_ysize;

int width;
int height;
int bands;
int order;

int current_width;
int current_height;

float lx = 0;
float ly = 0;
float lz = 1;

type g_mode;

//vector<pixel> pixels;
//vector<shpixel> shpixels;
//vector<shpixel> popixels;

int showMode=0;

void swap(pixel *p1, pixel *p2)
{
	double tmp;

	tmp=p1->ka;
	p1->ka=p2->ka;
	p2->ka=tmp;

	tmp=p1->kd;
	p1->kd=p2->kd;
	p2->kd=tmp;

	tmp=p1->ks;
	p1->ks=p2->ks;
	p2->ks=tmp;

	tmp=p1->n;
	p1->n=p2->n;
	p2->n=tmp;

	tmp=p1->albedo[0];
	p1->albedo[0]=p2->albedo[0];
	p2->albedo[0]=tmp;

	tmp=p1->albedo[1];
	p1->albedo[1]=p2->albedo[1];
	p2->albedo[1]=tmp;

	tmp=p1->albedo[2];
	p1->albedo[2]=p2->albedo[2];
	p2->albedo[2]=tmp;

	tmp=p1->normal[0];
	p1->normal[0]=p2->normal[0];
	p2->normal[0]=tmp;

	tmp=p1->normal[1];
	p1->normal[1]=p2->normal[1];
	p2->normal[1]=tmp;

	tmp=p1->normal[2];
	p1->normal[2]=p2->normal[2];
	p2->normal[2]=tmp;
}

void swapsh(shpixel *p1, shpixel *p2)
{
	double tmp;

	for(int i=0;i<9;i++)
	{
		tmp = p1->data_r[i];
		p1->data_r[i]=p2->data_r[i];
		p2->data_r[i]=tmp;

		tmp = p1->data_g[i];
		p1->data_g[i]=p2->data_g[i];
		p2->data_g[i]=tmp;

		tmp = p1->data_b[i];
		p1->data_b[i]=p2->data_b[i];
		p2->data_b[i]=tmp;
	}
}

