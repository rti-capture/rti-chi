/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef DIFFUSEGAIN_H
#define DIFFUSEGAIN_H

#include "renderingmode.h"

#include <QSlider>

//! Widget for Diffuse Gain settings.
/*!
  The class defines the widget that is showed in the Rendering Dialog to set the parameters of the rendering mode Diffuse Gain.
*/
class DiffuseGControl : public QWidget
{
	Q_OBJECT

private:
		
	QSlider* sliderGain; /*!< Slider to set the gain value. */

public:

	//! Constructor
	/*!
	  \param initValue value for the gain parameter.
	  \param parent
	*/
	DiffuseGControl(int initValue, QWidget *parent = 0);

signals:

	/*!
	  Emitted when the user changes the gain value.
	*/
	void gainChanged(int value);

};


//! Diffuse Gain class.
/*!
  The class defines the rendering mode Diffuse Gain.
*/
class DiffuseGain : public QObject, public RenderingMode
{

	Q_OBJECT

private:

	const float minGain; /*!< Minimum gain value. */
	const float maxGain; /*!< Maximum gain value. */
	float gain; /*!< Current gain value. */

public:

	//! Constructor.
	DiffuseGain();

	//! Deconstructor.
	~DiffuseGain();
	
	virtual QString getTitle();
	virtual QWidget* getControl(QWidget* parent = 0);
	virtual bool isLightInteractive();
	virtual bool supportRemoteView();
	virtual bool enabledLighting();

	virtual void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);
	
	virtual void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	void applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer){}

private:

	/*!
	  Applies the Diffuse Gain on one pixel.
	  \param a array of six coefficients.
	  \param nu, nv projections of the pixel normal on uv plane.
	  \param lu, lv projection of the light vector on uv plane.
	  \return the output value.
	*/
        float applyModel(const int* a, float nu, float nv, float lu, float lv);

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

#endif /* DIFFUSEGAIN_H */
