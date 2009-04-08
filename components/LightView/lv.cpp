#include <fstream>

#include "viewer.h"
#include "interface.h"
#include "lv.h"

using namespace std;


Cview view[MAX];

unsigned char* albedoImage;
unsigned char* Image;
unsigned char* normalImage;
unsigned char* ksImage;
unsigned char* kdImage;
unsigned char* kaImage;

int use_left_image = true;
int use_right_image = true;
int save_movie = false;
int use_color_interp = true;
int snap_nearest = true;
int keep_continuity = true;
int cylindrical_warp = false;

short* imginterp1;
short* imginterp2;
unsigned char* imginterp;
float * fillstate1;
float * fillstate2;

float * contrib;


const int EMPTY = 9999; // empty value for disparity array (16 bit)
const unsigned char NOTHING = 254; // empty value for interpolated image (8 bit)
const int channels = 3; // take this to other global section

void paintPixel(unsigned char *pix_warp, unsigned char*pix, double ratio) {
	bool pix_empty = true;
	for (int c=0;c<channels;c++) if (pix_warp[c]!=NOTHING) pix_empty=false;

	for (int c=0;c<channels;c++) {
		//if (pix_empty)
		//	pix_warp[c] = pix[c]*ratio;
		//else
		short val = pix_warp[c] + pix[c]*ratio;
		if (val>255) val = 255;
		pix_warp[c] = val;						
	}
}

const int FILLED = 80;

void linearWarp(unsigned char* imgref, short *disp, double ratio, short* imgout, float *fillstate) {
	int row_width = width*channels;
	double radius = width / 2.0; // used for cylindrical warping if enabled

	memset(imgout,0,sizeof(short)*height*width*channels);
	memset(contrib,0,sizeof(float)*height*width);

	unsigned char *pix = imgref;
	int fp = 0, last_map = 0;
	bool continous_stretch = false;

	for (int y=0;y<height;y++)	{
		for (int x=0;x<width;x++) {
			short go_right = disp[width*y +x];
			//&& pix[0]+pix[1]+pix[2]>FILLED
			if (go_right != EMPTY ) { 
				if (x + go_right < width && x + go_right > 0) {

					if (cylindrical_warp) {
						double theta = asin(go_right/(2*radius));
						double s = 2*theta*radius;

					}

					int left, right;
					short *pix_warp;
					left = floor(go_right*ratio);
					right = ceil(go_right*ratio);

					if (use_color_interp && left != right) {

						// blinear interpolation from the projected point

						double left_dis = abs(go_right*ratio - left);
						double right_dis = abs(right - go_right*ratio);
						pix_warp = &imgout[row_width*y + (x+left)*channels];
						fp = y*width + x+left;
						
						contrib[fp] += right_dis;
						if (fillstate[fp]>left_dis) {
							fillstate[fp] = left_dis;
						}

						if (x<width-1) {
							contrib[fp+1] += left_dis;
							if (fillstate[fp+1]>right_dis)
								fillstate[fp+1]=right_dis;
						}

						for (int c=0;c<channels;c++)  {
							pix_warp[c] += pix[c]*right_dis;

							if (x<width-1) {
								pix_warp[c+channels] += pix[c]*left_dis;
							}
						}
					}
					else
					{
						// nearest neighbour interpolation
						left = right = round(go_right*ratio);
						pix_warp = &imgout[row_width*y + (x+left)*channels];
						// how far the projected pixels falls from the nearest neighbour
						double fraction_offset = abs(go_right*ratio - left);		

						fp = y*width + x+left;
						
						// if this has been filled by a more closely projected pixel, skip it
						if (fraction_offset<fillstate[fp]) {
							fillstate[fp] = fraction_offset;
							contrib[fp] = 1;
							for (int c=0;c<channels;c++) pix_warp[c] = pix[c];
						}
					}

					// fill in continous blocks (in the input image) if they got warped with gaps 
					if (keep_continuity) {
						int gap_f = x+left-last_map;
						if (continous_stretch && gap_f>1 && gap_f<15) {
							
							short *last_pix_warp = &imgout[row_width*y + last_map*channels];
							
							for (int x_f = last_map+1; x_f < x+left; x_f ++) {
								int fp_f = y*width + x_f;
								
								// the fill state value is quite large, at least 2, meant to be overwritten
								// if any real pixels get mapped here.
								double fraction_offset = gap_f; 

								if (fraction_offset < fillstate[fp_f]) {
									short *current_pixel = &imgout[row_width*y + x_f*channels];
									int left_fp = y*width+last_map;  //&imgout[row_width*y + last_map*channels];
									int right_fp = y*width+x+left;  //&imgout[row_width*y + (x+left)*channels];
									fillstate[fp_f] = fraction_offset;
									contrib[fp_f] = 1;
									for (int c=0;c<channels;c++) {										
										current_pixel[c] = last_pix_warp[c]/contrib[left_fp] + (x_f - last_map)*(double(pix_warp[c]/contrib[right_fp]-last_pix_warp[c]/contrib[left_fp])/gap_f);
									}
								}							
									
							}
						}
						continous_stretch = true;
						last_map = x+right;
					}
				} 
			} else {
				continous_stretch = true;
			}

			pix += channels;
		}
		continous_stretch = false;
	}

	/* take the average of all contributions to each pixel */
	fp = 0;
	short * pix_o = imgout;
	for (int y=0;y<height;y++)
		for (int x=0;x<width;x++) {
		   if (contrib[fp]>0 && fillstate[fp]<1) 
			   for (int c=0;c<channels;c++) pix_o[c] /= contrib[fp];
		   fp++;
		   pix_o += channels;
		}
}

