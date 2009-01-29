/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

#include "unsharpmasking.h"

#include <QGridLayout>
#include <QLabel>
#include <QApplication>

UnsharpMControl::UnsharpMControl(int gain, QWidget *parent) : QWidget(parent)
{
	QLabel* label1 = new QLabel("Gain");
	sliderGain = new QSlider(Qt::Horizontal);
	sliderGain->setRange(0, 100);
	sliderGain->setValue(gain);
	sliderGain->setTracking(false);
	connect(sliderGain, SIGNAL(valueChanged(int)), this, SIGNAL(gainChanged(int)));

	QGridLayout *layout = new QGridLayout;
	layout->addWidget(label1, 0, 0);
	layout->addWidget(sliderGain, 0, 1);
	setLayout(layout);
}


UnsharpMasking::UnsharpMasking(int n) :
	gain(1.0f),
	minGain(0.01f),
	maxGain(4.0f),
	nIter(5),
	type(n)
	{

	}


UnsharpMasking::~UnsharpMasking() 
{

}


	
QString UnsharpMasking::getTitle() 
{
	if (type == 0)
		return "Image Unsharp Masking";
	else if (type == 1)
		return "Luminance Unsharp Masking";
	return "Unsharp Masking";
}


QWidget* UnsharpMasking::getControl(QWidget* parent)
{
	int initGain = (gain - minGain)*100/(maxGain - minGain);
	UnsharpMControl* control = new UnsharpMControl(initGain, parent);
	connect(control, SIGNAL(gainChanged(int)), this, SLOT(setGain(int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	return control;
}


bool UnsharpMasking::isLightInteractive()
{
	return false;
}


bool UnsharpMasking::supportRemoteView()
{
	return false;
}


bool UnsharpMasking::enabledLighting()
{
	return true;
}


void UnsharpMasking::setGain(int value)
{
	gain = minGain + value * (maxGain - minGain)/100;
	emit refreshImage();
}


void UnsharpMasking::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const int* coeffPtr = coeff.getLevel(info.level);
	const unsigned char* rgbPtr = rgb.getLevel(info.level);
	double* lumMap = new double[info.width*info.height];
	int width = mipMapSize[info.level].width();
	if (type == 0) //image unsharp masking
	{
		// Creates a map for Y component and a map for UV component. 
		double* uvMap = new double[info.width*info.height*2];
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset = y * width + x;
				int offset2 = (y - info.offy)*info.width + (x - info.offx);
				double lum = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				double r = rgbPtr[offset*3]*lum / 255.0;
				double g = rgbPtr[offset*3 + 1]*lum / 255.0;
				double b = rgbPtr[offset*3 + 2]*lum / 255.0;
				getYUV(r, g, b, lumMap[offset2], uvMap[offset2*2], uvMap[offset2*2 + 1]);
			}
		}
		// Computes the enhanced luminance.
		enhancedLuminance(lumMap, info.width, info.height, info.mode);
		// Creates the output texture.
		bool flag = (info.mode == LUM_UNSHARP_MODE || info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE ||info.mode == ENHANCED_MODE);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset2 =(y - info.offy)*info.width + (x - info.offx); 
				if (flag)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = tobyte(lumMap[offset2] * 255.0);
				}
				else
				{
					double r, g, b;
					getRGB(lumMap[offset2], uvMap[offset2*2], uvMap[offset2*2 +1], r, g, b);
					buffer[offsetBuf ] = tobyte(r*255);
					buffer[offsetBuf + 1] = tobyte(g*255);
					buffer[offsetBuf + 2] = tobyte(b*255);
				}
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
		delete[] uvMap;
	}
	else //unsharp masking luminance
	{
		// Creates a map for the polynomial luminance.
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * width + x;
				lumMap[(y - info.offy)*info.width + (x - info.offx)] = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
			}
		}
		// Computes the enhanced luminance
		enhancedLuminance(lumMap, info.width, info.height, info.mode);
		// Creates the output texture.
		bool flag = (info.mode == LUM_UNSHARP_MODE || info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE || info.mode == ENHANCED_MODE);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset = y * width + x;
				int offset2 =(y - info.offy)*info.width + (x - info.offx); 
				if (flag)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = tobyte(lumMap[offset2] / 2.0 * 255.0);
				}
				else
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * lumMap[offset2]);
				}
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
		
	}
	delete[] lumMap;
}


