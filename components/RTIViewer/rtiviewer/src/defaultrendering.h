/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef DEFAULT_REND_H
#define DEFAULT_REND_H

#include "renderingmode.h"

#include <QTimer>
#include <QWidget>
#include <QLabel>
#include <QVBoxLayout>


/*!
  Widget to show the progress of the downloading of a remote RTI
*/
class LoadRemoteWidget : public QWidget
{
	Q_OBJECT
private:

	QLabel* string;
	int i;
	
public:

	LoadRemoteWidget(bool remote, QWidget* parent = 0) : QWidget(parent)
	{
		i = 0;
		if (remote)
		{
			
			QVBoxLayout* layout = new QVBoxLayout;
			string = new QLabel("Downloading remote RTI ");
			layout->addWidget(string, 0, Qt::AlignVCenter);
			setLayout(layout);
			startTimer(500);
		}
	}
	
protected:

	void timerEvent(QTimerEvent * event)
	{
		i++;
		i = i % 10;
		QString point = "";
		for (int j = 0; j < i; j++)
			point.append(".");
		string->setText(tr("Downloading remote RTI ").append(point));
	}
};



//! Defaut Rendering for RTI images.
/*!
  The class defines the default rendering for RTI images.
*/
class DefaultRendering : public QObject, public RenderingMode
{
	Q_OBJECT

private:
	bool remote;

public:

	DefaultRendering(): remote(false){}
	void setRemote(bool flag) {remote = flag;}
	
	QString getTitle() {return "Default";}
	
	QWidget* getControl(QWidget* parent)
	{
		LoadRemoteWidget* control = new LoadRemoteWidget(remote, parent);
		disconnect(parent, SIGNAL(resetRemote()), 0, 0);
		connect(parent, SIGNAL(resetRemote()), this, SLOT(resetRemote())); 
		return control;
	}
	
	bool isLightInteractive() {return true;}
	bool supportRemoteView()  {return true;}
	bool enabledLighting() {return true;}
	
	void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		int offsetBuf = 0;
		const int* coeffPtr = coeff.getLevel(info.level);
		const unsigned char* rgbPtr = rgb.getLevel(info.level);
		int tempW = mipMapSize[info.level].width();
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * tempW + x;
				double lum = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				for (int i = 0; i < 3; i++)
					buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * lum);
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}


	void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		int offsetBuf = 0;
		const int* redPtr = redCoeff.getLevel(info.level);
		const int* greenPtr = greenCoeff.getLevel(info.level);
		const int* bluePtr = blueCoeff.getLevel(info.level);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * mipMapSize[info.level].width() + x;
				buffer[offsetBuf + 0] = tobyte(evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 1] = tobyte(evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 2] = tobyte(evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}

	}

	void applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		const float* redPtr = redCoeff.getLevel(info.level);
		const float* greenPtr = greenCoeff.getLevel(info.level);
		const float* bluePtr = blueCoeff.getLevel(info.level);
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
				double val = 0;
				for (int k = 0; k < info.ordlen; k++)
					val += redPtr[offset*info.ordlen + k] * hweights[k];
				buffer[offsetBuf + 0] = tobyte(val*255);
				val = 0;
				for (int k = 0; k < info.ordlen; k++)
					val += greenPtr[offset*info.ordlen + k] * hweights[k];
				buffer[offsetBuf + 1] = tobyte(val*255);
				val = 0;
				for (int k = 0; k < info.ordlen; k++)
					val += bluePtr[offset*info.ordlen + k] * hweights[k];
				buffer[offsetBuf + 2] = tobyte(val*255);
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	
	}

public slots:

	void resetRemote()
	{
		remote =  false;
	}

};

#endif //DEFAULT_REND_H