void stereoLinearInterp(double pos, unsigned char* imgleft, unsigned char* imgright, short* displeft, short* dispright) {
	int row_width = width*channels;
	const float NOTHING = 999;
	int fp;

	for (int y=0;y<height;y++)
		for (int x=0;x<width;x++) {
			fillstate1[width*y + x] = NOTHING;
			fillstate2[width*y + x] = NOTHING;
			
		}
	
	double ratio1 = pos;
	double ratio2 = 1.0 - pos;

	/* interpolate using the left-image as the base and using left disparity. */
	if (use_left_image)
		linearWarp(imgleft,displeft,ratio1,imginterp1,fillstate1);

	/* interpolate using the right-image as the base and using right disparity. */
	if (use_right_image) 
		linearWarp(imgright,dispright,ratio2,imginterp2,fillstate2);

	memset(imginterp,0,sizeof(unsigned char)*height*width*channels);

	short *pix_1 = imginterp1;
	short *pix_2 = imginterp2;
	unsigned char *dest = imginterp;
	fp = 0;
	for (int y=0;y<height;y++)
		for (int x=0;x<width;x++) {
			if (use_left_image && use_right_image) { 
				if (fillstate1[fp] < 1 && fillstate2[fp] < 1) {
					// (1) the ideal case. real pixel values from both images were mapped 
					// on to this pixel, just blend them. 
					for (int c=0;c<channels;c++) dest[c] = int((double)pix_1[c]*ratio2 + (double)pix_2[c]*ratio1);
				} else if (fillstate1[fp] < 1) {
					// (2) a real pixel value in the left image
					for (int c=0;c<channels;c++) dest[c] = pix_1[c];
					//dest[0]=255;
				} else if (fillstate2[fp] < 1) {
					// (3) a real pixel value in the right image
					for (int c=0;c<channels;c++) dest[c] = pix_2[c];
					//dest[1]=255;
				} else if (fillstate1[fp] < fillstate2[fp]) {
					// (4) hole-filled interpolated value in left image
					//     lesser value of the two means this hole was a smaller size
					for (int c=0;c<channels;c++) dest[c] = pix_1[c];
					//dest[0]=255;
				} else if (fillstate1[fp] > fillstate2[fp]) {
					// (5) hole-filled interpolated value in right image, same as before. 
					for (int c=0;c<channels;c++) dest[c] = pix_2[c];
					//dest[1]=255;
				} else {
					//unsigned char * left_ = &imgleft[row_width*y+x*channels];
					//unsigned char * right_ = &imgright[row_width*y+x*channels];
					//for (int c=0;c<channels;c++) dest[c] = int((double)left_[c]*ratio2 + (double)right_[c]*ratio1);
					//dest[2]=255;
				}
			} else if (use_right_image) {
				for (int c=0;c<channels;c++) dest[c] = pix_2[c];
			} else {
				for (int c=0;c<channels;c++) dest[c] = pix_1[c];
			}

			pix_1 += channels;
			pix_2 += channels;
			dest += channels;
			fp++;
		}

		/*
	const int FILLED = 85;

	// Fill any remaining one pixel wide holes!
	dest = imginterp;
	for (int y=0;y<height;y++)
		for (int x=0;x<width;x++) {
			if (use_left_image && use_right_image && x>1 && x<width-1) {
				if (dest[0]+dest[1]+dest[2]<FILLED) {
					unsigned char *left = dest-channels;
					unsigned char *right = dest+channels;
					if (left[0]+left[1]+left[2]>FILLED &&
						right[0]+right[1]+right[2]>FILLED) {
							for (int c=0;c<channels;c++) dest[c]=(left[c]+right[c])/2;
							//dest[0]=255;
					}
				}
			}
			dest+=channels;
		}*/


	imgshow = imginterp;
}



