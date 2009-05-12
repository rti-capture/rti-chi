/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef SPECULARENHANC_H
#define SPECULARENHANC_H

#include "renderingmode.h"

#include <vcg/space/point3.h>

#include <QSlider>

//! Widget for Specular Enhancement settings.
/*!
  The class defines the widget thta is showed in the Rendering Dialog to set the parameters of the rendering mode Specular Enhancement.
*/
class SpecularEControl : public QWidget
{
	Q_OBJECT

private:
	
	QSlider* sliderKd; /*!< Slider to set the diffusive constant. */
	QSlider* sliderKs; /*!< Slider to set the specular constant. */
	QSlider* sliderExp; /*!< Slider to set the specular exponent. */

public:

	//! Constructor
	/*!
	  \param kd value for diffusive constant.
	  \param ks value for specular constant.
	  \param exp value for specular exponent.
	  \param parent
	*/
	SpecularEControl(int kd, int ks, int exp,  QWidget *parent = 0);

signals:

	/*!
	  Emitted when the user changes the diffusive constant.
	*/
	void kdChanged(int value);
	
	/*!
	  Emitted when the user changes the specular constant.
	*/
	void ksChanged(int value);

	/*!
	  Emitted when the user changes the specular exponent.
	*/
	void expChanged(int value);

};


//! Specular Enhancement class.
/*!
  The class defines the rendering mode Specular Enhancement.
*/
class SpecularEnhancement : public QObject, public RenderingMode
{

	Q_OBJECT

private:

	const float minKd; /*!< Minumum diffusive constant value. */
	const float maxKd; /*!< Minumum diffusive constant value. */
	float kd; /*!< Current diffusive constant value. */

	const float minKs; /*!< Minumum specular constant value. */
	const float maxKs; /*!< Maximum specular constant value. */
	float ks; /*!< Current specular constant value. */

	const int minExp; /*!< Minumum specular exponent value. */
	const int maxExp; /*!< Maximum specular exponent value. */
	int exp; /*!< Current specular exponent value. */


public:

	//! Constructor.
	SpecularEnhancement();
	
	//! Deconstructor.
	~SpecularEnhancement();
	
	virtual QString getTitle();
	virtual QWidget* getControl(QWidget* parent = 0);
	virtual bool isLightInteractive();
	virtual bool supportRemoteView();
	virtual bool enabledLighting();

	virtual void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	virtual void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	virtual void applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

public slots:
	
	/*!
	  Sets the diffusive constant value.
	*/
	void setKd(int value);
	
	/*!
	  Sets the specular constant value.
	*/
	void setKs(int value);
	
	/*!
	  Sets the specular exponent value.
	*/
	void setExp(int value);

signals:

	/*!
	  Emitted to refresh the image in the browser.
	*/
	void refreshImage();

};

#endif /* SPECULARENHANCEMENT_H */
