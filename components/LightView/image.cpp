#include "viewer.h"

#include <iostream>
#include <fstream>
using namespace std;

void renderImagePhong(vector<pixel> &pixels)
{
	int i,j,index;
	double r,g,b;
	double inside;
	double hx,hy,hz,mag;
	double nl;
	int index_prime;

	r=0;g=0;b=0;
	hx=-lx;
	hy=ly;
	hz=lz+1;
	mag = sqrt(hx*hx+hy*hy+hz*hz);
	hx=hx/mag;
	hy=hy/mag;
	hz=hz/mag;

	for (j=0;j<height;j++)
	{
		for(i=0;i<width;i++)
		{
			index = (j*width+i)*3;
			index_prime	= ((width)*j + i);
			
			nl = (pixels[index_prime].normal[0]*-lx + pixels[index_prime].normal[1]*ly + pixels[index_prime].normal[2]*lz);
			if(nl<0)
			{
				r=0;
				g=0;
				b=0;
			}
			else
			{		
				if(showMode==0)
				{
					inside = pixels[index_prime].ka + 
						 pixels[index_prime].kd*nl + 
						 pixels[index_prime].ks*powf((pixels[index_prime].normal[0]*hx + pixels[index_prime].normal[1]*hy + pixels[index_prime].normal[2]*hz),pixels[index_prime].n);

					r = pixels[index_prime].albedo[0]*inside;
					g = pixels[index_prime].albedo[1]*inside;
					b = pixels[index_prime].albedo[2]*inside;			
				}
				else if(showMode==6)
				{
					inside = .1 + .4*nl + .4*powf((pixels[index_prime].normal[0]*hx + pixels[index_prime].normal[1]*hy + pixels[index_prime].normal[2]*hz),10.0);

					r = inside;
					g = inside;
					b = inside;			
				}
			}

			r = min(r,1.0);
			g = min(g,1.0);
			b = min(b,1.0);

			r = max(r,0);
			g = max(g,0);
			b = max(b,0);				

			Image[index+0] = (unsigned char)(r*255);
			Image[index+1] = (unsigned char)(g*255);
			Image[index+2] = (unsigned char)(b*255);
		}
	}
}



void renderImageWard(vector<pixel> &pixels)
{
	int i,j,index;
	double r,g,b;
	double inside;
	double hx,hy,hz,mag,angle;
	double nl,nh,nv;
	int index_prime;

	r=0;g=0;b=0;
	hx=lx;
	hy=ly;
	hz=lz+1;
	mag = sqrt(hx*hx+hy*hy+hz*hz);
	hx=hx/mag;
	hy=hy/mag;
	hz=hz/mag;

	for (j=0;j<height;j++)
	{
		for(i=0;i<width;i++)
		{
			index = (j*width+i)*3;
			index_prime	= ((width)*j + i);
			
			nl = (pixels[index_prime].normal[0]*lx + pixels[index_prime].normal[1]*ly + pixels[index_prime].normal[2]*lz);
			if(nl<0)
			{
				r=0;
				g=0;
				b=0;
			}
			else
			{		
				nh = (hx*pixels[index_prime].normal[0] + hy*pixels[index_prime].normal[1] + hz*pixels[index_prime].normal[2]);
				nv = pixels[index_prime].normal[2];
			    
				angle = tan(acos(nh));	
				inside = exp(-(angle*angle)/(pixels[index_prime].albedo[2]*pixels[index_prime].albedo[2])) / sqrt(nv*nl)*(4*PI*pixels[index_prime].albedo[2]*pixels[index_prime].albedo[2]);
			    
				r = (pixels[index_prime].ka*IPI+pixels[index_prime].n*inside)*nl;
				g = (pixels[index_prime].kd*IPI+pixels[index_prime].albedo[0]*inside)*nl;
				b = (pixels[index_prime].ks*IPI+pixels[index_prime].albedo[1]*inside)*nl;
			}

			r = min(r,1.0);
			g = min(g,1.0);
			b = min(b,1.0);

			r = max(r,0);
			g = max(g,0);
			b = max(b,0);				

			Image[index+0] = (unsigned char)(r*255);
			Image[index+1] = (unsigned char)(g*255);
			Image[index+2] = (unsigned char)(b*255);
		}
	}
}