unsigned char  mGet8U(IplImage * mat, int x, int y) 
{
	unsigned char * ptr = cvPtr2D(mat, y,x);//&((uchar*)(mat->data + mat->width*y))[x];
	return ptr[0];
}

//converts an opencv matrix into a layer on an image stack
void copyImagetoStack(IplImage  * mat, unsigned char *stack, int copytochannel, int channels, int width)
{
	for (int y=0; y<mat->height; y++)
		for (int x=0; x<mat->width; x++) 
			stack[y*width*channels + x*channels + copytochannel] = mGet8U(mat, x, y);
}

short * loadFlowFile(char *file) {
   FILE *fp;
   short *dest = NULL;
	double minx,maxx;
	char buffer[500];
	int flowheight, flowwidth, elemsize;
	double scale, bias;

   fp = fopen(file,"rb");
   char c = fgetc(fp);
   while (c=='#') 
   {
	   fgets(buffer,500,fp);
	   c = fgetc(fp);
   }
   fseek(fp,-1,SEEK_CUR);
   if(fp!=NULL)
   {
	   fgets(buffer,500,fp);
	   sscanf_s(buffer,"%d %d\n",&flowwidth,&flowheight);
	   fgets(buffer,500,fp);
	   sscanf_s(buffer,"%d %le %le\n",&elemsize, &scale,&bias);
	 //  fgets(buffer,500,fp);
	 //  sscanf_s(buffer,"%le\n",&maxx);

	   dest = (short*)malloc(flowwidth*flowheight*sizeof(short));

	   int x = sizeof(unsigned short);

	   printf("\n");
	   for(int j=0;j<flowheight;j++)
       {
           for(int i=0;i<flowwidth;i++)
           {
					unsigned char  val;
					fread(&val,sizeof(unsigned char),1,fp);
					double tmp = double(val)/255;
					tmp = tmp * scale + bias;
					int destindex = (flowheight - j -1)*flowwidth + i;
					dest[destindex] = round(tmp);
					//printf("%d ",dest[destindex]);					
		   }
		   //printf("\n");
		}

   }
   fclose(fp);
   return dest;
}

