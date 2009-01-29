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

#include "coeffenhanc.h"

#include <QGridLayout>
#include <QLabel>
#include <QApplication>

CoeffEnhancControl::CoeffEnhancControl(int gain, QWidget *parent) : QWidget(parent)
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


CoeffEnhancement::CoeffEnhancement() :
	gain(1.0f),
	minGain(0.01f),
	maxGain(6.0f),
	nIter(2)
	{

	}

CoeffEnhancement::~CoeffEnhancement() 
{

}

	
QString CoeffEnhancement::getTitle() 
{
	return "Coefficient Enhancement";
}


QWidget* CoeffEnhancement::getControl(QWidget* parent)
{
	int initGain = (gain - minGain)*100/(maxGain - minGain);
	CoeffEnhancControl* control = new CoeffEnhancControl(initGain, parent);
	connect(control, SIGNAL(gainChanged(int)), this, SLOT(setGain(int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	return control;
}


bool CoeffEnhancement::isLightInteractive()
{
	return false;
}


bool CoeffEnhancement::supportRemoteView()
{
	return false;
}


bool CoeffEnhancement::enabledLighting() 
{
	return true;
}


void CoeffEnhancement::setGain(int value)
{
	gain = minGain + value * (maxGain - minGain)/100;
	emit refreshImage();
}


void CoeffEnhancement::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const int* coeffPtr = coeff.getLevel(info.level);
	const unsigned char* rgbPtr = rgb.getLevel(info.level);
	int* coeffMap = new int[info.width*info.height*6];
	int width = mipMapSize[info.level].width();
	// Creates the map of the coefficients of the sub-image in the current view of the browser
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * width + x;
			memcpy(&coeffMap[((y - info.offy)*info.width + (x - info.offx))*6], &coeffPtr[offset*6], 6*sizeof(int));
		}
	}
	// Computes the enhanced coefficients.
	enhancedCoeff(coeffMap, info.width, info.height, 6);
	// Creates the output texture.
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * width + x;
			double lum = evalPoly(&coeffMap[((y - info.offy)*info.width + (x - info.offx))*6], info.light.X(), info.light.Y()) / 255.0;
			for (int i = 0; i < 3; i++)
				buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * lum);
			buffer[offsetBuf + 3] = 255;
			offsetBuf +=4;
		}
	}
	delete[] coeffMap;
}


void CoeffEnhancement::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const int* redPtr = redCoeff.getLevel(info.level);
	const int* greenPtr = greenCoeff.getLevel(info.level);
	const int* bluePtr = blueCoeff.getLevel(info.level);
	int lenght = info.width * info.height * 6;
	int* redC = new int[lenght];
	int* greenC = new int[lenght];
	int* blueC = new int[lenght];
	int width = mipMapSize[info.level].width();
	// Creates the map of the coefficients of the sub-image in the current view of the browser
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * width + x;
			int offset2 = ((y - info.offy)*info.width + (x - info.offx))*6;
			memcpy(&redC[offset2], &redPtr[offset*6], 6*sizeof(int));
			memcpy(&greenC[offset2], &greenPtr[offset*6], 6*sizeof(int));
			memcpy(&blueC[offset2], &bluePtr[offset*6], 6*sizeof(int));
		}
	}
	// Computes the enhanced coefficients
	enhancedCoeff(redC, info.width, info.height, 6);
	enhancedCoeff(greenC, info.width, info.height, 6);
	enhancedCoeff(blueC, info.width, info.height, 6);
	// Creates the output texture.
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset2 = ((y - info.offy)*info.width + (x - info.offx))*6;
			buffer[offsetBuf] = tobyte(evalPoly(&redC[offset2], info.light.X(), info.light.Y()));
			buffer[offsetBuf + 1] = tobyte(evalPoly(&greenC[offset2], info.light.X(), info.light.Y()));
			buffer[offsetBuf + 2] = tobyte(evalPoly(&blueC[offset2], info.light.X(), info.light.Y()));
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
	delete[] redC;
	delete[] greenC;
	delete[] blueC;
}


void CoeffEnhancement::enhancedCoeff(int *coeffMap, int width, int height, int ncomp)
{
	QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
	int dist = 1;
	float* tempCoeff = new float[width*height*ncomp];
	float* smootCoeff = new float[width*height*ncomp];
	int* nKernel = new int[width*height];
	for (int i = 0; i < width*height*ncomp; i++)
		smootCoeff[i] = coeffMap[i];
	for (int i = 0; i < nIter; i++)
	{
		memset(tempCoeff, 0, width*height*ncomp*sizeof(float));
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int offset = y * width + x;
				int sx, ex, sy, ey;
				sx = x == 0? x: x - 1;
				ex = x == width - 1? x: x + 1; 
				sy = y == 0? y: y - 1;
				ey = y == height - 1? y: y + 1;
				int n = (ex - sx + 1)*(ey - sy + 1);
				nKernel[offset] = n;
				if (x > 0)
				{
					for(int k = 0; k < ncomp; k++)
					{
						tempCoeff[offset*ncomp + k] = tempCoeff[(offset-1)*ncomp +k];
						if (x <= dist)
						{
							for(int jj = sy; jj <= ey; jj++)
								tempCoeff[offset*ncomp + k] += smootCoeff[(jj*width + x + dist)*ncomp + k];
						}
						else
						{
							for(int jj = sy; jj <= ey; jj++)
							{
								tempCoeff[offset*ncomp + k] -= smootCoeff[(jj*width + x - dist - 1)*ncomp + k];
								if (x + dist < width)
									tempCoeff[offset*ncomp + k] += smootCoeff[(jj*width + x + dist)*ncomp + k];
							}
						}
					}

				}
				else
				{
					for (int ii = sx; ii <= ex; ii++)
						for(int jj = sy; jj <= ey; jj++)
							for(int k = 0; k < ncomp; k++)
								tempCoeff[offset*ncomp + k] += smootCoeff[(jj*width + ii)*ncomp +k];					
				}
			}
		}
		for (int y = 0; y < height; y++)
		{		
			for (int x = 0; x < width; x++)
			{
				int offset = y * width + x;
				for (int k = 0; k < ncomp; k++)
					tempCoeff[offset*ncomp + k] /= nKernel[offset];
			}
		}
		memcpy(smootCoeff, tempCoeff, width*height*ncomp*sizeof(float));
	}
	delete[] tempCoeff;
	for (int i = 0; i < height*width*ncomp; i++)
		coeffMap[i] = coeffMap[i] + gain *(coeffMap[i] - smootCoeff[i]);
	delete[] smootCoeff;
	delete[] nKernel;
	QApplication::restoreOverrideCursor();
}