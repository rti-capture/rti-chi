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

#include "specularenhanc.h"

#include <QGridLayout>
#include <QLabel>

SpecularEControl::SpecularEControl(int kd, int ks, int exp, QWidget *parent) : QWidget(parent)
{
	QLabel* label1 = new QLabel("Kd");
	sliderKd = new QSlider(Qt::Horizontal);
	sliderKd->setRange(0, 100);
	sliderKd->setValue(kd);
	sliderKd->setTracking(false);
	connect(sliderKd, SIGNAL(valueChanged(int)), this, SIGNAL(kdChanged(int)));
	
	QLabel* label2 = new QLabel("Ks");
	sliderKs = new QSlider(Qt::Horizontal);
	sliderKs->setRange(0, 100);
	sliderKs->setValue(ks);
	sliderKs->setTracking(false);
	connect(sliderKs, SIGNAL(valueChanged(int)), this, SIGNAL(ksChanged(int)));
	
	QLabel* label3 = new QLabel("N");
	sliderExp = new QSlider(Qt::Horizontal);
	sliderExp->setRange(0, 100);
	sliderExp->setValue(exp);
	sliderExp->setTracking(false);
	connect(sliderExp, SIGNAL(valueChanged(int)), this, SIGNAL(expChanged(int)));
	
	QGridLayout *layout = new QGridLayout;
	layout->addWidget(label1, 0, 0);
	layout->addWidget(sliderKd, 0, 1);
	layout->addWidget(label2, 1, 0);
	layout->addWidget(sliderKs, 1, 1);
	layout->addWidget(label3, 2, 0);
	layout->addWidget(sliderExp, 2, 1);
	setLayout(layout);
}


SpecularEnhancement::SpecularEnhancement() :
	kd(0.4f),
	ks(0.7f),
	exp(75),
	minKd(0.0f),
	maxKd(1.0f),
	minKs(0.0f),
	maxKs(1.0f),
	minExp(1),
	maxExp(150)
	{	}

SpecularEnhancement::~SpecularEnhancement() {}


	
QString SpecularEnhancement::getTitle() 
{
	return "Specular Enhancement";
}

QWidget* SpecularEnhancement::getControl(QWidget* parent)
{
	int initKd = (kd - minKd)*100/(maxKd - minKd);
	int initKs = (ks - minKs)*100/(maxKs - minKs);
	int initExp = (exp - minExp)*100/(maxExp - minExp);
	SpecularEControl* control = new SpecularEControl(initKd, initKs, initExp, parent);
	connect(control, SIGNAL(kdChanged(int)), this, SLOT(setKd(int)));
	connect(control, SIGNAL(ksChanged(int)), this, SLOT(setKs(int)));
	connect(control, SIGNAL(expChanged(int)), this, SLOT(setExp(int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	return control;
}


bool SpecularEnhancement::isLightInteractive()
{
	return false;
}

bool SpecularEnhancement::supportRemoteView()
{
	return false;
}


bool SpecularEnhancement::enabledLighting()
{
	return true;
}


void SpecularEnhancement::setKd(int value)
{
	kd = minKd + value * (maxKd - minKd)/100;
	emit refreshImage();
}

void SpecularEnhancement::setKs(int value)
{
	ks = minKs + value * (maxKs - minKs)/100;
	emit refreshImage();
}

void SpecularEnhancement::setExp(int value)
{
	exp = minExp + value * (maxExp - minExp)/100;
	emit refreshImage();
}


void SpecularEnhancement::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	// Creates the output texture.
	int offsetBuf = 0;
	const int* coeffPtr = coeff.getLevel(info.level);
	const unsigned char* rgbPtr = rgb.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			double lum = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
			vcg::Point3f h(0, 0, 1);
			h += info.light;
			h /= 2;
			h.Normalize();
			float nDotH = h * normalsPtr[offset];
			if (nDotH < 0) 
				nDotH = 0.0;
			else if (nDotH > 1)
				nDotH = 1.0;
			nDotH = pow(nDotH, exp);
			nDotH *= ks*255;
			for (int i = 0; i < 3; i++)
				buffer[offsetBuf + i]  = tobyte((rgbPtr[offset*3 + i]*kd + nDotH)*lum);
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}

}


void SpecularEnhancement::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	// Creates the output texture.
	int offsetBuf = 0;
	const int* redPtr = redCoeff.getLevel(info.level);
	const int* greenPtr = greenCoeff.getLevel(info.level);
	const int* bluePtr = blueCoeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			vcg::Point3f h(0, 0, 1);
			h += info.light;
			h /= 2;
			h.Normalize();
			
			double nDotH = h * normalsPtr[offset];
			if (nDotH < 0) 
				nDotH = 0.0;
			else if (nDotH > 1)
				nDotH = 1.0;
			nDotH = pow(nDotH, exp);
			double r, g ,b;
			r = evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y());
			g = evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y());
			b = evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y());
			double temp = (r + g + b)/3;
			double lum =  temp * ks * 2 * nDotH;
			buffer[offsetBuf + 0] = tobyte( r * kd + lum);
			buffer[offsetBuf + 1] = tobyte( g * kd + lum );
			buffer[offsetBuf + 2] = tobyte( b * kd + lum );
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
}


void SpecularEnhancement::applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	const float* redPtr = redCoeff.getLevel(info.level);
	const float* greenPtr = greenCoeff.getLevel(info.level);
	const float* bluePtr = blueCoeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	int tempW = mipMapSize[info.level].width();
	double hweights[9];
	vcg::Point3d temp(info.light.X(), info.light.Y(), info.light.Z());
	temp.Normalize();
	double phi = atan2(temp.Y(), temp.X());
	double theta = acos(temp.Z()/temp.Norm());
	int offsetBuf = 0;
	getHSH(theta, phi, hweights);
	
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset= y * tempW + x;
			double red = 0, green = 0, blue = 0;
			for (int k = 0; k < info.ordlen; k++)
			{
				red += redPtr[offset*info.ordlen + k] * hweights[k];
				green += greenPtr[offset*info.ordlen + k] * hweights[k];
				blue += bluePtr[offset*info.ordlen + k] * hweights[k];
			}
			red *= 256;
			green *= 256;
			blue *= 256;
			
			vcg::Point3f h(0, 0, 1);
			h += info.light;
			h /= 2;
			h.Normalize();
			
			double nDotH = h * normalsPtr[offset];
			if (nDotH < 0) 
				nDotH = 0.0;
			else if (nDotH > 1)
				nDotH = 1.0;
			nDotH = pow(nDotH, exp/5);

			double temp = (red + green + blue)/3;
			double lum =  temp * ks * 4 * nDotH;
			buffer[offsetBuf + 0] = tobyte( red * kd + lum);
			buffer[offsetBuf + 1] = tobyte( green * kd + lum );
			buffer[offsetBuf + 2] = tobyte( blue * kd + lum );
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}


}