void renderImageSh(vector<shpixel> &shpixels,vector<shpixel> &popixels, unsigned char* Image)
{
	int i,j,index;
	double r,g,b;
	double weights[9];
	int index_prime;
	int startj=0;
	int endj=height;

	r=0;g=0;b=0;

	for(j=0;j<height;j++)
	{
		for(i=0;i<width;i++)
		{
			index_prime	= ((width)*j + i);

			weights[0] = .282095;
			weights[1] = .488603 * ly;
			weights[2] = .488603 * lz;
			weights[3] = .488603 * lx;
			weights[4] = 1.092548 * (lx*ly);
			weights[5] = 1.092548 * (ly*lz);
			weights[6] = .315392 * (3*lz*lz-1);
			weights[7] = 1.092548 * (lx*lz);								
			weights[8] = .546274 * (lx*lx - ly*ly);			
			
			r = 0;
			g = 0;
			b = 0;
			for(int q=0;q<9;q++)
			{
				r+=shpixels[index_prime].data_r[q]*weights[q];
				g+=shpixels[index_prime].data_g[q]*weights[q];
				b+=shpixels[index_prime].data_b[q]*weights[q];
			}

			r = min(r,1.0);
			g = min(g,1.0);
			b = min(b,1.0);
		
			r = max(r,0);
			g = max(g,0);
			b = max(b,0);				
			index = j*width*3  + i*3 ; // + (j*2) // hack

			Image[index] = (unsigned char)(r*255);
			Image[index+1] = (unsigned char)(g*255);
			Image[index+2] = (unsigned char)(b*255);
		}
	}
}

void renderImagePhotometric(vector<pixel> &pixels)
{
	int i,j,index;
	double r,g,b;
	double inside;
	double hx,hy,hz,mag,angle;
	double nl,nh,nv;
	int index_prime;

	r=0;g=0;b=0;
	hx=lx;
	hy=ly;
	hz=lz+1;
	mag = sqrt(hx*hx+hy*hy+hz*hz);
	hx=hx/mag;
	hy=hy/mag;
	hz=hz/mag;

	for (j=0;j<height;j++)
	{
		for(i=0;i<width;i++)
		{
			index = (j*width+i)*3;
			index_prime	= ((width)*j + i);
			
			nl = (pixels[index_prime].normal[0]*lx + pixels[index_prime].normal[1]*ly + pixels[index_prime].normal[2]*lz);
			if(nl<0)
			{
				r=0;
				g=0;
				b=0;
			}
			else
			{				    
				r = (pixels[index_prime].albedo[0]*nl);
				g = (pixels[index_prime].albedo[1]*nl);
				b = (pixels[index_prime].albedo[2]*nl);
			}

			r = min(r,1.0);
			g = min(g,1.0);
			b = min(b,1.0);

			r = max(r,0);
			g = max(g,0);
			b = max(b,0);				

			Image[index+0] = (unsigned char)(r*255);
			Image[index+1] = (unsigned char)(g*255);
			Image[index+2] = (unsigned char)(b*255);
		}
	}
}

void renderImageHSH(float * hshimage, unsigned char* Image)
{
	double weights[30];

	double phi = atan2(ly,lx);
	if (phi<0) phi+=2*PI;
	double theta = acos(lz);

	weights[0] = 1/sqrt(2*PI);
	weights[1]  = sqrt(6/PI)      *  (cos(phi)*sqrt(cos(theta)-cos(theta)*cos(theta)));
	weights[2]  = sqrt(3/(2*PI)) *  (-1 + 2*cos(theta));
	weights[3]  = sqrt(6/PI)      *  (sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));

	weights[4]  = sqrt(30/PI)     *  (cos(2*phi)*(-cos(theta) + cos(theta)*cos(theta)));
	weights[5]  = sqrt(30/PI)     *  (cos(phi)*(-1 + 2*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta)));
	weights[6]  = sqrt(5/(2*PI)) *  (1 - 6*cos(theta) + 6*cos(theta)*cos(theta));
	weights[7]  = sqrt(30/PI)     *  ((-1 + 2*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));
	weights[8]  = sqrt(30/PI)     *  ((-cos(theta) + cos(theta)*cos(theta))*sin(2*phi));

	weights[9]   = 2*sqrt(35/PI)  * cos(3*phi)*pow(cos(theta) - cos(theta)*cos(theta),(3/2));
	weights[10]  = (sqrt(210/PI)  * cos(2*phi)*(-1 + 2*cos(theta))*(-cos(theta) + cos(theta)*cos(theta)));
	weights[11]  = 2*sqrt(21/PI)  * cos(phi)*sqrt(cos(theta) - cos(theta)*cos(theta))*(1 - 5*cos(theta) + 5*cos(theta)*cos(theta));
	weights[12]  = sqrt(7/(2*PI)) * (-1 + 12*cos(theta) - 30*cos(theta)*cos(theta) + 20*cos(theta)*cos(theta)*cos(theta));
	weights[13]  = 2*sqrt(21/PI)  * sqrt(cos(theta) - cos(theta)*cos(theta))*(1 - 5*cos(theta) + 5*cos(theta)*cos(theta))*sin(phi);
	weights[14]  = (sqrt(210/PI)  * (-1 + 2*cos(theta))*(-cos(theta) + cos(theta)*cos(theta))*sin(2*phi));
	weights[15]  = 2*sqrt(35/PI)  * pow(cos(theta) - cos(theta)*cos(theta),(3/2))*sin(3*phi);

	//ofstream ofile;
	//ofile.open("debugvalues.txt");
   for(int j=0;j<height;j++)
   {
       for(int i=0;i<width;i++)
       {
		   for (int b=0; b<bands; b++)
		   {
			   double value = 0;
				for (int q=0;q<order*order;q++) 
				{
					value += hshimage[getindex(j,i,b,q)]*weights[q];
				}
				value = min(value,1);
				value = max(value,0);
				Image[j*width*bands+i*bands+b]=(unsigned char)(value*255);
				//if (b==0) ofile << (int)Image[j*width*bands+i*bands+b] << " ";
		   }
	   }
	   //ofile << endl;
   }
   //ofile.close();
}



