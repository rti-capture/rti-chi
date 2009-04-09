#include "viewer.h"
#include <iostream>
#include <fstream>


float * loadHSH(char* fn)
{
   FILE *fp;
   float *hshpixels = NULL;
	float scale[30],bias[30];
	unsigned char tmpuc[30];
	char buffer[500];
	g_mode=MODE_HEMISPHERICAL;

/*
   fp = fopen(fn,"rb");
   char c = fgetc(fp);
   while (c=='#') 
   {
	   fgets(buffer,500,fp);
	   c = fgetc(fp);
   }
   fseek(fp,-1,SEEK_CUR);

   int file_type, terms, basis_type, element_size;
   if(fp!=NULL)
   {
	   fscanf(fp,"%d\r\n",&file_type);
	   fscanf(fp,"%d %d %d\r\n",&width	,&height, &bands);
	   fscanf(fp,"%d %d %d\r\n",&terms	,&basis_type, &element_size);

	   order = sqrt((float)terms);
  
	   hshpixels = (float*)malloc(width*height*bands*order*order*sizeof(float));

	   fread(scale,sizeof(float),order*order,fp);
       fread(bias,sizeof(float),order*order,fp);

	   for (int i=0; i<terms; i++) 
		   cout << scale[i] << " ";
	   cout << endl;
	   for(int j=0;j<height;j++)
       {
           for(int i=0;i<width;i++)
           {
			   for (int b=0; b<bands; b++)
			   {
					fread(tmpuc,sizeof(char),order*order,fp);
					for (int q=0;q<order*order;q++) 
					{
						double tmp = double(tmpuc[q])/255;
						tmp = tmp * scale[q] + bias[q];
						hshpixels[getindex(height-1-j,i,b,q)] = tmp;   // flip the image (height - 1 - j) for opengl pixel rendering
					}
			   }
		   }
		}

   }
   fclose(fp);
*/

	ifstream infile(fn,ios::binary);

	while (infile.peek() == '#') {
		infile.getline(buffer,500);
	}

	int file_type, terms, basis_type, element_size;
	float dummy_scale, dummy_bias;
	infile >> file_type;
	infile >> width >> height >> bands;
	infile >> terms >> basis_type >> element_size;
	infile.getline(buffer,10);

	order = sqrt((float)terms);

	hshpixels = (float*)malloc(width*height*bands*order*order*sizeof(float));

	for (int i=0; i<terms; i++) 
		infile.read((char *)&scale[i],sizeof(float)); // scale
	for (int i=0; i<terms; i++) 
		infile.read((char *)&bias[i],sizeof(float)); // bias

	
	for (int j=0; j<height; j++)
		for (int i=0; i<width; i++)
			for (int b=0; b<bands; b++)
			{
				infile.read((char *)&tmpuc,sizeof(char)*terms);
				for (int q=0; q<terms; q++) 
				{
					float value = float(tmpuc[q])/255;
					value = (value*scale[q])+bias[q];
					hshpixels[getindex(height-1-j,i,b,q)] = value;   // flip the image (height - 1 - j) for opengl pixel rendering
				}
			}

	infile.close();
   return hshpixels;
}

float * loadHSHuncompressed(char* fn)
{
   FILE *fp;
   float *hshpixels = NULL;
	float scale[30],bias[30];
	float tmpf[30];
	char buffer[500];
	g_mode=MODE_HEMISPHERICAL;


   fp = fopen(fn,"rb");
   char c = fgetc(fp);
   while (c=='#') 
   {
	   fgets(buffer,500,fp);
	   c = fgetc(fp);
   }
   fseek(fp,-1,SEEK_CUR);
   if(fp!=NULL)
   {
       fread(&width,sizeof(int),1,fp);
       fread(&height,sizeof(int),1,fp);
       fread(&bands,sizeof(int),1,fp);
       fread(&order,sizeof(int),1,fp);

	   hshpixels = (float*)malloc(width*height*bands*order*order*sizeof(float));

	   fread(scale,sizeof(float),order*order,fp);
       fread(bias,sizeof(float),order*order,fp);

	   for(int j=0;j<height;j++)
       {
           for(int i=0;i<width;i++)
           {
			   for (int b=0; b<bands; b++)
			   {
					fread(tmpf,sizeof(float),order*order,fp);
					for (int q=0;q<order*order;q++) 
					{
						hshpixels[getindex(height-1-j,i,b,q)] = tmpf[q];   // flip the image (height - 1 - j) for opengl pixel rendering
					}
			   }
		   }
		}
 
   }
   fclose(fp);
   return hshpixels; 
}

