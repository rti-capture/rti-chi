/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#include "diffusegain.h"

#include <QHBoxLayout>
#include <QLabel>

DiffuseGControl::DiffuseGControl(int initValue, QWidget *parent) : QWidget(parent)
{
	QLabel* label = new QLabel("Gain");
	sliderGain = new QSlider(Qt::Horizontal);
	sliderGain->setRange(0, 100);
	sliderGain->setValue(initValue);
	sliderGain->setTracking(false);
	connect(sliderGain, SIGNAL(valueChanged(int)), this, SIGNAL(gainChanged(int)));
	
	QHBoxLayout *layout = new QHBoxLayout;
	layout->addWidget(label);
	layout->addWidget(sliderGain);
	setLayout(layout);
}


DiffuseGain::DiffuseGain() :
	gain(2.0f),
	minGain(1.0f),
	maxGain(10.0f)
	{	}

DiffuseGain::~DiffuseGain() {}


	
QString DiffuseGain::getTitle() 
{
	return "Diffuse Gain";
}

QWidget* DiffuseGain::getControl(QWidget* parent)
{
	int initValue = (gain - minGain)*100/(maxGain - minGain);
	DiffuseGControl* control = new DiffuseGControl(initValue, parent);
	connect(control, SIGNAL(gainChanged(int)), this, SLOT(setGain(int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	return control;
}


bool DiffuseGain::isLightInteractive()
{
	return true;
}


bool DiffuseGain::supportRemoteView()
{
	return true;
}


bool DiffuseGain::enabledLighting()
{
	return true;
}


void DiffuseGain::setGain(int value)
{
	gain = minGain + value * (maxGain - minGain)/100;
	emit refreshImage();
}


void DiffuseGain::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const unsigned char* rgbPtr = rgb.getLevel(info.level);
	const int* coeffPtr = coeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	// Creates the output texture.
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			double lum = applyModel(&coeffPtr[offset*6], normalsPtr[offset].X(), normalsPtr[offset].Y(), info.light.X(), info.light.Y());
			lum /= 256.0;
			for (int i = 0; i < 3; i++)
				buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * lum);
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}

}


void DiffuseGain::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int offsetBuf = 0;
	const int* redPtr = redCoeff.getLevel(info.level);
	const int* greenPtr = greenCoeff.getLevel(info.level);
	const int* bluePtr = blueCoeff.getLevel(info.level);
	const vcg::Point3f* normalsPtr = normals.getLevel(info.level);
	// Creates the output texture.
	for (int y = info.offy; y < info.offy + info.height; y++)
	{
		for (int x = info.offx; x < info.offx + info.width; x++)
		{
			int offset = y * mipMapSize[info.level].width() + x;
			buffer[offsetBuf + 0] = tobyte(applyModel(&redPtr[offset*6], normalsPtr[offset].X(), normalsPtr[offset].Y(), info.light.X(), info.light.Y()));
			buffer[offsetBuf + 1] = tobyte(applyModel(&greenPtr[offset*6], normalsPtr[offset].X(), normalsPtr[offset].Y(), info.light.X(), info.light.Y()));
			buffer[offsetBuf + 2] = tobyte(applyModel(&bluePtr[offset*6], normalsPtr[offset].X(), normalsPtr[offset].Y(), info.light.X(), info.light.Y()));
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
}


double DiffuseGain::applyModel(const int* a, float nu, float nv, double lu, double lv)
{
	double a0 = gain * a[0];
	double a1 = gain * a[1];
	double a2 = gain * a[2];
	double a3 = (1 - gain) * (2*a[0]*nu + a[2]*nv) + a[3];
	double a4 = (1 - gain) * (2*a[1]*nv + a[2]*nu) + a[4];
	double a5 = (1 - gain) * (a[0]*nu*nu + a[1]*nv*nv + a[2]*nu*nv) + (a[3] - a3) * nu
				+ (a[4] - a4) * nv + a[5];
	return a0*lu*lu + a1*lv*lv + a2*lu*lv + a3*lu + a4*lv + a5; 
}