void renderImage(vector<pixel> &pixels,vector<shpixel> &shpixels,vector<shpixel> &popixels, unsigned char* Image)
{
	if(g_mode==MODE_PHONG)
		renderImagePhong(pixels);
	else if(g_mode==MODE_WARD)
		renderImageWard(pixels);
	else if(g_mode==MODE_SPHERICAL || g_mode == MODE_POLY)
		renderImageSh(shpixels,popixels,Image);
	else if(g_mode==MODE_PHOTOMETRIC)
		renderImagePhotometric(pixels);
}

void generateImages(vector<pixel> &pixels)
{
	int i,j,index;
	int r,g,b;
	int index_prime;

	r=0;g=0;b=0;
	
	for (j=0;j<height;j++)
	{
		for(i=0;i<width;i++)
		{
			index = (j*width+i)*3;
			index_prime		= ((width)*j + i);

			if(g_mode == MODE_PHONG || g_mode == MODE_WARD)
			{
				albedoImage[index+0] = (unsigned char)(pixels[index_prime].albedo[0]*255);
				albedoImage[index+1] = (unsigned char)(pixels[index_prime].albedo[1]*255);
				albedoImage[index+2] = (unsigned char)(pixels[index_prime].albedo[2]*255);

				normalImage[index+0] = (unsigned char)(((pixels[index_prime].normal[0]+1.0)/2.0)*255);
				normalImage[index+1] = (unsigned char)(((pixels[index_prime].normal[1]+1.0)/2.0)*255);
				normalImage[index+2] = (unsigned char)(((pixels[index_prime].normal[2]+1.0)/2.0)*255);
			}

			if(g_mode==MODE_PHONG)
			{
				kdImage[index+0] = (unsigned char)(pixels[index_prime].kd*255);
				kdImage[index+1] = (unsigned char)(pixels[index_prime].kd*255);
				kdImage[index+2] = (unsigned char)(pixels[index_prime].kd*255);
			}	
			else if(g_mode==MODE_WARD)
			{
				kdImage[index+0] = (unsigned char)(pixels[index_prime].n*255);
				kdImage[index+1] = (unsigned char)(pixels[index_prime].albedo[0]*255);
				kdImage[index+2] = (unsigned char)(pixels[index_prime].albedo[1]*255);
			}

			if(g_mode==MODE_PHONG)
			{
				kaImage[index+0] = (unsigned char)(pixels[index_prime].ka*255);
				kaImage[index+1] = (unsigned char)(pixels[index_prime].ka*255);
				kaImage[index+2] = (unsigned char)(pixels[index_prime].ka*255);				
			}
			else if(g_mode==MODE_WARD)
			{
				kaImage[index+0] = (unsigned char)(pixels[index_prime].ka*255);
				kaImage[index+1] = (unsigned char)(pixels[index_prime].kd*255);
				kaImage[index+2] = (unsigned char)(pixels[index_prime].ks*255);
			}
			
			if(g_mode==MODE_PHONG)
			{
				ksImage[index+0] = (unsigned char)(pixels[index_prime].ks*255);
				ksImage[index+1] = (unsigned char)(pixels[index_prime].ks*255);
				ksImage[index+2] = (unsigned char)(pixels[index_prime].ks*255);
			}
			else if(g_mode==MODE_WARD)
			{
				ksImage[index+0] = (unsigned char)(pixels[index_prime].albedo[2]*255);
				ksImage[index+1] = (unsigned char)(pixels[index_prime].albedo[2]*255);
				ksImage[index+2] = (unsigned char)(pixels[index_prime].albedo[2]*255);
			}

		}
	}
}