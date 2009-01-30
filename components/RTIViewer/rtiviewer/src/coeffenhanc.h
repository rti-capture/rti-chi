/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef COEFFENHANCEMENT_H
#define COEFFENHANCEMENT_H

#include "renderingmode.h"

#include <vcg/space/point3.h>

#include <QSlider>

//! Widget for Coefficient Enhancement settings.
/*!
  The class defines the widget that is showed in the Rendering Dialogto to set the parameters of the rendering mode Coefficient Enhancement.
*/
class CoeffEnhancControl : public QWidget
{
	Q_OBJECT

private:
	
	QSlider* sliderGain; /*!< Slider to set the gain parameter. */
 
public:

	//! Constructor.
	/*!
	  /param gain value for the gain parameter.
	*/
	CoeffEnhancControl(int gain, QWidget *parent = 0);

signals:

	/*!
	  Emitted when the user changes the gain value.
	*/
	void gainChanged(int value);

};


//! Coefficient Enhancement class.
/*!
  The class defines the rendering mode Coefficient Enhancement.
*/
class CoeffEnhancement : public QObject, public RenderingMode
{

	Q_OBJECT

private:

	float minGain; /*!< Minimum gain value. */ 
	float maxGain; /*!< Maximum gain value. */
	float gain; /*!< Current gain value. */

	int nIter; /*!< Number of smoothing iterations. */

	
public:

	//! Constructor.
	CoeffEnhancement();

	//! Deconstructor
	~CoeffEnhancement();
	
	virtual QString getTitle();
	virtual QWidget* getControl(QWidget* parent = 0);
	virtual bool isLightInteractive();
	virtual bool supportRemoteView();
	virtual bool enabledLighting();

	virtual void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);
	
	virtual void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

private:

	/*!
	  Computes the smoothed version of the coefficients.
	  \param coeffMap pointer to coefficients map.
	  \param width width in pixel of the map.
	  \param height height in pixel of the map.
	  \param ncomp number of coefficient per pixel,
	*/
	void enhancedCoeff(int* coeffMap, int width, int height, int ncomp);
	
public slots:

	/*!
	  Sets the gain value.
	*/
	void setGain(int value);

signals:

	/*!
	  Emitted to refresh the image in the browser.
	*/
	void refreshImage();
};

#endif /* COEFFENHANCEMENT_H */