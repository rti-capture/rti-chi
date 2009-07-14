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

#include "normalenhanc.h"
#include "loadingdlg.h"

#include <QGridLayout>
#include <QLabel>
#include <QApplication>
#include <QTime>


NormalEControl::NormalEControl(int gain, int kd, int envIll, QWidget *parent) : QWidget(parent)
{
	QLabel* label1 = new QLabel("Gain");
	sliderGain = new QSlider(Qt::Horizontal);
	sliderGain->setRange(0, 100);
	sliderGain->setValue(gain);
	sliderGain->setTracking(false);
	connect(sliderGain, SIGNAL(valueChanged(int)), this, SIGNAL(gainChanged(int)));

	QLabel* label2 = new QLabel("Diffuse");
	sliderKd = new QSlider(Qt::Horizontal);
	sliderKd->setRange(0, 100);
	sliderKd->setValue(kd);
	sliderKd->setTracking(false);
	connect(sliderKd, SIGNAL(valueChanged(int)), this, SIGNAL(kdChanged(int)));
	
	QLabel* label3 = new QLabel("Environment");
	sliderEnvIll = new QSlider(Qt::Horizontal);
	sliderEnvIll->setRange(0, 100);
	sliderEnvIll->setValue(envIll);
	sliderEnvIll->setTracking(false);
	connect(sliderEnvIll, SIGNAL(valueChanged(int)), this, SIGNAL(envIllChanged(int)));

	QGridLayout *layout = new QGridLayout;
	layout->addWidget(label1, 0, 0);
	layout->addWidget(sliderGain, 0, 1);
	/*layout->addWidget(label2, 1, 0);
	layout->addWidget(sliderKd, 1, 1);*/
	layout->addWidget(label3, 1, 0);
	layout->addWidget(sliderEnvIll, 1, 1);
	setLayout(layout);
}


NormalEnhancement::NormalEnhancement() :
	gain(1.0f),
	minGain(0.01f),
	maxGain(4.0f),
	kd(1.0f),
	minKd(0.5f),
	maxKd(1.5f),
	envIll(0.5f),
	minEnvIll(0.1f),
	maxEnvIll(2.0f),
	nIter(5),
	smooted(false),
	loadParent(NULL)
	{
		
	}

NormalEnhancement::~NormalEnhancement() 
{
	
}


	
QString NormalEnhancement::getTitle() 
{
	return "Normal Enhancement";
}


QWidget* NormalEnhancement::getControl(QWidget* parent)
{
	int initGain = (gain - minGain)*100/(maxGain - minGain);
	int initKd = (kd - minKd)*100/(maxKd - minKd);
	int initEnvIll = (envIll - minEnvIll)*100/(maxEnvIll - minEnvIll);
	NormalEControl* control = new NormalEControl(initGain, initKd, initEnvIll, parent);
	connect(control, SIGNAL(gainChanged(int)), this, SLOT(setGain(int)));
	connect(control, SIGNAL(kdChanged(int)), this, SLOT(setKd(int)));
	connect(control, SIGNAL(envIllChanged(int)), this, SLOT(setEnvIll(int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	loadParent = parent;
	return control;
}


bool NormalEnhancement::isLightInteractive()
{
	return false;
}


bool NormalEnhancement::supportRemoteView()
{
	return false;
}


bool NormalEnhancement::enabledLighting()
{
	return true;
}



void NormalEnhancement::setGain(int value)
{
	gain = minGain + value * (maxGain - minGain)/100;
	emit refreshImage();
}

void NormalEnhancement::setKd(int value)
{
	kd = minKd + value * (maxKd - minKd) / 100;
	emit refreshImage();
}

void NormalEnhancement::setEnvIll(int value)
{
	envIll = minEnvIll + value * (maxEnvIll - minEnvIll) / 100;
	emit refreshImage();
}


void NormalEnhancement::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{

#ifdef PRINT_DEBUG
	QTime first2 = QTime::currentTime();
#endif

	// Computes the smoothed normals
	calcSmooting(normals, mipMapSize);

#ifdef PRINT_DEBUG
	QTime second2 = QTime::currentTime();
	double diff = first2.msecsTo(second2) / 1000.0;
	printf("Normal smooting: %.5f s\n", diff);
#endif

	// Creates the output texture.
	int offsetBuf = 0;
	const int* coeffPtr = coeff.getLevel(info.level);
	const unsigned char* rgbPtr = rgb.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	const vcg::Point3f* normalsLPtr = normalsL.getLevel(info.level);
	bool flag = (info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE || info.mode == ENHANCED_MODE);
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			double lum = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
			if (flag)
			{
				vcg::Point3f n;
				switch(info.mode)
				{
					case SMOOTH_MODE: 
						n = normalsLPtr[offset]; break;
					case CONTRAST_MODE: 
						n = getContrastNormal(normalsPtr[offset], normalsLPtr[offset]); break;
					case ENHANCED_MODE:
						n = getEnhancedNormal(normalsPtr[offset], normalsLPtr[offset]); break;
				}
				if (info.mode == CONTRAST_MODE)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = n[i]*255;
				}
				else
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = toColor(n[i]);
				}
			}
			else
			{
				double diff = applyModel(normalsPtr[offset], normalsLPtr[offset], info.light) * lum;
				for (int i = 0; i < 3; i++)
					buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * diff);
			}
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
}


void NormalEnhancement::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
#ifdef PRINT_DEBUG
	QTime first2 = QTime::currentTime();
#endif

	// Computes the smoothed normals.
	calcSmooting(normals, mipMapSize);

