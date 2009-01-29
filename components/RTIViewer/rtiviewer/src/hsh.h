/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef HSH_H
#define HSH_H

#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

// Local headers
#include "rti.h"
#include "pyramid.h"
#include "renderingmode.h"

// Qt headers
#include <QFile>
#include <QImage>
#include <QVector>


/*!
  Rendering mode for HSH image.
*/
enum RenderingHsh
{
	NORMAL_HSH,
};


//! Defaut Rending for HSH image.
/*!
  The class defines the default rendering for HSH image.
*/
class DefaultRenderingHsh : public RenderingMode
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




//! HSH class
class Hsh : public Rti
{
// private data member
protected:
	
	QString version; /*!< Version. */
 	QSize mipMapSize[MIP_MAPPING_LEVELS]; /*!< Size of mip-mapping levels. */

	PyramidCoeffF redCoefficients; /*!< Coefficients for red component. */
	PyramidCoeffF greenCoefficients; /*!< Coefficients for green component. */
	PyramidCoeffF blueCoefficients; /*!< Coefficients for blue component. */

	float gmin[9]; /*!< Min coefficient value. */
	float gmax[9]; /*!< Max coefficient value. */

	int bands; /*!< Number of colors. */
	int ordlen; /*!< Number of cofficients per pixel. */


public:

	//! Constructor.
	Hsh();

	//! Deconstructor.
	virtual ~Hsh();

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

};

#endif //HSH_H
