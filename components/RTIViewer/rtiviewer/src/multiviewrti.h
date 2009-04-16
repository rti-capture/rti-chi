/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef MULTIVIEWRTI_H
#define MULTIVIEWRTI_H

#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

// Local headers
#include "rti.h"
#include "renderingmode.h"
#include "universalrti.h" 

// Qt headers
#include <QFile>
#include <QImage>
#include <QVector>

#include <vcg/math/matrix.h>


/*!
  Rendering mode for Multiview RTI image.
*/
enum RenderingMRti
{
	NORMAL_MULTIVIEW,
};


//! Defaut Rending for Multiview RTI image.
/*!
  The class defines the default rendering for Multiview RTI image.
*/
class DefaultRenderingMRti : public RenderingMode
{

public:
	
	QString getTitle() {return "Default";}
	QWidget* getControl(QWidget* parent) {return new QWidget(parent);}
	bool isLightInteractive() {return true;}
	bool supportRemoteView()  {return true;}
	bool enabledLighting() {return true;}
	
	void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		
	}


	void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{

	}
};


struct OpticalFlowData
{
	std::vector<int> up;
	std::vector<int> down;
	std::vector<int> left;
	std::vector<int> right;
};



//! HSH class
class MultiviewRti : public Rti
{
// private data member
protected:
	
	QString version; /*!< Version. */
 	
	int maxViewX, maxViewY, nViewpoint;
	int startX, startY;
	float separationX, separationY;
	bool useFlow;
	vcg::ndim::Matrix<int> viewpointLayout;
	std::vector<UniversalRti> images;
	std::vector<OpticalFlowData> flow;


public:

	//! Constructor.
	MultiviewRti();

	//! Deconstructor.
	virtual ~MultiviewRti();

	// protected methods
protected:

	
	
public:

	virtual int load(CallBackPos * cb = 0);
	virtual int load(QString name, CallBackPos * cb = 0);
	virtual int save(QString name);
	virtual int loadCompressed();
	virtual int loadCompressed(QString name);
	virtual int loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name);
	virtual int saveCompressed(QString name);
	virtual int saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name);
	virtual int createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level = 0, int mode = 0);
	virtual QImage* createPreview(int width, int height);
	virtual int allocateRemoteImage(int width, int height, int maxResLevel);  
	virtual int loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level); 
	virtual int loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb = 0, QString& text = QString());

};

#endif //MULTIVIEWRTI_H