short * loadScaleImage(char *file, int sizescale, int elementscale ) {
	short zero_offset_16bit = 10000;
	short zero_offset_8bit = 127;
	IplImage *imgorig = cvLoadImage(file,CV_LOAD_IMAGE_ANYDEPTH | CV_LOAD_IMAGE_ANYCOLOR);
/*
	if (sizescale > 1) {
		CvSize size = cvSize(imgorig->width/sizescale, imgorig->height/sizescale);
		IplImage *imgnew = cvCreateImage(size,imgorig->depth,imgorig->nChannels);
		cvResize(imgorig,imgnew);
		cvConvertScale(imgnew, imgnew, (double)1/elementscale);
		cvReleaseImage(&imgorig);
		imgorig = imgnew;
	}
	*/
	//cvConvertImage(imgorig,imgorig, CV_CVTIMG_FLIP );

	short * dest = (short*)malloc(width*height*sizeof(short));

	for (int y=0; y<imgorig->height; y++) {
		for (int x=0; x<imgorig->width; x++) {
			short value = 0;
			if (imgorig->depth == 16) {
				short * sourceptr = (short*)(imgorig->imageData + y*imgorig->widthStep + x*imgorig->nChannels*sizeof(short));
				if (sourceptr[0]!=0) value = sourceptr[0] - zero_offset_16bit; else value = EMPTY;
			} else if (imgorig->depth == 8) {
				unsigned char * sourceptr = (unsigned char*)(imgorig->imageData + y*imgorig->widthStep + x*imgorig->nChannels);
				if (sourceptr[0]!=0) value = sourceptr[0] - zero_offset_8bit; else value = EMPTY; 
			}

			int destindex = (height - 1 - y)*width + x;
			dest[destindex] = value;
		}
		//std::cout << std::endl;
	}

	cvReleaseImage(&imgorig);

	return dest;
}

unsigned char * loadColorImage(char *file)
{
	IplImage *imgorig = cvLoadImage(file);
	cvConvertImage(imgorig,imgorig, CV_CVTIMG_FLIP );
	cvConvertImage(imgorig,imgorig, CV_CVTIMG_SWAP_RB );

	if (width ==0 && height ==0)
	{
		width = imgorig->width;
		height = imgorig->height;
		bands = 3;
		order = 1;
	}
	unsigned char* dest = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));
	for (int y=0; y<imgorig->height; y++)
		for (int x=0; x<imgorig->width; x++) {
			unsigned char * sourceptr = (unsigned char*)(imgorig->imageData + y*imgorig->widthStep + x*imgorig->nChannels);
			int destindex = y*width*bands + x*bands;
			dest[destindex] = sourceptr[0];
			dest[destindex+1] = sourceptr[1];
			dest[destindex+2] = sourceptr[2];
		}

	cvReleaseImage(&imgorig);

	return dest;
}

void loadRelightingImages(char *file, int index) {
	char* fn;
	char* ext;
	int strcount=0;
	width = 0;
	height = 0;

	printf("loading object\n");
	fn = file;
	//look at file extension
	strcount=strlen(fn);
	ext=&fn[strcount-3];		
	if(strcmp(ext,"phl")==0)
		g_mode=MODE_PHONG;
	else if(strcmp(ext,"wrd")==0)
		g_mode=MODE_WARD;
	else if(strcmp(ext,"sph")==0)
		g_mode=MODE_SPHERICAL;
	else if(strcmp(ext,"pol")==0)
		g_mode=MODE_POLY;
	else if(strcmp(ext,"phs")==0)
		g_mode=MODE_PHOTOMETRIC;
	else if(strcmp(ext,"rti")==0 || strcmp(ext,"hsh")==0)
		g_mode=MODE_HEMISPHERICAL;
	else if(strcmp(ext,"jpg")==0 || strcmp(ext,"png")==0 || strcmp(ext,"bmp")==0)
		g_mode=MODE_IMAGE;
	else
		fn = "obj.phl";


	// Call the actual load functions based on the image type
	// Taken out all but Hemi-spherical harmonics, should add them back later
	if (g_mode == MODE_HEMISPHERICAL)
		view[index].floatpixels = loadHSH(fn);


	if (g_mode == MODE_IMAGE) // Plain image based interpolation, no relighting involved. Just load the stored image directly 
	{						 // in to the memory image
		view[index].Image = loadColorImage(fn);
	}
	else
	{
		printf("allocating memory\n");  // Allocate memory for image, used later for storing the relit image for caching
		view[index].Image = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));
	}
		

	printf("creating images\n");
	//generateImages(); (not used in MODE_SPHERICAL)
	if (g_mode == MODE_HEMISPHERICAL)
		renderImageHSH(view[index].floatpixels,view[index].Image);

	//renderImage(view[index].pixels,view[index].shpixels,view[index].popixels, view[index].Image );

}