void load(char* fn,vector<pixel> &pixels,vector<shpixel> &shpixels,vector<shpixel> &popixels, int scale)
{
   FILE *fp;
   shpixel shp;
	float minx,maxx;
	unsigned char tmpuc9[30];
	g_mode=MODE_SPHERICAL;

   fp = fopen(fn,"rb");
   if(fp!=NULL)
   {
       fread(&width,sizeof(int),1,fp);
       fread(&height,sizeof(int),1,fp);
       fread(&minx,sizeof(float),1,fp);
       fread(&maxx,sizeof(float),1,fp);

	   for(int j=0;j<height;j++)
       {
           for(int i=0;i<width;i++)
           {
                   fread(&tmpuc9,sizeof(unsigned char),9,fp);

                   for(int q=0;q<9;q++)
                       shp.data_r[q] =((((float)tmpuc9[q])/255.0)*(maxx-minx))+minx;

                   fread(&tmpuc9,sizeof(unsigned char),9,fp);

                   for(int q=0;q<9;q++)
                       shp.data_g[q] =((((float)tmpuc9[q])/255.0)*(maxx-minx))+minx;

                   fread(&tmpuc9,sizeof(unsigned char),9,fp);

                   for(int q=0;q<9;q++)
                       shp.data_b[q] =((((float)tmpuc9[q])/255.0)*(maxx-minx))+minx;

                   shpixels.push_back(shp);
               }
		}

	    int op = shpixels.size();

		for(int j=0;j<height/2;j++)
		{			
			for (int i=0;i<width;i++)
			{			
				int index1 = j*width+i;
				int index2 = ((height-1)-j)*width+i;

				if(g_mode==MODE_PHONG || g_mode==MODE_WARD || g_mode==MODE_PHOTOMETRIC)
					swap(&(pixels[index1]),&(pixels[index2]));
				else if (g_mode==MODE_SPHERICAL)
				{
					swapsh(&(shpixels[index1]),&(shpixels[index2]));
				}
				else if(g_mode==MODE_POLY)
				{
					swapsh(&(popixels[index1]),&(popixels[index2]));
				}
			}
		}

   }

/*
	FILE *fp;
	pixel p;
	shpixel shp, pop;
	int i,j;
	
	float tmpf[30];
	
	int fullwidth,fullheight;
	int skipscalex = scale, skipscaley = scale;
	int index1,index2;
	bool readnow = true;

	width=0;
	height=0;

	fp = fopen(fn,"rb");
	if(	fp!=NULL)
	{
		fread(&fullwidth,sizeof(int),1,fp);
		fread(&fullheight,sizeof(int),1,fp);	

		width = fullwidth / scale;
		height = fullheight / scale;

		if(g_mode==MODE_SPHERICAL || g_mode==MODE_POLY)
		{
			fread(&minx,sizeof(float),1,fp);
			fread(&maxx,sizeof(float),1,fp);
		}

		printf("size: %i, %i, scale: %f, %f\n",width,height,minx,maxx);

		basewindow_xsize=max(width+300,300);
		basewindow_ysize=max(height,300);

		drawwindow_xsize=width;
		drawwindow_ysize=height;
	
		current_width=width+300;
		current_height=height;
	
		for(j=0;j<fullheight;j++)
		{
			// if its scaled down, read the proper subsampled rows only.
			for(i=0;i<fullwidth;i++)
			{
				if (skipscaley == 1) 
				{
					if (skipscalex == 1)  
					{
						readnow = true;
						skipscalex = scale;
					}
					else 
					{
						readnow = false;
						skipscalex--;
					}
				}
				else 
				{
					readnow = false;
				}

				if(g_mode==MODE_POLY)
				{
					fread(&tmpuc,sizeof(unsigned char),18,fp);

					if (readnow) {
						for(int q=0;q<6;q++)
							pop.data_r[q] = ((((double)tmpuc[q])/255.0)*(maxx-minx))+minx;

						for(int q=0;q<6;q++)
							pop.data_g[q] = ((((double)tmpuc[q+6])/255.0)*(maxx-minx))+minx;

						for(int q=0;q<6;q++)
							pop.data_b[q] = ((((double)tmpuc[q+12])/255.0)*(maxx-minx))+minx;

						popixels.push_back(pop);
					}
				}
				
				if(g_mode==MODE_SPHERICAL)
				{
					fread(&tmpuc,sizeof(unsigned char),27,fp);
					
					if (readnow)
					{						
						for(int q=0;q<9;q++)
							shp.data_r[q] = ((((double)tmpuc[q])/255.0)*(maxx-minx))+minx;
						
						for(int q=0;q<9;q++)
							shp.data_g[q] = ((((double)tmpuc[q+9])/255.0)*(maxx-minx))+minx;
		
						for(int q=0;q<9;q++)
							shp.data_b[q] = ((((double)tmpuc[q+18])/255.0)*(maxx-minx))+minx;

						shpixels.push_back(shp);
					}
				}	
				
				if(g_mode==MODE_PHOTOMETRIC)					
				{
					fread(&tmpf,sizeof(float),6,fp);
					if (readnow) 
					{
						p.normal[0]=(double)tmpf[0];
						p.normal[1]=(double)tmpf[1];
						p.normal[2]=(double)tmpf[2];
						p.albedo[0]=(double)tmpf[3];
						p.albedo[1]=(double)tmpf[4];
						p.albedo[2]=(double)tmpf[5];
						pixels.push_back(p);
					}
				}
				if(g_mode==MODE_WARD)
				{
					fread(&tmpf,sizeof(float),10,fp);
					if (readnow) 
					{
						p.albedo[0] = ((double)tmpf[0])/1.0;
						p.albedo[1] = ((double)tmpf[1])/1.0;				
						p.albedo[2] = ((double)tmpf[2])/1.0;
						if(g_mode==1 && p.albedo[2]==0)
						{
							p.albedo[2]=.001;
						}
						p.normal[0] = ((double)tmpf[3])/1.0;
						p.normal[1] = ((double)tmpf[4])/1.0;
						p.normal[2] = ((double)tmpf[5])/1.0;
						p.ka=((double)tmpf[6])/1.0;
						p.kd=((double)tmpf[7])/1.0;
						p.ks=((double)tmpf[8])/1.0;
						p.n = ((double)tmpf[9])/1.0;
						pixels.push_back(p);	
					}
				}

				//printf("read: %d %d %d, %d %d %d ,%d %d %d %d\n",p.albedo[0],p.albedo[1],p.albedo[2],p.normal[0],p.normal[1],p.normal[2],p.ka,p.kd,p.ks,p.n);
				//getchar();
			}		
			if (skipscaley == 1) 
				skipscaley = scale;
			else
				skipscaley--;
		}


		for(j=0;j<height/2;j++)
		{			
			for (i=0;i<width;i++)
			{			
				index1 = j*width+i;
				index2 = ((height-1)-j)*width+i;

				if(g_mode==MODE_PHONG || g_mode==MODE_WARD || g_mode==MODE_PHOTOMETRIC)
					swap(&(pixels[index1]),&(pixels[index2]));
				else if (g_mode==MODE_SPHERICAL)
				{
					swapsh(&(shpixels[index1]),&(shpixels[index2]));
				}
				else if(g_mode==MODE_POLY)
				{
					swapsh(&(popixels[index1]),&(popixels[index2]));
				}
			}
		}
			
	}

	*/
}