void UnsharpMasking::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const int* redPtr = redCoeff.getLevel(info.level);
	const int* greenPtr = greenCoeff.getLevel(info.level);
	const int* bluePtr = blueCoeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	double* lumMap = new double[info.width*info.height];
	int width = mipMapSize[info.level].width();
	if (type == 0) //classic unsharp masking
	{
		double* uvMap = new double[info.width*info.height*2];
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset = y * width + x;
				int offset2 = (y - info.offy)*info.width + (x - info.offx);
				double r = evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				double g = evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				double b = evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				getYUV(r, g, b, lumMap[offset2], uvMap[offset2*2], uvMap[offset2*2 + 1]);
			}
		}
		enhancedLuminance(lumMap, info.width, info.height, info.mode);
		bool flag = (info.mode == LUM_UNSHARP_MODE || info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE || info.mode == ENHANCED_MODE);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset2 =(y - info.offy)*info.width + (x - info.offx); 
				if (flag)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = tobyte(lumMap[offset2] * 255.0);
				}
				else
				{
					double r, g, b;
					getRGB(lumMap[offset2], uvMap[offset2*2], uvMap[offset2*2 +1], r, g, b);
					buffer[offsetBuf] = tobyte(r*255);
					buffer[offsetBuf + 1] = tobyte(g*255);
					buffer[offsetBuf + 2] = tobyte(b*255);
				}
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
		delete[] uvMap;
	}
	else //luminance unsharp masking
	{
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * width + x;
				lumMap[(y - info.offy)*info.width + (x - info.offx)] = getLum(normalsPtr[offset], info.light);
			}
		}
		enhancedLuminance(lumMap, info.width, info.height, info.mode);
		bool flag = (info.mode == LUM_UNSHARP_MODE || info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE || info.mode == ENHANCED_MODE);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset = y * width + x;
				double lum = lumMap[(y - info.offy)*info.width + (x - info.offx)];
				if (flag)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = tobyte(lum / 2.0 * 255.0);
				}
				else
				{
					buffer[offsetBuf] = tobyte(evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y()) * lum);
					buffer[offsetBuf + 1] = tobyte(evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y()) * lum);
					buffer[offsetBuf + 2] = tobyte(evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y()) * lum);
				}
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}
	delete[] lumMap;
}


void UnsharpMasking::enhancedLuminance(double* lumMap, int width, int height, int mode)
{
	QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
	int dist = 2;
	double* tempLum = new double[width*height];
	double* smootLum = new double[width*height];
	int* nKernel = new int[width*height];
	memcpy(smootLum, lumMap, width*height*sizeof(double));
	for (int i = 0; i < nIter; i++)
	{
		memset(tempLum, 0, width*height*sizeof(double));
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int offset = y * width + x;
				int sx, ex, sy, ey;
				sx = x - dist < 0? 0 : x - dist;
				ex = x >= width - dist? width - 1: x + dist; 
				sy = y - dist < 0? 0: y - dist;
				ey = y >= height - dist? height - 1: y + dist;
				int n = (ex - sx + 1)*(ey - sy + 1);
				nKernel[offset] = n;
				if (x > 0)
				{
					tempLum[offset] = tempLum[offset-1];
					if (x <= dist)
					{
						for(int jj = sy; jj <= ey; jj++)
							tempLum[offset] += smootLum[jj*width + x + dist];
					}
					else
					{
						for(int jj = sy; jj <= ey; jj++)
						{
							tempLum[offset] -= smootLum[jj*width + x - dist - 1];
							if (x + dist < width)
								tempLum[offset] += smootLum[jj*width + x + dist];
						}
					}

				}
				else
				{
					for (int ii = sx; ii <= ex; ii++)
						for(int jj = sy; jj <= ey; jj++)
							tempLum[offset] += smootLum[jj*width + ii];					
				}
			}
		}
		for (int ii = 0; ii < width*height; ii++)
			tempLum[ii] /= static_cast<double>(nKernel[ii]);
		memcpy(smootLum, tempLum, width*height*sizeof(double));
	}
	delete[] tempLum;
	for (int i = 0; i < height*width; i++)
	{
		switch(mode)
		{
		case LUM_UNSHARP_MODE:
			break;
		case SMOOTH_MODE:
			lumMap[i] = smootLum[i]; break;
		case CONTRAST_MODE:
			lumMap[i] = (lumMap[i] - smootLum[i])*4; break;
		default:
			lumMap[i] = lumMap[i] + gain *(lumMap[i] - smootLum[i]);
		}
	}
	delete[] smootLum;
	delete[] nKernel;
	QApplication::restoreOverrideCursor();
}


double UnsharpMasking::getLum(const vcg::Point3f& normal, const vcg::Point3f& l)
{
	double nDotL = normal*l;
	if (nDotL < 0) 
		nDotL = 0.0;
	else if (nDotL > 1)
		nDotL = 1.0;
	return nDotL;
}


void UnsharpMasking::getYUV(double r, double g, double b, double& l, double& u, double& v)
{
	l = r * 0.299 + g * 0.587 + b * 0.144;
	u = r * -0.14713 + g * -0.28886 + b * 0.436;
	v = r * 0.615 + g * -0.51499 + b * -0.10001;
}

void UnsharpMasking::getRGB(double y, double u, double v, double& r, double& g, double& b)
{
	r = y + v * 1.13983;
	g = y + u * -0.39465 + v * -0.5806;
	b = y + u * 2.03211;
}