void readSingleHSHfile(char *baseimage) {
	viewpoints = 2;
	loadRelightingImages(baseimage,0);
	loadRelightingImages(baseimage,1);

	/*
	// load the disparity maps for each viewpoint
	for (int i=0;i<viewpoints-1;i++) {
		cout << filedisparityL[i] ;
		view[i].imgdispleft = loadFlowFile(filedisparityL[i]);
		cout << " / " << filedisparityR[i] << endl;
		view[i].imgdispright = loadFlowFile(filedisparityR[i]);
	}

	imginterp = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));//cvCloneImage(view[0].imgbase);

	//CvSize size = cvSize(imgshow->width, imgshow->height);
	imginterp1 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
	imginterp2 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
	imgshow = imginterp;

	fillstate1 =  (float*)malloc(width*height*sizeof(float));
	fillstate2 =  (float*)malloc(width*height*sizeof(float));
	contrib = (float*)malloc(width*height*sizeof(float));
*/
}




void readInputFiles(char *baseimages) {
	ifstream filebases(baseimages);
	char filerelighting[MAX][80]; 
	char filedisparityL[MAX][80], filedisparityR[MAX][80];
	char flowleft[80], flowright[80], flowup[80], flowdown[80];
	int yviews, dummy, startview, use_flow, use_turntable;
	float sep_x, sep_y;
	filebases >> viewpoints >> yviews >> dummy;
	filebases >> startview >> dummy;
	filebases >> use_flow >> use_turntable >> sep_x >> sep_y;
	for (int i=0;i<viewpoints;i++) {
		filebases >> dummy >> filerelighting[i] ;
	}

	for (int i=0;i<viewpoints;i++) {
		filebases >> dummy;  // right now we consider all viewpoints to be along a single line anyway.
	}

	for (int i=0;i<viewpoints;i++) {
		filebases >> dummy >> flowleft >> flowright >> flowup >> flowdown;
		if (i==0) {
			strcpy(filedisparityL[0],flowright);
		} else if (i==viewpoints-1) {
			strcpy(filedisparityR[viewpoints-2],flowleft);
		} else {
			strcpy(filedisparityL[i],flowright);
			strcpy(filedisparityR[i-1],flowleft);
		}
	}


	filebases.close();

	//ifstream filedisparities(disparitymaps);
	//for (int i=0;i<viewpoints-1;i++) {
	//	filedisparities >> filedisparityL[i] >> filedisparityR[i] ;
	//}
	//filedisparities.close();

	// now load the liglighthting parameters, in form of wrd / sph etc.
	for (int i=0;i<viewpoints;i++) {
		cout << filerelighting[i] << endl;
		loadRelightingImages(filerelighting[i],i);
	}
	// load the disparity maps for each viewpoint
	for (int i=0;i<viewpoints-1;i++) {
		cout << filedisparityL[i] ;
		view[i].imgdispleft = loadFlowFile(filedisparityL[i]);
		cout << " / " << filedisparityR[i] << endl;
		view[i].imgdispright = loadFlowFile(filedisparityR[i]);
	}

	imginterp = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));//cvCloneImage(view[0].imgbase);

	//CvSize size = cvSize(imgshow->width, imgshow->height);
	imginterp1 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
	imginterp2 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
	imgshow = imginterp;

	fillstate1 =  (float*)malloc(width*height*sizeof(float));
	fillstate2 =  (float*)malloc(width*height*sizeof(float));
	contrib = (float*)malloc(width*height*sizeof(float));

}