#ifdef PRINT_DEBUG
	QTime second2 = QTime::currentTime();
	double diff = first2.msecsTo(second2) / 1000.0;
	printf("Normal smooting: %.5f s\n", diff);
#endif
	
	// Creates the output texture.
	int offsetBuf = 0;
	const int* redPtr = redCoeff.getLevel(info.level);
	const int* greenPtr = greenCoeff.getLevel(info.level);
	const int* bluePtr = blueCoeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	const vcg::Point3f* normalsLPtr = normalsL.getLevel(info.level);
	bool flag = (info.mode == SMOOTH_MODE || info.mode == CONTRAST_MODE || info.mode == ENHANCED_MODE);
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			if (flag)
			{
				vcg::Point3f n;
				switch(info.mode)
				{
					case SMOOTH_MODE: 
						n = normalsLPtr[offset]; break;
					case CONTRAST_MODE: 
						n = getContrastNormal(normalsPtr[offset], normalsLPtr[offset]); break;
					case ENHANCED_MODE:
						n = getEnhancedNormal(normalsPtr[offset], normalsLPtr[offset]); break;
				}
				if (info.mode == CONTRAST_MODE)
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = n[i]*255;
				}
				else
				{
					for (int i = 0; i < 3; i++)
						buffer[offsetBuf + i] = toColor(n[i]);
				}
			}
			else
			{
				double diff = applyModel(normalsPtr[offset], normalsLPtr[offset], info.light);
				buffer[offsetBuf + 0] = tobyte(evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y()) * diff);
				buffer[offsetBuf + 1] = tobyte(evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y()) * diff);
				buffer[offsetBuf + 2] = tobyte(evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y()) * diff);
			}
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
}


void NormalEnhancement::calcSmooting(const PyramidNormals& normals, const QSize* mipMapSize)
{
	if (smooted) return;
	smooted = true;
	QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
	LoadingDlg* loading = new LoadingDlg(loadParent);
	loading->setWindowTitle("Smooting...");
	loading->show();
	CallBackPos* cb = LoadingDlg::QCallBack;

	int dist = 2;
	for (int level = 0; level < MIP_MAPPING_LEVELS; level++)
	{
		vcg::Point3f* dest = new vcg::Point3f[normals.getLevelLenght(level)];
		memcpy(dest, normals.getLevel(level), normals.getLevelLenght(level)*sizeof(vcg::Point3f));
		vcg::Point3f* tempNormals = new vcg::Point3f[normals.getLevelLenght(level)];
		int* nKernel = new int[normals.getLevelLenght(level)];
		int width = mipMapSize[level].width();
		int height = mipMapSize[level].height();
		for (int i = 0; i < nIter; i++)
		{
			if (cb != NULL)(*cb)(100/(4.0*nIter) * (nIter*level + i) , "Normal smooting...");
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int offset = y * width + x;
					int sx, ex, sy, ey;
					sx = x - dist < 0? 0: x - dist;
					ex = x >= width - dist ? width - 1: x + dist; 
					sy = y - dist < 0? 0: y - dist;
					ey = y >= height - dist? height - 1: y + dist;
					int n = (ex - sx + 1)*(ey - sy + 1);
					nKernel[offset] = n;
					if (x > 0)
					{
						tempNormals[offset] = tempNormals[offset-1];
						if (x <= dist)
						{
							for(int jj = sy; jj <= ey; jj++)
								tempNormals[offset] += dest[jj*width + x + dist];
						}
						else
						{
							for(int jj = sy; jj <= ey; jj++)
							{
								tempNormals[offset] -= dest[jj*width + x - dist -1];
								if (x + dist < width)
									tempNormals[offset] += dest[jj*width + x + dist];
							}
						}

					}
					else
					{
						tempNormals[offset] = vcg::Point3f(0,0,0);
						for (int ii = sx; ii <= ex; ii++)
							for(int jj = sy; jj <= ey; jj++)
								tempNormals[offset] += dest[jj*width + ii];
					}
				}
			}
			for (int ii = 0; ii < normals.getLevelLenght(level); ii++)
				tempNormals[ii] /= static_cast<float>(nKernel[ii]);
			memcpy(dest, tempNormals, normals.getLevelLenght(level)*sizeof(vcg::Point3f));
		}
		for (int ii = 0; ii < normals.getLevelLenght(level); ii++)
			dest[ii].Normalize();
		normalsL.setLevel(dest, normals.getLevelLenght(level), level);
		delete[] tempNormals;
		delete[] nKernel;
	}

	loading->close();
	delete loading;
	QApplication::restoreOverrideCursor();
}


double NormalEnhancement::applyModel(const vcg::Point3f& normal, const vcg::Point3f& normalL, const vcg::Point3f& light)
{
	vcg::Point3f normalE= normal + (normal - normalL) * gain;
	normalE.Normalize();
	double nDotL =  normalE * light;
	if (nDotL < 0.0)
		nDotL = 0.0;
	else if (nDotL > 1.0)
		nDotL = 1.0;
	return (kd*nDotL + envIll)/(kd + envIll);
}	


vcg::Point3f NormalEnhancement::getContrastNormal(const vcg::Point3f& normal, const vcg::Point3f& normalL)
{
	float dot = normal*normalL;
	double angle = acos(dot);
	if(angle < 0.0)
		angle *= -1.0;
	angle = fmod(angle, M_PI_2);
	angle /= M_PI_2;
	return vcg::Point3f(angle, angle, angle);
}


vcg::Point3f NormalEnhancement::getEnhancedNormal(const vcg::Point3f& normal, const vcg::Point3f& normalL)
{
	vcg::Point3f normalE = normal + (normal - normalL) * gain;
	normalE.Normalize();
	return normalE;
}