void lowMemoryBatchMode(char *baseimages, char *disparitymaps) {
	ifstream filebases(baseimages);
	char filerelighting[MAX][80]; 
	char filedisparityL[MAX][80], filedisparityR[MAX][80];
	int viewpoints_all;
	filebases >> viewpoints_all;
	for (int i=0;i<viewpoints_all;i++) {
		filebases >> filerelighting[i] ;
	}
	filebases.close();

	ifstream filedisparities(disparitymaps);
	for (int i=0;i<viewpoints_all-1;i++) {
		filedisparities >> filedisparityL[i] >> filedisparityR[i] ;
	}
	filedisparities.close();

	bool first_time = true;
	int frame_offset = 0;
	batch_mode = true;
	total_movie_frames = (viewpoints_all-1)*movie_frames;

	for (int i=0;i<viewpoints_all-1;i++) {
		viewpoints = 2;
		cout << filerelighting[i] << endl;
		loadRelightingImages(filerelighting[i],0);
		cout << filerelighting[i+1] << endl;
		loadRelightingImages(filerelighting[i+1],1);

		cout << filedisparityL[i] ;
		view[0].imgdispleft = loadScaleImage(filedisparityL[i],scale,scale);
		cout << " / " << filedisparityR[i] << endl;
		view[0].imgdispright = loadScaleImage(filedisparityR[i],scale,scale);
		
		if (first_time) {
			imginterp = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));//cvCloneImage(view[0].imgbase);

			imginterp1 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
			imginterp2 =  (short *)malloc(width*height*3*sizeof(short));//cvCreateImage(size, imgshow->depth, imgshow->nChannels);
			imgshow = imginterp;

			fillstate1 =  (float*)malloc(width*height*sizeof(float));
			fillstate2 =  (float*)malloc(width*height*sizeof(float));
			contrib = (float*)malloc(width*height*sizeof(float));

			windowheight = height;
			windowwidth = width;
			first_time = false;
		}
	

		cout << " View " << i << " saving movie frame offset " << frame_offset << endl;
		saveMovie(frame_offset);
		frame_offset+=movie_frames;

		for (int j=0;j<viewpoints-1;j++) {
			free(view[j].imgdispleft);
			free(view[j].imgdispright);
		}
		for (int j=0;j<viewpoints;j++) {
			free(view[j].Image);
			free(view[j].floatpixels);
		}

	}

	////////////////////////////////////

	for (int i=viewpoints_all-2;i>=0;i--) {
		viewpoints = 2;
		cout << filerelighting[i] << endl;
		loadRelightingImages(filerelighting[i],0);
		cout << filerelighting[i+1] << endl;
		loadRelightingImages(filerelighting[i+1],1);

		cout << filedisparityL[i] ;
		view[0].imgdispleft = loadScaleImage(filedisparityL[i],scale,scale);
		cout << " / " << filedisparityR[i] << endl;
		view[0].imgdispright = loadScaleImage(filedisparityR[i],scale,scale);
		
		cout << " View " << i << " saving movie frame offset " << frame_offset << endl;
		saveMovieReverse(frame_offset);
		frame_offset+=movie_frames;

		for (int j=0;j<viewpoints-1;j++) {
			free(view[j].imgdispleft);
			free(view[j].imgdispright);
		}
		for (int j=0;j<viewpoints;j++) {
			free(view[j].Image);
			free(view[j].floatpixels);
		}

	}

	/////////////////////////////////////

	for (int i=0;i<viewpoints_all-1;i++) {
		viewpoints = 2;
		cout << filerelighting[i] << endl;
		loadRelightingImages(filerelighting[i],0);
		cout << filerelighting[i+1] << endl;
		loadRelightingImages(filerelighting[i+1],1);

		cout << filedisparityL[i] ;
		view[0].imgdispleft = loadScaleImage(filedisparityL[i],scale,scale);
		cout << " / " << filedisparityR[i] << endl;
		view[0].imgdispright = loadScaleImage(filedisparityR[i],scale,scale);
		
		cout << " View " << i << " saving movie frame offset " << frame_offset << endl;
		saveMovie(frame_offset);
		frame_offset+=movie_frames;

		for (int j=0;j<viewpoints-1;j++) {
			free(view[j].imgdispleft);
			free(view[j].imgdispright);
		}
		for (int j=0;j<viewpoints;j++) {
			free(view[j].Image);
			free(view[j].floatpixels);
		}

	}


}

void clearUp(bool clear_views) {
	//imginterp = NULL;
	free(imginterp1);
	free(imginterp2);
	free(imginterp);
	free(fillstate1);
	free(fillstate2);
	free(contrib);

	if (clear_views) {
		for (int i=0;i<viewpoints-1;i++) {
			free(view[i].imgdispleft);
			free(view[i].imgdispright);
		}
		for (int i=0;i<viewpoints;i++) {
			free(view[i].Image);
		}
	}
}

